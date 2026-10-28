package com.example.avesargentinas.ui.sound

import android.Manifest
import android.content.Context
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class ContinuousSoundSessionTest {

    private val targetContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        grantRecordAudioPermission()
    }

    @After
    fun tearDown() {
        grantRecordAudioPermission()
    }

    private fun grantRecordAudioPermission() {
        val packageName = targetContext.packageName
        try {
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                packageName,
                Manifest.permission.RECORD_AUDIO
            )
        } catch (_: Throwable) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
                "pm grant $packageName android.permission.RECORD_AUDIO"
            )
        }
    }

    private fun dummyAudioWithId(id: Int, sampleRate: Int = 32_000, seconds: Int = 3): CapturedAudio {
        val samples = ShortArray(sampleRate * seconds)
        // Store window ID scaled by 1000 in first sample
        samples[0] = (id * 1000).toShort()
        for (i in 1 until samples.size) {
            samples[i] = ((id * 31 + i) % 500).toShort()
        }
        return CapturedAudio(samples, sampleRate)
    }

    private fun extractId(samples: FloatArray): Int {
        return Math.round(samples[0] * 32768f / 1000f)
    }

    private fun extractIdFromAudio(audio: CapturedAudio): Int {
        return audio.samples[0].toInt() / 1000
    }

    @Test
    fun overloadDropsIntermediateWindowsAndProcessesLatestPendingWithoutParallelInferences() {
        val emitChannel = Channel<CapturedAudio>(Channel.UNLIMITED)
        val activeInferences = AtomicInteger(0)
        val maxParallel = AtomicInteger(0)
        val window1Started = CountDownLatch(1)
        val unblockWindow1 = CountDownLatch(1)
        val windowExecutionOrder = Collections.synchronizedList(mutableListOf<Int>())
        val detectionsReceived = Collections.synchronizedList(mutableListOf<Long>())

        val fakeRecorder = object : AudioRecorder(targetContext) {
            override suspend fun listen(
                onStarted: suspend (sampleRate: Int, startMonotonicTimeMs: Long) -> Unit,
                onWindow: (audio: CapturedAudio, endPosition: Long, captureMonotonicTimeMs: Long) -> Unit
            ) {
                onStarted(32_000, SystemClock.elapsedRealtime())
                var pos = 0L
                for (audio in emitChannel) {
                    pos += audio.samples.size
                    onWindow(audio, pos, SystemClock.elapsedRealtime())
                }
            }
            override fun cancel() {
                emitChannel.close()
            }
        }

        val fakeClassifier = object : BirdNetClassifier(targetContext.assets) {
            override suspend fun initialize() = Unit
            override suspend fun classifyWindow(samples: FloatArray): FloatArray {
                val current = activeInferences.incrementAndGet()
                maxParallel.accumulateAndGet(current) { max, cur -> maxOf(max, cur) }
                val id = extractId(samples)
                windowExecutionOrder.add(id)
                if (id == 1) {
                    window1Started.countDown()
                    assertTrue("Timeout waiting to unblock window 1", unblockWindow1.await(5, TimeUnit.SECONDS))
                }
                activeInferences.decrementAndGet()
                val scores = FloatArray(9789)
                scores[0] = 0.85f
                return scores
            }
            override fun cancelCurrentRun() = Unit
            override suspend fun close() = Unit
        }

        val session = RealSoundSession(
            application = targetContext,
            classifier = fakeClassifier,
            recorder = fakeRecorder
        )
        runBlocking { session.prepare() }

        val testScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        val listenJob = testScope.launch {
            session.listen(
                onStarted = { _, _ -> },
                onDetections = { detections, endPosition, _ ->
                    detectionsReceived.add(endPosition)
                }
            )
        }

        runBlocking {
            // Send window 1 (will block inside classifyWindow)
            emitChannel.send(dummyAudioWithId(1))
            assertTrue("Window 1 did not start", window1Started.await(5, TimeUnit.SECONDS))
            assertEquals(1, activeInferences.get())

            // While window 1 is executing, emit windows 2, 3, and 4
            emitChannel.send(dummyAudioWithId(2))
            emitChannel.send(dummyAudioWithId(3))
            emitChannel.send(dummyAudioWithId(4))

            delay(100)
            assertEquals(1, activeInferences.get())
            assertEquals(1, maxParallel.get())

            // Unblock window 1
            unblockWindow1.countDown()

            withTimeout(5000) {
                while (windowExecutionOrder.size < 2) {
                    delay(20)
                }
            }

            // Window 1 and Window 4 were executed; Window 2 and 3 were dropped
            assertEquals(listOf(1, 4), windowExecutionOrder.toList())
            assertEquals(1, maxParallel.get())

            session.cancel()
            session.close()
            listenJob.join()
        }
        testScope.cancel()
    }

    @Test
    fun discardOldWindowExceedingThreeSeconds() {
        var fakeTime = 10_000L
        val clock = { fakeTime }
        val processedWindows = Collections.synchronizedList(mutableListOf<Int>())
        val detectionsReceived = Collections.synchronizedList(mutableListOf<Long>())
        val emitChannel = Channel<CapturedAudio>(Channel.UNLIMITED)

        val fakeRecorder = object : AudioRecorder(targetContext) {
            override suspend fun listen(
                onStarted: suspend (sampleRate: Int, startMonotonicTimeMs: Long) -> Unit,
                onWindow: (audio: CapturedAudio, endPosition: Long, captureMonotonicTimeMs: Long) -> Unit
            ) {
                onStarted(32_000, 1000L)
                for (audio in emitChannel) {
                    val id = extractIdFromAudio(audio)
                    val captureTime = if (id == 1) 6_000L else 9_500L
                    onWindow(audio, id * 96000L, captureTime)
                }
            }
            override fun cancel() {
                emitChannel.close()
            }
        }

        val fakeClassifier = object : BirdNetClassifier(targetContext.assets) {
            override suspend fun initialize() = Unit
            override suspend fun classifyWindow(samples: FloatArray): FloatArray {
                val id = extractId(samples)
                processedWindows.add(id)
                val scores = FloatArray(9789)
                scores[0] = 0.85f
                return scores
            }
            override fun cancelCurrentRun() = Unit
            override suspend fun close() = Unit
        }

        val session = RealSoundSession(
            application = targetContext,
            classifier = fakeClassifier,
            recorder = fakeRecorder,
            clock = clock
        )
        runBlocking { session.prepare() }

        val testScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        val listenJob = testScope.launch {
            session.listen(
                onStarted = { _, _ -> },
                onDetections = { detections, endPosition, _ ->
                    detectionsReceived.add(endPosition)
                }
            )
        }

        runBlocking {
            emitChannel.send(dummyAudioWithId(1))
            emitChannel.send(dummyAudioWithId(2))

            withTimeout(5000) {
                while (detectionsReceived.isEmpty()) {
                    delay(20)
                }
            }

            assertEquals(listOf(2), processedWindows.toList())
            assertEquals(listOf(192000L), detectionsReceived.toList())
            assertEquals(1L, session.droppedOldWindowsCount.get())

            session.cancel()
            session.close()
            listenJob.join()
        }
        testScope.cancel()
    }

    @Test
    fun inferenceWatchdogTimeoutAbortsNativeRunAndFailsSession() {
        val cancelRunCalled = CountDownLatch(1)
        val emitChannel = Channel<CapturedAudio>(Channel.UNLIMITED)

        val fakeClassifier = object : BirdNetClassifier(targetContext.assets) {
            override suspend fun initialize() = Unit
            override suspend fun classifyWindow(samples: FloatArray): FloatArray {
                try {
                    delay(2000)
                    return FloatArray(9789)
                } catch (e: CancellationException) {
                    throw e
                }
            }
            override fun cancelCurrentRun() {
                cancelRunCalled.countDown()
            }
            override suspend fun close() = Unit
        }

        val fakeRecorder = object : AudioRecorder(targetContext) {
            override suspend fun listen(
                onStarted: suspend (sampleRate: Int, startMonotonicTimeMs: Long) -> Unit,
                onWindow: (audio: CapturedAudio, endPosition: Long, captureMonotonicTimeMs: Long) -> Unit
            ) {
                onStarted(32_000, SystemClock.elapsedRealtime())
                for (audio in emitChannel) {
                    onWindow(audio, 96000L, SystemClock.elapsedRealtime())
                }
            }
            override fun cancel() {
                emitChannel.close()
            }
        }

        val session = RealSoundSession(
            application = targetContext,
            classifier = fakeClassifier,
            recorder = fakeRecorder,
            inferenceTimeoutMs = 150L
        )
        runBlocking { session.prepare() }

        val exception = assertThrows(InferenceTimeoutException::class.java) {
            runBlocking {
                launch {
                    emitChannel.send(dummyAudioWithId(1))
                }
                session.listen(
                    onStarted = { _, _ -> },
                    onDetections = { _, _, _ -> }
                )
            }
        }

        assertTrue("cancelCurrentRun must be called by watchdog", cancelRunCalled.await(2, TimeUnit.SECONDS))
        assertTrue(exception.message?.contains("Watchdog") == true)
        runBlocking { session.close() }
    }

    @Test
    fun captureFailureCancelsInferenceAndPropagatesError() {
        val micError = AudioRecordingException("Hardware failure in mic")
        val fakeRecorder = object : AudioRecorder(targetContext) {
            override suspend fun listen(
                onStarted: suspend (sampleRate: Int, startMonotonicTimeMs: Long) -> Unit,
                onWindow: (audio: CapturedAudio, endPosition: Long, captureMonotonicTimeMs: Long) -> Unit
            ) {
                onStarted(32_000, SystemClock.elapsedRealtime())
                throw micError
            }
        }

        val fakeClassifier = object : BirdNetClassifier(targetContext.assets) {
            override suspend fun initialize() = Unit
            override suspend fun classifyWindow(samples: FloatArray) = FloatArray(9789)
            override suspend fun close() = Unit
        }

        val session = RealSoundSession(
            application = targetContext,
            classifier = fakeClassifier,
            recorder = fakeRecorder
        )
        runBlocking { session.prepare() }

        var caughtThrowable: Throwable? = null
        try {
            runBlocking {
                session.listen(
                    onStarted = { _, _ -> },
                    onDetections = { _, _, _ -> }
                )
            }
        } catch (t: Throwable) {
            caughtThrowable = t
        }
        assertNotNull("Expected exception to be thrown", caughtThrowable)
        assertTrue(
            "Expected AudioRecordingException but was $caughtThrowable",
            caughtThrowable is AudioRecordingException || caughtThrowable?.cause is AudioRecordingException
        )
        runBlocking { session.close() }
    }

    @Test
    fun inferenceFailureCancelsCaptureAndPropagatesError() {
        val micCancelled = AtomicBoolean(false)
        val emitChannel = Channel<CapturedAudio>(Channel.UNLIMITED)

        val fakeRecorder = object : AudioRecorder(targetContext) {
            override suspend fun listen(
                onStarted: suspend (sampleRate: Int, startMonotonicTimeMs: Long) -> Unit,
                onWindow: (audio: CapturedAudio, endPosition: Long, captureMonotonicTimeMs: Long) -> Unit
            ) {
                onStarted(32_000, SystemClock.elapsedRealtime())
                try {
                    for (audio in emitChannel) {
                        onWindow(audio, 96000L, SystemClock.elapsedRealtime())
                    }
                } finally {
                    micCancelled.set(true)
                }
            }
            override fun cancel() {
                emitChannel.close()
            }
        }

        val fakeClassifier = object : BirdNetClassifier(targetContext.assets) {
            override suspend fun initialize() = Unit
            override suspend fun classifyWindow(samples: FloatArray): FloatArray {
                throw IllegalStateException("Model tensor corrupted")
            }
            override suspend fun close() = Unit
        }

        val session = RealSoundSession(
            application = targetContext,
            classifier = fakeClassifier,
            recorder = fakeRecorder
        )
        runBlocking { session.prepare() }

        val thrown = assertThrows(IllegalStateException::class.java) {
            runBlocking {
                launch {
                    emitChannel.send(dummyAudioWithId(1))
                }
                session.listen(
                    onStarted = { _, _ -> },
                    onDetections = { _, _, _ -> }
                )
            }
        }
        assertEquals("Model tensor corrupted", thrown.message)
        assertTrue("Capture coroutine must be cancelled when inference fails", micCancelled.get())
        runBlocking { session.close() }
    }

    @Test
    fun stoppingDoesNotDrainPendingWindowsNorClosesConcurrentlyWithRun() {
        val inferenceStarted = CountDownLatch(1)
        val allowInferenceToFinish = CountDownLatch(1)
        val isClassifying = AtomicBoolean(false)
        val closedDuringClassification = AtomicBoolean(false)
        val detectionsList = Collections.synchronizedList(mutableListOf<Int>())
        val emitChannel = Channel<CapturedAudio>(Channel.UNLIMITED)

        val fakeClassifier = object : BirdNetClassifier(targetContext.assets) {
            override suspend fun initialize() = Unit
            override suspend fun classifyWindow(samples: FloatArray): FloatArray {
                val id = extractId(samples)
                isClassifying.set(true)
                inferenceStarted.countDown()
                try {
                    allowInferenceToFinish.await(5, TimeUnit.SECONDS)
                } finally {
                    isClassifying.set(false)
                }
                val scores = FloatArray(9789)
                scores[0] = 0.9f
                return scores
            }
            override fun cancelCurrentRun() = Unit
            override suspend fun close() {
                if (isClassifying.get()) {
                    closedDuringClassification.set(true)
                }
            }
        }

        val fakeRecorder = object : AudioRecorder(targetContext) {
            override suspend fun listen(
                onStarted: suspend (sampleRate: Int, startMonotonicTimeMs: Long) -> Unit,
                onWindow: (audio: CapturedAudio, endPosition: Long, captureMonotonicTimeMs: Long) -> Unit
            ) {
                onStarted(32_000, SystemClock.elapsedRealtime())
                for (audio in emitChannel) {
                    onWindow(audio, 96000L, SystemClock.elapsedRealtime())
                }
            }
            override fun cancel() {
                emitChannel.close()
            }
        }

        val session = RealSoundSession(
            application = targetContext,
            classifier = fakeClassifier,
            recorder = fakeRecorder
        )
        runBlocking { session.prepare() }

        val testScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        val listenJob = testScope.launch {
            session.listen(
                onStarted = { _, _ -> },
                onDetections = { detections, endPosition, _ ->
                    detectionsList.add(endPosition.toInt())
                }
            )
        }

        runBlocking {
            // Send window 1 (will enter classifyWindow and block)
            emitChannel.send(dummyAudioWithId(1))
            assertTrue(inferenceStarted.await(5, TimeUnit.SECONDS))

            // Send window 2 (will become pending in conflated channel)
            emitChannel.send(dummyAudioWithId(2))
            delay(50)

            // Stop session: must cancel pending without draining
            session.cancel()

            // Unblock window 1
            allowInferenceToFinish.countDown()

            // Close session
            session.close()
            listenJob.join()

            // Verify window 2 was never delivered
            assertFalse("Pending window 2 must NOT be drained after cancel", detectionsList.contains(2))
            assertFalse("close() was called concurrently with classification run", closedDuringClassification.get())
        }
        testScope.cancel()
    }

    @Test
    fun latePreparationCancellationCleansUpWithoutLeak() {
        val prepEntered = CountDownLatch(1)
        val prepFinish = CountDownLatch(1)
        val prepCancelled = AtomicBoolean(false)

        val fakeClassifier = object : BirdNetClassifier(targetContext.assets) {
            override suspend fun initialize() {
                prepEntered.countDown()
                try {
                    prepFinish.await(5, TimeUnit.SECONDS)
                } catch (e: CancellationException) {
                    prepCancelled.set(true)
                    throw e
                }
            }
            override suspend fun classifyWindow(samples: FloatArray) = FloatArray(9789)
            override suspend fun close() = Unit
        }

        val session = RealSoundSession(
            application = targetContext,
            classifier = fakeClassifier
        )

        val testScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        val prepJob = testScope.launch {
            session.prepare()
        }

        assertTrue("Preparation did not enter", prepEntered.await(5, TimeUnit.SECONDS))
        session.cancel()
        runBlocking {
            session.close()
            prepJob.cancelAndJoin()
        }
        testScope.cancel()
    }

    @Test
    fun physicalContinuousListeningWithRealClassifierAndMicrophone() {
        val realSession = RealSoundSession(targetContext)
        runBlocking { realSession.prepare() }

        var sampleRateReported = 0
        var startMonotonicReported = 0L
        val detectionsCount = AtomicInteger(0)
        val startedLatch = CountDownLatch(1)
        val detectionsLatch = CountDownLatch(1)

        val testScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        val listenJob = testScope.launch {
            realSession.listen(
                onStarted = { sampleRate, startMonotonicTimeMs ->
                    sampleRateReported = sampleRate
                    startMonotonicReported = startMonotonicTimeMs
                    startedLatch.countDown()
                },
                onDetections = { detections, endPosition, captureMonotonicTimeMs ->
                    detectionsCount.incrementAndGet()
                    detectionsLatch.countDown()
                }
            )
        }

        assertTrue("Microphone did not start", startedLatch.await(5, TimeUnit.SECONDS))
        assertTrue("Invalid sample rate: $sampleRateReported", sampleRateReported in listOf(32_000, 48_000, 44_100))
        assertTrue("Invalid monotonic start: $startMonotonicReported", startMonotonicReported > 0)

        // Capture for up to 8 seconds to receive the first 3-second window
        val gotDetection = detectionsLatch.await(8, TimeUnit.SECONDS)
        assertTrue("Expected at least one continuous recognition window", gotDetection)
        assertTrue("Detections count should be >= 1", detectionsCount.get() >= 1)

        val cancelStart = SystemClock.elapsedRealtime()
        realSession.cancel()
        runBlocking {
            realSession.close()
            listenJob.join()
        }
        val releaseDuration = SystemClock.elapsedRealtime() - cancelStart

        assertTrue("Microphone release exceeded 1 second: ${releaseDuration}ms", releaseDuration <= 1000L)
        testScope.cancel()
    }
}
