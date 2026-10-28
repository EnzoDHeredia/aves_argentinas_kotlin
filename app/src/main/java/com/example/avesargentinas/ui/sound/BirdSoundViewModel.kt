package com.example.avesargentinas.ui.sound

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.SystemClock
import androidx.annotation.MainThread
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.util.Locale

enum class BirdSoundPhase {
    READY,
    PREPARING,
    LISTENING,
    STOPPING,
    STOPPED,
    ERROR
}

data class BirdSoundState(
    val phase: BirdSoundPhase = BirdSoundPhase.READY,
    val detections: List<BirdDetection> = emptyList(),
    val elapsedSeconds: Long = 0L,
    val errorReason: ErrorReason? = null
) {
    enum class ErrorReason {
        INTERRUPTED,
        RECORDING,
        RECOGNITION
    }

    val formattedTime: String
        get() = formatElapsedTime(elapsedSeconds)

    companion object {
        val Ready = BirdSoundState(BirdSoundPhase.READY)
        val Preparing = BirdSoundState(BirdSoundPhase.PREPARING)
        val Stopping = BirdSoundState(BirdSoundPhase.STOPPING)
        val Stopped = BirdSoundState(BirdSoundPhase.STOPPED)
        val NoDetections = BirdSoundState(BirdSoundPhase.STOPPED, detections = emptyList())
        fun Results(detections: List<BirdDetection>) = BirdSoundState(BirdSoundPhase.STOPPED, detections = detections)
        fun Error(reason: ErrorReason) = BirdSoundState(BirdSoundPhase.ERROR, errorReason = reason)

        fun formatElapsedTime(elapsedSeconds: Long): String {
            val totalSeconds = elapsedSeconds.coerceAtLeast(0L)
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
            }
        }
    }
}

internal class InferenceTimeoutException(message: String, cause: Throwable? = null) : Exception(message, cause)

private data class WindowPayload(
    val audio: CapturedAudio,
    val endPosition: Long,
    val captureMonotonicTimeMs: Long
)

internal interface SoundSession {
    suspend fun prepare()
    suspend fun listen(
        onStarted: suspend (sampleRate: Int, startMonotonicTimeMs: Long) -> Unit = { _, _ -> },
        onDetections: suspend (detections: List<BirdDetection>, endPosition: Long, captureMonotonicTimeMs: Long) -> Unit
    ) = Unit
    fun cancel()
    suspend fun close()
}

internal open class RealSoundSession internal constructor(
    private val application: Context,
    private val classifier: BirdNetClassifier = BirdNetClassifier(application.assets),
    private val recorder: AudioRecorder = AudioRecorder(application),
    private val catalogProvider: suspend () -> BirdSoundCatalog = { BirdSoundCatalog.load(application) },
    private val clock: () -> Long = { SystemClock.elapsedRealtime() },
    private val inferenceTimeoutMs: Long = 5_000L
) : SoundSession {
    constructor(app: Application) : this(app as Context)

    private var catalog: BirdSoundCatalog? = null
    private val sessionLock = Any()
    private var activeJob: Job? = null
    private var activeChannel: Channel<WindowPayload>? = null
    private var runningListen = false
    private var closed = false

    val droppedOldWindowsCount = java.util.concurrent.atomic.AtomicLong(0)
    val processedWindowsCount = java.util.concurrent.atomic.AtomicLong(0)

    override suspend fun prepare() {
        classifier.initialize()
        if (catalog == null) catalog = withContext(Dispatchers.IO) { catalogProvider() }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override suspend fun listen(
        onStarted: suspend (sampleRate: Int, startMonotonicTimeMs: Long) -> Unit,
        onDetections: suspend (detections: List<BirdDetection>, endPosition: Long, captureMonotonicTimeMs: Long) -> Unit
    ): Unit {
        val currentCatalog = checkNotNull(catalog) { "Sesión no preparada: llamar a prepare() primero" }
        val channel = Channel<WindowPayload>(Channel.CONFLATED)

        try {
            coroutineScope {
                synchronized(sessionLock) {
                    check(!closed) { "Sesión cerrada" }
                    check(!runningListen) { "Ya hay una escucha en curso" }
                    runningListen = true
                    activeChannel = channel
                    activeJob = coroutineContext.job
                }

                val captureJob = launch(Dispatchers.IO) {
                    try {
                        recorder.listen(
                            onStarted = { sampleRate, startMonotonicTimeMs ->
                                onStarted(sampleRate, startMonotonicTimeMs)
                            },
                            onWindow = { audio, endPosition, captureMonotonicTimeMs ->
                                channel.trySend(WindowPayload(audio, endPosition, captureMonotonicTimeMs))
                            }
                        )
                    } catch (_: CancellationException) {
                    } finally {
                        channel.close()
                    }
                }

                val inferenceJob = launch(Dispatchers.Default) {
                    try {
                        for (payload in channel) {
                            currentCoroutineContext().ensureActive()
                            val now = clock()
                            val ageMs = now - payload.captureMonotonicTimeMs
                            if (ageMs > 3_000L) {
                                droppedOldWindowsCount.incrementAndGet()
                                continue
                            }

                            val modelSamples = BirdSoundAudio.toModelSamples(payload.audio)
                            val scores = runInferenceWithWatchdog(modelSamples)
                            val detections = currentCatalog.selectAll(scores, minimumScore = 0.50f)
                            processedWindowsCount.incrementAndGet()
                            onDetections(detections, payload.endPosition, payload.captureMonotonicTimeMs)
                        }
                    } catch (_: CancellationException) {
                    }
                }
            }
        } finally {
            classifier.cancelCurrentRun()
            channel.cancel()
            synchronized(sessionLock) {
                runningListen = false
                activeChannel = null
                activeJob = null
            }
        }
    }

    private suspend fun runInferenceWithWatchdog(samples: FloatArray): FloatArray {
        val timedOut = java.util.concurrent.atomic.AtomicBoolean(false)
        val watchdogJob = CoroutineScope(Dispatchers.Default).launch {
            delay(inferenceTimeoutMs)
            timedOut.set(true)
            classifier.cancelCurrentRun()
        }
        return try {
            val scores = classifier.classifyWindow(samples)
            if (timedOut.get()) {
                throw InferenceTimeoutException("Watchdog: timeout de inferencia de $inferenceTimeoutMs ms superado")
            }
            scores
        } catch (e: Throwable) {
            if (timedOut.get() || e is InferenceTimeoutException) {
                throw InferenceTimeoutException("Watchdog: timeout de inferencia de $inferenceTimeoutMs ms superado", e)
            }
            throw e
        } finally {
            watchdogJob.cancel()
        }
    }

    override fun cancel() {
        synchronized(sessionLock) {
            activeChannel?.cancel()
            activeJob?.cancel()
        }
        recorder.cancel()
        classifier.cancelCurrentRun()
    }

    override suspend fun close() {
        synchronized(sessionLock) {
            closed = true
        }
        cancel()
        activeJob?.join()
        classifier.close()
    }
}

@MainThread
class BirdSoundViewModel internal constructor(
    app: Application,
    private val clock: () -> Long = { SystemClock.elapsedRealtime() },
    private val createSession: () -> SoundSession
) : AndroidViewModel(app) {
    constructor(app: Application) : this(app, { SystemClock.elapsedRealtime() }, { RealSoundSession(app) })
    internal constructor(app: Application, createSession: () -> SoundSession) : this(app, { SystemClock.elapsedRealtime() }, createSession)

    private val mutableState = MutableStateFlow(BirdSoundState.Ready)
    val state = mutableState.asStateFlow()

    private val accumulatedMap = LinkedHashMap<String, BirdDetection>()
    private var token = 0L
    private var job: Job? = null
    private var timerJob: Job? = null
    private var session: SoundSession? = null
    private var abandoned = false

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun start() {
        if (abandoned) return
        val currentPhase = mutableState.value.phase
        if (currentPhase == BirdSoundPhase.PREPARING ||
            currentPhase == BirdSoundPhase.LISTENING ||
            currentPhase == BirdSoundPhase.STOPPING
        ) {
            return
        }

        val currentToken = ++token
        accumulatedMap.clear()
        timerJob?.cancel()
        timerJob = null
        mutableState.value = BirdSoundState(
            phase = BirdSoundPhase.PREPARING,
            detections = emptyList(),
            elapsedSeconds = 0L,
            errorReason = null
        )

        job?.cancel()
        job = viewModelScope.launch {
            var sessionStarted = false
            var activeSession: SoundSession? = null
            try {
                activeSession = session ?: createSession().also { session = it }
                activeSession.prepare()
                ensureActive()
                if (currentToken != token) return@launch

                activeSession.listen(
                    onStarted = { _, startMonotonicTimeMs ->
                        withContext(Dispatchers.Main.immediate) {
                            if (currentToken != token) return@withContext
                            sessionStarted = true
                            mutableState.value = mutableState.value.copy(
                                phase = BirdSoundPhase.LISTENING,
                                elapsedSeconds = 0L
                            )
                            timerJob?.cancel()
                            timerJob = viewModelScope.launch {
                                while (isActive) {
                                    delay(1000L)
                                    if (currentToken != token) break
                                    val elapsed = ((clock() - startMonotonicTimeMs) / 1000L).coerceAtLeast(0L)
                                    if (mutableState.value.phase == BirdSoundPhase.LISTENING) {
                                        mutableState.value = mutableState.value.copy(elapsedSeconds = elapsed)
                                    }
                                }
                            }
                        }
                    },
                    onDetections = { detections, _, _ ->
                        withContext(Dispatchers.Main.immediate) {
                            if (currentToken != token) return@withContext
                            if (mutableState.value.phase != BirdSoundPhase.LISTENING) return@withContext
                            var hasChanges = false
                            for (det in detections) {
                                val key = canonicalKey(det.scientificName)
                                val existing = accumulatedMap[key]
                                if (existing == null) {
                                    accumulatedMap[key] = det
                                    hasChanges = true
                                } else if (det.score > existing.score) {
                                    accumulatedMap[key] = det
                                    hasChanges = true
                                }
                            }
                            if (hasChanges) {
                                mutableState.value = mutableState.value.copy(
                                    detections = accumulatedMap.values.toList()
                                )
                            }
                        }
                    }
                )
            } catch (_: CancellationException) {
            } catch (e: Throwable) {
                withContext(Dispatchers.Main.immediate) {
                    if (currentToken == token) {
                        val reason = if (sessionStarted) {
                            if (e is InferenceTimeoutException) BirdSoundState.ErrorReason.RECOGNITION
                            else BirdSoundState.ErrorReason.RECORDING
                        } else {
                            BirdSoundState.ErrorReason.RECOGNITION
                        }
                        val frozenDetections = mutableState.value.detections
                        val frozenElapsed = mutableState.value.elapsedSeconds
                        mutableState.value = BirdSoundState(
                            phase = BirdSoundPhase.ERROR,
                            detections = frozenDetections,
                            elapsedSeconds = frozenElapsed,
                            errorReason = reason
                        )
                    }
                }
            } finally {
                timerJob?.cancel()
                timerJob = null
                withContext(NonCancellable) {
                    if (currentToken == token) {
                        val toClose = session ?: activeSession
                        session = null
                        toClose?.close()
                    }
                }
            }
        }
    }

    fun stop() {
        if (mutableState.value.phase != BirdSoundPhase.PREPARING &&
            mutableState.value.phase != BirdSoundPhase.LISTENING
        ) {
            return
        }

        val stoppingToken = ++token
        timerJob?.cancel()
        timerJob = null
        val frozenDetections = mutableState.value.detections
        val frozenElapsed = mutableState.value.elapsedSeconds

        mutableState.value = BirdSoundState(
            phase = BirdSoundPhase.STOPPING,
            detections = frozenDetections,
            elapsedSeconds = frozenElapsed,
            errorReason = null
        )

        val oldSession = session
        session = null
        val currentJob = job
        job = null

        viewModelScope.launch {
            var failed = false
            try {
                oldSession?.cancel()
                currentJob?.cancelAndJoin()
                withContext(NonCancellable + Dispatchers.IO) {
                    oldSession?.close()
                }
            } catch (_: Throwable) {
                failed = true
                if (token == stoppingToken && !abandoned) {
                    mutableState.value = BirdSoundState(
                        phase = BirdSoundPhase.ERROR,
                        detections = frozenDetections,
                        elapsedSeconds = frozenElapsed,
                        errorReason = BirdSoundState.ErrorReason.RECORDING
                    )
                }
            } finally {
                if (!failed && token == stoppingToken && !abandoned) {
                    mutableState.value = BirdSoundState(
                        phase = BirdSoundPhase.STOPPED,
                        detections = frozenDetections,
                        elapsedSeconds = frozenElapsed,
                        errorReason = null
                    )
                }
            }
        }
    }

    suspend fun stopAndAwait(): BirdSoundState {
        val currentPhase = mutableState.value.phase
        if (currentPhase != BirdSoundPhase.PREPARING &&
            currentPhase != BirdSoundPhase.LISTENING &&
            currentPhase != BirdSoundPhase.STOPPING
        ) {
            return mutableState.value
        }
        if (currentPhase != BirdSoundPhase.STOPPING) {
            stop()
        }
        return state.first { it.phase != BirdSoundPhase.STOPPING }
    }

    fun cancel() = stop()

    fun background() {
        if (mutableState.value.phase != BirdSoundPhase.PREPARING &&
            mutableState.value.phase != BirdSoundPhase.LISTENING
        ) {
            return
        }

        val stoppingToken = ++token
        timerJob?.cancel()
        timerJob = null
        val frozenDetections = mutableState.value.detections
        val frozenElapsed = mutableState.value.elapsedSeconds

        mutableState.value = BirdSoundState(
            phase = BirdSoundPhase.STOPPING,
            detections = frozenDetections,
            elapsedSeconds = frozenElapsed,
            errorReason = null
        )

        val oldSession = session
        session = null
        val currentJob = job
        job = null

        viewModelScope.launch {
            try {
                oldSession?.cancel()
                currentJob?.cancelAndJoin()
                withContext(NonCancellable + Dispatchers.IO) {
                    oldSession?.close()
                }
            } finally {
                if (token == stoppingToken && !abandoned) {
                    mutableState.value = BirdSoundState(
                        phase = BirdSoundPhase.ERROR,
                        detections = frozenDetections,
                        elapsedSeconds = frozenElapsed,
                        errorReason = BirdSoundState.ErrorReason.INTERRUPTED
                    )
                }
            }
        }
    }

    fun abandon() {
        if (abandoned) return
        abandoned = true
        ++token
        timerJob?.cancel()
        timerJob = null
        val oldSession = session
        session = null
        val currentJob = job
        job = null
        currentJob?.cancel()
        if (oldSession != null) {
            CoroutineScope(Dispatchers.IO).launch {
                oldSession.cancel()
                oldSession.close()
            }
        }
        mutableState.value = BirdSoundState.Ready
    }

    override fun onCleared() {
        abandon()
        super.onCleared()
    }

    private fun canonicalKey(scientificName: String): String =
        scientificName.trim().lowercase(Locale.ROOT)
}
