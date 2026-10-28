package com.example.avesargentinas.ui.sound

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.util.Log
import android.os.Debug
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.PI
import kotlin.math.sin

@RunWith(AndroidJUnit4::class)
class BirdNetClassifierTest {
    @Test
    fun syntheticWindowsMatchIndependentRuntimeForEveryClass() = runBlocking {
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        val classifier = BirdNetClassifier(InstrumentationRegistry.getInstrumentation().targetContext.assets)
        try {
            classifier.initialize()
            var maxDifference = 0f
            for (n in 1..3) {
                fun readFloats(name: String): FloatArray {
                    val bytes = assets.open("birdnet/$name-$n.f32").use { it.readBytes() }
                    val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()
                    return FloatArray(buffer.remaining()).also { buffer.get(it) }
                }
                val audio = FloatArray(320_000)
                readFloats("signal").copyInto(audio)
                val expected = readFloats("reference")
                val actual = classifier.classify(audio)
                assertEquals(9789, expected.size)
                val difference = expected.indices.maxOf { kotlin.math.abs(expected[it] - actual[it]) }
                Log.i("BirdNetT4", "fixture=$n maxDifference=$difference")
                maxDifference = maxOf(maxDifference, difference)
            }
            assertTrue("Maximum per-class difference $maxDifference exceeds 0.001", maxDifference <= 0.001f)
        } finally {
            classifier.close()
        }
    }

    @Test
    fun classifyWindowMatchesIndependentRuntimeForEveryClass() = runBlocking {
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        val classifier = BirdNetClassifier(InstrumentationRegistry.getInstrumentation().targetContext.assets)
        try {
            classifier.initialize()
            var maxDifference = 0f
            for (n in 1..3) {
                fun readFloats(name: String): FloatArray {
                    val bytes = assets.open("birdnet/$name-$n.f32").use { it.readBytes() }
                    val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()
                    return FloatArray(buffer.remaining()).also { buffer.get(it) }
                }
                val signal = readFloats("signal")
                val expected = readFloats("reference")
                val actual = classifier.classifyWindow(signal)
                assertEquals(9789, expected.size)
                assertEquals(9789, actual.size)
                val difference = expected.indices.maxOf { kotlin.math.abs(expected[it] - actual[it]) }
                Log.i("BirdNetT2", "classifyWindow fixture=$n maxDifference=$difference")
                maxDifference = maxOf(maxDifference, difference)
            }
            assertTrue("Maximum per-class difference $maxDifference exceeds 0.001", maxDifference <= 0.001f)
        } finally {
            classifier.close()
        }
    }

    @Test
    fun classifyWindowAndClassifyEquivalentRoutesProduceIdenticalScores() = runBlocking {
        val classifier = BirdNetClassifier(InstrumentationRegistry.getInstrumentation().targetContext.assets)
        try {
            classifier.initialize()
            val window = FloatArray(96_000) { i -> (0.2 * sin(2 * PI * 440 * i / 32_000)).toFloat() }
            val audio = FloatArray(320_000)
            window.copyInto(audio, destinationOffset = 0)
            val windowScores = classifier.classifyWindow(window)
            val fullScores = classifier.classify(audio)
            assertEquals(9789, windowScores.size)
            assertEquals(9789, fullScores.size)
            assertTrue("Expected non-zero scores", windowScores.any { it > 0f })
            for (i in windowScores.indices) {
                assertEquals("Mismatch at class $i", windowScores[i], fullScores[i], 0.000001f)
            }
        } finally {
            classifier.close()
        }
    }

    @Test
    fun classifyWindowConstantAudioReturnsZerosAndRejectsInvalidInput() = runBlocking {
        val classifier = BirdNetClassifier(InstrumentationRegistry.getInstrumentation().targetContext.assets)
        try {
            classifier.initialize()
            val zeroScores = classifier.classifyWindow(FloatArray(96_000))
            assertEquals(9789, zeroScores.size)
            assertTrue(zeroScores.all { it == 0f })

            val constScores = classifier.classifyWindow(FloatArray(96_000) { 0.42f })
            assertEquals(9789, constScores.size)
            assertTrue(constScores.all { it == 0f })

            assertThrows(IllegalArgumentException::class.java) { runBlocking { classifier.classifyWindow(FloatArray(95_999)) } }
            assertThrows(IllegalArgumentException::class.java) { runBlocking { classifier.classifyWindow(FloatArray(96_001)) } }
            assertThrows(IllegalArgumentException::class.java) { runBlocking { classifier.classifyWindow(FloatArray(320_000)) } }

            val withNan = FloatArray(96_000)
            withNan[10] = Float.NaN
            assertThrows(IllegalArgumentException::class.java) { runBlocking { classifier.classifyWindow(withNan) } }
            val withInf = FloatArray(96_000)
            withInf[10] = Float.POSITIVE_INFINITY
            assertThrows(IllegalArgumentException::class.java) { runBlocking { classifier.classifyWindow(withInf) } }
        } finally {
            classifier.close()
        }
        assertThrows(IllegalStateException::class.java) { runBlocking { classifier.classifyWindow(FloatArray(96_000)) } }
        Unit
    }

    @Test
    fun cancelWhileNativeRunIsActiveOnClassifyWindowAndThenRetry() = runBlocking {
        val classifier = BirdNetClassifier(InstrumentationRegistry.getInstrumentation().targetContext.assets)
        classifier.initialize()
        val result = AtomicReference<Any>()
        val finished = CountDownLatch(1)
        val signal = FloatArray(96_000) { i -> (0.2 * sin(2 * PI * 440 * i / 32_000)).toFloat() }
        val worker = Thread {
            try { result.set(runBlocking { classifier.classifyWindow(signal) }) }
            catch (t: Throwable) { result.set(t) }
            finally { finished.countDown() }
        }
        try {
            worker.start()
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
            var executing = false
            while (System.nanoTime() < deadline && finished.count > 0) {
                executing = Thread.getAllStackTraces().values.any { stack ->
                    stack.any { it.className == "ai.onnxruntime.OrtSession" && it.methodName == "run" && it.isNativeMethod }
                }
                if (executing) break
                Thread.yield()
            }
            assertTrue("Native inference was not observed", executing)
            classifier.cancelCurrentRun()
            assertTrue("Cancelled inference did not finish", finished.await(10, TimeUnit.SECONDS))
            assertTrue("Cancelled inference returned scores", result.get() is Throwable)
            assertTrue(classifier.classifyWindow(signal).any { it > 0f })
        } finally {
            classifier.close()
            worker.join(10_000)
        }
    }

    @Test
    fun closeDuringNativeInferenceOnClassifyWindowSuppressesResult() = runBlocking {
        val classifier = BirdNetClassifier(InstrumentationRegistry.getInstrumentation().targetContext.assets)
        classifier.initialize()
        val result = AtomicReference<Any>()
        val finished = CountDownLatch(1)
        val signal = FloatArray(96_000) { i -> (0.2 * sin(2 * PI * 440 * i / 32_000)).toFloat() }
        val worker = Thread {
            try { result.set(runBlocking { classifier.classifyWindow(signal) }) }
            catch (t: Throwable) { result.set(t) }
            finally { finished.countDown() }
        }
        try {
            worker.start()
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
            var executing = false
            while (System.nanoTime() < deadline && finished.count > 0) {
                executing = Thread.getAllStackTraces().values.any { stack ->
                    stack.any { it.className == "ai.onnxruntime.OrtSession" && it.methodName == "run" && it.isNativeMethod }
                }
                if (executing) break
                Thread.yield()
            }
            assertTrue("Native inference was not observed", executing)
            classifier.close()
            assertTrue(finished.await(10, TimeUnit.SECONDS))
            assertTrue("Closed classifier returned scores", result.get() is Throwable)
            assertThrows(IllegalStateException::class.java) { runBlocking { classifier.classifyWindow(signal) } }
            Unit
        } finally {
            classifier.close()
            worker.join(10_000)
        }
    }

    @Test
    fun constantAudioHasNoDetectionsAndClosedClassifierCannotRun() = runBlocking {
        val classifier = BirdNetClassifier(InstrumentationRegistry.getInstrumentation().targetContext.assets)
        classifier.initialize()
        assertTrue(classifier.classify(FloatArray(320_000)).all { it == 0f })
        classifier.close()
        assertThrows(IllegalStateException::class.java) { runBlocking { classifier.classify(FloatArray(320_000)) } }
        Unit
    }

    @Test
    fun finalWindowContributesRealScores() = runBlocking {
        val classifier = BirdNetClassifier(InstrumentationRegistry.getInstrumentation().targetContext.assets)
        try {
            classifier.initialize()
            val audio = FloatArray(320_000)
            for (i in 288_000 until audio.size) audio[i] = (0.2 * sin(2 * PI * 440 * i / 32_000)).toFloat()
            val scores = classifier.classify(audio)
            assertEquals(9789, scores.size)
            assertTrue(scores.all { it.isFinite() && it in 0f..1f })
            assertTrue(scores.any { it > 0f })
        } finally {
            classifier.close()
        }
    }

    @Test
    fun fourSeparatedEventsAggregateByMaximum() = runBlocking {
        val classifier = BirdNetClassifier(InstrumentationRegistry.getInstrumentation().targetContext.assets)
        try {
            classifier.initialize()
            val combined = FloatArray(320_000)
            val expected = FloatArray(9789)
            for ((event, start) in listOf(0, 96_000, 192_000, 288_000).withIndex()) {
                val separate = FloatArray(320_000)
                for (i in start until start + 32_000) {
                    separate[i] = (0.2 * sin(2 * PI * (440 + event * 310) * i / 32_000)).toFloat()
                }
                val scores = classifier.classify(separate)
                assertTrue("event $event did not contribute", scores.any { it > 0f })
                for (i in expected.indices) expected[i] = maxOf(expected[i], scores[i])
                for (i in separate.indices) combined[i] += separate[i]
            }
            val actual = classifier.classify(combined)
            for (i in expected.indices) assertEquals("class $i", expected[i], actual[i], 0.000001f)
        } finally {
            classifier.close()
        }
    }

    @Test
    fun cancelWhileNativeRunIsActiveAndThenRetry() = runBlocking {
        val classifier = BirdNetClassifier(InstrumentationRegistry.getInstrumentation().targetContext.assets)
        classifier.initialize()
        val result = AtomicReference<Any>()
        val finished = CountDownLatch(1)
        val signal = FloatArray(320_000) { i -> (0.2 * sin(2 * PI * 440 * i / 32_000)).toFloat() }
        val worker = Thread {
            try { result.set(runBlocking { classifier.classify(signal) }) }
            catch (t: Throwable) { result.set(t) }
            finally { finished.countDown() }
        }
        try {
            worker.start()
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
            var executing = false
            while (System.nanoTime() < deadline && finished.count > 0) {
                executing = Thread.getAllStackTraces().values.any { stack ->
                    stack.any { it.className == "ai.onnxruntime.OrtSession" && it.methodName == "run" && it.isNativeMethod }
                }
                if (executing) break
                Thread.yield()
            }
            assertTrue("Native inference was not observed", executing)
            classifier.cancelCurrentRun()
            assertTrue("Cancelled inference did not finish", finished.await(10, TimeUnit.SECONDS))
            assertTrue("Cancelled inference returned scores", result.get() is Throwable)
            assertTrue(classifier.classify(signal).any { it > 0f })
        } finally {
            classifier.close()
            worker.join(10_000)
        }
    }

    @Test
    fun cancelledNativeInitializationDoesNotPublishSessionAndCanRetry() = runBlocking {
        val classifier = BirdNetClassifier(InstrumentationRegistry.getInstrumentation().targetContext.assets)
        val result = AtomicReference<Any>()
        val finished = CountDownLatch(1)
        val worker = Thread {
            try { runBlocking { classifier.initialize() }; result.set("published") }
            catch (t: Throwable) { result.set(t) }
            finally { finished.countDown() }
        }
        try {
            worker.start()
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
            var creating = false
            while (System.nanoTime() < deadline && finished.count > 0) {
                creating = Thread.getAllStackTraces().values.any { stack ->
                    stack.any { it.className == "ai.onnxruntime.OrtSession" && it.methodName == "createSession" && it.isNativeMethod }
                }
                if (creating) break
                Thread.yield()
            }
            assertTrue("Native session creation was not observed", creating)
            classifier.cancelCurrentRun()
            assertTrue(finished.await(10, TimeUnit.SECONDS))
            assertTrue("Cancelled initialization returned normally", result.get() is Throwable)
            assertThrows(IllegalStateException::class.java) { runBlocking { classifier.classify(FloatArray(320_000)) } }
            classifier.initialize()
            assertEquals(9789, classifier.classify(FloatArray(320_000)).size)
        } finally {
            classifier.close()
            worker.join(10_000)
        }
    }

    @Test
    fun closeDuringNativeInferenceSuppressesResult() = runBlocking {
        val classifier = BirdNetClassifier(InstrumentationRegistry.getInstrumentation().targetContext.assets)
        classifier.initialize()
        val result = AtomicReference<Any>()
        val finished = CountDownLatch(1)
        val signal = FloatArray(320_000) { i -> (0.2 * sin(2 * PI * 440 * i / 32_000)).toFloat() }
        val worker = Thread {
            try { result.set(runBlocking { classifier.classify(signal) }) }
            catch (t: Throwable) { result.set(t) }
            finally { finished.countDown() }
        }
        try {
            worker.start()
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
            var executing = false
            while (System.nanoTime() < deadline && finished.count > 0) {
                executing = Thread.getAllStackTraces().values.any { stack ->
                    stack.any { it.className == "ai.onnxruntime.OrtSession" && it.methodName == "run" && it.isNativeMethod }
                }
                if (executing) break
                Thread.yield()
            }
            assertTrue("Native inference was not observed", executing)
            classifier.close()
            assertTrue(finished.await(10, TimeUnit.SECONDS))
            assertTrue("Closed classifier returned scores", result.get() is Throwable)
            assertThrows(IllegalStateException::class.java) { runBlocking { classifier.classify(signal) } }
            Unit
        } finally {
            classifier.close()
            worker.join(10_000)
        }
    }

    @Test
    fun invalidInputFailsAndRepeatedSessionsCanRunAndClose() = runBlocking {
        repeat(3) {
            val classifier = BirdNetClassifier(InstrumentationRegistry.getInstrumentation().targetContext.assets)
            try {
                classifier.initialize()
                assertThrows(IllegalArgumentException::class.java) { runBlocking { classifier.classify(FloatArray(3)) } }
                val invalid = FloatArray(320_000)
                invalid[0] = Float.NaN
                assertThrows(IllegalArgumentException::class.java) { runBlocking { classifier.classify(invalid) } }
                val signal = FloatArray(320_000) { i -> (0.2 * sin(2 * PI * 440 * i / 32_000)).toFloat() }
                assertTrue(classifier.classify(signal).any { it > 0f })
            } finally {
                classifier.close()
            }
        }
    }

    @Test
    fun missingModelFailsAndFreshClassifierCanRetry() = runBlocking {
        val missing = BirdNetClassifier(InstrumentationRegistry.getInstrumentation().context.assets)
        try {
            assertThrows(Exception::class.java) { runBlocking { missing.initialize() } }
        } finally {
            missing.close()
        }
        val classifier = BirdNetClassifier(InstrumentationRegistry.getInstrumentation().targetContext.assets)
        try {
            classifier.initialize()
            assertEquals(9789, classifier.classify(FloatArray(320_000)).size)
        } finally {
            classifier.close()
        }
    }

    @Test
    fun repeatedCyclesDoNotRetainNativeMemory() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val signal = FloatArray(320_000) { i -> (0.2 * sin(2 * PI * 440 * i / 32_000)).toFloat() }

        fun gcAndGetNativeHeap(): Long {
            System.gc()
            System.runFinalization()
            System.gc()
            return Debug.getNativeHeapAllocatedSize()
        }

        val warmup = BirdNetClassifier(appContext.assets)
        warmup.initialize()
        val warmupScores = warmup.classify(signal)
        assertTrue(warmupScores.any { it > 0f })
        warmup.close()

        val baselineAllocated = gcAndGetNativeHeap()
        val memInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(memInfo)
        Log.i("BirdNetNativeRetention", "baselineAllocated=$baselineAllocated baselinePss=${memInfo.totalPss} baselineNativePss=${memInfo.nativePss}")

        val cycleCount = 5
        val allocations = LongArray(cycleCount)
        for (i in 0 until cycleCount) {
            val classifier = BirdNetClassifier(appContext.assets)
            classifier.initialize()
            val scores = classifier.classify(signal)
            assertTrue(scores.any { it > 0f })
            classifier.close()
            allocations[i] = gcAndGetNativeHeap()
            Debug.getMemoryInfo(memInfo)
            val delta = allocations[i] - baselineAllocated
            Log.i("BirdNetNativeRetention", "cycle=$i allocated=${allocations[i]} delta=$delta pss=${memInfo.totalPss} nativePss=${memInfo.nativePss}")
        }

        val finalAllocated = gcAndGetNativeHeap()
        Debug.getMemoryInfo(memInfo)
        val finalDelta = finalAllocated - baselineAllocated
        Log.i("BirdNetNativeRetention", "finalAllocated=$finalAllocated netDelta=$finalDelta finalPss=${memInfo.totalPss} finalNativePss=${memInfo.nativePss}")

        assertTrue(
            "Native heap retained $finalDelta bytes after $cycleCount cycles (exceeds 15 MB)",
            finalDelta < 15 * 1024 * 1024
        )
    }

    @Test
    fun mainThreadRemainsResponsiveWhileClosingInference() = runBlocking {
        val classifier = BirdNetClassifier(InstrumentationRegistry.getInstrumentation().targetContext.assets)
        classifier.initialize()
        val finished = CountDownLatch(1)
        val result = AtomicReference<Throwable>()
        val signal = FloatArray(320_000) { i -> (0.2 * sin(2 * PI * 440 * i / 32_000)).toFloat() }
        val worker = Thread {
            try { runBlocking { classifier.classify(signal) } } catch (_: Throwable) { }
            finally { finished.countDown() }
        }
        try {
            worker.start()
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
            var executing = false
            while (System.nanoTime() < deadline && finished.count > 0) {
                executing = Thread.getAllStackTraces().values.any { stack ->
                    stack.any { it.className == "ai.onnxruntime.OrtSession" && it.methodName == "run" && it.isNativeMethod }
                }
                if (executing) break
                Thread.yield()
            }
            assertTrue("Native inference was not observed", executing)
            val mainReturned = CountDownLatch(1)
            val closed = CountDownLatch(1)
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                CoroutineScope(Dispatchers.Main).launch {
                    try { classifier.close() } catch (t: Throwable) { result.set(t) }
                    finally { closed.countDown() }
                }
                handler.post { mainReturned.countDown() }
            }
            assertTrue("Main thread did not respond", mainReturned.await(2, TimeUnit.SECONDS))
            assertTrue("Close did not finish", closed.await(10, TimeUnit.SECONDS))
            assertNull(result.get())
            assertTrue(finished.await(10, TimeUnit.SECONDS))
        } finally {
            classifier.close()
            worker.join(10_000)
        }
    }
}
