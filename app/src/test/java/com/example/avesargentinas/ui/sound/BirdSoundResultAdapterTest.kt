package com.example.avesargentinas.ui.sound

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BirdSoundResultAdapterTest {

    @Test
    fun diffCallbackMatchesSameCanonicalScientificNameCaseInsensitiveAndTrimmed() {
        val diff = BirdSoundResultAdapter.BirdDetectionDiffCallback

        val item1 = BirdDetection(
            modelIndex = 1,
            modelScientificName = "Furnarius rufus",
            scientificName = "Furnarius rufus",
            commonName = "Hornero",
            score = 0.85f,
            localKey = "hornero",
            distributionUri = null
        )

        val item1Upper = BirdDetection(
            modelIndex = 1,
            modelScientificName = "FURNARIUS RUFUS",
            scientificName = "  furnarius rufus  ",
            commonName = "Hornero Común",
            score = 0.95f,
            localKey = "hornero",
            distributionUri = null
        )

        val item2 = BirdDetection(
            modelIndex = 2,
            modelScientificName = "Pitangus sulphuratus",
            scientificName = "Pitangus sulphuratus",
            commonName = "Benteveo",
            score = 0.70f,
            localKey = "benteveo",
            distributionUri = null
        )

        assertTrue(diff.areItemsTheSame(item1, item1Upper))
        assertFalse(diff.areItemsTheSame(item1, item2))
    }

    @Test
    fun diffCallbackDetectsContentChangesAndReturnsPayloadForScoreUpdate() {
        val diff = BirdSoundResultAdapter.BirdDetectionDiffCallback

        val item1 = BirdDetection(
            modelIndex = 1,
            modelScientificName = "Furnarius rufus",
            scientificName = "Furnarius rufus",
            commonName = "Hornero",
            score = 0.85f,
            localKey = "hornero",
            distributionUri = null
        )

        val item1Same = item1.copy()
        assertTrue(diff.areContentsTheSame(item1, item1Same))
        assertNull(diff.getChangePayload(item1, item1Same))

        val item1ScoreImproved = item1.copy(score = 0.92f)
        assertFalse(diff.areContentsTheSame(item1, item1ScoreImproved))
        val payload = diff.getChangePayload(item1, item1ScoreImproved)
        assertNotNull(payload)
        assertEquals(BirdSoundResultAdapter.SCORE_IMPROVED_PAYLOAD, payload)

        val item1ScoreDecreased = item1.copy(score = 0.75f)
        assertFalse(diff.areContentsTheSame(item1, item1ScoreDecreased))
        val decreasedPayload = diff.getChangePayload(item1, item1ScoreDecreased)
        assertNotNull(decreasedPayload)
        assertEquals(BirdSoundResultAdapter.SCORE_CHANGE_PAYLOAD, decreasedPayload)

        val item1NameChanged = item1.copy(commonName = "Hornero Común")
        assertFalse(diff.areContentsTheSame(item1, item1NameChanged))
        val namePayload = diff.getChangePayload(item1, item1NameChanged)
        assertNotNull(namePayload)
        assertEquals(BirdSoundResultAdapter.SCORE_CHANGE_PAYLOAD, namePayload)
    }

    @Test
    fun findNewSpeciesKeysIdentifiesOnlyNewDetectionsCaseInsensitively() {
        val item1 = BirdDetection(1, "Furnarius rufus", "Furnarius rufus", "Hornero", 0.85f, "hornero", null)
        val item2 = BirdDetection(2, "Pitangus sulphuratus", "Pitangus sulphuratus", "Benteveo", 0.70f, "benteveo", null)
        val item2Variant = BirdDetection(2, "PITANGUS SULPHURATUS", "  pitangus sulphuratus  ", "Benteveo", 0.80f, "benteveo", null)
        val item3 = BirdDetection(3, "Turdus rufiventris", "Turdus rufiventris", "Zorzal", 0.65f, "zorzal", null)

        val previous = listOf(item1, item2)
        val nextWithImprovementAndNew = listOf(item1.copy(score = 0.95f), item2Variant, item3)

        val newKeys = BirdSoundResultAdapter.findNewSpeciesKeys(previous, nextWithImprovementAndNew)
        assertEquals(setOf("turdus rufiventris"), newKeys)

        val initialList = listOf(item1, item2)
        val initialKeys = BirdSoundResultAdapter.findNewSpeciesKeys(emptyList(), initialList)
        assertEquals(setOf("furnarius rufus", "pitangus sulphuratus"), initialKeys)
    }

    @Test
    fun diffCallbackAllows200DistinctSpeciesItemsWithoutCollisions() {
        val diff = BirdSoundResultAdapter.BirdDetectionDiffCallback
        val items = (1..200).map { i ->
            BirdDetection(
                modelIndex = i,
                modelScientificName = "Species $i",
                scientificName = "Species $i",
                commonName = "Ave $i",
                score = 0.5f + (i % 50) * 0.01f,
                localKey = if (i % 2 == 0) "key_$i" else null,
                distributionUri = null
            )
        }

        assertEquals(200, items.size)
        for (i in 0 until 199) {
            assertFalse(diff.areItemsTheSame(items[i], items[i + 1]))
            assertTrue(diff.areItemsTheSame(items[i], items[i].copy(score = 0.99f)))
        }
    }

    @Test
    fun scoreToPercentConvertsAndRoundsFloatScoreCorrectly() {
        assertEquals(50, BirdSoundResultAdapter.scoreToPercent(0.5f))
        assertEquals(70, BirdSoundResultAdapter.scoreToPercent(0.7f))
        assertEquals(0, BirdSoundResultAdapter.scoreToPercent(0.0f))
        assertEquals(100, BirdSoundResultAdapter.scoreToPercent(1.0f))
        assertEquals(74, BirdSoundResultAdapter.scoreToPercent(0.74f))
        assertEquals(95, BirdSoundResultAdapter.scoreToPercent(0.95f))
        assertEquals(50, BirdSoundResultAdapter.scoreToPercent(0.504f))
        assertEquals(51, BirdSoundResultAdapter.scoreToPercent(0.506f))
        assertEquals(0, BirdSoundResultAdapter.scoreToPercent(-0.1f))
        assertEquals(100, BirdSoundResultAdapter.scoreToPercent(1.05f))
        assertEquals(0, BirdSoundResultAdapter.scoreToPercent(Float.NaN))
    }

    @Test
    fun detectionPreservesExactScoreValueWithoutAlteringForPresentation() {
        val detection = BirdDetection(
            modelIndex = 1,
            modelScientificName = "Furnarius rufus",
            scientificName = "Furnarius rufus",
            commonName = "Hornero",
            score = 0.7425f,
            localKey = "hornero",
            distributionUri = null
        )

        assertEquals(0.7425f, detection.score, 0.00001f)
        assertEquals(74, BirdSoundResultAdapter.scoreToPercent(detection.score))
        assertEquals(0.7425f, detection.score, 0.00001f)
    }
}
