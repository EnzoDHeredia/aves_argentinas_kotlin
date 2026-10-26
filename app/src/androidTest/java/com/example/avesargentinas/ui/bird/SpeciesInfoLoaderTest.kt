package com.example.avesargentinas.ui.bird

import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpeciesInfoLoaderTest {
    private val assets = InstrumentationRegistry.getInstrumentation().context.assets
    private val loader = SpeciesInfoLoader(assets)
    private fun fixture() = assets.open("species_info_fixture.json").bufferedReader().use { JSONObject(it.readText()) }
    private fun JSONObject.entry() = getJSONArray("species").getJSONObject(0)

    // Complete T5 catalog: structural checks do not certify photo permissions or biological claims.
    @Test fun completeCatalogCoversRealLabelsWithSharedBiologyAndOriginalRepresentativePhotos() {
        val appAssets = InstrumentationRegistry.getInstrumentation().targetContext.assets
        val catalog = SpeciesInfoLoader(appAssets).load()
        val keys = listOf("phalacrocorax_brasilianus", "aramus_guarauna", "chauna_torquata",
            "syrigma_sibilatrix", "ciconia_maguari", "cygnus_melancoryphus", "coscoroba_coscoroba",
            "phimosus_infuscatus", "plegadis_chihi", "platalea_ajaja", "phoenicopterus_chilensis",
            "fulica_leucoptera", "fulica_rufifrons", "fulica_armillata", "egretta_thula",
            "ardea_alba", "nycticorax_nycticorax", "ardea_cocoi", "chroicocephalus_maculipennis",
            "chroicocephalus_cirrocephalus", "larus_dominicanus", "progne_chalybea", "hirundo_rustica",
            "tigrisoma_lineatum", "aramides_ypecaha", "jacana_jacana", "rollandia_rolland",
            "podiceps_major", "podilymbus_podiceps", "anas_flavirostris",
            "anas_versicolor", "anas_cyanoptera", "anas_platalea", "amazonetta_brasiliensis",
            "callonetta_leucophrys", "anas_bahamensis", "anas_georgica", "anas_sibilatrix",
            "netta_peposaca", "dendrocygna_bicolor", "dendrocygna_viduata", "oxyura_vittata",
            "gallinula_galeata", "vanellus_chilensis", "himantopus_mexicanus")
        assertTrue(catalog.issues.toString(), catalog.issues.isEmpty())
        assertEquals(keys.toSet(), catalog.species.keys)
        val manifest = appAssets.open("learning/photo_cards.json").bufferedReader().use { JSONArray(it.readText()) }
        assertEquals(45, manifest.length())
        val labels = appAssets.open("labels.txt").bufferedReader().use { it.readLines() }
        assertEquals(50, labels.size)
        assertEquals(50, labels.distinct().size)
        val pairedKeys = setOf("amazonetta_brasiliensis", "anas_cyanoptera", "callonetta_leucophrys",
            "netta_peposaca", "oxyura_vittata")
        val labelKeys = labels.map { label ->
            val identity = label.substringAfter('.')
            val variant = when {
                identity.endsWith("_macho") -> "macho"
                identity.endsWith("_hembra") -> "hembra"
                else -> null
            }
            val key = if (variant == null) identity else identity.removeSuffix("_$variant")
            val bird = catalog.species.getValue(key)
            if (variant != null) {
                assertSame(bird, catalog.find(key.replace('_', ' ')))
                assertEquals(PhotoSex.valueOf(variant.uppercase()), bird.photoForVariant(variant)!!.sex)
            } else assertNotNull(key, bird.photoForVariant(null))
            key
        }
        assertEquals(keys.toSet(), labelKeys.toSet())
        assertEquals(pairedKeys, labelKeys.groupingBy { it }.eachCount().filterValues { it == 2 }.keys)
        assertFalse("oxyura_jamaicensis" in catalog.species)
        val hashes = mutableSetOf<String>()
        keys.forEachIndexed { index, key ->
            val bird = catalog.species.getValue(key)
            val fields = mapOf("commonName" to bird.commonName, "scientificName" to bird.scientificName,
                "englishName" to bird.englishName, "family" to bird.family, "group" to bird.group,
                "order" to bird.order, "argentineRegions" to bird.argentineRegions,
                "description" to bird.description, "habitat" to bird.habitat, "diet" to bird.diet,
                "seasonalMovements" to bird.seasonalMovements)
            fields.forEach { (field, value) ->
                if (field == "seasonalMovements" && key == "gallinula_galeata") {
                    assertNull("Documented unavailable seasonal movements", value)
                } else assertFalse("$key.$field", value.isNullOrBlank())
                if (value != null) assertFalse("$key.$field sources", bird.fieldSources[field].isNullOrEmpty())
            }
            bird.fieldSources.values.flatten().forEach { assertTrue(it, it in catalog.sources) }
            listOfNotNull(bird.conservationGlobal, bird.conservationNational).forEach { status ->
                assertFalse(key, status.sourceIds.isEmpty())
                status.sourceIds.forEach { assertTrue(it, it in catalog.sources) }
            }
            assertNotNull(key, bird.conservationGlobal)
            assertEquals(2015, bird.conservationNational!!.assessmentYear)
            val card = manifest.getJSONObject(index)
            assertEquals("PHOTO-%03d".format(index + 1), card.getString("id"))
            assertEquals(key, card.getString("speciesKey"))
            assertEquals(1, bird.photos.count { it.representative })
            assertEquals(if (key in pairedKeys) 2 else 1, bird.photos.size)
            val photo = bird.photoForVariant(null)!!
            assertEquals(card.getString("imagePath"), photo.imagePath)
            photo.sourceIds.forEach { assertTrue(it, it in catalog.sources) }
            if (key in pairedKeys) {
                val complementarySex = if (key == "oxyura_vittata") "macho" else "hembra"
                val representativeSex = if (complementarySex == "macho") "hembra" else "macho"
                assertEquals(card.getString("imagePath"), bird.photoForVariant(representativeSex)!!.imagePath)
                assertEquals("birds/photos/${key}_${complementarySex}.webp",
                    bird.photoForVariant(complementarySex)!!.imagePath)
                assertNotEquals(bird.photoForVariant("macho")!!.imagePath,
                    bird.photoForVariant("hembra")!!.imagePath)
            }
            bird.photos.forEach { selected ->
                selected.sourceIds.forEach { assertTrue(it, it in catalog.sources) }
                val bytes = appAssets.open(selected.imagePath).use { it.readBytes() }
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                assertNotNull(selected.imagePath, bitmap)
                bitmap!!.recycle()
                assertTrue(selected.imagePath, hashes.add(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(bytes).joinToString("") { "%02x".format(it) }))
            }
        }
        assertEquals(50, hashes.size)
        val flamingo = catalog.species.getValue("phoenicopterus_chilensis")
        assertEquals("Casi amenazada (NT)", flamingo.conservationGlobal!!.category)
        assertEquals("Vulnerable (VU)", flamingo.conservationNational!!.category)
    }

    // Asset checks do not certify biological identity or permission; see fotos-ficha-especies.md.
    @Test fun preparedSexPhotosDecodeAtTheirDocumentedDimensionsWithoutDuplicateFiles() {
        val appAssets = InstrumentationRegistry.getInstrumentation().targetContext.assets
        val photos = listOf(
            Triple("learning/photos/photo-034.webp", 1024, 683),
            Triple("birds/photos/amazonetta_brasiliensis_hembra.webp", 1024, 827),
            Triple("learning/photos/photo-032.webp", 1024, 768),
            Triple("birds/photos/anas_cyanoptera_hembra.webp", 1024, 681),
            Triple("learning/photos/photo-035.webp", 1024, 683),
            Triple("birds/photos/callonetta_leucophrys_hembra.webp", 1024, 683),
            Triple("learning/photos/photo-039.webp", 1024, 768),
            Triple("birds/photos/netta_peposaca_hembra.webp", 1024, 683),
            Triple("learning/photos/photo-042.webp", 1024, 641),
            Triple("birds/photos/oxyura_vittata_macho.webp", 1024, 768)
        )
        val hashes = mutableSetOf<String>()
        for ((path, width, height) in photos) {
            assertTrue("Missing asset: $path", appAssets.list(path.substringBeforeLast('/'))
                .orEmpty().contains(path.substringAfterLast('/')))
            val bytes = appAssets.open(path).use { it.readBytes() }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            assertNotNull("Undecodable asset: $path", bitmap)
            bitmap!!.let {
                assertEquals(path, width, it.width)
                assertEquals(path, height, it.height)
                it.recycle()
            }
            val hash = java.security.MessageDigest.getInstance("SHA-256").digest(bytes)
                .joinToString("") { "%02x".format(it) }
            assertTrue("Duplicate photo: $path", hashes.add(hash))
        }
    }

    @Test fun readsTheLocalContractWithSharedBiologyAndSeparateConservationScopes() {
        val catalog = loader.load("species_info_fixture.json")
        assertTrue(catalog.issues.toString(), catalog.issues.isEmpty())
        assertNotNull(catalog.find("Fixture bird"))
        val bird = catalog.find("Fixture bird")!!
        assertEquals("Ave ficticia (solo pruebas)", bird.commonName)
        assertEquals("Fixture bird", bird.scientificName)
        assertEquals("Test bird", bird.englishName)
        assertEquals("Familia de prueba", bird.family)
        assertEquals("Grupo de prueba", bird.group)
        assertEquals("Orden de prueba", bird.order)
        assertEquals("Región ficticia", bird.argentineRegions)
        assertEquals("Texto ficticio, sin valor biológico", bird.description)
        assertEquals("Hábitat de prueba", bird.habitat)
        assertEquals("Dieta de prueba", bird.diet)
        assertEquals("Movimientos de prueba", bird.seasonalMovements)
        assertEquals("Categoría global ficticia", bird.conservationGlobal!!.category)
        assertEquals(2020, bird.conservationGlobal.assessmentYear)
        assertEquals("Categoría nacional ficticia", bird.conservationNational!!.category)
        assertEquals(2017, bird.conservationNational.assessmentYear)
        assertEquals(listOf("fixture-source"), bird.fieldSources["description"])
        val photo = bird.photoForVariant("HEMBRA")!!
        assertEquals("birds/photos/fixture-female.webp", photo.imagePath)
        assertEquals("Autor ficticio", photo.author)
        assertEquals("Permiso ficticio de prueba", photo.license)
        assertEquals(listOf("fixture-source"), photo.sourceIds)
        assertEquals("learning/photos/fixture-male.webp", bird.photoForVariant(null)!!.imagePath)
        assertEquals(SpeciesSource("fixture-source", "Referencia ficticia de prueba", "Pruebas",
            "https://example.invalid/fixture", "2026-10-22"), catalog.sources["fixture-source"])
    }

    @Test fun rejectsInvalidDocumentsAndReportsUnreadableAssets() {
        for (text in listOf("{", "[]", "{}", "{\"sources\":[],\"species\":[]} trailing")) {
            val result = loader.parse(text)
            assertTrue(result.species.isEmpty())
            assertTrue(text, result.issues.isNotEmpty())
        }
        val missing = loader.load("missing-species-fixture.json")
        assertTrue(missing.species.isEmpty())
        assertTrue(missing.issues.isNotEmpty())
    }

    @Test fun isolatesMalformedEntriesAndRejectsDuplicateOrInvalidIdentities() {
        val document = fixture()
        val entries = document.getJSONArray("species")
        entries.put(JSONObject(document.entry().toString()).put("commonName", "Duplicado"))
        entries.put(JSONObject().put("speciesKey", "../invalid"))
        entries.put(42)
        entries.put(JSONObject().put("speciesKey", "second_bird"))
        val result = loader.parse(document.toString())
        assertEquals(setOf("fixture_bird", "second_bird"), result.species.keys)
        assertEquals("Ave ficticia (solo pruebas)", result.species["fixture_bird"]!!.commonName)
        assertTrue(result.issues.isNotEmpty())
    }

    @Test fun absentOrInvalidOptionalFieldsBecomeUnavailableWithoutDiscardingTheBird() {
        val document = fixture()
        document.entry().apply {
            remove("habitat")
            put("diet", 123)
            put("description", JSONObject.NULL)
            put("conservationGlobal", "invalid")
            getJSONObject("conservationNational").put("assessmentYear", "2017")
        }
        val result = loader.parse(document.toString())
        assertNotNull(result.species["fixture_bird"])
        val bird = result.species["fixture_bird"]!!
        assertNull(bird.habitat)
        assertNull(bird.diet)
        assertNull(bird.description)
        assertNull(bird.conservationGlobal)
        assertNull(bird.conservationNational!!.assessmentYear)
        assertEquals("Grupo de prueba", bird.group)
        assertTrue(result.issues.isNotEmpty())
    }

    @Test fun badReferencesDoNotDiscardValidReferencesOrBiologicalFields() {
        val document = fixture()
        document.getJSONArray("sources").put(JSONObject().put("id", "broken"))
        document.entry().getJSONObject("fieldSources").put("description", JSONArray()
            .put("missing").put("fixture-source").put(17))
        document.entry().getJSONArray("photos").getJSONObject(0)
            .put("sourceIds", JSONArray().put("missing").put("fixture-source"))
        val result = loader.parse(document.toString())
        assertNotNull(result.species["fixture_bird"])
        val bird = result.species["fixture_bird"]!!
        assertEquals(setOf("fixture-source"), result.sources.keys)
        assertEquals(listOf("fixture-source"), bird.fieldSources["description"])
        assertEquals(listOf("fixture-source"), bird.photoForVariant(null)!!.sourceIds)
        assertEquals("Texto ficticio, sin valor biológico", bird.description)
        assertTrue(result.issues.isNotEmpty())
    }

    @Test fun unsafePhotoPathsAreRejectedWithoutLosingTheSpeciesOrOtherPhotos() {
        for (path in listOf("/tmp/bird.webp", "C:/birds/bird.webp", "C:\\birds\\bird.webp",
            "https://example.invalid/bird.webp", "//server/bird.webp", "file:///bird.webp",
            "../bird.webp", "birds/../bird.webp", "birds\\..\\bird.webp", "birds/%2e%2e/bird.webp")) {
            val document = fixture()
            document.entry().getJSONArray("photos").getJSONObject(1).put("imagePath", path)
            val result = loader.parse(document.toString())
            assertNotNull(result.species["fixture_bird"])
            val bird = result.species["fixture_bird"]!!
            assertNull(path, bird.photoForVariant("hembra"))
            assertNotNull(path, bird.photoForVariant(null))
            assertTrue(path, result.issues.isNotEmpty())
        }
    }

    @Test fun invalidSexDoesNotBecomeAGeneralOrOppositeSexPhoto() {
        val document = fixture()
        document.entry().getJSONArray("photos").getJSONObject(1).put("sex", "juvenil")
        val result = loader.parse(document.toString())
        assertNotNull(result.species["fixture_bird"])
        assertNull(result.species["fixture_bird"]!!.photoForVariant("hembra"))
        assertTrue(result.issues.isNotEmpty())
    }

    @Test fun duplicateSourcesAndInvalidDatesAreReportedWithoutOverwritingTheFirstSource() {
        val document = fixture()
        val sources = document.getJSONArray("sources")
        sources.put(JSONObject(sources.getJSONObject(0).toString()).put("title", "Duplicada"))
        sources.put(JSONObject(sources.getJSONObject(0).toString()).put("id", "bad-date")
            .put("consultedOn", "2026-02-30"))
        val result = loader.parse(document.toString())
        assertEquals(setOf("fixture-source"), result.sources.keys)
        assertEquals("Referencia ficticia de prueba", result.sources["fixture-source"]!!.title)
        assertTrue(result.issues.isNotEmpty())
    }

    @Test fun missingOrAmbiguousRepresentativeIsReportedAndNeverChosenArbitrarily() {
        for (representative in listOf(false, true)) {
            val document = fixture()
            val photos = document.entry().getJSONArray("photos")
            photos.getJSONObject(0).put("representative", representative)
            photos.getJSONObject(1).put("representative", representative)
            val result = loader.parse(document.toString())
            assertNotNull(result.species["fixture_bird"])
            assertNull(result.species["fixture_bird"]!!.photoForVariant(null))
            assertNotNull(result.species["fixture_bird"]!!.photoForVariant("macho"))
            assertTrue(result.issues.isNotEmpty())
        }
    }

    @Test fun invalidPhotoAndConservationFieldsDoNotLoseOtherPhotosOrBiology() {
        val document = fixture()
        document.entry().apply {
            getJSONObject("conservationGlobal").put("category", 42)
            getJSONArray("photos").getJSONObject(1).put("representative", "false")
            getJSONObject("fieldSources").put("diet", "fixture-source")
        }
        val result = loader.parse(document.toString())
        assertNotNull(result.species["fixture_bird"])
        val bird = result.species["fixture_bird"]!!
        assertNull(bird.conservationGlobal)
        assertNull(bird.photoForVariant("hembra"))
        assertNotNull(bird.photoForVariant("macho"))
        assertEquals("Dieta de prueba", bird.diet)
        assertTrue(result.issues.isNotEmpty())
    }
}
