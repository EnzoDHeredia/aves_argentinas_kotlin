package com.example.avesargentinas.ui.sound

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class BirdSoundNamesResourceTest {

    private fun findAsset(relativePath: String): File {
        val candidates = listOf(
            File("src/main/assets", relativePath),
            File("app/src/main/assets", relativePath),
            File("../app/src/main/assets", relativePath)
        )
        return candidates.firstOrNull { it.exists() }
            ?: throw AssertionError("Activo no encontrado en candidatos: $relativePath")
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = file.readBytes()
        return digest.digest(bytes).joinToString("") { "%02x".format(it) }
    }

    private fun parseFlatJson(text: String): Map<String, String> {
        val map = linkedMapOf<String, String>()
        val regex = Regex("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"")
        for (match in regex.findAll(text)) {
            val key = match.groupValues[1]
            val value = match.groupValues[2]
            assertFalse("Clave duplicada en JSON: $key", map.containsKey(key))
            map[key] = value
        }
        return map
    }

    @Test
    fun soundNamesResourceExistsAndHasExpectedEntries() {
        val file = findAsset("birdnet/sound_names_ar.json")
        assertTrue("sound_names_ar.json debe existir", file.exists())
        assertTrue("sound_names_ar.json debe contener datos", file.length() > 40_000)

        val map = parseFlatJson(file.readText(Charsets.UTF_8))
        assertEquals(988, map.size)

        for ((key, name) in map) {
            assertTrue("La clave '$key' no debe estar vacía", key.isNotBlank())
            assertTrue(
                "La clave '$key' debe tener formato snake_case",
                key.matches(Regex("^[a-z]+(_[a-z]+)+$"))
            )
            assertTrue("El nombre común para '$key' no debe estar vacío", name.isNotBlank())
            assertFalse("El nombre no debe contener asteriscos", name.endsWith("*"))
            assertFalse("El nombre no debe contener marcas de endemismo", name.contains("( e )") || name.contains("(e)"))
            assertFalse("El nombre no debe contener marcas poblacionales", name.contains("(continental)", ignoreCase = true))
            assertFalse("El nombre no debe contener marcas poblacionales", name.contains("(malvinas)", ignoreCase = true))
        }
    }

    @Test
    fun soundNamesPrioritizesBuenosAiresAndKeyArgentineSpecies() {
        val file = findAsset("birdnet/sound_names_ar.json")
        val map = parseFlatJson(file.readText(Charsets.UTF_8))

        val keyExamples = mapOf(
            "vanellus_chilensis" to "Tero común",
            "furnarius_rufus" to "Hornero",
            "turdus_rufiventris" to "Zorzal colorado",
            "pitangus_sulphuratus" to "Benteveo común",
            "guira_guira" to "Pirincho",
            "zonotrichia_capensis" to "Chingolo",
            "zenaida_auriculata" to "Torcaza",
            "daptrius_chimango" to "Chimango",
            "caracara_plancus" to "Carancho",
            "troglodytes_aedon" to "Ratona común",
            "troglodytes_musculus" to "Ratona común",
            "athene_cunicularia" to "Lechucita vizcachera",
            "mimus_saturninus" to "Calandria grande",
            "myiopsitta_monachus" to "Cotorra",
            "sicalis_flaveola" to "Jilguero dorado",
            "tyrannus_melancholicus" to "Suirirí real",
            "nannopterum_brasilianum" to "Biguá",
            "spatula_versicolor" to "Pato capuchino",
            "spatula_cyanoptera" to "Pato colorado",
            "spatula_platalea" to "Pato cuchara",
            "mareca_sibilatrix" to "Pato overo",
            "netta_peposaca" to "Pato picazo",
            "rhea_americana" to "Ñandú",
            "molothrus_bonariensis" to "Tordo renegrido",
            "passer_domesticus" to "Gorrión",
            "colaptes_campestris" to "Carpintero campestre",
            "podiceps_major" to "Macá grande"
        )

        for ((key, expected) in keyExamples) {
            assertTrue("El catálogo debe contener la clave '$key'", map.containsKey(key))
            assertEquals("El nombre para '$key' debe coincidir", expected, map[key])
        }
    }

    @Test
    fun soundCatalogParserAcceptsSoundNamesResourceDirectly() {
        val file = findAsset("birdnet/sound_names_ar.json")
        val parsed = BirdSoundCatalog.parseSoundNames(file.readText(Charsets.UTF_8))
        assertEquals(988, parsed.size)
        assertEquals("Tero común", parsed["vanellus_chilensis"])
        assertEquals("Hornero", parsed["furnarius_rufus"])

        val loaded = BirdSoundCatalog.loadSoundNames({ file.inputStream() })
        assertEquals(988, loaded.size)
        assertNull(BirdSoundCatalog.lastLoadError)
    }

    @Test
    fun verifyPreexistingModelAndLabelsMapAreUnmodified() {
        val speciesFile = findAsset("birdnet/species.json")
        assertEquals(
            "species.json no debe modificarse",
            "5e3598bdbc08a0684ba4edc47e7760c44515ce6febb6e5cfd0b0668f778244b7",
            sha256(speciesFile)
        )

        val labelsMapFile = findAsset("labels_map.json")
        assertEquals(
            "labels_map.json no debe modificarse",
            "ef23d3a6006c98f8ad54af80d1b19361b51aaa0846e8dc351c0133f7f974db25",
            sha256(labelsMapFile)
        )

        val csvFile = findAsset("birdnet/BirdNET+_V3.0-preview3.1_Global_10K-pruned_Labels.csv")
        assertEquals(
            "Labels.csv no debe modificarse",
            "b2ba8b1383fc6a03ae417f735afb9a5d02cd623e49d82ab147d2b6aaaeea7de9",
            sha256(csvFile)
        )

        val onnxFile = findAsset("birdnet/BirdNET+_V3.0-preview3.1_Global_10K-pruned_FP16_CPU.onnx")
        assertEquals(
            "Modelo ONNX no debe modificarse",
            "cc696820bc4882f2469a2cd0071b299aa400858aad7843e1f65879867bf5eaa5",
            sha256(onnxFile)
        )
    }
}
