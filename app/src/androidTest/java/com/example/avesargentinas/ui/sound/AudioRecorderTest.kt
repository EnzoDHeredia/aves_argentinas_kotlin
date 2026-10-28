package com.example.avesargentinas.ui.sound

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class AudioRecorderTest {

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

    @Test
    fun physicalCaptureRecordsTenEffectiveSecondsWithProgress() = runBlocking {
        val recorder = AudioRecorder(targetContext)
        val progressUpdates = mutableListOf<Float>()
        val timestamps = mutableListOf<Long>()

        val audio = recorder.record { progress ->
            progressUpdates.add(progress)
            timestamps.add(SystemClock.elapsedRealtime())
        }

        assertNotNull(audio)
        assertTrue(
            "Frecuencia no soportada: ${audio.sampleRate}",
            audio.sampleRate in listOf(32_000, 48_000, 44_100)
        )
        val expectedSamples = audio.sampleRate * 10
        assertEquals("Muestras efectivas incorrectas para 10 segundos", expectedSamples, audio.samples.size)

        // Verificar progreso
        assertTrue("No hubo actualizaciones de progreso", progressUpdates.isNotEmpty())
        assertEquals("El último progreso debe ser 1.0f", 1.0f, progressUpdates.last(), 0.001f)
        for (i in 1 until progressUpdates.size) {
            assertTrue(
                "El progreso debe ser no decreciente",
                progressUpdates[i] >= progressUpdates[i - 1]
            )
        }

        // Tasa de progreso: como máximo 10 actualizaciones por segundo (intervalos >= 90 ms aprox entre emisiones intermedias)
        for (i in 1 until timestamps.size - 1) {
            val delta = timestamps[i] - timestamps[i - 1]
            assertTrue("Intervalo entre actualizaciones menor a 90 ms: $delta ms", delta >= 90L)
        }

        // Verificar que el micrófono quedó liberado y se puede realizar una nueva captura
        assertFalse("El grabador quedó en estado activo", recorder.isRecordingActive)
    }

    @Test
    fun cancellationReleasesMicrophoneUnderOneSecondAndAllowsRetry() = runBlocking {
        val recorder = AudioRecorder(targetContext)
        val startedLatch = CountDownLatch(1)

        val job = launch(Dispatchers.IO) {
            recorder.record { progress ->
                if (progress > 0f) {
                    startedLatch.countDown()
                }
            }
        }

        assertTrue("La captura no inició en 5 segundos", startedLatch.await(5, TimeUnit.SECONDS))
        delay(200)

        val cancelStart = SystemClock.elapsedRealtime()
        job.cancel()
        job.join()
        val releaseDuration = SystemClock.elapsedRealtime() - cancelStart

        assertTrue("La liberación del micrófono tardó más de 1000 ms: ${releaseDuration} ms", releaseDuration < 1000L)
        assertFalse("El grabador sigue activo tras cancelar", recorder.isRecordingActive)

        // Reintento tras cancelar: debe poder grabar o iniciar sin quedar bloqueado
        val progressList = mutableListOf<Float>()
        val retryJob = launch(Dispatchers.IO) {
            recorder.record { progress ->
                progressList.add(progress)
            }
        }
        delay(300)
        retryJob.cancel()
        retryJob.join()
        assertFalse("El grabador sigue activo tras segundo cancel", recorder.isRecordingActive)
    }

    @Test
    fun cancelMethodExplicitlyCancelsAndReleasesUnderOneSecond() = runBlocking {
        val recorder = AudioRecorder(targetContext)
        val startedLatch = CountDownLatch(1)
        var cancelledEx: Throwable? = null

        val job = launch(Dispatchers.IO) {
            try {
                recorder.record {
                    startedLatch.countDown()
                }
            } catch (t: Throwable) {
                cancelledEx = t
            }
        }

        assertTrue(startedLatch.await(5, TimeUnit.SECONDS))
        val cancelStart = SystemClock.elapsedRealtime()
        recorder.cancel()
        job.join()
        val duration = SystemClock.elapsedRealtime() - cancelStart

        assertTrue("Liberación tardó más de 1000 ms: $duration ms", duration < 1000L)
        assertTrue("Excepción no fue de cancelación: $cancelledEx", cancelledEx is CancellationException)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun initializationFallbackToAlternativeSampleRates() = runBlocking {
        val createdRates = mutableListOf<Int>()
        val fakeFactory = AudioRecordFactory { source, sampleRate, channelConfig, audioFormat, bufferSize ->
            createdRates.add(sampleRate)
            if (sampleRate == 32_000) {
                FakeAudioRecordClient(
                    sampleRate = sampleRate,
                    audioSource = source,
                    initialState = AudioRecord.STATE_UNINITIALIZED
                )
            } else {
                FakeAudioRecordClient(
                    sampleRate = sampleRate,
                    audioSource = source,
                    initialState = AudioRecord.STATE_INITIALIZED,
                    samplesToDeliverPerRead = 4800
                )
            }
        }

        var virtualTime = 1000L
        val recorder = AudioRecorder(
            context = targetContext,
            factory = fakeFactory,
            clock = { virtualTime }
        )

        val audio = recorder.record()
        assertEquals(48_000, audio.sampleRate)
        assertEquals(480_000, audio.samples.size)
        assertTrue("No intentó 32.000 primero", createdRates.first() == 32_000)
        assertTrue("No pasó a 48.000 como alternativa", createdRates.contains(48_000))
    }

    @Test
    fun fallbackTo44100WhenBoth32000And48000Fail() = runBlocking {
        val createdRates = mutableListOf<Int>()
        val fakeFactory = AudioRecordFactory { source, sampleRate, channelConfig, audioFormat, bufferSize ->
            createdRates.add(sampleRate)
            if (sampleRate == 32_000 || sampleRate == 48_000) {
                FakeAudioRecordClient(
                    sampleRate = sampleRate,
                    audioSource = source,
                    initialState = AudioRecord.STATE_UNINITIALIZED
                )
            } else {
                FakeAudioRecordClient(
                    sampleRate = sampleRate,
                    audioSource = source,
                    initialState = AudioRecord.STATE_INITIALIZED,
                    samplesToDeliverPerRead = 4410
                )
            }
        }

        val recorder = AudioRecorder(
            context = targetContext,
            factory = fakeFactory,
            clock = { 1000L }
        )

        val audio = recorder.record()
        assertEquals(44_100, audio.sampleRate)
        assertEquals(441_000, audio.samples.size)
        assertEquals(listOf(32_000, 48_000, 44_100), createdRates)
    }

    @Test
    fun allSampleRatesFailInitializationThrowsAudioRecordingException() = runBlocking {
        val fakeFactory = AudioRecordFactory { source, sampleRate, channelConfig, audioFormat, bufferSize ->
            FakeAudioRecordClient(
                sampleRate = sampleRate,
                audioSource = source,
                initialState = AudioRecord.STATE_UNINITIALIZED
            )
        }

        val recorder = AudioRecorder(
            context = targetContext,
            factory = fakeFactory,
            clock = { 1000L }
        )

        val ex = assertThrows(AudioRecordingException::class.java) {
            runBlocking { recorder.record() }
        }
        assertTrue(ex.message?.contains("AudioRecord") == true)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun startRecordingFailureThrowsExceptionAndReleases() = runBlocking {
        val fakeFactory = AudioRecordFactory { source, sampleRate, channelConfig, audioFormat, bufferSize ->
            FakeAudioRecordClient(
                sampleRate = sampleRate,
                audioSource = source,
                initialState = AudioRecord.STATE_INITIALIZED,
                failStartRecording = true
            )
        }

        val recorder = AudioRecorder(
            context = targetContext,
            factory = fakeFactory,
            clock = { 1000L }
        )

        val ex = assertThrows(AudioRecordingException::class.java) {
            runBlocking { recorder.record() }
        }
        assertTrue(ex.message?.contains("iniciar") == true)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun stallWatchdogTriggersWhenNoSamplesArriveForTwoSeconds() = runBlocking {
        var virtualTime = 1000L
        val fakeClient = FakeAudioRecordClient(
            sampleRate = 32_000,
            audioSource = MediaRecorder.AudioSource.MIC,
            initialState = AudioRecord.STATE_INITIALIZED,
            samplesToDeliverPerRead = 0 // siempre retorna 0
        )

        val recorder = AudioRecorder(
            context = targetContext,
            factory = { _, _, _, _, _ -> fakeClient },
            clock = {
                virtualTime += 500L
                virtualTime
            }
        )

        val ex = assertThrows(AudioRecordingException::class.java) {
            runBlocking { recorder.record() }
        }
        assertTrue("Excepción debe mencionar watchdog/2 segundos: ${ex.message}", ex.message?.contains("2 segundos") == true)
        assertTrue("El cliente debe ser liberado", fakeClient.isReleased)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun totalTimeoutWatchdogTriggersAtFifteenSeconds() = runBlocking {
        var virtualTime = 1000L
        val fakeClient = FakeAudioRecordClient(
            sampleRate = 32_000,
            audioSource = MediaRecorder.AudioSource.MIC,
            initialState = AudioRecord.STATE_INITIALIZED,
            samplesToDeliverPerRead = 10
        )

        val recorder = AudioRecorder(
            context = targetContext,
            factory = { _, _, _, _, _ -> fakeClient },
            clock = {
                virtualTime += 100L // 100 ms por consulta; como se leen muestras cada vez, el stall watchdog de 2s no salta
                virtualTime
            }
        )

        val ex = assertThrows(AudioRecordingException::class.java) {
            runBlocking { recorder.record() }
        }
        assertTrue("Excepción debe mencionar timeout de 15 segundos: ${ex.message}", ex.message?.contains("15 segundos") == true)
        assertTrue("El cliente debe ser liberado", fakeClient.isReleased)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun hardwareReadErrorThrowsExceptionAndReleasesMicrophone() = runBlocking {
        val fakeClient = FakeAudioRecordClient(
            sampleRate = 32_000,
            audioSource = MediaRecorder.AudioSource.MIC,
            initialState = AudioRecord.STATE_INITIALIZED,
            errorCodeToReturn = AudioRecord.ERROR_DEAD_OBJECT
        )

        val recorder = AudioRecorder(
            context = targetContext,
            factory = { _, _, _, _, _ -> fakeClient },
            clock = { 1000L }
        )

        val ex = assertThrows(AudioRecordingException::class.java) {
            runBlocking { recorder.record() }
        }
        assertTrue(ex.message?.contains("lectura") == true || ex.message?.contains("${AudioRecord.ERROR_DEAD_OBJECT}") == true)
        assertTrue(fakeClient.isReleased)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun silencedClientOnApi29ThrowsException() = runBlocking {
        val fakeClient = FakeAudioRecordClient(
            sampleRate = 32_000,
            audioSource = MediaRecorder.AudioSource.MIC,
            initialState = AudioRecord.STATE_INITIALIZED,
            silenced = true
        )

        val recorder = AudioRecorder(
            context = targetContext,
            factory = { _, _, _, _, _ -> fakeClient },
            clock = { 1000L }
        )

        val ex = assertThrows(AudioRecordingException::class.java) {
            runBlocking { recorder.record() }
        }
        assertTrue("Mensaje debe indicar captura silenciada: ${ex.message}", ex.message?.contains("silenciada") == true)
        assertTrue(fakeClient.isReleased)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun permissionDeniedThrowsSecurityException() = runBlocking {
        val deniedContext = object : ContextWrapper(targetContext) {
            override fun checkPermission(permission: String, pid: Int, uid: Int): Int {
                if (permission == Manifest.permission.RECORD_AUDIO) {
                    return PackageManager.PERMISSION_DENIED
                }
                return super.checkPermission(permission, pid, uid)
            }

            override fun checkSelfPermission(permission: String): Int {
                if (permission == Manifest.permission.RECORD_AUDIO) {
                    return PackageManager.PERMISSION_DENIED
                }
                return super.checkSelfPermission(permission)
            }
        }

        val recorder = AudioRecorder(deniedContext)
        assertThrows(SecurityException::class.java) {
            runBlocking { recorder.record() }
        }
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun permissionRevokedDuringRecordingThrowsSecurityException() = runBlocking {
        var permissionGranted = true
        val revocableContext = object : ContextWrapper(targetContext) {
            override fun checkPermission(permission: String, pid: Int, uid: Int): Int {
                if (permission == Manifest.permission.RECORD_AUDIO && !permissionGranted) {
                    return PackageManager.PERMISSION_DENIED
                }
                return super.checkPermission(permission, pid, uid)
            }

            override fun checkSelfPermission(permission: String): Int {
                if (permission == Manifest.permission.RECORD_AUDIO && !permissionGranted) {
                    return PackageManager.PERMISSION_DENIED
                }
                return super.checkSelfPermission(permission)
            }
        }

        val fakeClient = FakeAudioRecordClient(
            sampleRate = 32_000,
            audioSource = MediaRecorder.AudioSource.MIC,
            initialState = AudioRecord.STATE_INITIALIZED,
            samplesToDeliverPerRead = 1000
        )

        var virtualTime = 1000L
        val recorder = AudioRecorder(
            context = revocableContext,
            factory = { _, _, _, _, _ -> fakeClient },
            clock = {
                virtualTime += 100L
                virtualTime
            }
        )

        val ex = assertThrows(SecurityException::class.java) {
            runBlocking {
                recorder.record { progress ->
                    if (progress > 0.05f) {
                        permissionGranted = false
                    }
                }
            }
        }
        assertTrue("Excepción debe indicar permiso revocado: ${ex.message}", ex.message?.contains("revocado") == true)
        assertTrue("El cliente debe ser liberado", fakeClient.isReleased)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun concurrentRecordingBlocked() = runBlocking {
        val recorder = AudioRecorder(targetContext)
        val started = CountDownLatch(1)
        val finished = CountDownLatch(1)

        val job = launch(Dispatchers.IO) {
            try {
                recorder.record {
                    started.countDown()
                }
            } finally {
                finished.countDown()
            }
        }

        assertTrue(started.await(5, TimeUnit.SECONDS))

        assertThrows(IllegalStateException::class.java) {
            runBlocking { recorder.record() }
        }

        recorder.cancel()
        job.join()
        finished.await(2, TimeUnit.SECONDS)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun noAudioFilesCreatedInStorage() = runBlocking {
        val beforeFiles = listAudioFiles(targetContext.filesDir) +
                listAudioFiles(targetContext.cacheDir) +
                listAudioFiles(targetContext.externalCacheDir)

        val recorder = AudioRecorder(targetContext)
        val audio = recorder.record()
        assertNotNull(audio)

        val afterFiles = listAudioFiles(targetContext.filesDir) +
                listAudioFiles(targetContext.cacheDir) +
                listAudioFiles(targetContext.externalCacheDir)

        val created = afterFiles - beforeFiles.toSet()
        assertTrue("Se crearon archivos de audio en disco: $created", created.isEmpty())
    }

    private fun listAudioFiles(dir: File?): List<File> {
        if (dir == null || !dir.exists()) return emptyList()
        return dir.walkTopDown().filter { file ->
            file.isFile && (file.extension.equals("wav", true) ||
                    file.extension.equals("pcm", true) ||
                    file.extension.equals("mp3", true) ||
                    file.extension.equals("m4a", true) ||
                    file.name.contains("audio", true) ||
                    file.name.contains("record", true))
        }.toList()
    }

    @Test
    fun physicalContinuousListeningSixtySecondsWithoutRestartOrTenFifteenSecondLimit() = runBlocking {
        val recorder = AudioRecorder(targetContext)
        var startedSampleRate = 0
        var startedClockOrigin = 0L
        val startCount = java.util.concurrent.atomic.AtomicInteger(0)
        val windows = java.util.Collections.synchronizedList(mutableListOf<AudioWindow>())
        val windowTimestamps = java.util.Collections.synchronizedList(mutableListOf<Long>())

        val testStart = SystemClock.elapsedRealtime()
        val job = launch(Dispatchers.IO) {
            recorder.listen(
                onStarted = { rate, startMonotonic ->
                    startedSampleRate = rate
                    startedClockOrigin = startMonotonic
                    startCount.incrementAndGet()
                },
                onWindow = { audio, endPos, captureTime ->
                    windows.add(AudioWindow(audio, endPos))
                    windowTimestamps.add(captureTime)
                }
            )
        }

        // Aguardar 60 segundos de captura física continua
        while (SystemClock.elapsedRealtime() - testStart < 60_000L) {
            delay(200)
            assertTrue("La escucha se detuvo antes de los 60 segundos", job.isActive)
        }

        val elapsed = SystemClock.elapsedRealtime() - testStart
        assertTrue("La escucha no duró al menos 60 segundos: $elapsed ms", elapsed >= 60_000L)

        // Comprobar que superó holgadamente los 10 y 15 segundos sin interrupción ni reinicio
        assertEquals("AudioRecord debe iniciarse una única vez", 1, startCount.get())
        assertTrue("Frecuencia no soportada: $startedSampleRate", startedSampleRate in listOf(32_000, 48_000, 44_100))
        assertTrue("Origen de reloj debe ser positivo", startedClockOrigin > 0L)

        // Cancelar y medir tiempo de liberación
        val cancelStart = SystemClock.elapsedRealtime()
        job.cancel()
        job.join()
        val releaseDuration = SystemClock.elapsedRealtime() - cancelStart

        assertTrue("La liberación del micrófono tardó más de 1000 ms: ${releaseDuration} ms", releaseDuration <= 1000L)
        assertFalse("El grabador sigue activo tras cancelar", recorder.isRecordingActive)

        // En 60 segundos con ventanas de 3s cada 1s: primera en 3s, siguientes en 4, 5... 60s
        // Se esperan al menos 57 ventanas (típicamente 58 a 60 según precisión del reloj)
        assertTrue("Ventanas insuficientes en 60 segundos: ${windows.size}", windows.size >= 57)

        val rate = startedSampleRate
        for (i in 0 until windows.size) {
            val w = windows[i]
            assertEquals("Frecuencia de ventana $i incorrecta", rate, w.audio.sampleRate)
            assertEquals("Tamaño de ventana $i incorrecto", rate * 3, w.audio.samples.size)
            val expectedEnd = (3L + i) * rate
            assertEquals("Posición final de ventana $i incorrecta", expectedEnd, w.endPosition)
        }

        for (i in 1 until windowTimestamps.size) {
            assertTrue("Las marcas de tiempo de las ventanas deben ser no decrecientes", windowTimestamps[i] >= windowTimestamps[i - 1])
        }
    }

    @Test
    fun listenCancellationReleasesMicrophoneUnderOneSecondAndAllowsRetry() = runBlocking {
        val recorder = AudioRecorder(targetContext)
        val startedLatch = CountDownLatch(1)

        val job = launch(Dispatchers.IO) {
            recorder.listen(
                onStarted = { _, _ -> startedLatch.countDown() },
                onWindow = { _, _, _ -> }
            )
        }

        assertTrue("La escucha no inició en 5 segundos", startedLatch.await(5, TimeUnit.SECONDS))
        delay(300)

        val cancelStart = SystemClock.elapsedRealtime()
        job.cancel()
        job.join()
        val releaseDuration = SystemClock.elapsedRealtime() - cancelStart

        assertTrue("La liberación tardó más de 1000 ms: ${releaseDuration} ms", releaseDuration <= 1000L)
        assertFalse("El grabador sigue activo tras cancelar", recorder.isRecordingActive)

        // Reintento tras cancelar: debe poder escuchar de nuevo sin quedar bloqueado
        val retryStartedLatch = CountDownLatch(1)
        val retryJob = launch(Dispatchers.IO) {
            recorder.listen(
                onStarted = { _, _ -> retryStartedLatch.countDown() },
                onWindow = { _, _, _ -> }
            )
        }
        assertTrue("El reintento no inició en 5 segundos", retryStartedLatch.await(5, TimeUnit.SECONDS))
        delay(300)
        retryJob.cancel()
        retryJob.join()
        assertFalse("El grabador sigue activo tras segundo cancel", recorder.isRecordingActive)
    }

    @Test
    fun listenCancelMethodExplicitlyCancelsAndReleasesUnderOneSecond() = runBlocking {
        val recorder = AudioRecorder(targetContext)
        val startedLatch = CountDownLatch(1)
        var cancelledEx: Throwable? = null

        val job = launch(Dispatchers.IO) {
            try {
                recorder.listen(
                    onStarted = { _, _ -> startedLatch.countDown() },
                    onWindow = { _, _, _ -> }
                )
            } catch (t: Throwable) {
                cancelledEx = t
            }
        }

        assertTrue(startedLatch.await(5, TimeUnit.SECONDS))
        val cancelStart = SystemClock.elapsedRealtime()
        recorder.cancel()
        job.join()
        val duration = SystemClock.elapsedRealtime() - cancelStart

        assertTrue("Liberación tardó más de 1000 ms: $duration ms", duration <= 1000L)
        assertTrue("Excepción no fue de cancelación: $cancelledEx", cancelledEx is CancellationException)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun listenWatchdogTriggersWhenNoSamplesArriveForTwoSeconds() = runBlocking {
        var virtualTime = 1000L
        val fakeClient = FakeAudioRecordClient(
            sampleRate = 32_000,
            audioSource = MediaRecorder.AudioSource.MIC,
            initialState = AudioRecord.STATE_INITIALIZED,
            samplesToDeliverPerRead = 0 // siempre retorna 0
        )

        val recorder = AudioRecorder(
            context = targetContext,
            factory = { _, _, _, _, _ -> fakeClient },
            clock = {
                virtualTime += 500L
                virtualTime
            }
        )

        val ex = assertThrows(AudioRecordingException::class.java) {
            runBlocking {
                recorder.listen(onWindow = { _, _, _ -> })
            }
        }
        assertTrue("Excepción debe mencionar watchdog/2 segundos: ${ex.message}", ex.message?.contains("2 segundos") == true)
        assertTrue("El cliente debe ser liberado", fakeClient.isReleased)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun listenDoesNotHaveFifteenSecondTimeoutAndContinuouslyEmitsWindows() = runBlocking {
        var virtualTime = 1000L
        val fakeClient = FakeAudioRecordClient(
            sampleRate = 32_000,
            audioSource = MediaRecorder.AudioSource.MIC,
            initialState = AudioRecord.STATE_INITIALIZED,
            samplesToDeliverPerRead = 1024
        )

        val recorder = AudioRecorder(
            context = targetContext,
            factory = { _, _, _, _, _ -> fakeClient },
            clock = {
                virtualTime += 10L // avanza 10 ms por lectura -> sin watchdog de 2s
                virtualTime
            }
        )

        val windows = mutableListOf<AudioWindow>()
        val job = launch(Dispatchers.IO) {
            recorder.listen(
                onWindow = { audio, endPos, _ ->
                    windows.add(AudioWindow(audio, endPos))
                }
            )
        }

        // Dejar que avance virtualmente hasta 25 segundos (25.000 ms), superando 15s
        while (virtualTime < 26_000L) {
            delay(10)
        }

        // Debe haber emitido ventanas superando los 15 segundos sin lanzar excepción
        assertTrue("La corrutina no debe haber fallado por timeout de 15s", job.isActive)
        assertTrue("Debe haber emitido más de 15 ventanas: ${windows.size}", windows.size >= 15)

        recorder.cancel()
        job.join()
        assertTrue(fakeClient.isReleased)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun listenHardwareReadErrorThrowsExceptionAndReleasesMicrophone() = runBlocking {
        val fakeClient = FakeAudioRecordClient(
            sampleRate = 32_000,
            audioSource = MediaRecorder.AudioSource.MIC,
            initialState = AudioRecord.STATE_INITIALIZED,
            errorCodeToReturn = AudioRecord.ERROR_DEAD_OBJECT
        )

        val recorder = AudioRecorder(
            context = targetContext,
            factory = { _, _, _, _, _ -> fakeClient },
            clock = { 1000L }
        )

        val ex = assertThrows(AudioRecordingException::class.java) {
            runBlocking {
                recorder.listen(onWindow = { _, _, _ -> })
            }
        }
        assertTrue(ex.message?.contains("lectura") == true || ex.message?.contains("${AudioRecord.ERROR_DEAD_OBJECT}") == true)
        assertTrue(fakeClient.isReleased)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun listenSilencedClientOnApi29ThrowsException() = runBlocking {
        val fakeClient = FakeAudioRecordClient(
            sampleRate = 32_000,
            audioSource = MediaRecorder.AudioSource.MIC,
            initialState = AudioRecord.STATE_INITIALIZED,
            silenced = true
        )

        val recorder = AudioRecorder(
            context = targetContext,
            factory = { _, _, _, _, _ -> fakeClient },
            clock = { 1000L }
        )

        val ex = assertThrows(AudioRecordingException::class.java) {
            runBlocking {
                recorder.listen(onWindow = { _, _, _ -> })
            }
        }
        assertTrue("Mensaje debe indicar captura silenciada: ${ex.message}", ex.message?.contains("silenciada") == true)
        assertTrue(fakeClient.isReleased)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun listenPermissionDeniedThrowsSecurityException() = runBlocking {
        val deniedContext = object : ContextWrapper(targetContext) {
            override fun checkPermission(permission: String, pid: Int, uid: Int): Int {
                if (permission == Manifest.permission.RECORD_AUDIO) {
                    return PackageManager.PERMISSION_DENIED
                }
                return super.checkPermission(permission, pid, uid)
            }

            override fun checkSelfPermission(permission: String): Int {
                if (permission == Manifest.permission.RECORD_AUDIO) {
                    return PackageManager.PERMISSION_DENIED
                }
                return super.checkSelfPermission(permission)
            }
        }

        val recorder = AudioRecorder(deniedContext)
        assertThrows(SecurityException::class.java) {
            runBlocking {
                recorder.listen(onWindow = { _, _, _ -> })
            }
        }
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun listenPermissionRevokedDuringRecordingThrowsSecurityException() = runBlocking {
        var permissionGranted = true
        val revocableContext = object : ContextWrapper(targetContext) {
            override fun checkPermission(permission: String, pid: Int, uid: Int): Int {
                if (permission == Manifest.permission.RECORD_AUDIO && !permissionGranted) {
                    return PackageManager.PERMISSION_DENIED
                }
                return super.checkPermission(permission, pid, uid)
            }

            override fun checkSelfPermission(permission: String): Int {
                if (permission == Manifest.permission.RECORD_AUDIO && !permissionGranted) {
                    return PackageManager.PERMISSION_DENIED
                }
                return super.checkSelfPermission(permission)
            }
        }

        val fakeClient = FakeAudioRecordClient(
            sampleRate = 32_000,
            audioSource = MediaRecorder.AudioSource.MIC,
            initialState = AudioRecord.STATE_INITIALIZED,
            samplesToDeliverPerRead = 1000
        )

        var virtualTime = 1000L
        val recorder = AudioRecorder(
            context = revocableContext,
            factory = { _, _, _, _, _ -> fakeClient },
            clock = {
                virtualTime += 100L
                virtualTime
            }
        )

        val ex = assertThrows(SecurityException::class.java) {
            runBlocking {
                recorder.listen(
                    onWindow = { _, endPosition, _ ->
                        if (endPosition >= 32_000 * 3) {
                            permissionGranted = false
                        }
                    }
                )
            }
        }
        assertTrue("Excepción debe indicar permiso revocado: ${ex.message}", ex.message?.contains("revocado") == true)
        assertTrue("El cliente debe ser liberado", fakeClient.isReleased)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun listenConcurrentRecordingOrListeningBlocked() = runBlocking {
        val recorder = AudioRecorder(targetContext)
        val started = CountDownLatch(1)
        val finished = CountDownLatch(1)

        val job = launch(Dispatchers.IO) {
            try {
                recorder.listen(
                    onStarted = { _, _ -> started.countDown() },
                    onWindow = { _, _, _ -> }
                )
            } finally {
                finished.countDown()
            }
        }

        assertTrue(started.await(5, TimeUnit.SECONDS))

        assertThrows(IllegalStateException::class.java) {
            runBlocking { recorder.listen(onWindow = { _, _, _ -> }) }
        }

        assertThrows(IllegalStateException::class.java) {
            runBlocking { recorder.record() }
        }

        recorder.cancel()
        job.join()
        finished.await(2, TimeUnit.SECONDS)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun listenInitializationFallbackToAlternativeSampleRates() = runBlocking {
        val createdRates = mutableListOf<Int>()
        val fakeFactory = AudioRecordFactory { source, sampleRate, channelConfig, audioFormat, bufferSize ->
            createdRates.add(sampleRate)
            if (sampleRate == 32_000) {
                FakeAudioRecordClient(
                    sampleRate = sampleRate,
                    audioSource = source,
                    initialState = AudioRecord.STATE_UNINITIALIZED
                )
            } else {
                FakeAudioRecordClient(
                    sampleRate = sampleRate,
                    audioSource = source,
                    initialState = AudioRecord.STATE_INITIALIZED,
                    samplesToDeliverPerRead = 4800
                )
            }
        }

        val recorder = AudioRecorder(
            context = targetContext,
            factory = fakeFactory,
            clock = { 1000L }
        )

        var startedRate = 0
        var windowReceived: CapturedAudio? = null
        val job = launch(Dispatchers.IO) {
            recorder.listen(
                onStarted = { rate, _ -> startedRate = rate },
                onWindow = { audio, _, _ -> windowReceived = audio }
            )
        }

        delay(300)
        recorder.cancel()
        job.join()

        assertEquals(48_000, startedRate)
        assertNotNull(windowReceived)
        assertEquals(48_000, windowReceived!!.sampleRate)
        assertEquals(48_000 * 3, windowReceived!!.samples.size)
        assertTrue("No intentó 32.000 primero", createdRates.first() == 32_000)
        assertTrue("No pasó a 48.000 como alternativa", createdRates.contains(48_000))
    }

    @Test
    fun listenFallbackTo44100WhenBoth32000And48000Fail() = runBlocking {
        val createdRates = mutableListOf<Int>()
        val fakeFactory = AudioRecordFactory { source, sampleRate, channelConfig, audioFormat, bufferSize ->
            createdRates.add(sampleRate)
            if (sampleRate == 32_000 || sampleRate == 48_000) {
                FakeAudioRecordClient(
                    sampleRate = sampleRate,
                    audioSource = source,
                    initialState = AudioRecord.STATE_UNINITIALIZED
                )
            } else {
                FakeAudioRecordClient(
                    sampleRate = sampleRate,
                    audioSource = source,
                    initialState = AudioRecord.STATE_INITIALIZED,
                    samplesToDeliverPerRead = 4410
                )
            }
        }

        val recorder = AudioRecorder(
            context = targetContext,
            factory = fakeFactory,
            clock = { 1000L }
        )

        var startedRate = 0
        var windowReceived: CapturedAudio? = null
        val job = launch(Dispatchers.IO) {
            recorder.listen(
                onStarted = { rate, _ -> startedRate = rate },
                onWindow = { audio, _, _ -> windowReceived = audio }
            )
        }

        delay(300)
        recorder.cancel()
        job.join()

        assertEquals(44_100, startedRate)
        assertNotNull(windowReceived)
        assertEquals(44_100, windowReceived!!.sampleRate)
        assertEquals(44_100 * 3, windowReceived!!.samples.size)
        assertEquals(listOf(32_000, 48_000, 44_100), createdRates)
    }

    @Test
    fun listenAllSampleRatesFailInitializationThrowsAudioRecordingException() = runBlocking {
        val fakeFactory = AudioRecordFactory { source, sampleRate, channelConfig, audioFormat, bufferSize ->
            FakeAudioRecordClient(
                sampleRate = sampleRate,
                audioSource = source,
                initialState = AudioRecord.STATE_UNINITIALIZED
            )
        }

        val recorder = AudioRecorder(
            context = targetContext,
            factory = fakeFactory,
            clock = { 1000L }
        )

        val ex = assertThrows(AudioRecordingException::class.java) {
            runBlocking { recorder.listen(onWindow = { _, _, _ -> }) }
        }
        assertTrue(ex.message?.contains("AudioRecord") == true)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun listenStartRecordingFailureThrowsExceptionAndReleases() = runBlocking {
        val fakeFactory = AudioRecordFactory { source, sampleRate, channelConfig, audioFormat, bufferSize ->
            FakeAudioRecordClient(
                sampleRate = sampleRate,
                audioSource = source,
                initialState = AudioRecord.STATE_INITIALIZED,
                failStartRecording = true
            )
        }

        val recorder = AudioRecorder(
            context = targetContext,
            factory = fakeFactory,
            clock = { 1000L }
        )

        val ex = assertThrows(AudioRecordingException::class.java) {
            runBlocking { recorder.listen(onWindow = { _, _, _ -> }) }
        }
        assertTrue(ex.message?.contains("iniciar") == true)
        assertFalse(recorder.isRecordingActive)
    }

    @Test
    fun listenNoAudioFilesCreatedInStorage() = runBlocking {
        val beforeFiles = listAudioFiles(targetContext.filesDir) +
                listAudioFiles(targetContext.cacheDir) +
                listAudioFiles(targetContext.externalCacheDir)

        val recorder = AudioRecorder(targetContext)
        val startedLatch = CountDownLatch(1)
        val job = launch(Dispatchers.IO) {
            recorder.listen(
                onStarted = { _, _ -> startedLatch.countDown() },
                onWindow = { _, _, _ -> }
            )
        }
        assertTrue(startedLatch.await(5, TimeUnit.SECONDS))
        delay(1500) // escucha 1.5s
        recorder.cancel()
        job.join()

        val afterFiles = listAudioFiles(targetContext.filesDir) +
                listAudioFiles(targetContext.cacheDir) +
                listAudioFiles(targetContext.externalCacheDir)

        val created = afterFiles - beforeFiles.toSet()
        assertTrue("Se crearon archivos de audio en disco durante listen: $created", created.isEmpty())
    }
}

internal class FakeAudioRecordClient(
    override val sampleRate: Int,
    override val audioSource: Int,
    private val initialState: Int = AudioRecord.STATE_INITIALIZED,
    private val samplesToDeliverPerRead: Int = 1024,
    private val failStartRecording: Boolean = false,
    private val errorCodeToReturn: Int? = null,
    private val silenced: Boolean = false
) : AudioRecordClient {

    private var _state = initialState
    private var _recordingState = AudioRecord.RECORDSTATE_STOPPED
    var isReleased = false
        private set

    override val state: Int get() = _state
    override val recordingState: Int get() = _recordingState
    override val isClientSilenced: Boolean get() = silenced

    override fun startRecording() {
        if (!failStartRecording && _state == AudioRecord.STATE_INITIALIZED) {
            _recordingState = AudioRecord.RECORDSTATE_RECORDING
        }
    }

    override fun stop() {
        _recordingState = AudioRecord.RECORDSTATE_STOPPED
    }

    override fun release() {
        _recordingState = AudioRecord.RECORDSTATE_STOPPED
        _state = AudioRecord.STATE_UNINITIALIZED
        isReleased = true
    }

    override fun read(audioData: ShortArray, offsetInShorts: Int, sizeInShorts: Int, readMode: Int): Int {
        if (_recordingState != AudioRecord.RECORDSTATE_RECORDING) {
            return AudioRecord.ERROR_INVALID_OPERATION
        }
        if (errorCodeToReturn != null) {
            return errorCodeToReturn
        }
        if (samplesToDeliverPerRead == 0) {
            return 0
        }
        val count = minOf(sizeInShorts, samplesToDeliverPerRead)
        for (i in 0 until count) {
            audioData[offsetInShorts + i] = 100
        }
        return count
    }
}
