package com.example.avesargentinas.ui.sound

import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

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
        val top5 = catalog.select(scores, minimumScore = .35f)
        assertEquals(5, top5.size)
        assertEquals(listOf(1, 2, 4, 5, 6), top5.map { it.modelIndex })
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

    @Test fun soundNamesTakesPriorityOverRegionalAndSpanish() {
        val testSpecies = listOf(
            entry(0, "Vanellus chilensis", spanish = "Queltehue"),
            entry(1, "Furnarius rufus", spanish = "Hornero común")
        )
        val regional = mapOf(
            "vanellus_chilensis" to "Tero",
            "furnarius_rufus" to "Casero"
        )
        val soundNames = mapOf(
            "vanellus_chilensis" to "Tero común",
            "furnarius_rufus" to "Hornero"
        )
        val maps = mapOf("vanellus_chilensis" to "tero-map-uri")
        val c = BirdSoundCatalog(testSpecies, regional, maps, soundNames)

        val results = c.selectAll(floatArrayOf(0.9f, 0.8f))
        assertEquals(2, results.size)
        // Prioridad 1: Nombre de sonido
        assertEquals("Tero común", results[0].commonName)
        assertEquals("Hornero", results[1].commonName)
        // Fichas y mapas conservan sus fuentes regionales
        assertEquals("vanellus_chilensis", results[0].localKey)
        assertEquals("tero-map-uri", results[0].distributionUri)
        // Furnarius rufus no tiene mapa en 'maps', por lo que localKey es null
        assertNull(results[1].localKey)
        assertNull(results[1].distributionUri)
    }

    @Test fun fallbacksPreserveHierarchyWhenSoundNameMissingOrBlank() {
        val testSpecies = listOf(
            entry(0, "Aves uno", spanish = "Español Uno"),
            entry(1, "Aves dos", spanish = "Español Dos"),
            entry(2, "Aves tres", spanish = null),
            entry(3, "Aves cuatro", spanish = "   ")
        )
        val regional = mapOf(
            "aves_uno" to "Regional Uno",
            "aves_dos" to "   ",
            "aves_tres" to "Regional Tres"
        )
        val soundNames = mapOf(
            "aves_uno" to "   " // Blanco debe ser ignorado y caer en regional
        )
        val c = BirdSoundCatalog(testSpecies, regional, emptyMap(), soundNames)
        val results = c.selectAll(floatArrayOf(0.9f, 0.8f, 0.7f, 0.6f))

        // aves_uno: sonido en blanco -> regional "Regional Uno"
        assertEquals("Regional Uno", results[0].commonName)
        // aves_dos: sin sonido, regional en blanco -> español "Español Dos"
        assertEquals("Español Dos", results[1].commonName)
        // aves_tres: sin sonido, regional "Regional Tres", sin español -> "Regional Tres"
        assertEquals("Regional Tres", results[2].commonName)
        // aves_cuatro: sin sonido, sin regional, español en blanco -> null
        assertNull(results[3].commonName)
    }

    @Test fun soundNamesDoNotGrantLocalSheetOrMap() {
        val testSpecies = listOf(
            entry(0, "Sound only bird", canonical = "Sound only bird"),
            entry(1, "Regional and sound bird", canonical = "Regional and sound bird")
        )
        val soundNames = mapOf(
            "sound_only_bird" to "Pájaro Sólo Sonido",
            "regional_and_sound_bird" to "Pájaro Mixto"
        )
        val regional = mapOf(
            "regional_and_sound_bird" to "Pájaro Regional"
        )
        val maps = mapOf(
            "regional_and_sound_bird" to "map-uri-mixto"
        )
        val c = BirdSoundCatalog(testSpecies, regional, maps, soundNames)
        val results = c.selectAll(floatArrayOf(0.9f, 0.85f))

        // Sound only: tiene nombre de sonido, pero NO ficha ni mapa
        assertEquals("Pájaro Sólo Sonido", results[0].commonName)
        assertNull(results[0].localKey)
        assertNull(results[0].distributionUri)

        // Mixto: tiene nombre de sonido Y conserva ficha y mapa de fuentes regionales
        assertEquals("Pájaro Mixto", results[1].commonName)
        assertEquals("regional_and_sound_bird", results[1].localKey)
        assertEquals("map-uri-mixto", results[1].distributionUri)
    }

    @Test fun preservesIdentityIndicesScoresAndTiesWithSoundNames() {
        val testSpecies = listOf(
            entry(0, "Alpha model", canonical = "Alpha canonical"),
            entry(1, "Alpha alias", canonical = "Alpha canonical"),
            entry(2, "Beta model", canonical = "Beta canonical")
        )
        val soundNames = mapOf(
            "alpha_canonical" to "Alfa común",
            "beta_canonical" to "Beta común"
        )
        val c = BirdSoundCatalog(testSpecies, emptyMap(), emptyMap(), soundNames)

        // Empate en puntuación entre 0 y 1 para Alpha: debe prevalecer el índice menor (0)
        val results = c.selectAll(floatArrayOf(0.85f, 0.85f, 0.90f))
        assertEquals(listOf(2, 0), results.map { it.modelIndex })
        assertEquals("Beta model", results[0].modelScientificName)
        assertEquals("Beta canonical", results[0].scientificName)
        assertEquals("Beta común", results[0].commonName)
        assertEquals(0.90f, results[0].score, 0f)

        assertEquals("Alpha model", results[1].modelScientificName)
        assertEquals("Alpha canonical", results[1].scientificName)
        assertEquals("Alfa común", results[1].commonName)
        assertEquals(0.85f, results[1].score, 0f)
    }

    @Test fun parseSoundNamesValidatesCorrectly() {
        val validJson = """
            {
              "furnarius_rufus": "Hornero",
              "vanellus_chilensis": "Tero común"
            }
        """.trimIndent()
        val parsed = BirdSoundCatalog.parseSoundNames(validJson)
        assertEquals(2, parsed.size)
        assertEquals("Hornero", parsed["furnarius_rufus"])
        assertEquals("Tero común", parsed["vanellus_chilensis"])

        // Vacío o en blanco
        assertThrows(IllegalArgumentException::class.java) { BirdSoundCatalog.parseSoundNames("") }
        assertThrows(IllegalArgumentException::class.java) { BirdSoundCatalog.parseSoundNames("   ") }

        // No es objeto JSON (array o primitivo)
        assertThrows(IllegalArgumentException::class.java) { BirdSoundCatalog.parseSoundNames("[\"a\", \"b\"]") }
        assertThrows(IllegalArgumentException::class.java) { BirdSoundCatalog.parseSoundNames("\"string\"") }

        // Contenido después del objeto
        assertThrows(IllegalArgumentException::class.java) {
            BirdSoundCatalog.parseSoundNames("""{"a": "b"} trailing""")
        }

        // Claves duplicadas
        val duplicateJson = """
            {
              "furnarius_rufus": "Hornero",
              "furnarius_rufus": "Hornero duplicado"
            }
        """.trimIndent()
        assertThrows(IllegalArgumentException::class.java) { BirdSoundCatalog.parseSoundNames(duplicateJson) }

        // Clave duplicada por diferencia de formato/espacios
        val duplicateNormalizedJson = """
            {
              "furnarius_rufus": "Hornero",
              "Furnarius rufus": "Hornero dos"
            }
        """.trimIndent()
        assertThrows(IllegalArgumentException::class.java) { BirdSoundCatalog.parseSoundNames(duplicateNormalizedJson) }

        // Clave vacía
        val emptyKeyJson = """
            {
              "": "Hornero"
            }
        """.trimIndent()
        assertThrows(IllegalArgumentException::class.java) { BirdSoundCatalog.parseSoundNames(emptyKeyJson) }

        // Valor vacío
        val emptyValueJson = """
            {
              "furnarius_rufus": "   "
            }
        """.trimIndent()
        assertThrows(IllegalArgumentException::class.java) { BirdSoundCatalog.parseSoundNames(emptyValueJson) }
    }

    @Test fun loadSoundNamesHandlesMissingOrInvalidResourceGracefully() {
        BirdSoundCatalog.lastLoadError = null

        // 1. Recurso ausente (IOException)
        val missingResult = BirdSoundCatalog.loadSoundNames(
            openStream = { throw IOException("Archivo no encontrado") },
            path = "non_existent.json"
        )
        assertTrue(missingResult.isEmpty())
        assertNotNull(BirdSoundCatalog.lastLoadError)
        assertTrue(BirdSoundCatalog.lastLoadError is IOException)
        assertEquals("Archivo no encontrado", BirdSoundCatalog.lastLoadError?.message)

        // 2. Recurso corrupto (JSON inválido)
        BirdSoundCatalog.lastLoadError = null
        val corruptResult = BirdSoundCatalog.loadSoundNames(
            openStream = { "{ corrupt json".byteInputStream() },
            path = "corrupt.json"
        )
        assertTrue(corruptResult.isEmpty())
        assertNotNull(BirdSoundCatalog.lastLoadError)

        // 3. Catálogo construido con mapa vacío preserva comportamiento anterior
        val fallbackCatalog = BirdSoundCatalog(entries, mapOf("alpha_one" to "Regional"), mapOf("alpha_one" to "map-uri"), missingResult)
        val detection = fallbackCatalog.select(floatArrayOf(0.9f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)).single()
        assertEquals("Regional", detection.commonName)
        assertEquals("alpha_one", detection.localKey)
    }

    @Test fun announcementAndNavigationCoherentlyConsumeDetectionNames() {
        val detectionWithName = BirdDetection(
            modelIndex = 1,
            modelScientificName = "Furnarius rufus",
            scientificName = "Furnarius rufus",
            commonName = "Hornero",
            score = 0.92f,
            localKey = "furnarius_rufus",
            distributionUri = "map-uri"
        )
        val detectionWithoutCommonName = BirdDetection(
            modelIndex = 2,
            modelScientificName = "Wild birdus",
            scientificName = "Wild birdus",
            commonName = null,
            score = 0.75f,
            localKey = null,
            distributionUri = null
        )

        // Anuncio TalkBack coherente
        val nameForAnnouncement1 = detectionWithName.commonName ?: detectionWithName.scientificName
        assertEquals("Hornero", nameForAnnouncement1)
        val nameForAnnouncement2 = detectionWithoutCommonName.commonName ?: detectionWithoutCommonName.scientificName
        assertEquals("Wild birdus", nameForAnnouncement2)

        // Título de navegación a ficha coherente
        val nameForInfoSheet1 = detectionWithName.commonName ?: detectionWithName.scientificName
        assertEquals("Hornero", nameForInfoSheet1)
        val nameForInfoSheet2 = detectionWithoutCommonName.commonName ?: detectionWithoutCommonName.scientificName
        assertEquals("Wild birdus", nameForInfoSheet2)
    }
}
