package com.example.avesargentinas.ui.sound

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class BirdSoundViewModelTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val app = instrumentation.targetContext.applicationContext as Application

    private fun main(block: () -> Unit) = instrumentation.runOnMainSync(block)

    private fun await(condition: () -> Boolean) {
        val end = System.currentTimeMillis() + 5000
        while (!condition() && System.currentTimeMillis() < end) Thread.sleep(10)
        assertTrue("Condition was not met within 5000ms", condition())
    }

    @Test
    fun accumulationWithSixOrMoreBirdsPreservesOrderDuplicatesUpdateOnlyIfHigherScore() {
        var onDetectionsCb: (suspend (List<BirdDetection>, Long, Long) -> Unit)? = null
        val sessionStarted = CountDownLatch(1)

        val bird1 = BirdDetection(1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.90f, "hornero", null)
        val bird2 = BirdDetection(2, "Pitangus sulphuratus", "Pitangus sulphuratus", "Benteveo", 0.85f, "benteveo", null)
        val bird3 = BirdDetection(3, "Turdus rufiventris", "Turdus rufiventris", "Zorzal Colorado", 0.76f, "zorzal_colorado", null)
        val bird4 = BirdDetection(4, "Zonotrichia capensis", "Zonotrichia capensis", "Chingolo", 0.65f, "chingolo", null)
        val bird5 = BirdDetection(5, "Colaptes campestris", "Colaptes campestris", "Carpintero Campestre", 0.55f, "carpintero", null)
        val bird6 = BirdDetection(6, "Milvago chimango", "Milvago chimango", "Chimango", 0.50f, "chimango", null)

        val vm = BirdSoundViewModel(app) {
            object : SoundSession {
                override suspend fun prepare() = Unit
                override suspend fun listen(
                    onStarted: suspend (Int, Long) -> Unit,
                    onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
                ) {
                    onDetectionsCb = onDetections
                    onStarted(32000, 1000L)
                    sessionStarted.countDown()
                    awaitCancellation()
                }
                override fun cancel() = Unit
                override suspend fun close() = Unit
            }
        }

        main { vm.start() }
        assertTrue(sessionStarted.await(5, TimeUnit.SECONDS))
        await { vm.state.value.phase == BirdSoundPhase.LISTENING }

        // Ventana 1: 6 aves iniciales
        runBlocking {
            onDetectionsCb?.invoke(listOf(bird1, bird2, bird3, bird4, bird5, bird6), 96000L, 2000L)
        }
        await { vm.state.value.detections.size == 6 }

        val firstList = vm.state.value.detections
        assertEquals("Furnarius rufus", firstList[0].scientificName)
        assertEquals("Pitangus sulphuratus", firstList[1].scientificName)
        assertEquals("Turdus rufiventris", firstList[2].scientificName)
        assertEquals("Zonotrichia capensis", firstList[3].scientificName)
        assertEquals("Colaptes campestris", firstList[4].scientificName)
        assertEquals("Milvago chimango", firstList[5].scientificName)

        // Ventana 2:
        // - bird2 con mejor puntuación (0.85 -> 0.95) -> Debe actualizarse en su posición 1
        // - bird3 con menor puntuación (0.76 -> 0.70) -> Debe conservar 0.76
        // - bird4 con igual puntuación (0.65 -> 0.65) -> Debe conservar 0.65
        // - bird7 nueva especie -> Debe agregarse al final (posición 6)
        val bird2Better = bird2.copy(score = 0.95f)
        val bird3Worse = bird3.copy(score = 0.70f)
        val bird4Equal = bird4.copy(score = 0.65f)
        val bird7 = BirdDetection(7, "Sicalis flaveola", "Sicalis flaveola", "Jilguero Dorado", 0.80f, "jilguero", null)

        runBlocking {
            onDetectionsCb?.invoke(listOf(bird2Better, bird3Worse, bird4Equal, bird7), 192000L, 3000L)
        }
        await { vm.state.value.detections.size == 7 }

        val secondList = vm.state.value.detections
        assertEquals(7, secondList.size)
        // Posición 0: Hornero intacto
        assertEquals("Furnarius rufus", secondList[0].scientificName)
        assertEquals(0.90f, secondList[0].score, 0.001f)
        // Posición 1: Benteveo conserva posición y mejora puntuación
        assertEquals("Pitangus sulphuratus", secondList[1].scientificName)
        assertEquals(0.95f, secondList[1].score, 0.001f)
        // Posición 2: Zorzal conserva posición y conserva puntuación máxima anterior
        assertEquals("Turdus rufiventris", secondList[2].scientificName)
        assertEquals(0.76f, secondList[2].score, 0.001f)
        // Posición 3: Chingolo conserva posición y puntuación
        assertEquals("Zonotrichia capensis", secondList[3].scientificName)
        assertEquals(0.65f, secondList[3].score, 0.001f)
        // Posición 4 y 5 conservadas
        assertEquals("Colaptes campestris", secondList[4].scientificName)
        assertEquals("Milvago chimango", secondList[5].scientificName)
        // Posición 6: Jilguero agregado al final
        assertEquals("Sicalis flaveola", secondList[6].scientificName)
        assertEquals(0.80f, secondList[6].score, 0.001f)

        main { vm.abandon() }
    }

    @Test
    fun emptyDetectionsWindowDoesNotClearOrReorderList() {
        var onDetectionsCb: (suspend (List<BirdDetection>, Long, Long) -> Unit)? = null
        val started = CountDownLatch(1)

        val bird = BirdDetection(1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.90f, "hornero", null)

        val vm = BirdSoundViewModel(app) {
            object : SoundSession {
                override suspend fun prepare() = Unit
                override suspend fun listen(
                    onStarted: suspend (Int, Long) -> Unit,
                    onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
                ) {
                    onDetectionsCb = onDetections
                    onStarted(32000, 1000L)
                    started.countDown()
                    awaitCancellation()
                }
                override fun cancel() = Unit
                override suspend fun close() = Unit
            }
        }

        main { vm.start() }
        assertTrue(started.await(5, TimeUnit.SECONDS))
        await { vm.state.value.phase == BirdSoundPhase.LISTENING }

        runBlocking { onDetectionsCb?.invoke(listOf(bird), 96000L, 2000L) }
        await { vm.state.value.detections.size == 1 }

        val originalSnapshot = vm.state.value.detections

        // Ventana vacía
        runBlocking { onDetectionsCb?.invoke(emptyList(), 192000L, 3000L) }
        instrumentation.waitForIdleSync()

        assertEquals(1, vm.state.value.detections.size)
        assertSame("La instancia de lista no debe cambiar si no hubo detecciones nuevas", originalSnapshot, vm.state.value.detections)

        main { vm.abandon() }
    }

    @Test
    fun clockTicksDoNotRebuildOrChangeDetectionsListReference() {
        var simulatedClock = 1000L
        val started = CountDownLatch(1)

        val bird = BirdDetection(1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.90f, "hornero", null)

        val vm = BirdSoundViewModel(app, clock = { simulatedClock }) {
            object : SoundSession {
                override suspend fun prepare() = Unit
                override suspend fun listen(
                    onStarted: suspend (Int, Long) -> Unit,
                    onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
                ) {
                    onStarted(32000, 1000L)
                    onDetections(listOf(bird), 96000L, 1000L)
                    started.countDown()
                    awaitCancellation()
                }
                override fun cancel() = Unit
                override suspend fun close() = Unit
            }
        }

        main { vm.start() }
        assertTrue(started.await(5, TimeUnit.SECONDS))
        await { vm.state.value.detections.size == 1 }

        val initialList = vm.state.value.detections

        // Avanzar el reloj
        simulatedClock = 3500L
        await { vm.state.value.elapsedSeconds >= 2L }

        // La lista debe ser exactamente la misma instancia (sin reconstrucción)
        assertSame(initialList, vm.state.value.detections)
        assertEquals(2L, vm.state.value.elapsedSeconds)
        assertEquals("00:02", vm.state.value.formattedTime)

        main { vm.abandon() }
    }

    @Test
    fun doubleStartIsIgnoredAndDoesNotDuplicateSessions() {
        val prepareCount = AtomicInteger(0)
        val listenCount = AtomicInteger(0)
        val vm = BirdSoundViewModel(app) {
            object : SoundSession {
                override suspend fun prepare() {
                    prepareCount.incrementAndGet()
                }
                override suspend fun listen(
                    onStarted: suspend (Int, Long) -> Unit,
                    onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
                ) {
                    listenCount.incrementAndGet()
                    onStarted(32000, 1000L)
                    awaitCancellation()
                }
                override fun cancel() = Unit
                override suspend fun close() = Unit
            }
        }

        main {
            vm.start()
            vm.start()
        }
        await { vm.state.value.phase == BirdSoundPhase.LISTENING }

        assertEquals(1, prepareCount.get())
        assertEquals(1, listenCount.get())

        main { vm.abandon() }
    }

    @Test
    fun stopBeforeThreeSecondsFreezesEmptyListAndAllowsCleanRetry() {
        val started = CountDownLatch(1)
        val vm = BirdSoundViewModel(app) {
            object : SoundSession {
                override suspend fun prepare() = Unit
                override suspend fun listen(
                    onStarted: suspend (Int, Long) -> Unit,
                    onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
                ) {
                    onStarted(32000, 1000L)
                    started.countDown()
                    awaitCancellation()
                }
                override fun cancel() = Unit
                override suspend fun close() = Unit
            }
        }

        main { vm.start() }
        assertTrue(started.await(5, TimeUnit.SECONDS))
        await { vm.state.value.phase == BirdSoundPhase.LISTENING }

        // Detener antes de recibir detecciones
        main { vm.stop() }
        await { vm.state.value.phase == BirdSoundPhase.STOPPED }

        assertTrue(vm.state.value.detections.isEmpty())
        assertNull(vm.state.value.errorReason)

        // Reintentar debe iniciar limpiamente
        main { vm.start() }
        await { vm.state.value.phase == BirdSoundPhase.LISTENING }

        main { vm.abandon() }
    }

    @Test
    fun stopFreezesPublishedDetectionsImmediatelyAndBlocksLateDetections() {
        var onDetectionsCb: (suspend (List<BirdDetection>, Long, Long) -> Unit)? = null
        val started = CountDownLatch(1)
        val bird = BirdDetection(1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.90f, "hornero", null)
        val lateBird = BirdDetection(2, "Late bird", "Late bird", "Tardía", 0.99f, null, null)

        val vm = BirdSoundViewModel(app) {
            object : SoundSession {
                override suspend fun prepare() = Unit
                override suspend fun listen(
                    onStarted: suspend (Int, Long) -> Unit,
                    onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
                ) {
                    onDetectionsCb = onDetections
                    onStarted(32000, 1000L)
                    started.countDown()
                    awaitCancellation()
                }
                override fun cancel() = Unit
                override suspend fun close() = Unit
            }
        }

        main { vm.start() }
        assertTrue(started.await(5, TimeUnit.SECONDS))
        await { vm.state.value.phase == BirdSoundPhase.LISTENING }

        runBlocking { onDetectionsCb?.invoke(listOf(bird), 96000L, 2000L) }
        await { vm.state.value.detections.size == 1 }

        // Detener congela inmediatamente
        main { vm.stop() }
        assertTrue("Debe pasar a STOPPING o STOPPED inmediatamente",
            vm.state.value.phase == BirdSoundPhase.STOPPING || vm.state.value.phase == BirdSoundPhase.STOPPED)
        assertEquals(1, vm.state.value.detections.size)

        // Llegada de detección tardía de la sesión anterior
        runBlocking { onDetectionsCb?.invoke(listOf(lateBird), 192000L, 3000L) }
        instrumentation.waitForIdleSync()

        await { vm.state.value.phase == BirdSoundPhase.STOPPED }
        assertEquals("La detección tardía no debe alterar la lista congelada", 1, vm.state.value.detections.size)
        assertEquals("Furnarius rufus", vm.state.value.detections[0].scientificName)

        main { vm.abandon() }
    }

    @Test
    fun latePreparationIsCancelledAndClosedWithoutRecordingOrListening() {
        val entered = CountDownLatch(1)
        val finish = CountDownLatch(1)
        val events = java.util.Collections.synchronizedList(mutableListOf<String>())

        val vm = BirdSoundViewModel(app) {
            object : SoundSession {
                override suspend fun prepare() {
                    entered.countDown()
                    withContext(Dispatchers.IO + NonCancellable) {
                        finish.await()
                        events.add("prepared")
                    }
                }
                override suspend fun listen(
                    onStarted: suspend (Int, Long) -> Unit,
                    onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
                ) {
                    events.add("listen")
                }
                override fun cancel() {
                    events.add("cancel")
                }
                override suspend fun close() {
                    events.add("close")
                }
            }
        }

        main { vm.start() }
        assertTrue(entered.await(5, TimeUnit.SECONDS))

        // Cancelar / detener durante preparación
        main { vm.stop() }
        finish.countDown()

        await { vm.state.value.phase == BirdSoundPhase.STOPPED }
        assertFalse("Nunca debe iniciar listen() tras cancelar preparación tardía", events.contains("listen"))
        assertTrue("Debe cerrarse la sesión", events.contains("close"))

        main { vm.abandon() }
    }

    @Test
    fun preparationFailureSetsErrorAndDoesNotRecoverOldResults() {
        var runs = 0
        val bird = BirdDetection(1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.90f, "hornero", null)

        val vm = BirdSoundViewModel(app) {
            object : SoundSession {
                override suspend fun prepare() {
                    runs++
                    if (runs == 2) throw IllegalStateException("Fallo de preparación")
                }
                override suspend fun listen(
                    onStarted: suspend (Int, Long) -> Unit,
                    onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
                ) {
                    onStarted(32000, 1000L)
                    onDetections(listOf(bird), 96000L, 2000L)
                    awaitCancellation()
                }
                override fun cancel() = Unit
                override suspend fun close() = Unit
            }
        }

        // Sesión 1: éxito con 1 ave
        main { vm.start() }
        await { vm.state.value.detections.size == 1 }
        main { vm.stop() }
        await { vm.state.value.phase == BirdSoundPhase.STOPPED }

        // Sesión 2: falla en prepare()
        main { vm.start() }
        await { vm.state.value.phase == BirdSoundPhase.ERROR }

        assertEquals(BirdSoundState.ErrorReason.RECOGNITION, vm.state.value.errorReason)
        assertTrue("Un fallo en nueva sesión no debe recuperar resultados de la sesión anterior",
            vm.state.value.detections.isEmpty())

        main { vm.abandon() }
    }

    @Test
    fun listenFailurePreservesAccumulatedDetectionsAndSetsError() {
        val started = CountDownLatch(1)
        var onDetectionsCb: (suspend (List<BirdDetection>, Long, Long) -> Unit)? = null
        val bird = BirdDetection(1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.90f, "hornero", null)

        val vm = BirdSoundViewModel(app) {
            object : SoundSession {
                override suspend fun prepare() = Unit
                override suspend fun listen(
                    onStarted: suspend (Int, Long) -> Unit,
                    onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
                ) {
                    onDetectionsCb = onDetections
                    onStarted(32000, 1000L)
                    started.countDown()
                    delay(300)
                    throw AudioRecordingException("Hardware read error")
                }
                override fun cancel() = Unit
                override suspend fun close() = Unit
            }
        }

        main { vm.start() }
        assertTrue(started.await(5, TimeUnit.SECONDS))
        runBlocking { onDetectionsCb?.invoke(listOf(bird), 96000L, 2000L) }
        await { vm.state.value.detections.size == 1 }

        await { vm.state.value.phase == BirdSoundPhase.ERROR }
        assertEquals(BirdSoundState.ErrorReason.RECORDING, vm.state.value.errorReason)
        assertEquals("Las detecciones publicadas antes del error deben conservarse", 1, vm.state.value.detections.size)

        // Reintentar limpia la lista inmediatamente
        main { vm.start() }
        assertTrue("Reintentar debe limpiar la lista", vm.state.value.detections.isEmpty())
        await { vm.state.value.phase == BirdSoundPhase.LISTENING || vm.state.value.phase == BirdSoundPhase.ERROR }

        main { vm.abandon() }
    }

    @Test
    fun backgroundDuringListeningSetsInterruptedAndPreservesDetections() {
        val started = CountDownLatch(1)
        val bird = BirdDetection(1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.90f, "hornero", null)

        val vm = BirdSoundViewModel(app) {
            object : SoundSession {
                override suspend fun prepare() = Unit
                override suspend fun listen(
                    onStarted: suspend (Int, Long) -> Unit,
                    onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
                ) {
                    onStarted(32000, 1000L)
                    onDetections(listOf(bird), 96000L, 2000L)
                    started.countDown()
                    awaitCancellation()
                }
                override fun cancel() = Unit
                override suspend fun close() = Unit
            }
        }

        main { vm.start() }
        assertTrue(started.await(5, TimeUnit.SECONDS))
        await { vm.state.value.detections.size == 1 }

        main { vm.background() }
        await { vm.state.value.phase == BirdSoundPhase.ERROR }

        assertEquals(BirdSoundState.ErrorReason.INTERRUPTED, vm.state.value.errorReason)
        assertEquals(1, vm.state.value.detections.size)

        main { vm.abandon() }
    }

    @Test
    fun stoppingBlocksNewStartUntilCleanupFinishes() {
        val releaseGate = CountDownLatch(1)
        val started = CountDownLatch(1)

        val vm = BirdSoundViewModel(app) {
            object : SoundSession {
                override suspend fun prepare() = Unit
                override suspend fun listen(
                    onStarted: suspend (Int, Long) -> Unit,
                    onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
                ) {
                    onStarted(32000, 1000L)
                    started.countDown()
                    try {
                        awaitCancellation()
                    } finally {
                        withContext(NonCancellable + Dispatchers.IO) {
                            releaseGate.await()
                        }
                    }
                }
                override fun cancel() = Unit
                override suspend fun close() = Unit
            }
        }

        main { vm.start() }
        assertTrue(started.await(5, TimeUnit.SECONDS))
        await { vm.state.value.phase == BirdSoundPhase.LISTENING }

        main {
            vm.stop()
            // Intentar arrancar inmediatamente mientras está deteniendo
            vm.start()
        }
        assertEquals(BirdSoundPhase.STOPPING, vm.state.value.phase)

        // Liberar el gate de limpieza
        releaseGate.countDown()
        await { vm.state.value.phase == BirdSoundPhase.STOPPED }

        // Ahora en STOPPED, start sí debe ser aceptado
        main { vm.start() }
        await { vm.state.value.phase == BirdSoundPhase.PREPARING || vm.state.value.phase == BirdSoundPhase.LISTENING }

        main { vm.abandon() }
    }

    @Test
    fun viewModelStoreRetainsSessionAndClearingClosesIt() {
        val store = androidx.lifecycle.ViewModelStore()
        val started = CountDownLatch(1)
        val closed = CountDownLatch(1)
        var created = 0

        val factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                created++
                return BirdSoundViewModel(app) {
                    object : SoundSession {
                        override suspend fun prepare() = Unit
                        override suspend fun listen(
                            onStarted: suspend (Int, Long) -> Unit,
                            onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
                        ) {
                            onStarted(32000, 1000L)
                            started.countDown()
                            awaitCancellation()
                        }
                        override fun cancel() = Unit
                        override suspend fun close() {
                            closed.countDown()
                        }
                    }
                } as T
            }
        }

        lateinit var vm: BirdSoundViewModel
        main {
            vm = androidx.lifecycle.ViewModelProvider(store, factory)[BirdSoundViewModel::class.java]
            vm.start()
        }
        assertTrue(started.await(5, TimeUnit.SECONDS))

        main {
            assertSame(vm, androidx.lifecycle.ViewModelProvider(store, factory)[BirdSoundViewModel::class.java])
            assertEquals(1, created)
            store.clear()
        }

        assertTrue(closed.await(5, TimeUnit.SECONDS))
    }

    @Test
    fun observableStatesContainNoAudioOrRestorationHandle() {
        val types = listOf(
            BirdSoundState::class.java,
            BirdSoundPhase::class.java,
            BirdSoundState.ErrorReason::class.java
        )
        for (type in types) {
            for (field in type.declaredFields) {
                assertNotEquals(ShortArray::class.java, field.type)
                assertNotEquals(FloatArray::class.java, field.type)
                assertNotEquals(CapturedAudio::class.java, field.type)
            }
        }
        assertTrue(BirdSoundViewModel::class.java.declaredConstructors.none { constructor ->
            constructor.parameterTypes.any { it.name == "androidx.lifecycle.SavedStateHandle" || it.name == "android.os.Bundle" }
        })
    }

    @Test
    fun formatElapsedTimeWorksWithSecondsAndHours() {
        assertEquals("00:00", BirdSoundState.formatElapsedTime(0))
        assertEquals("00:05", BirdSoundState.formatElapsedTime(5))
        assertEquals("00:59", BirdSoundState.formatElapsedTime(59))
        assertEquals("01:00", BirdSoundState.formatElapsedTime(60))
        assertEquals("59:59", BirdSoundState.formatElapsedTime(3599))
        assertEquals("1:00:00", BirdSoundState.formatElapsedTime(3600))
        assertEquals("1:01:05", BirdSoundState.formatElapsedTime(3665))
        assertEquals("2:02:05", BirdSoundState.formatElapsedTime(7325))
    }

    @Test
    fun stopAndAwaitReturnsImmediatelyWhenAlreadyStoppedOrReady() = runBlocking {
        val vm = BirdSoundViewModel(app) {
            object : SoundSession {
                override suspend fun prepare() = Unit
                override suspend fun listen(
                    onStarted: suspend (Int, Long) -> Unit,
                    onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
                ) = Unit
                override fun cancel() = Unit
                override suspend fun close() = Unit
            }
        }
        val readyState = vm.stopAndAwait()
        assertEquals(BirdSoundPhase.READY, readyState.phase)

        main { vm.stop() }
        val stoppedState = vm.stopAndAwait()
        assertEquals(BirdSoundPhase.READY, stoppedState.phase)
    }

    @Test
    fun stopAndAwaitWhenListeningFreezesResultsWaitsForCleanupAndReturnsStopped() {
        val started = CountDownLatch(1)
        val closed = CountDownLatch(1)
        val bird = BirdDetection(1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.90f, "hornero", null)

        val vm = BirdSoundViewModel(app) {
            object : SoundSession {
                override suspend fun prepare() = Unit
                override suspend fun listen(
                    onStarted: suspend (Int, Long) -> Unit,
                    onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
                ) {
                    onStarted(32000, 1000L)
                    onDetections(listOf(bird), 96000L, 2000L)
                    started.countDown()
                    awaitCancellation()
                }
                override fun cancel() = Unit
                override suspend fun close() {
                    delay(100)
                    closed.countDown()
                }
            }
        }

        main { vm.start() }
        assertTrue(started.await(5, TimeUnit.SECONDS))
        await { vm.state.value.phase == BirdSoundPhase.LISTENING && vm.state.value.detections.size == 1 }

        val finalState = runBlocking { vm.stopAndAwait() }
        assertEquals(BirdSoundPhase.STOPPED, finalState.phase)
        assertEquals(1, finalState.detections.size)
        assertEquals("Furnarius rufus", finalState.detections[0].scientificName)
        assertTrue("Session close() must be completed before stopAndAwait returns", closed.await(0, TimeUnit.MILLISECONDS))

        main { vm.abandon() }
    }

    @Test
    fun stopFailureSetsErrorRecordingStateAndDoesNotTransitionToStopped() {
        val started = CountDownLatch(1)
        val bird = BirdDetection(1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.90f, "hornero", null)

        val vm = BirdSoundViewModel(app) {
            object : SoundSession {
                override suspend fun prepare() = Unit
                override suspend fun listen(
                    onStarted: suspend (Int, Long) -> Unit,
                    onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
                ) {
                    onStarted(32000, 1000L)
                    onDetections(listOf(bird), 96000L, 2000L)
                    started.countDown()
                    awaitCancellation()
                }
                override fun cancel() = Unit
                override suspend fun close() {
                    throw IllegalStateException("Hardware release failed")
                }
            }
        }

        main { vm.start() }
        assertTrue(started.await(5, TimeUnit.SECONDS))
        await { vm.state.value.phase == BirdSoundPhase.LISTENING }

        val finalState = runBlocking { vm.stopAndAwait() }
        assertEquals(BirdSoundPhase.ERROR, finalState.phase)
        assertEquals(BirdSoundState.ErrorReason.RECORDING, finalState.errorReason)
        assertEquals("Detections must be preserved upon stop failure", 1, finalState.detections.size)

        main { vm.abandon() }
    }

    @Test
    fun stopAndAwaitWhenStoppingWaitsForExistingStoppingToFinish() {
        val started = CountDownLatch(1)
        val gate = CountDownLatch(1)

        val vm = BirdSoundViewModel(app) {
            object : SoundSession {
                override suspend fun prepare() = Unit
                override suspend fun listen(
                    onStarted: suspend (Int, Long) -> Unit,
                    onDetections: suspend (List<BirdDetection>, Long, Long) -> Unit
                ) {
                    onStarted(32000, 1000L)
                    started.countDown()
                    awaitCancellation()
                }
                override fun cancel() = Unit
                override suspend fun close() {
                    gate.await()
                }
            }
        }

        main { vm.start() }
        assertTrue(started.await(5, TimeUnit.SECONDS))
        await { vm.state.value.phase == BirdSoundPhase.LISTENING }

        main { vm.stop() }
        assertEquals(BirdSoundPhase.STOPPING, vm.state.value.phase)

        val resultStateJob = CoroutineScope(Dispatchers.Default).async {
            vm.stopAndAwait()
        }

        Thread.sleep(50)
        assertFalse(resultStateJob.isCompleted)

        gate.countDown()
        val finalState = runBlocking { resultStateJob.await() }
        assertEquals(BirdSoundPhase.STOPPED, finalState.phase)

        main { vm.abandon() }
    }
}
