package com.example.avesargentinas.ui.sound

import org.junit.Assert.*
import org.junit.Test

class BirdSoundCatalogTest {
    private fun entry(i: Int, name: String, canonical: String = name, group: String = "Aves", spanish: String? = null) =
        SoundSpecies(i, name, canonical, group, spanish)
    private val entries = listOf(
        entry(0, "Alpha one", spanish = "Oficial"), entry(1, "Alpha variant", "Alpha one"),
        entry(2, "Beta two"), entry(3, "Not bird", group = "Mammalia"),
        entry(4, "Gamma three"), entry(5, "Delta four"), entry(6, "Epsilon five"), entry(7, "Zeta six")
    )
    private val catalog = BirdSoundCatalog(entries, mapOf("alpha_one" to "Regional"), mapOf("alpha_one" to "map-uri"))

    @Test fun groupsByMaximumFiltersAndOrdersWithStableTies() {
        val found = catalog.select(floatArrayOf(.35f, .8f, .8f, 1f, .7f, .6f, .5f, .4f))
        assertEquals(listOf(1, 2, 4, 5, 6), found.map { it.modelIndex })
        assertEquals("Regional", found[0].commonName)
        assertEquals("alpha_one", found[0].localKey)
        assertEquals("map-uri", found[0].distributionUri)
        assertEquals(.8f, found[0].score, 0f)
        assertNull(found[1].commonName)
        assertNull(found[1].localKey)
    }

    @Test fun thresholdIsInclusiveAndFirstAndLastIndicesWork() {
        val found = catalog.select(floatArrayOf(.35f, 0f, 0f, 0f, 0f, 0f, 0f, .35f))
        assertEquals(listOf(0, 7), found.map { it.modelIndex })
        assertTrue(catalog.select(FloatArray(8)).isEmpty())
        assertEquals(1, catalog.select(floatArrayOf(0f, 0f, .4f, 0f, 0f, 0f, 0f, 0f)).size)
        assertEquals(1, catalog.select(FloatArray(8) { 1f }, limit = 1).size)
    }

    @Test fun rejectsIncompatibleOrInvalidScoresEvenForNonBirds() {
        assertThrows(IllegalArgumentException::class.java) { catalog.select(FloatArray(7)) }
        for (value in listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, -.01f, 1.01f)) {
            assertThrows(IllegalArgumentException::class.java) { catalog.select(FloatArray(8).apply { this[3] = value }) }
        }
        assertThrows(IllegalArgumentException::class.java) { catalog.select(FloatArray(8), minimumScore = Float.NaN) }
        assertThrows(IllegalArgumentException::class.java) { catalog.select(FloatArray(8), limit = 6) }
    }

    @Test fun similarNamesNeverResolveAnotherSpeciesAndRegionalNameDoesNotRequireMap() {
        val c = BirdSoundCatalog(listOf(entry(0, "Alpha one", spanish = "Oficial"), entry(1, "Alphaa one")),
            mapOf("alpha_one" to "Regional"), emptyMap())
        val results = c.select(floatArrayOf(.9f, .8f))
        assertEquals("Regional", results[0].commonName)
        assertNull(results[0].localKey)
        assertNull(results[1].commonName)
        assertNull(results[1].localKey)
    }

    @Test fun selectAllReturnsMoreThanFiveEligibleSpeciesInDeterministicOrder() {
        val scores = floatArrayOf(.35f, .8f, .8f, 1f, .7f, .6f, .5f, .4f)
        val all = catalog.selectAll(scores, minimumScore = .35f)
        assertEquals(6, all.size)
        assertEquals(listOf(1, 2, 4, 5, 6, 7), all.map { it.modelIndex })
        // top 5 contract preserved in select()
        val top5 = catalog.select(scores, minimumScore = .35f)
        assertEquals(5, top5.size)
        assertEquals(listOf(1, 2, 4, 5, 6), top5.map { it.modelIndex })
        // custom limit
        val top3 = catalog.select(scores, minimumScore = .35f, limit = 3)
        assertEquals(listOf(1, 2, 4), top3.map { it.modelIndex })
    }

    @Test fun selectAllHandlesMissingNamesAndMapsAndPreservesCanonicalIdentity() {
        val scores = floatArrayOf(.35f, .8f, .8f, 1f, .7f, .6f, .5f, .4f)
        val all = catalog.selectAll(scores, minimumScore = .35f)
        val winnerCanonical = all.first { it.modelIndex == 1 }
        assertEquals("Alpha variant", winnerCanonical.modelScientificName)
        assertEquals("Alpha one", winnerCanonical.scientificName)
        assertEquals("Regional", winnerCanonical.commonName)
        assertEquals("alpha_one", winnerCanonical.localKey)
        assertEquals("map-uri", winnerCanonical.distributionUri)
        assertEquals(.8f, winnerCanonical.score, 0f)

        val missingAll = all.first { it.modelIndex == 2 }
        assertEquals("Beta two", missingAll.modelScientificName)
        assertEquals("Beta two", missingAll.scientificName)
        assertNull(missingAll.commonName)
        assertNull(missingAll.localKey)
        assertNull(missingAll.distributionUri)
    }

    @Test fun selectAllRejectsInvalidScoresAndThresholds() {
        assertThrows(IllegalArgumentException::class.java) { catalog.selectAll(FloatArray(7)) }
        for (value in listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, -.01f, 1.01f)) {
            assertThrows(IllegalArgumentException::class.java) { catalog.selectAll(FloatArray(8).apply { this[3] = value }) }
        }
        assertThrows(IllegalArgumentException::class.java) { catalog.selectAll(FloatArray(8), minimumScore = Float.NaN) }
        assertThrows(IllegalArgumentException::class.java) { catalog.selectAll(FloatArray(8), minimumScore = -.01f) }
        assertThrows(IllegalArgumentException::class.java) { catalog.selectAll(FloatArray(8), minimumScore = 1.01f) }
    }

    @Test fun selectAllThresholdIsInclusive() {
        val found = catalog.selectAll(floatArrayOf(.35f, 0f, 0f, 0f, 0f, 0f, 0f, .35f), minimumScore = .35f)
        assertEquals(listOf(0, 7), found.map { it.modelIndex })
        val empty = catalog.selectAll(floatArrayOf(.3499f, 0f, 0f, 0f, 0f, 0f, 0f, .3499f), minimumScore = .35f)
        assertTrue(empty.isEmpty())
    }

    @Test fun rejectsShiftedIndicesAndUsesSpanishWhenRegionalMissing() {
        assertThrows(IllegalArgumentException::class.java) { BirdSoundCatalog(listOf(entry(1, "Wrong")), emptyMap(), emptyMap()) }
        val c = BirdSoundCatalog(listOf(entry(0, "Alpha one", spanish = "Oficial")), emptyMap(), emptyMap())
        assertEquals("Oficial", c.select(floatArrayOf(1f)).single().commonName)
        assertNull(c.select(floatArrayOf(1f)).single().localKey)
    }
}
