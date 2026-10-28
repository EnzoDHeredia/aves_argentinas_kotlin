package com.example.avesargentinas.ui.sound

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class BirdSoundEvaluationTest {

    private fun testAssets() = InstrumentationRegistry.getInstrumentation().context.assets
    private fun targetAssets() = InstrumentationRegistry.getInstrumentation().targetContext.assets

    private fun readManifest(): JSONObject {
        val jsonStr = testAssets().open("birdnet/corpus_manifest.json").use { it.reader(Charsets.UTF_8).readText() }
        return JSONObject(jsonStr)
    }

    private fun readPcmSamples(filename: String): FloatArray {
        val bytes = testAssets().open("birdnet/corpus/$filename").use { it.readBytes() }
        assertEquals("PCM file $filename must be exactly 640000 bytes (320000 samples)", 640000, bytes.size)
        val shorts = ShortArray(320000)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
        return BirdSoundAudio.toModelSamples(CapturedAudio(shorts, 32000))
    }

    private fun normalizeKey(name: String) = name.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), "_")

    @Test
    fun corpusIntegrityAndManifestVerification() {
        val manifest = readManifest()
        assertEquals(42, manifest.getInt("total_items"))
        val partitions = manifest.getJSONObject("partitions")
        assertEquals(14, partitions.getInt("calibration"))
        assertEquals(28, partitions.getInt("evaluation"))

        val items = manifest.getJSONArray("items")
        assertEquals(42, items.length())

        val seenObs = mutableSetOf<String>()
        val sha256 = MessageDigest.getInstance("SHA-256")

        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val filename = item.getString("filename")
            val expectedHash = item.getString("sha256")
            val partition = item.getString("partition")

            assertTrue(partition == "calibration" || partition == "evaluation")
            val bytes = testAssets().open("birdnet/corpus/$filename").use { it.readBytes() }
            assertEquals(640000, bytes.size)

            val digest = sha256.digest(bytes).joinToString("") { "%02x".format(it) }
            assertEquals("Hash mismatch for $filename", expectedHash, digest)

            val obsId = item.optString("obs_id")
            if (obsId.isNotBlank() && !obsId.startsWith("synthetic")) {
                assertFalse("Duplicate observation ID across corpus: $obsId", obsId in seenObs)
                seenObs.add(obsId)
            }
        }
    }

    @Test
    fun calibrationPartitionDeterminesThreshold() {
        runBlocking {
        val manifest = readManifest()
        val items = manifest.getJSONArray("items")
        val catalog = BirdSoundCatalog.load(InstrumentationRegistry.getInstrumentation().targetContext)
        val classifier = BirdNetClassifier(targetAssets())
        classifier.initialize()

        try {
            val calItems = mutableListOf<JSONObject>()
            for (i in 0 until items.length()) {
                val it = items.getJSONObject(i)
                if (it.getString("partition") == "calibration") {
                    calItems.add(it)
                }
            }
            assertEquals(14, calItems.size)

            // Pre-classify all calibration items
            val calScores = calItems.associate { it.getString("id") to classifier.classify(readPcmSamples(it.getString("filename"))) }

            val candidateThresholds = listOf(0.35f, 0.50f, 0.65f, 0.80f)
            var chosenThreshold: Float? = null

            for (t in candidateThresholds) {
                var positivesPassed = 0
                var realControlFalsePositives = 0

                for (item in calItems) {
                    val scores = calScores.getValue(item.getString("id"))
                    val detections = catalog.select(scores, minimumScore = t, limit = 5)

                    if (!item.isNull("target_canonical")) {
                        val targetKey = normalizeKey(item.getString("target_canonical"))
                        val inTop5 = detections.any { normalizeKey(it.scientificName) == targetKey }
                        if (inTop5) positivesPassed++
                    } else {
                        if (detections.isNotEmpty()) {
                            realControlFalsePositives++
                        }
                    }
                }

                val meetsCriteria = (positivesPassed >= 8 && realControlFalsePositives == 0)
                Log.i("CorpusCalib", "Threshold T=$t: positives=$positivesPassed/10, control_fps=$realControlFalsePositives/4, meets=$meetsCriteria")

                if (meetsCriteria && chosenThreshold == null) {
                    chosenThreshold = t
                }
            }

            assertNotNull("A valid threshold must be found among candidate thresholds", chosenThreshold)
            assertEquals("Minimum threshold meeting calibration criteria must be 0.35", 0.35f, chosenThreshold!!)
            Log.i("CorpusCalib", "Officially calibrated threshold fixed at: $chosenThreshold")
        } finally {
            classifier.close()
        }
    }
}

    @Test
    fun reservedPartitionEvaluationMeetsAcceptanceCriteria() {
        runBlocking {
            val manifest = readManifest()
        val items = manifest.getJSONArray("items")
        val catalog = BirdSoundCatalog.load(InstrumentationRegistry.getInstrumentation().targetContext)
        val classifier = BirdNetClassifier(targetAssets())
        classifier.initialize()

        try {
            val evalItems = mutableListOf<JSONObject>()
            for (i in 0 until items.length()) {
                val it = items.getJSONObject(i)
                if (it.getString("partition") == "evaluation") {
                    evalItems.add(it)
                }
            }
            assertEquals(28, evalItems.size)

            val fixedThreshold = 0.35f
            var positivesInTop5 = 0
            var positivesTop1 = 0
            var realControlFalsePositives = 0
            var silenceFalsePositives = 0

            var positiveTotal = 0
            var realControlTotal = 0
            var silenceTotal = 0

            for (item in evalItems) {
                val filename = item.getString("filename")
                val scores = classifier.classify(readPcmSamples(filename))
                val detections = catalog.select(scores, minimumScore = fixedThreshold, limit = 5)

                if (!item.isNull("target_canonical")) {
                    positiveTotal++
                    val targetKey = normalizeKey(item.getString("target_canonical"))
                    val rank = detections.indexOfFirst { normalizeKey(it.scientificName) == targetKey }
                    if (rank >= 0) {
                        positivesInTop5++
                        if (rank == 0) positivesTop1++
                    }
                    val detectionNames = detections.map { "${it.scientificName}(${"%.3f".format(Locale.ROOT, it.score)})" }
                    Log.i("CorpusEval", "Positive [${item.getString("id")}]: target=${item.getString("species")}, rank=${if (rank >= 0) rank + 1 else "MISS"}, detections=$detectionNames")
                } else if (item.getString("id").contains("silencio")) {
                    silenceTotal++
                    if (detections.isNotEmpty()) {
                        silenceFalsePositives++
                    }
                } else {
                    realControlTotal++
                    if (detections.isNotEmpty()) {
                        realControlFalsePositives++
                        val detectionNames = detections.map { "${it.scientificName}(${"%.3f".format(Locale.ROOT, it.score)})" }
                        Log.w("CorpusEval", "False positive in real control [${item.getString("id")}]: $detectionNames")
                    }
                }
            }

            assertEquals(20, positiveTotal)
            assertEquals(4, realControlTotal)
            assertEquals(4, silenceTotal)

            Log.i("CorpusEval", "=== RESERVED EVALUATION METRICS (T=$fixedThreshold) ===")
            Log.i("CorpusEval", "Positives in top 5: $positivesInTop5 / 20 (required >= 16)")
            Log.i("CorpusEval", "Positives top 1: $positivesTop1 / 20 (${positivesTop1 * 100f / 20f}%)")
            Log.i("CorpusEval", "Real control false positives: $realControlFalsePositives / 4 (required <= 1)")
            Log.i("CorpusEval", "Silence false positives: $silenceFalsePositives / 4 (required == 0)")

            assertTrue("At least 16/20 target birds must be in top 5 (observed: $positivesInTop5)", positivesInTop5 >= 16)
            assertTrue("At most 1/4 real controls can have a false positive (observed: $realControlFalsePositives)", realControlFalsePositives <= 1)
            assertEquals("Silence controls must have 0 detections (observed: $silenceFalsePositives)", 0, silenceFalsePositives)
        } finally {
            classifier.close()
        }
    }
}
}
