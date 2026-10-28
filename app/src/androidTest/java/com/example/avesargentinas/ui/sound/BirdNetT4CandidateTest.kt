package com.example.avesargentinas.ui.sound

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.TensorInfo
import android.provider.Settings
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.security.MessageDigest
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class BirdNetT4CandidateTest {
    @Test fun candidateMatchesAllReferencesOffline() {
        if (InstrumentationRegistry.getArguments().getString("t4aCandidate") != "true") {
            Log.i("BirdNetT4CandidateTest", "Skipping candidate test: t4aCandidate argument not set")
            return
        }
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        assertEquals(1, Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0))
        val file = File(context.cacheDir, "t4a-candidate.onnx")
        assertEquals(65_830_813L, file.length())
        val expectedHash = "cc696820bc4882f2469a2cd0071b299aa400858aad7843e1f65879867bf5eaa5"
        val model = file.inputStream().use { it.channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length()) }
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(model.duplicate())
        assertEquals(expectedHash, digest.digest().joinToString("") { "%02x".format(it) })
        val env = OrtEnvironment.getEnvironment()
        assertEquals("1.22.0", env.version)
        OrtSession.SessionOptions().use { options ->
            options.setExecutionMode(OrtSession.SessionOptions.ExecutionMode.SEQUENTIAL)
            options.setIntraOpNumThreads(2)
            env.createSession(model, options).use { session ->
                assertEquals(setOf("input"), session.inputNames)
                val input = session.inputInfo.getValue("input").info as TensorInfo
                val output = session.outputInfo.getValue("predictions").info as TensorInfo
                assertEquals(OnnxJavaType.FLOAT, input.type)
                assertEquals(OnnxJavaType.FLOAT, output.type)
                assertEquals(2, input.shape.size)
                assertTrue(input.shape[0] == 1L || input.shape[0] < 0)
                assertTrue(input.shape[1] == 96000L || input.shape[1] < 0)
                assertEquals(2, output.shape.size)
                assertTrue(output.shape[0] == 1L || output.shape[0] < 0)
                assertEquals(9789L, output.shape[1])
                val testAssets = instrumentation.context.assets
                val signalHashes = listOf("59c9fdaecf52cfe3f7e9784ebaa63ccdf60141711563fdc759fd564a29bfc33e",
                    "b4deef20023a7c18e7d105b615f37f397ede4b82774476c1299fbbebc1c3e824",
                    "231abf90251a6916b6ff792c67a2d712286efd44ec3624b4e49871ec8dcd4dae")
                val referenceHashes = listOf("696659c62242310d98ab7426863d116e94aafaa451b033285a5b7cf4afe83b04",
                    "01b10b59c2111836e87f81b190064f85ec53861e3e92d5a21a70d9573e5f2f81",
                    "e4d46af631ed599fc831ea2130843ce922cfdd7a2c66e93e4cabca0c52282bec")
                fun hash(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes)
                    .joinToString("") { "%02x".format(it) }
                for (n in 1..3) {
                    val bytes = testAssets.open("birdnet/signal-$n.f32").use { it.readBytes() }
                    val reference = testAssets.open("birdnet/reference-$n.f32").use { it.readBytes() }
                    assertEquals(96000 * 4, bytes.size)
                    assertEquals(9789 * 4, reference.size)
                    assertEquals(signalHashes[n - 1], hash(bytes))
                    assertEquals(referenceHashes[n - 1], hash(reference))
                    val floats = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).asFloatBuffer()
                    floats.put(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()).rewind()
                    val expected = ByteBuffer.wrap(reference).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()
                    OnnxTensor.createTensor(env, floats, longArrayOf(1, 96000)).use { tensor ->
                        session.run(mapOf("input" to tensor), setOf("predictions")).use { result ->
                            val prediction = result[0] as OnnxTensor
                            assertEquals(OnnxJavaType.FLOAT, prediction.info.type)
                            assertArrayEquals(longArrayOf(1, 9789), prediction.info.shape)
                            val scores = (prediction.value as Array<*>).single() as FloatArray
                            assertEquals(9789, scores.size)
                            var maxDifference = 0f
                            for (score in scores) {
                                assertTrue(score.isFinite() && score in 0f..1f)
                                val value = expected.get()
                                assertTrue(value.isFinite() && value in 0f..1f)
                                maxDifference = maxOf(maxDifference, abs(value - score))
                            }
                            Log.i("BirdNetT4a", "fixture=$n maxDifference=$maxDifference")
                            assertTrue("fixture $n difference $maxDifference > 0.001", maxDifference <= 0.001f)
                        }
                    }
                }
            }
        }
    }
}
