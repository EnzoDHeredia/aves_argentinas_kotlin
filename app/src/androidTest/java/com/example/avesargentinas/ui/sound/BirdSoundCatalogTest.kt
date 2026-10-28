package com.example.avesargentinas.ui.sound

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BirdSoundCatalogTest {
    @Test fun packagedCatalogResolvesOnlyVerifiedLocalMapsAndKeepsUntranslatedSpecies() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val catalog = BirdSoundCatalog.load(context)
        val scores = FloatArray(9789).apply { this[748] = .9f; this[833] = .8f; this[0] = .7f; this[9788] = .6f }
        val results = catalog.select(scores)
        assertEquals(listOf(748, 833, 0, 9788), results.map { it.modelIndex })
        assertEquals("Aramus guarauna", results[0].scientificName)
        assertEquals("Carau", results[0].commonName)
        assertEquals("aramus_guarauna", results[0].localKey)
        assertNotNull(results[0].distributionUri)
        context.contentResolver.openInputStream(android.net.Uri.parse(results[0].distributionUri)).use { assertNotNull(it) }
        assertEquals("Arizelocichla milanjensis", results[1].scientificName)
        assertNull(results[1].commonName)
        assertNull(results[1].localKey)
        assertNull(results[1].distributionUri)
        assertEquals("Abeillia abeillei", results[2].scientificName)
        assertEquals("Colibrí de Abeillé", results[2].commonName)
        assertNull(results[2].localKey)
        assertNull(results[2].distributionUri)
        assertEquals("Zosterornis whiteheadi", results[3].scientificName)
        assertEquals("Timalí de Whitehead", results[3].commonName)
        assertNull(results[3].localKey)
        assertNull(results[3].distributionUri)
        assertThrows(IllegalArgumentException::class.java) { catalog.select(FloatArray(9788)) }
    }

    @Test fun packagedCatalogSelectAllReturnsMoreThanFiveAndPreservesContracts() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val catalog = BirdSoundCatalog.load(context)
        val scores = FloatArray(9789).apply {
            this[748] = .9f
            this[833] = .8f
            this[0] = .7f
            this[9788] = .6f
            this[1] = .5f
            this[2] = .4f
            this[3] = .35f
        }
        val all = catalog.selectAll(scores, minimumScore = .35f)
        assertEquals(7, all.size)
        assertEquals(listOf(748, 833, 0, 9788, 1, 2, 3), all.map { it.modelIndex })
        val top5 = catalog.select(scores, minimumScore = .35f, limit = 5)
        assertEquals(5, top5.size)
        assertEquals(listOf(748, 833, 0, 9788, 1), top5.map { it.modelIndex })
    }
}
