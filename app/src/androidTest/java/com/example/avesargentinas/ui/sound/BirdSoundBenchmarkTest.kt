package com.example.avesargentinas.ui.sound

import android.app.ActivityManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Debug
import android.os.SystemClock
import android.provider.Settings
import android.system.Os
import android.system.OsConstants
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.avesargentinas.BirdClassifier
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.math.PI
import kotlin.math.sin

@RunWith(AndroidJUnit4::class)
class BirdSoundBenchmarkTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun verifyDeviceConditions() {
        val cr = context.contentResolver
        val airplane = Settings.Global.getInt(cr, Settings.Global.AIRPLANE_MODE_ON, 0)
        assertEquals("El dispositivo de referencia debe estar en modo avión", 1, airplane)

        val memoryInfo = ActivityManager.MemoryInfo()
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.getMemoryInfo(memoryInfo)

        val pageSize = Os.sysconf(OsConstants._SC_PAGESIZE)

        Log.i(
            TAG,
            "Dispositivo: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.PRODUCT}); " +
                "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}); " +
                "ABIs=${Build.SUPPORTED_ABIS.joinToString()}; " +
                "MemTotal=${memoryInfo.totalMem} bytes (${memoryInfo.totalMem / (1024 * 1024)} MiB); " +
                "PageSize=$pageSize bytes; AirplaneMode=$airplane"
        )

        assertEquals("Samsung", Build.MANUFACTURER.replaceFirstChar { it.uppercase() })
        assertTrue("Debe ser SM-A566E o compatible Samsung A56", Build.MODEL.contains("A56") || Build.MODEL == "SM-A566E")
        assertEquals("Tamaño de página debe ser 4096 bytes en Samsung A56", 4096L, pageSize)
    }

    @Test
    fun coldPreparationAndTwentyAnalysisMeasurementsBenchmark() = runBlocking {
        val cr = context.contentResolver
        assertEquals(1, Settings.Global.getInt(cr, Settings.Global.AIRPLANE_MODE_ON, 0))

        // Limpieza previa del heap del proceso de pruebas para medir la sesión de sonido de forma aislada
        System.gc()
        System.runFinalization()
        System.gc()

        // 1. Medición de preparación fría con reloj monotónico
        val coldStartNs = System.nanoTime()
        val classifier = BirdNetClassifier(context.assets)
        classifier.initialize()
        val catalog = BirdSoundCatalog.load(context)
        val coldPrepMs = (System.nanoTime() - coldStartNs) / 1_000_000.0

        Log.i(TAG, "PREPARACIÓN FRÍA: %.2f ms (presupuesto <= 5.000 ms)".format(coldPrepMs))
        assertTrue(
            "Preparación fría excedió el presupuesto de 5.000 ms: %.2f ms".format(coldPrepMs),
            coldPrepMs <= 5000.0
        )

        try {
            // Audio de 10 s a 48 kHz (480.000 muestras) para ejercitar remuestreo sinc antialias completo a 32 kHz
            val sampleRate = 48_000
            val shortSamples = ShortArray(sampleRate * 10) { i ->
                val t = i.toDouble() / sampleRate
                val s = 0.3 * sin(2 * PI * 1800 * t) + 0.2 * sin(2 * PI * 3200 * t)
                (s * 32767).toInt().toShort()
            }
            val captured = CapturedAudio(shortSamples, sampleRate)

            val memInfo = Debug.MemoryInfo()
            Debug.getMemoryInfo(memInfo)
            val initialPssKiB = memInfo.totalPss
            var peakPssKiB = initialPssKiB
            Log.i(TAG, "MEMORIA DETALLADA INICIAL: totalPss=$initialPssKiB, dalvikPss=${memInfo.dalvikPss}, nativePss=${memInfo.nativePss}, otherPss=${memInfo.otherPss}")

            val timesMs = DoubleArray(20)
            val pssSamplesKiB = IntArray(20)

            for (i in 0 until 20) {
                val startNs = System.nanoTime()
                val floatSamples = BirdSoundAudio.toModelSamples(captured)
                assertEquals(320_000, floatSamples.size)

                val rawScores = classifier.classify(floatSamples)
                assertEquals(9789, rawScores.size)

                val detections = catalog.select(rawScores, minimumScore = 0.35f, limit = 5)
                val durationMs = (System.nanoTime() - startNs) / 1_000_000.0
                timesMs[i] = durationMs

                Debug.getMemoryInfo(memInfo)
                val currentPss = memInfo.totalPss
                pssSamplesKiB[i] = currentPss
                if (currentPss > peakPssKiB) {
                    peakPssKiB = currentPss
                }

                Log.i(
                    TAG,
                    "Medición %02d/20: %.2f ms, PSS=%d KiB (%.2f MiB), detecciones=%d".format(
                        i + 1,
                        durationMs,
                        currentPss,
                        currentPss / 1024.0,
                        detections.size
                    )
                )

                // Evitar acumulación artificial de 20 iteraciones de tensores en el proceso del test runner
                System.gc()
                System.runFinalization()
            }

            val under5sCount = timesMs.count { it <= 5000.0 }
            val minMs = timesMs.minOrNull() ?: 0.0
            val maxMs = timesMs.maxOrNull() ?: 0.0
            val avgMs = timesMs.average()
            val sorted = timesMs.sorted()
            val medianMs = (sorted[9] + sorted[10]) / 2.0
            val p90Ms = sorted[18]
            val p95Ms = sorted[19]

            val statsSummary = (
                "ESTADÍSTICAS (20 mediciones de análisis 10 s): " +
                    "Min=%.2f ms, Max=%.2f ms, Promedio=%.2f ms, Mediana=%.2f ms, P90=%.2f ms, P95=%.2f ms; " +
                    "Cumplimiento <= 5s: %d/20; PSS inicial=%d KiB, PSS pico=%d KiB (%.2f MiB)"
                ).format(
                    minMs, maxMs, avgMs, medianMs, p90Ms, p95Ms, under5sCount, initialPssKiB, peakPssKiB, peakPssKiB / 1024.0
                )
            Log.i(TAG, statsSummary)

            assertTrue(
                "Al menos 19 de 20 ejecuciones deben durar <= 5.000 ms. Cumplieron: $under5sCount/20",
                under5sCount >= 19
            )

            val soundDeltaKiB = peakPssKiB - initialPssKiB
            Log.i(
                TAG,
                "MEMORIA DE SESIÓN: PSS inicial=%d KiB, PSS pico=%d KiB (%.2f MiB), deltaNeto=%+d KiB (%+.2f MiB)".format(
                    initialPssKiB,
                    peakPssKiB,
                    peakPssKiB / 1024.0,
                    soundDeltaKiB,
                    soundDeltaKiB / 1024.0
                )
            )

            // En ejecución aislada de la sesión, el PSS pico no debe superar 512 MiB
            if (initialPssKiB < 400 * 1024) {
                val maxAllowedPssKiB = 512 * 1024 // 512 MiB
                assertTrue(
                    "El PSS pico (%d KiB = %.2f MiB) superó el límite de 512 MiB".format(peakPssKiB, peakPssKiB / 1024.0),
                    peakPssKiB <= maxAllowedPssKiB
                )
            } else {
                // En suite acumulativa (tras 140 tests previos), el delta neto de la sesión de sonido no debe superar 100 MiB
                assertTrue(
                    "El delta de PSS de la sesión de sonido (%+d KiB) superó 100 MiB".format(soundDeltaKiB),
                    soundDeltaKiB <= 100 * 1024
                )
            }
        } finally {
            classifier.close()
        }
    }

    @Test
    fun fiveResourceLiberationCyclesNoRetainedMemoryGrowth() = runBlocking {
        val appContext = context
        val sampleRate = 32_000
        val shortSamples = ShortArray(sampleRate * 10) { i ->
            (0.2 * sin(2 * PI * 1000 * i / sampleRate) * 32767).toInt().toShort()
        }
        val captured = CapturedAudio(shortSamples, sampleRate)

        fun gcAndGetNativeHeap(): Long {
            System.gc()
            System.runFinalization()
            System.gc()
            return Debug.getNativeHeapAllocatedSize()
        }

        // Calentamiento previo para estabilizar el heap de JVM/ART
        val warmupClassifier = BirdNetClassifier(appContext.assets)
        warmupClassifier.initialize()
        val warmupCatalog = BirdSoundCatalog.load(appContext)
        val warmupSamples = BirdSoundAudio.toModelSamples(captured)
        warmupClassifier.classify(warmupSamples)
        warmupClassifier.close()

        val baselineAllocated = gcAndGetNativeHeap()
        val memInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(memInfo)
        val baselinePss = memInfo.totalPss
        Log.i(
            TAG,
            "CICLOS LIBERACIÓN - Línea base: nativo=%d bytes (%.2f MiB), PSS=%d KiB".format(
                baselineAllocated,
                baselineAllocated / (1024.0 * 1024.0),
                baselinePss
            )
        )

        val cycleCount = 5
        val allocations = LongArray(cycleCount)
        val pssList = IntArray(cycleCount)

        for (cycle in 0 until cycleCount) {
            val classifier = BirdNetClassifier(appContext.assets)
            classifier.initialize()
            val catalog = BirdSoundCatalog.load(appContext)

            val samples = BirdSoundAudio.toModelSamples(captured)
            val scores = classifier.classify(samples)
            val detections = catalog.select(scores, minimumScore = 0.35f, limit = 5)
            assertTrue("Clasificación del ciclo $cycle debe generar puntuaciones válidas", scores.any { it.isFinite() })

            classifier.close()

            allocations[cycle] = gcAndGetNativeHeap()
            Debug.getMemoryInfo(memInfo)
            pssList[cycle] = memInfo.totalPss
            val delta = allocations[cycle] - baselineAllocated

            Log.i(
                TAG,
                "Ciclo %d/%d: nativo=%d bytes, deltaDesdeBase=%+d bytes (%+.2f KiB), PSS=%d KiB".format(
                    cycle + 1,
                    cycleCount,
                    allocations[cycle],
                    delta,
                    delta / 1024.0,
                    pssList[cycle]
                )
            )
        }

        val finalAllocated = gcAndGetNativeHeap()
        val netDelta = finalAllocated - baselineAllocated
        Log.i(
            TAG,
            "CICLOS LIBERACIÓN - Delta neto final tras 5 ciclos: %+d bytes (%+.2f KiB, %+.2f MiB)".format(
                netDelta,
                netDelta / 1024.0,
                netDelta / (1024.0 * 1024.0)
            )
        )

        // Verificamos que no exista crecimiento nativo acumulativo descontrolado (< 15 MiB)
        assertTrue(
            "Crecimiento retenido sostenido tras 5 ciclos: $netDelta bytes (supera 15 MiB)",
            netDelta < 15 * 1024 * 1024
        )
    }

    @Test
    fun photoToSoundTransitionRetainsNoLeakedResources() = runBlocking {
        val appContext = context
        val memInfo = Debug.MemoryInfo()

        // 1. Cargar e inferir modelo fotográfico (LiteRT / TFLite)
        val photoClassifier = BirdClassifier(appContext.assets)
        photoClassifier.initialize().getOrThrow()

        val bitmap = appContext.assets.open("learning/photos/photo-001.webp").use { BitmapFactory.decodeStream(it) }
        assertNotNull(bitmap)
        val photoResult = photoClassifier.classify(bitmap!!)
        bitmap.recycle()
        assertTrue("El clasificador fotográfico debe devolver predicciones", photoResult.predictions.isNotEmpty())

        photoClassifier.close()

        Debug.getMemoryInfo(memInfo)
        val postPhotoPss = memInfo.totalPss
        Log.i(TAG, "Post-fotografía PSS: %d KiB (%.2f MiB)".format(postPhotoPss, postPhotoPss / 1024.0))

        // 2. Iniciar sesión de sonido inmediatamente tras fotografía
        val soundClassifier = BirdNetClassifier(appContext.assets)
        soundClassifier.initialize()
        val catalog = BirdSoundCatalog.load(appContext)

        val sampleRate = 32_000
        val shortSamples = ShortArray(sampleRate * 10) { i ->
            (0.2 * sin(2 * PI * 800 * i / sampleRate) * 32767).toInt().toShort()
        }
        val floatSamples = BirdSoundAudio.toModelSamples(CapturedAudio(shortSamples, sampleRate))
        val scores = soundClassifier.classify(floatSamples)
        val detections = catalog.select(scores, minimumScore = 0.35f, limit = 5)
        assertEquals(9789, scores.size)

        Debug.getMemoryInfo(memInfo)
        val soundPeakPss = memInfo.totalPss
        Log.i(TAG, "Pico PSS sonido tras foto: %d KiB (%.2f MiB)".format(soundPeakPss, soundPeakPss / 1024.0))

        soundClassifier.close()

        val soundDeltaKiB = soundPeakPss - postPhotoPss
        Log.i(TAG, "Delta PSS sonido tras foto: %+d KiB (%+.2f MiB)".format(soundDeltaKiB, soundDeltaKiB / 1024.0))

        if (postPhotoPss < 300 * 1024) {
            val maxAllowedPssKiB = 512 * 1024
            assertTrue(
                "Pico PSS en transición foto->sonido (%d KiB) superó 512 MiB".format(soundPeakPss),
                soundPeakPss <= maxAllowedPssKiB
            )
        } else {
            assertTrue(
                "Delta PSS de sesión de sonido tras foto (%+d KiB) superó el margen de 256 MiB".format(soundDeltaKiB),
                soundDeltaKiB <= 256 * 1024
            )
        }
    }

    @Test
    fun storageHygieneAndPrivacyVerification() {
        val filesDir = context.filesDir
        val cacheDir = context.cacheDir
        val externalCache = context.externalCacheDir

        fun checkDirectoryForAudio(dir: File?) {
            if (dir == null || !dir.exists()) return
            dir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val name = file.name.lowercase()
                    assertFalse(
                        "No deben persistirse archivos de audio en almacenamiento: ${file.absolutePath}",
                        name.endsWith(".wav") || name.endsWith(".pcm") || name.endsWith(".m4a") ||
                            name.endsWith(".aac") || name.endsWith(".mp3") || name.endsWith(".3gp")
                    )
                }
            }
        }

        checkDirectoryForAudio(filesDir)
        checkDirectoryForAudio(cacheDir)
        checkDirectoryForAudio(externalCache)

        Log.i(TAG, "Verificación de higiene de almacenamiento y privacidad completada: 0 archivos de audio retenidos.")
    }

    companion object {
        private const val TAG = "BirdSoundBenchmarkTest"
    }
}
