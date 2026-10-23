package com.example.avesargentinas.ui.bird

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class SpeciesInfoTest {
    private val male = SpeciesPhoto("learning/photos/male.webp", PhotoSex.MACHO, true, null, null, emptyList())
    private val female = SpeciesPhoto("birds/photos/female.webp", PhotoSex.HEMBRA, false, null, null, emptyList())
    private val info = SpeciesInfo("oxyura_vittata", photos = listOf(male, female))

    @Test fun generalConsultationSelectsOnlyTheRepresentative() {
        for (variant in listOf(null, "", "  ")) assertSame(male, info.photoForVariant(variant))
        assertNull(info.copy(photos = listOf(female)).photoForVariant(null))
        assertNull(info.copy(photos = listOf(male, female.copy(representative = true))).photoForVariant(null))
    }

    @Test fun sexSelectionIsExactAndCaseInsensitive() {
        assertSame(male, info.photoForVariant("MaChO"))
        assertSame(female, info.photoForVariant(" HEMBRA "))
        assertNull(info.photoForVariant("juvenil"))
        assertNull(info.copy(photos = listOf(male)).photoForVariant("hembra"))
        assertNull(info.copy(photos = listOf(male, male.copy(imagePath = "other.webp"))).photoForVariant("macho"))
    }

    @Test fun identityResolutionUsesCanonicalKeyAndRootLocaleWithoutFuzzyMatching() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            val catalog = SpeciesInfoCatalog(species = mapOf(info.speciesKey to info))
            assertSame(info, catalog.find("OXYURA VITTATA"))
            assertSame(info, catalog.find("oxyura-vittata"))
            assertSame(info, catalog.find("oxyura_vittata"))
            assertNull(catalog.find("oxyura_jamaicensis"))
            assertNull(catalog.find("Pato zambullidor chico"))
        } finally {
            Locale.setDefault(previous)
        }
    }
}
