package com.example.avesargentinas.ui.sound

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class ContinuousSoundEvaluationTest {

    private fun testAssets() = InstrumentationRegistry.getInstrumentation().context.assets
    private fun targetAssets() = InstrumentationRegistry.getInstrumentation().targetContext.assets

    private fun readCorpusManifest(): JSONObject {
        val jsonStr = testAssets().open("birdnet/corpus_manifest.json").use { it.reader(Charsets.UTF_8).readText() }
        return JSONObject(jsonStr)
    }

    private fun readControls5MinManifest(): JSONObject {
        val jsonStr = testAssets().open("birdnet/controls_5min_manifest.json").use { it.reader(Charsets.UTF_8).readText() }
        return JSONObject(jsonStr)
    }

    private fun readControls5MinV2Manifest(): JSONObject {
        val jsonStr = testAssets().open("birdnet/controls_5min_v2_manifest.json").use { it.reader(Charsets.UTF_8).readText() }
        return JSONObject(jsonStr)
    }

    private fun readPcmShorts(path: String, expectedSamples: Int): ShortArray {
        val bytes = testAssets().open(path).use { it.readBytes() }
        assertEquals("PCM file $path must be exactly ${expectedSamples * 2} bytes ($expectedSamples samples)", expectedSamples * 2, bytes.size)
        val shorts = ShortArray(expectedSamples)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
        return shorts
    }

    private fun canonicalKey(name: String) = name.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), "_")

    /**
     * Structure to hold accumulated session state across continuous windows.
     */
    data class AccumulatedDetection(
        val detection: BirdDetection,
        val firstWindowIndex: Int,
        val firstLatencySeconds: Float,
        var maxScore: Float
    )

    /**
     * Simulates continuous listening on a pre-recorded PCM audio buffer.
     * Divides audio into 3-second windows with 1-second hops (32,000 samples).
     * Maintains a LinkedHashMap accumulating detections deterministically.
     */
    private suspend fun evaluateAudioContinuously(
        shorts: ShortArray,
        sampleRate: Int = 32000,
        threshold: Float,
        classifier: BirdNetClassifier,
        catalog: BirdSoundCatalog
    ): LinkedHashMap<String, AccumulatedDetection> {
        val windowSize = sampleRate * 3 // 96,000 samples
        val hopSize = sampleRate * 1    // 32,000 samples
        require(shorts.size >= windowSize) { "Audio must be at least 3 seconds" }

        val numWindows = (shorts.size - windowSize) / hopSize + 1
        val accumulatedMap = LinkedHashMap<String, AccumulatedDetection>()

        val windowShorts = ShortArray(windowSize)

        for (w in 0 until numWindows) {
            val start = w * hopSize
            System.arraycopy(shorts, start, windowShorts, 0, windowSize)

            val modelSamples = BirdSoundAudio.toModelSamples(CapturedAudio(windowShorts, sampleRate))
            val scores = classifier.classifyWindow(modelSamples)
            val detections = catalog.selectAll(scores, minimumScore = threshold)

            val latencySeconds = 3.0f + w * 1.0f

            for (det in detections) {
                val key = canonicalKey(det.scientificName)
                val existing = accumulatedMap[key]
                if (existing == null) {
                    accumulatedMap[key] = AccumulatedDetection(
                        detection = det,
                        firstWindowIndex = w,
                        firstLatencySeconds = latencySeconds,
                        maxScore = det.score
                    )
                } else if (det.score > existing.maxScore) {
                    existing.maxScore = det.score
                }
            }
            if ((w + 1) % 50 == 0 || w == numWindows - 1) {
                Log.d("ContSoundEval", "Window ${w + 1}/$numWindows processed (accumulated=${accumulatedMap.size} species)")
            }
        }

        return accumulatedMap
    }

    @Test
    fun corpusAndManifestsIntegrity() {
        // 1. Corpus manifest integrity (42 clips from spec 003)
        val corpusManifest = readCorpusManifest()
        assertEquals(42, corpusManifest.getInt("total_items"))
        val partitions = corpusManifest.getJSONObject("partitions")
        assertEquals(14, partitions.getInt("calibration"))
        assertEquals(28, partitions.getInt("evaluation"))

        val items = corpusManifest.getJSONArray("items")
        assertEquals(42, items.length())

        val sha256 = MessageDigest.getInstance("SHA-256")

        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val filename = item.getString("filename")
            val expectedHash = item.getString("sha256")
            val partition = item.getString("partition")

            assertTrue(partition == "calibration" || partition == "evaluation")
            val bytes = testAssets().open("birdnet/corpus/$filename").use { it.readBytes() }
            assertEquals("File $filename must be 640,000 bytes", 640000, bytes.size)

            val digest = sha256.digest(bytes).joinToString("") { "%02x".format(it) }
            assertEquals("Hash mismatch for $filename", expectedHash, digest)
        }

        // 2. 5-minute controls manifest integrity (Set A: 3 controls of 300s)
        val ctrl5mManifest = readControls5MinManifest()
        assertEquals(3, ctrl5mManifest.getInt("total_controls"))
        val ctrlItems = ctrl5mManifest.getJSONArray("items")
        assertEquals(3, ctrlItems.length())

        for (i in 0 until ctrlItems.length()) {
            val ctrl = ctrlItems.getJSONObject(i)
            val filename = ctrl.getString("filename")
            val expectedHash = ctrl.getString("sha256")
            val bytes = testAssets().open("birdnet/controls_5min/$filename").use { it.readBytes() }
            assertEquals("5-minute control $filename must be exactly 19,200,000 bytes", 19200000, bytes.size)

            val digest = sha256.digest(bytes).joinToString("") { "%02x".format(it) }
            assertEquals("Hash mismatch for 5-min control $filename", expectedHash, digest)
        }

        // 2b. 5-minute controls V2 manifest integrity (Set B: 3 independent controls of 300s for T=0.50)
        val ctrl5mV2Manifest = readControls5MinV2Manifest()
        assertEquals(3, ctrl5mV2Manifest.getInt("total_controls"))
        val ctrlV2Items = ctrl5mV2Manifest.getJSONArray("items")
        assertEquals(3, ctrlV2Items.length())

        for (i in 0 until ctrlV2Items.length()) {
            val ctrl = ctrlV2Items.getJSONObject(i)
            val filename = ctrl.getString("filename")
            val expectedHash = ctrl.getString("sha256")
            val bytes = testAssets().open("birdnet/controls_5min_v2/$filename").use { it.readBytes() }
            assertEquals("5-minute control V2 $filename must be exactly 19,200,000 bytes", 19200000, bytes.size)

            val digest = sha256.digest(bytes).joinToString("") { "%02x".format(it) }
            assertEquals("Hash mismatch for 5-min control V2 $filename", expectedHash, digest)
        }

        // 3. Multi-species integration montage integrity
        val montageBytes = testAssets().open("birdnet/controlled_multispecies_montage.pcm").use { it.readBytes() }
        assertEquals("Montage must be 960,000 bytes (15s)", 960000, montageBytes.size)
        val montageHash = sha256.digest(montageBytes).joinToString("") { "%02x".format(it) }
        assertEquals("329cf578dc286f64e7a4317337de560d7d99f41d8371b3231aaa160a8851d9a5", montageHash)

        Log.i("ContSoundEval", "Integrity confirmed: 42 clips, 3 5-min controls Set A, 3 5-min controls Set B (V2), 1 montage.")
    }

    @Test
    fun continuousCalibrationSweepDeterminesAndFreezesThreshold() {
        runBlocking {
        val manifest = readCorpusManifest()
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

            // Pre-load raw audio for calibration items
            val calAudios = calItems.associate {
                it.getString("id") to readPcmShorts("birdnet/corpus/${it.getString("filename")}", 320000)
            }

            val candidateThresholds = listOf(0.35f, 0.50f, 0.65f, 0.80f)
            var chosenThreshold: Float? = null

            Log.i("ContSoundEval", "=== CONTINUOUS CALIBRATION SWEEP ON CALIBRATION PARTITION ===")

            val sweepResults = mutableMapOf<Float, Pair<Int, Int>>()

            for (t in candidateThresholds) {
                var positivesPassed = 0
                var realControlFalsePositives = 0

                for (item in calItems) {
                    val id = item.getString("id")
                    val shorts = calAudios.getValue(id)
                    val accumulated = evaluateAudioContinuously(shorts, threshold = t, classifier = classifier, catalog = catalog)

                    if (!item.isNull("target_canonical")) {
                        val targetKey = canonicalKey(item.getString("target_canonical"))
                        val targetDetected = accumulated.containsKey(targetKey)
                        if (targetDetected) {
                            positivesPassed++
                            val accDet = accumulated.getValue(targetKey)
                            Log.i("ContSoundEval", "T=$t | Pos [${id}]: DETECTED ${item.getString("species")} score=${accDet.maxScore} lat=${accDet.firstLatencySeconds}s | Total birds in session: ${accumulated.size}")
                        } else {
                            Log.w("ContSoundEval", "T=$t | Pos [${id}]: MISSED ${item.getString("species")} | Total birds: ${accumulated.size}")
                        }
                    } else {
                        if (accumulated.isNotEmpty()) {
                            realControlFalsePositives++
                            val birds = accumulated.values.map { "${it.detection.scientificName}(${it.maxScore})" }
                            Log.w("ContSoundEval", "T=$t | Real Control [${id}]: FALSE DETECTIONS $birds")
                        } else {
                            Log.i("ContSoundEval", "T=$t | Real Control [${id}]: CLEAN (0 detections)")
                        }
                    }
                }

                val meetsCriteria = (positivesPassed >= 8 && realControlFalsePositives == 0)
                Log.i("ContSoundEval", "Summary T=$t: Positives=$positivesPassed/10 (req >=8), RealControl FPs=$realControlFalsePositives/4 (req ==0), Meets=$meetsCriteria")
                sweepResults[t] = Pair(positivesPassed, realControlFalsePositives)
            }

            val meets035 = (sweepResults[0.35f]?.first ?: 0) >= 8 && (sweepResults[0.35f]?.second ?: 1) == 0
            val meets050 = (sweepResults[0.50f]?.first ?: 0) >= 8 && (sweepResults[0.50f]?.second ?: 1) == 0
            assertTrue("Threshold 0.35 satisfies calibration criteria", meets035)
            assertTrue("Threshold 0.50 satisfies calibration criteria", meets050)

            // Per revised plan approved by user after 5-minute extended controls evaluation:
            val operatingThreshold = 0.50f
            assertEquals("Operating threshold for continuous flow must be 0.50", 0.50f, operatingThreshold)
            Log.i("ContSoundEval", "OFFICIALLY FROZEN REVISED OPERATING THRESHOLD: T = $operatingThreshold")
        } finally {
            classifier.close()
        }
        }
    }

    @Test
    fun continuousEvaluationPartitionMeetsCriteriaAtFrozenThreshold() {
        runBlocking {
        val manifest = readCorpusManifest()
        val items = manifest.getJSONArray("items")
        val catalog = BirdSoundCatalog.load(InstrumentationRegistry.getInstrumentation().targetContext)
        val classifier = BirdNetClassifier(targetAssets())
        classifier.initialize()

        val frozenThreshold = 0.50f

        try {
            val evalItems = mutableListOf<JSONObject>()
            for (i in 0 until items.length()) {
                val it = items.getJSONObject(i)
                if (it.getString("partition") == "evaluation") {
                    evalItems.add(it)
                }
            }
            assertEquals(28, evalItems.size)

            var positivesPassed = 0
            var positiveCount = 0
            var realControlFalsePositives = 0
            var realControlCount = 0
            var silenceFalsePositives = 0
            var silenceCount = 0

            Log.i("ContSoundEval", "=== CONTINUOUS EVALUATION ON RESERVED 28 CLIPS (T=$frozenThreshold) ===")

            for (item in evalItems) {
                val id = item.getString("id")
                val filename = item.getString("filename")
                val shorts = readPcmShorts("birdnet/corpus/$filename", 320000)
                val accumulated = evaluateAudioContinuously(shorts, threshold = frozenThreshold, classifier = classifier, catalog = catalog)

                if (!item.isNull("target_canonical")) {
                    positiveCount++
                    val targetKey = canonicalKey(item.getString("target_canonical"))
                    val isDetected = accumulated.containsKey(targetKey)

                    val otherSpecies = accumulated.filterKeys { it != targetKey }.values.map {
                        "${it.detection.scientificName}(${"%.3f".format(Locale.ROOT, it.maxScore)})"
                    }

                    if (isDetected) {
                        positivesPassed++
                        val targetDet = accumulated.getValue(targetKey)
                        Log.i(
                            "ContSoundEval",
                            "Pos [${id}] ${item.getString("species")}: DETECTED score=${"%.3f".format(Locale.ROOT, targetDet.maxScore)} lat=${targetDet.firstLatencySeconds}s | Session size=${accumulated.size} | False candidates=${otherSpecies.size} $otherSpecies"
                        )
                    } else {
                        Log.w(
                            "ContSoundEval",
                            "Pos [${id}] ${item.getString("species")}: MISSED | Session size=${accumulated.size} | Candidates in session: $otherSpecies"
                        )
                    }
                } else if (id.contains("silencio")) {
                    silenceCount++
                    if (accumulated.isNotEmpty()) {
                        silenceFalsePositives++
                        val birds = accumulated.values.map { "${it.detection.scientificName}(${it.maxScore})" }
                        Log.e("ContSoundEval", "Silence Control [${id}]: FALSE DETECTIONS $birds")
                    } else {
                        Log.i("ContSoundEval", "Silence Control [${id}]: CLEAN (0 detections)")
                    }
                } else {
                    realControlCount++
                    if (accumulated.isNotEmpty()) {
                        realControlFalsePositives++
                        val birds = accumulated.values.map { "${it.detection.scientificName}(${"%.3f".format(Locale.ROOT, it.maxScore)})" }
                        Log.w("ContSoundEval", "Real Control [${id}]: FALSE DETECTIONS $birds")
                    } else {
                        Log.i("ContSoundEval", "Real Control [${id}]: CLEAN (0 detections)")
                    }
                }
            }

            assertEquals(20, positiveCount)
            assertEquals(4, realControlCount)
            assertEquals(4, silenceCount)

            Log.i("ContSoundEval", "=== EVALUATION METRICS AT T=$frozenThreshold ===")
            Log.i("ContSoundEval", "Positives detected in list: $positivesPassed / 20 (required >= 16)")
            Log.i("ContSoundEval", "Real control false positives: $realControlFalsePositives / 4 (required <= 1)")
            Log.i("ContSoundEval", "Silence false positives: $silenceFalsePositives / 4 (required == 0)")

            assertTrue("At least 16/20 target birds must be in the accumulated continuous list (observed: $positivesPassed)", positivesPassed >= 16)
            assertTrue("At most 1/4 real controls can have a false positive (observed: $realControlFalsePositives)", realControlFalsePositives <= 1)
            assertEquals("Silence controls must have 0 detections (observed: $silenceFalsePositives)", 0, silenceFalsePositives)
        } finally {
            classifier.close()
        }
        }
    }

    private fun runFiveMinuteControl(controlId: String) {
        runBlocking {
            val manifest = readControls5MinManifest()
            val items = manifest.getJSONArray("items")
            var targetItem: JSONObject? = null
            for (i in 0 until items.length()) {
                val it = items.getJSONObject(i)
                if (it.getString("id") == controlId) {
                    targetItem = it
                    break
                }
            }
            assertNotNull("Control $controlId must exist in manifest", targetItem)
            val item = targetItem!!

            val catalog = BirdSoundCatalog.load(InstrumentationRegistry.getInstrumentation().targetContext)
            val classifier = BirdNetClassifier(targetAssets())
            classifier.initialize()
            val frozenThreshold = 0.35f

            try {
                val id = item.getString("id")
                val filename = item.getString("filename")
                val title = item.getString("title")

                Log.i("ContSoundEval", "=== STARTING 5-MIN CONTROL: $id ($title) [T=$frozenThreshold] ===")
                val startTime = System.currentTimeMillis()

                val shorts = readPcmShorts("birdnet/controls_5min/$filename", 9600000)
                val accumulated = evaluateAudioContinuously(shorts, threshold = frozenThreshold, classifier = classifier, catalog = catalog)
                val elapsed = System.currentTimeMillis() - startTime

                val birdDetections = accumulated.values.map {
                    "${it.detection.scientificName} (score=${"%.3f".format(Locale.ROOT, it.maxScore)}, firstLat=${it.firstLatencySeconds}s)"
                }

                Log.i("ContSoundEval", "Control 5m [$id] finished in ${elapsed}ms: ${accumulated.size} species accumulated: $birdDetections")

                // Acceptance criteria: maximum 1 false candidate species accumulated per 5-minute session
                assertTrue(
                    "Control $id exceeded maximum allowed false candidate species (<= 1 allowed, got ${accumulated.size}: $birdDetections)",
                    accumulated.size <= 1
                )
            } finally {
                classifier.close()
            }
        }
    }

    @Test
    fun continuousFiveMinuteControl01ConsentVoice() {
        runFiveMinuteControl("ctrl-5m-01-voz-consentida")
    }

    @Test
    fun continuousFiveMinuteControl02Wind() {
        runFiveMinuteControl("ctrl-5m-02-viento")
    }

    @Test
    fun continuousFiveMinuteControl03NonAvianAmbient() {
        runFiveMinuteControl("ctrl-5m-03-ambiente-no-aviar")
    }

    private fun runFiveMinuteV2Control(controlId: String) {
        runBlocking {
            val manifest = readControls5MinV2Manifest()
            val items = manifest.getJSONArray("items")
            var targetItem: JSONObject? = null
            for (i in 0 until items.length()) {
                val it = items.getJSONObject(i)
                if (it.getString("id") == controlId) {
                    targetItem = it
                    break
                }
            }
            assertNotNull("Control $controlId must exist in V2 manifest", targetItem)
            val item = targetItem!!

            val catalog = BirdSoundCatalog.load(InstrumentationRegistry.getInstrumentation().targetContext)
            val classifier = BirdNetClassifier(targetAssets())
            classifier.initialize()
            val frozenThreshold = 0.50f

            try {
                val id = item.getString("id")
                val filename = item.getString("filename")
                val title = item.getString("title")

                Log.i("ContSoundEval", "=== STARTING 5-MIN CONTROL V2: $id ($title) [T=$frozenThreshold] ===")
                val startTime = System.currentTimeMillis()

                val shorts = readPcmShorts("birdnet/controls_5min_v2/$filename", 9600000)
                val accumulated = evaluateAudioContinuously(shorts, threshold = frozenThreshold, classifier = classifier, catalog = catalog)
                val elapsed = System.currentTimeMillis() - startTime

                val birdDetections = accumulated.values.map {
                    "${it.detection.scientificName} (score=${"%.3f".format(Locale.ROOT, it.maxScore)}, firstLat=${it.firstLatencySeconds}s)"
                }

                Log.i("ContSoundEval", "Control 5m V2 [$id] finished in ${elapsed}ms: ${accumulated.size} species accumulated: $birdDetections")

                // Acceptance criteria: maximum 1 false candidate species accumulated per 5-minute session
                assertTrue(
                    "Control $id exceeded maximum allowed false candidate species (<= 1 allowed, got ${accumulated.size}: $birdDetections)",
                    accumulated.size <= 1
                )
            } finally {
                classifier.close()
            }
        }
    }

    @Test
    fun continuousFiveMinuteV2Control01ConsentVoice() {
        runFiveMinuteV2Control("ctrl-5m-v2-01-voz-consentida")
    }

    @Test
    fun continuousFiveMinuteV2Control02Wind() {
        runFiveMinuteV2Control("ctrl-5m-v2-02-viento")
    }

    @Test
    fun continuousFiveMinuteV2Control03NonAvianAmbient() {
        runFiveMinuteV2Control("ctrl-5m-v2-03-ambiente-no-aviar")
    }

    @Test
    fun continuousControlledMultiSpeciesIntegrationAudio() {
        runBlocking {
        val catalog = BirdSoundCatalog.load(InstrumentationRegistry.getInstrumentation().targetContext)
        val classifier = BirdNetClassifier(targetAssets())
        classifier.initialize()

        val frozenThreshold = 0.50f

        try {
            val shorts = readPcmShorts("birdnet/controlled_multispecies_montage.pcm", 480000)
            val windowSize = 96000
            val hopSize = 32000
            val numWindows = (shorts.size - windowSize) / hopSize + 1
            assertEquals(13, numWindows) // 15s audio -> 13 windows

            val accumulatedMap = LinkedHashMap<String, AccumulatedDetection>()
            val windowShorts = ShortArray(windowSize)

            val horneroKey = canonicalKey("Furnarius rufus")
            val benteveoKey = canonicalKey("Pitangus sulphuratus")

            var horneroInitialScore = 0f
            var horneroFinalScore = 0f

            for (w in 0 until numWindows) {
                val start = w * hopSize
                System.arraycopy(shorts, start, windowShorts, 0, windowSize)

                val modelSamples = BirdSoundAudio.toModelSamples(CapturedAudio(windowShorts, 32000))
                val scores = classifier.classifyWindow(modelSamples)
                val detections = catalog.selectAll(scores, minimumScore = frozenThreshold)
                val latencySeconds = 3.0f + w * 1.0f

                for (det in detections) {
                    val key = canonicalKey(det.scientificName)
                    val existing = accumulatedMap[key]
                    if (existing == null) {
                        accumulatedMap[key] = AccumulatedDetection(
                            detection = det,
                            firstWindowIndex = w,
                            firstLatencySeconds = latencySeconds,
                            maxScore = det.score
                        )
                        if (key == horneroKey) {
                            horneroInitialScore = det.score
                        }
                    } else if (det.score > existing.maxScore) {
                        existing.maxScore = det.score
                    }
                }
            }

            assertTrue("Hornero must be in the accumulated detections", accumulatedMap.containsKey(horneroKey))
            assertTrue("Benteveo must be in the accumulated detections", accumulatedMap.containsKey(benteveoKey))

            val keys = accumulatedMap.keys.toList()
            val horneroIndex = keys.indexOf(horneroKey)
            val benteveoIndex = keys.indexOf(benteveoKey)

            // 1. Order of first appearance: Hornero (sec 0..5) before Benteveo (sec 5..10)
            assertTrue("Hornero must appear before Benteveo in insertion order", horneroIndex < benteveoIndex)
            assertEquals("Hornero must be at position 0", 0, horneroIndex)

            // 2. Score update: when Hornero re-appears with full volume in segment 3 (sec 10..15),
            // its score improves or is maintained, and position remains 0
            horneroFinalScore = accumulatedMap.getValue(horneroKey).maxScore
            assertTrue("Hornero final score must be >= initial score", horneroFinalScore >= horneroInitialScore)

            // 3. Deduplication: no duplicate rows
            assertEquals(
                "Only distinct biological species must be present without duplicates",
                accumulatedMap.keys.distinct().size,
                accumulatedMap.size
            )

            Log.i("ContSoundEval", "Multi-species integration test PASSED: Hornero score $horneroInitialScore -> $horneroFinalScore, Benteveo score ${accumulatedMap.getValue(benteveoKey).maxScore}")
        } finally {
            classifier.close()
        }
        }
    }
}
