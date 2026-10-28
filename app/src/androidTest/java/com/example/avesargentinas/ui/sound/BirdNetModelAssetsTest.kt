package com.example.avesargentinas.ui.sound

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.TensorInfo
import android.app.ActivityManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.system.Os
import android.system.OsConstants
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.avesargentinas.BirdClassifier
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.tensorflow.lite.TensorFlowLite
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.security.MessageDigest
import kotlin.math.PI
import kotlin.math.sin

@RunWith(AndroidJUnit4::class)
class BirdNetModelAssetsTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun existingPhotoNativeRuntimeLoads() {
        val version = TensorFlowLite.runtimeVersion()
        assertTrue(version.isNotBlank())
        Log.i(TAG, "photoRuntimeVersion=$version; page=${Os.sysconf(OsConstants._SC_PAGESIZE)}")
    }

    @Test
    fun existingPhotoModelPredictsForFixedFixtures() {
        // Referencias medidas con TFLite 2.16.1 en API24/25, antes de cambiar el runtime.
        val references = mapOf(
            "photo-001.webp" to (listOf(38, 40, 2) to listOf(0.90181565f, 0.0034073265f, 0.0033567683f)),
            "photo-002.webp" to (listOf(11, 29, 37) to listOf(0.91397464f, 0.0030245832f, 0.002625227f)),
            "photo-003.webp" to (listOf(16, 12, 26) to listOf(0.87790924f, 0.0042506633f, 0.003572709f))
        )
        val classifier = BirdClassifier(context.assets)
        try {
            classifier.initialize().getOrThrow()
            for ((file, reference) in references) {
                val bitmap = context.assets.open("learning/photos/$file").use { BitmapFactory.decodeStream(it) }
                assertNotNull(bitmap)
                try {
                    val predictions = classifier.classify(bitmap!!).predictions
                    assertEquals(3, predictions.size)
                    assertTrue(predictions.all { it.probability.isFinite() && it.probability in 0f..1f })
                    assertEquals(file, reference.first, predictions.map { it.index })
                    predictions.forEachIndexed { i, prediction ->
                        assertEquals("$file score $i", reference.second[i], prediction.probability, 0.001f)
                    }
                    Log.i(TAG, "photoReference=$file; scores=" +
                        predictions.joinToString { "${it.index}:${it.probability}" })
                } finally {
                    bitmap?.recycle()
                }
            }
        } finally {
            classifier.close()
        }
    }

    @Test
    fun preparedMetadataMatchesEveryPackagedLabelAndHash() {
        val manifest = JSONObject(context.assets.open("birdnet/manifest.json").bufferedReader().use { it.readText() })
        val species = JSONArray(context.assets.open("birdnet/species.json").bufferedReader().use { it.readText() })
        assertEquals("2b4dbfe6b3ff4e5120b6b2abb1141bd318e1b846", manifest.getString("source_commit"))
        assertEquals(BIRDNET_MODEL_FILE, manifest.getString("model_file"))
        assertEquals("306e74f3edc5b95acc6e87ea7f3f1c701fdc0b18ec868b7615208cd6fc2f9457",
            manifest.getJSONObject("sources").getJSONObject("model").getString("sha256"))
        assertEquals("cc696820bc4882f2469a2cd0071b299aa400858aad7843e1f65879867bf5eaa5",
            manifest.getJSONObject("files").getJSONObject(BIRDNET_MODEL_FILE).getString("sha256"))
        assertEquals("306e74f3edc5b95acc6e87ea7f3f1c701fdc0b18ec868b7615208cd6fc2f9457",
            manifest.getJSONObject("derivation").getString("source_sha256"))
        val derivation = manifest.getJSONObject("derivation")
        assertEquals("2026-10-27T02:24:32.873525+00:00", derivation.getString("generated_at_utc"))
        assertEquals("3.10.11", derivation.getString("python"))
        assertEquals("20bca6495d06925631e201f2b257cc37086752e8fe7b6c83a67c6509f4759bc9", derivation.getString("runtime_wheel_sha256"))
        assertEquals("tools/prepare_birdnet_t4.py", derivation.getString("preparer"))
        assertEquals("b2ba8b1383fc6a03ae417f735afb9a5d02cd623e49d82ab147d2b6aaaeea7de9",
            manifest.getJSONObject("sources").getJSONObject("labels").getString("sha256"))
        assertEquals("c1052902a6a003f4040548644bbbc02e8dd24a42425585842b97900ac4e3ad16",
            manifest.getJSONObject("sources").getJSONObject("taxonomy").getString("sha256"))
        assertEquals(9789, species.length())
        // Las cuatro erratas se contrastaron con Cornell; cualquier otra diferencia es error.
        val expectedCorrections = mapOf(
            410 to listOf("767", "Anarhynchus montanus", "Insecta", "Diptera", "BN00678"),
            411 to listOf("768", "Anarhynchus pallidus", "Insecta", "Diptera", "BN00681"),
            2510 to listOf("4442", "Corvus celebensis", "Mammalia", "Artiodactyla", "BN03724"),
            5592 to listOf("10203", "Microtarsus urostictus", "Insecta", "Hemiptera", "BN08926")
        )
        val corrections = manifest.getJSONArray("class_corrections")
        assertEquals(4, corrections.length())
        val correctedIndices = mutableSetOf<Int>()
        for (i in 0 until corrections.length()) {
            val correction = corrections.getJSONObject(i)
            val idx = correction.getInt("idx")
            assertTrue(correctedIndices.add(idx))
            val expected = expectedCorrections.getValue(idx)
            assertEquals(expected, listOf(correction.getInt("id").toString(),
                correction.getString("scientific_name"), correction.getString("label_class"),
                correction.getString("label_order"), correction.getString("taxonomy_id")))
            assertEquals("Aves", correction.getString("class"))
            assertEquals(manifest.getJSONObject("sources").getJSONObject("taxonomy").getString("url"),
                correction.getString("taxonomy_source"))
        }
        assertEquals(expectedCorrections.keys, correctedIndices)
        val labelsFile = manifest.getString("labels_file")
        val labels = context.assets.open("birdnet/$labelsFile").bufferedReader().use { it.readLines() }
        assertEquals(9790, labels.size)
        val ids = mutableSetOf<Int>()
        for (idx in 0 until species.length()) {
            val entry = species.getJSONObject(idx)
            val label = labels[idx + 1].split(';')
            assertEquals(idx, entry.getInt("idx"))
            assertEquals(idx, label[0].toInt())
            assertEquals(label[1].toInt(), entry.getInt("id"))
            assertTrue(ids.add(entry.getInt("id")))
            assertEquals(label[2], entry.getString("scientific_name"))
            val correction = expectedCorrections[idx]
            if (correction != null) {
                assertEquals(correction.take(4), listOf(label[1], label[2], label[4], label[5]))
            }
            assertEquals(if (correction != null) "Aves" else label[4], entry.getString("class"))
        }
        for (name in manifest.getJSONObject("files").keys()) {
            if (name.endsWith(".onnx")) continue
            val expected = manifest.getJSONObject("files").getJSONObject(name)
            val digest = MessageDigest.getInstance("SHA-256")
            var size = 0L
            context.assets.open("birdnet/$name").use { stream ->
                val chunk = ByteArray(8192)
                while (true) {
                    val count = stream.read(chunk)
                    if (count < 0) break
                    digest.update(chunk, 0, count)
                    size += count
                }
            }
            assertEquals(name, expected.getLong("bytes"), size)
            assertEquals(name, expected.getString("sha256"), digest.digest().hex())
        }
    }

    @Test
    fun derivedUncompressedModelRunsOffline() {
        assertEquals("Run this verification in airplane mode", 1,
            Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0))
        val memory = ActivityManager.MemoryInfo()
        (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(memory)
        Log.i(TAG, "device=${Build.MANUFACTURER}/${Build.MODEL}; Android=${Build.VERSION.RELEASE}; " +
            "API=${Build.VERSION.SDK_INT}; ABI=${Build.SUPPORTED_ABIS.joinToString()}; " +
            "RAM=${memory.totalMem}; page=${Os.sysconf(OsConstants._SC_PAGESIZE)}")
        val model = context.assets.openFd("birdnet/$BIRDNET_MODEL_FILE").use { asset ->
            assertEquals(65_830_813L, asset.length)
            asset.createInputStream().use { stream ->
                stream.channel.map(FileChannel.MapMode.READ_ONLY, asset.startOffset, asset.length)
            }
        }
        assertTrue(model.isDirect)
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(model.duplicate())
        assertEquals("cc696820bc4882f2469a2cd0071b299aa400858aad7843e1f65879867bf5eaa5", digest.digest().hex())

        val started = SystemClock.elapsedRealtime()
        OrtEnvironment.getEnvironment().use { environment ->
            assertEquals("1.22.0", environment.version)
            OrtSession.SessionOptions().use { options ->
                options.setExecutionMode(OrtSession.SessionOptions.ExecutionMode.SEQUENTIAL)
                options.setIntraOpNumThreads(2)
                environment.createSession(model, options).use { session ->
                    val prepared = SystemClock.elapsedRealtime()
                    assertEquals(setOf("input"), session.inputNames)
                    val inputInfo = session.inputInfo.getValue("input").info as TensorInfo
                    val outputInfo = session.outputInfo.getValue("predictions").info as TensorInfo
                    assertEquals(OnnxJavaType.FLOAT, inputInfo.type)
                    assertEquals(OnnxJavaType.FLOAT, outputInfo.type)
                    assertEquals(2, inputInfo.shape.size)
                    assertTrue(inputInfo.shape[0] == 1L || inputInfo.shape[0] < 0)
                    assertTrue(inputInfo.shape[1] == 96000L || inputInfo.shape[1] < 0)
                    assertEquals(2, outputInfo.shape.size)
                    assertTrue(outputInfo.shape[0] == 1L || outputInfo.shape[0] < 0)
                    assertEquals(9789L, outputInfo.shape[1])
                    Log.i(TAG, "runtime=${environment.version}; " +
                        "input=$inputInfo; predictions=$outputInfo; preparationMs=${prepared - started}")

                    // Señal generada no constante: prueba ejecución, no identificación de aves.
                    val input = ByteBuffer.allocateDirect(96000 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
                    repeat(96000) { i ->
                        input.put((0.25 * sin(2 * PI * 440 * i / 32000) +
                            0.1 * sin(2 * PI * 1200 * i / 32000)).toFloat())
                    }
                    input.rewind()
                    OnnxTensor.createTensor(environment, input, longArrayOf(1, 96000)).use { tensor ->
                        OrtSession.RunOptions().use { runOptions ->
                            session.run(mapOf("input" to tensor), setOf("predictions"), runOptions).use { result ->
                                val prediction = result[0] as OnnxTensor
                                assertArrayEquals(longArrayOf(1, 9789), prediction.info.shape)
                                assertEquals(OnnxJavaType.FLOAT, prediction.info.type)
                                val scores = (prediction.value as Array<*>).single() as FloatArray
                                assertEquals(9789, scores.size)
                                assertTrue(scores.all { it.isFinite() && it in 0f..1f })
                                Log.i(TAG, "inferenceMs=${SystemClock.elapsedRealtime() - prepared}; " +
                                    "finiteScores=${scores.size}; range=[${scores.minOrNull()},${scores.maxOrNull()}]")
                            }
                        }
                    }
                }
            }
        }
        // Mantener vivo el mapeo hasta después del cierre de sesión y tensores.
        assertEquals(65_830_813, model.capacity())
        Log.i(TAG, "Session, options, tensor and result closed; no microphone or download used")
    }

    private fun ByteArray.hex() = joinToString("") { "%02x".format(it) }

    companion object {
        private const val TAG = "BirdNetModelAssetsTest"
    }
}
