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
        assertEquals(BirdSoundResultAdapter.SCORE_CHANGE_PAYLOAD, payload)
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
}
