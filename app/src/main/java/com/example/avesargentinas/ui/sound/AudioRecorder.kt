package com.example.avesargentinas.ui.sound

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

internal class AudioRecordingException(message: String, cause: Throwable? = null) : Exception(message, cause)

internal interface AudioRecordClient {
    val state: Int
    val recordingState: Int
    val sampleRate: Int
    val audioSource: Int
    val isClientSilenced: Boolean get() = false
    fun startRecording()
    fun stop()
    fun release()
    fun read(audioData: ShortArray, offsetInShorts: Int, sizeInShorts: Int, readMode: Int): Int
}

internal fun interface AudioRecordFactory {
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun create(
        source: Int,
        sampleRate: Int,
        channelConfig: Int,
        audioFormat: Int,
        bufferSizeInBytes: Int
    ): AudioRecordClient
}

private class RealAudioRecordClient(private val record: AudioRecord) : AudioRecordClient {
    override val state: Int get() = record.state
    override val recordingState: Int get() = record.recordingState
    override val sampleRate: Int get() = record.sampleRate
    override val audioSource: Int get() = record.audioSource
    override val isClientSilenced: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                record.activeRecordingConfiguration?.isClientSilenced == true
            } catch (_: Throwable) {
                false
            }
        } else {
            false
        }

    override fun startRecording() = record.startRecording()
    override fun stop() = record.stop()
    override fun release() = record.release()
    override fun read(audioData: ShortArray, offsetInShorts: Int, sizeInShorts: Int, readMode: Int): Int =
        record.read(audioData, offsetInShorts, sizeInShorts, readMode)
}

internal class RealAudioRecordFactory : AudioRecordFactory {
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun create(
        source: Int,
        sampleRate: Int,
        channelConfig: Int,
        audioFormat: Int,
        bufferSizeInBytes: Int
    ): AudioRecordClient {
        val record = AudioRecord.Builder()
            .setAudioSource(source)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .build()
            )
            .setBufferSizeInBytes(bufferSizeInBytes)
            .build()
        return RealAudioRecordClient(record)
    }
}

internal open class AudioRecorder internal constructor(
    private val context: Context? = null,
    private val factory: AudioRecordFactory = RealAudioRecordFactory(),
    private val clock: () -> Long = { SystemClock.elapsedRealtime() }
) {
    constructor(context: Context? = null) : this(
        context = context,
        factory = RealAudioRecordFactory(),
        clock = { SystemClock.elapsedRealtime() }
    )

    private val stateLock = Any()
    private var activeClient: AudioRecordClient? = null
    private var isRecording = false
    private var isCancelled = false

    val isRecordingActive: Boolean
        get() = synchronized(stateLock) { isRecording }

    open fun cancel() {
        synchronized(stateLock) {
            isCancelled = true
            activeClient?.let { client ->
                try {
                    if (client.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        client.stop()
                    }
                } catch (_: Throwable) {
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    open suspend fun listen(
        onStarted: suspend (sampleRate: Int, startMonotonicTimeMs: Long) -> Unit = { _, _ -> },
        onWindow: (audio: CapturedAudio, endPosition: Long, captureMonotonicTimeMs: Long) -> Unit
    ): Unit = withContext(Dispatchers.IO) {
        checkRecordPermission()

        synchronized(stateLock) {
            check(!isRecording) { "Ya hay una grabación en curso" }
            isRecording = true
            isCancelled = false
        }

        try {
            val client = initClient()
            try {
                startClient(client)

                val rate = client.sampleRate
                val startTime = clock()
                onStarted(rate, startTime)

                val windowBuffer = AudioWindowBuffer(rate)
                val chunkSize = 1024
                val readBuffer = ShortArray(chunkSize)
                var lastSampleTime = startTime

                while (true) {
                    currentCoroutineContext().ensureActive()
                    synchronized(stateLock) {
                        if (isCancelled) throw CancellationException("Recording cancelled")
                    }

                    checkPermissionNotRevoked()

                    val now = clock()
                    if (now - lastSampleTime >= 2_000L) {
                        throw AudioRecordingException("Watchdog: timeout de 2 segundos sin nuevas muestras")
                    }
                    if (client.isClientSilenced) {
                        throw AudioRecordingException("Captura silenciada por el sistema")
                    }

                    val readResult = client.read(readBuffer, 0, chunkSize, AudioRecord.READ_NON_BLOCKING)

                    if (readResult < 0) {
                        synchronized(stateLock) {
                            if (isCancelled) throw CancellationException("Recording cancelled")
                        }
                        throw AudioRecordingException("Error de lectura de AudioRecord: $readResult")
                    } else if (readResult == 0) {
                        delay(10)
                    } else {
                        val sampleTime = clock()
                        lastSampleTime = sampleTime
                        val windows = windowBuffer.write(readBuffer, 0, readResult)
                        for (window in windows) {
                            onWindow(window.audio, window.endPosition, sampleTime)
                        }
                    }
                }
            } finally {
                stopAndReleaseClient(client)
            }
        } finally {
            synchronized(stateLock) {
                isRecording = false
                activeClient = null
            }
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    open suspend fun record(onProgress: (Float) -> Unit = {}): CapturedAudio = withContext(Dispatchers.IO) {
        checkRecordPermission()

        synchronized(stateLock) {
            check(!isRecording) { "Ya hay una grabación en curso" }
            isRecording = true
            isCancelled = false
        }

        try {
            val client = initClient()
            try {
                startClient(client)

                val rate = client.sampleRate
                val samplesTarget = rate * 10
                val samples = ShortArray(samplesTarget)
                var samplesRead = 0
                val startTime = clock()
                var lastSampleTime = startTime
                var lastProgressTime = 0L
                val chunkSize = 1024

                while (samplesRead < samplesTarget) {
                    currentCoroutineContext().ensureActive()
                    synchronized(stateLock) {
                        if (isCancelled) throw CancellationException("Recording cancelled")
                    }

                    checkPermissionNotRevoked()

                    val now = clock()
                    if (now - startTime >= 15_000L) {
                        throw AudioRecordingException("Watchdog: timeout de 15 segundos sin completar la captura")
                    }
                    if (now - lastSampleTime >= 2_000L) {
                        throw AudioRecordingException("Watchdog: timeout de 2 segundos sin nuevas muestras")
                    }
                    if (client.isClientSilenced) {
                        throw AudioRecordingException("Captura silenciada por el sistema")
                    }

                    val toRead = minOf(chunkSize, samplesTarget - samplesRead)
                    val readResult = client.read(samples, samplesRead, toRead, AudioRecord.READ_NON_BLOCKING)

                    if (readResult < 0) {
                        synchronized(stateLock) {
                            if (isCancelled) throw CancellationException("Recording cancelled")
                        }
                        throw AudioRecordingException("Error de lectura de AudioRecord: $readResult")
                    } else if (readResult == 0) {
                        delay(10)
                    } else {
                        samplesRead += readResult
                        lastSampleTime = clock()
                        val progressNow = clock()
                        if (progressNow - lastProgressTime >= 100L) {
                            onProgress(samplesRead.toFloat() / samplesTarget)
                            lastProgressTime = progressNow
                        }
                    }
                }

                onProgress(1.0f)
                CapturedAudio(samples, rate)
            } finally {
                stopAndReleaseClient(client)
            }
        } finally {
            synchronized(stateLock) {
                isRecording = false
                activeClient = null
            }
        }
    }

    private fun checkRecordPermission() {
        if (context != null && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("Permiso RECORD_AUDIO no concedido")
        }
    }

    private fun checkPermissionNotRevoked() {
        if (context != null && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("Permiso RECORD_AUDIO revocado durante la captura")
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun initClient(): AudioRecordClient {
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val supportsUnprocessed = audioManager?.getProperty(
            AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED
        ) == "true"

        val candidateSources = if (supportsUnprocessed) {
            listOf(MediaRecorder.AudioSource.UNPROCESSED, MediaRecorder.AudioSource.MIC)
        } else {
            listOf(MediaRecorder.AudioSource.MIC)
        }

        val candidateRates = listOf(32_000, 48_000, 44_100)
        var initializedClient: AudioRecordClient? = null

        for (rate in candidateRates) {
            val minBufferSize = AudioRecord.getMinBufferSize(
                rate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBufferSize <= 0) continue
            val bufferSize = maxOf(minBufferSize * 2, 4096)

            for (source in candidateSources) {
                try {
                    val client = factory.create(
                        source,
                        rate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize
                    )
                    if (client.state == AudioRecord.STATE_INITIALIZED) {
                        initializedClient = client
                        break
                    } else {
                        client.release()
                    }
                } catch (_: Throwable) {
                }
            }
            if (initializedClient != null) break
        }

        return initializedClient
            ?: throw AudioRecordingException("No se pudo inicializar AudioRecord en ninguna frecuencia admitida")
    }

    private fun startClient(client: AudioRecordClient) {
        synchronized(stateLock) {
            if (isCancelled) {
                throw CancellationException("Recording cancelled")
            }
            activeClient = client
        }

        client.startRecording()
        if (client.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
            throw AudioRecordingException("AudioRecord falló al iniciar grabación")
        }
    }

    private fun stopAndReleaseClient(client: AudioRecordClient) {
        try {
            if (client.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                client.stop()
            }
        } catch (_: Throwable) {
        } finally {
            client.release()
        }
    }
}
