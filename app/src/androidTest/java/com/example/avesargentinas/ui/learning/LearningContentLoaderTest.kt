package com.example.avesargentinas.ui.learning

import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.avesargentinas.BirdLabelsManager
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.security.MessageDigest

@RunWith(AndroidJUnit4::class)
class LearningContentLoaderTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val testAssets = instrumentation.context.assets
    private val loader = LearningContentLoader(testAssets, BirdLabelsManager.loadLabels(context))

    @Test
    fun packagedQuestionsMatchAllFieldsOfTheApprovedBank() {
        val cards = LearningContentLoader(context).load().filterIsInstance<LearningCard.Question>()
        assertEquals((1..40).map { "AVE-%03d".format(it) }, cards.map { it.id })
        assertEquals(8, cards.map { it.topic }.distinct().size)
        assertTrue(cards.groupBy { it.topic }.values.all { it.size == 5 })
        val canonical = cards.joinToString("\u001e") {
            listOf(it.id, it.topic, it.level, it.question, it.answer, it.clarification.orEmpty(),
                it.sources.joinToString("\n")).joinToString("\u001f")
        }
        val digest = MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        // Calculado desde las tablas y referencias del banco aprobado, antes de generar el JSON.
        assertEquals("60b9b43b8196907341072a587b73f2e2df22588340162f1e1c15e4852811469d", digest)
    }

    @Test
    fun packagedPhotosCoverThe45CollectionSpeciesWithDecodableWebpImages() {
        val cards = LearningContentLoader(context).load().filterIsInstance<LearningCard.Photo>()
        val names = BirdLabelsManager.loadLabels(context)
        // La colección no contiene Oxyura jamaicensis; las carpetas por sexo se agrupan.
        val expectedSpecies = names.keys - "oxyura_jamaicensis"
        assertEquals(45, expectedSpecies.size)
        assertEquals(45, cards.size)
        assertEquals(expectedSpecies, cards.map { it.speciesKey }.toSet())
        assertEquals(45, cards.map { it.id }.toSet().size)
        assertEquals(45, cards.map { it.imagePath }.toSet().size)
        val manifest = context.assets.open("learning/photo_cards.json").bufferedReader().use {
            JSONArray(it.readText())
        }
        assertEquals(45, manifest.length())
        assertEquals(cards.map { it.imagePath.substringAfterLast('/') }.toSet(),
            context.assets.list("learning/photos")!!.toSet())
        for (card in cards) {
            assertEquals(names[card.speciesKey], card.commonName)
            assertTrue(card.scientificName.isNotBlank())
            assertFalse(card.scientificName.contains('_'))
            assertTrue(card.imagePath.endsWith(".webp"))
            context.assets.open(card.imagePath).use { stream ->
                val options = BitmapFactory.Options()
                val bitmap = BitmapFactory.decodeStream(stream, null, options)
                assertNotNull(card.imagePath, bitmap)
                bitmap!!.let {
                    try {
                        assertEquals("image/webp", options.outMimeType)
                        assertTrue(card.imagePath, it.width in 1..1024 && it.height in 1..1024)
                    } finally {
                        it.recycle()
                    }
                }
            }
        }
        val session = LearningSession(cards)
        session.startRound(LearningMode.SPECIES)
        assertEquals(45, session.state.availableCount)
        assertEquals(10, session.state.total)
        assertEquals(10, session.state.cards.filterIsInstance<LearningCard.Photo>()
            .map { it.speciesKey }.toSet().size)
        assertNull(session.state.notice)
    }

    @Test
    fun invalidQuestionFieldsDoNotDiscardValidEntries() {
        for (field in listOf("id", "topic", "level", "question", "answer")) {
            for (badValue in listOf("", "  ", JSONObject.NULL, 7, JSONArray())) {
                val invalid = question().put(field, badValue)
                assertEquals(field, listOf("AVE-002"), loader.parseQuestions(
                    JSONArray().put(invalid).put(question("AVE-002")).toString()
                ).map { it.id })
            }
            val missing = question().apply { remove(field) }
            assertTrue(field, loader.parseQuestions(JSONArray().put(missing).toString()).isEmpty())
        }
    }

    @Test
    fun sourcesMustBeANonEmptyListOfNonBlankStrings() {
        for (badSources in listOf(JSONObject.NULL, "F01", JSONArray(),
            JSONArray().put(" "), JSONArray().put(12))) {
            assertTrue(loader.parseQuestions(JSONArray().put(question().put("sources", badSources))
                .toString()).isEmpty())
        }
        assertTrue(loader.parseQuestions(JSONArray().put(question().apply { remove("sources") })
            .toString()).isEmpty())
    }

    @Test
    fun optionalClarificationIsPreservedAndWrongTypesAreRejected() {
        val withClarification = question().put("clarification", "Una aclaración.")
        assertEquals("Una aclaración.", loader.parseQuestions("[$withClarification]").single().clarification)
        assertNull(loader.parseQuestions("[${question()}]").single().clarification)
        assertTrue(loader.parseQuestions("[${question().put("clarification", 42)}]").isEmpty())
    }

    @Test
    fun firstValidQuestionWinsAndInvalidEntriesDoNotReserveIdentifiers() {
        val entries = JSONArray().put(question().put("answer", " "))
            .put(question().put("answer", "Primera válida."))
            .put(question().put("answer", "Duplicada."))
            .put(JSONObject.NULL).put(5).put(question("AVE-002"))
        val cards = loader.parseQuestions(entries.toString())
        assertEquals(listOf("AVE-001", "AVE-002"), cards.map { it.id })
        assertEquals("Primera válida.", cards.first().answer)
    }

    @Test
    fun malformedOrWrongRootDocumentsYieldNoCards() {
        for (text in listOf("", "[", "[{}", "{}", "null", "[] trailing")) {
            assertTrue(text, loader.parseQuestions(text).isEmpty())
            assertTrue(text, loader.parsePhotos(text).isEmpty())
        }
    }

    @Test
    fun identifiersMustNotContainWhitespaceOrPathSeparators() {
        for (id in listOf("AVE 001", "../AVE-001", "AVE/001", "AVE:001")) {
            assertTrue(id, loader.parseQuestions("[${question(id)}]").isEmpty())
            assertTrue(id, loader.parsePhotos("[${photo(id)}]").isEmpty())
        }
    }

    @Test
    fun photoNamesComeFromTheExistingCatalogAndCanonicalKey() {
        val card = loader.parsePhotos("[${photo()}]").single()
        assertEquals("PHOTO-001", card.id)
        assertEquals("vanellus_chilensis", card.speciesKey)
        assertEquals("tero comun", card.commonName)
        assertEquals("Vanellus chilensis", card.scientificName)
        assertEquals("learning/photos/valid.png", card.imagePath)
        testAssets.open(card.imagePath).use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)
            assertNotNull(bitmap)
            bitmap!!.recycle()
        }
    }

    @Test
    fun unknownSpeciesAndDatasetFolderNamesAreExcluded() {
        for (key in listOf("unknown_species", "Vanellus_chilensis", "01_vanellus_chilensis",
            "vanellus_chilensis_macho", "", " ")) {
            assertTrue(key, loader.parsePhotos("[${photo().put("speciesKey", key)}]").isEmpty())
        }
    }

    @Test
    fun blankCatalogNamesAreExcluded() {
        val invalidNames = LearningContentLoader(testAssets, mapOf("vanellus_chilensis" to " "))
        assertTrue(invalidNames.parsePhotos("[${photo()}]").isEmpty())
    }

    @Test
    fun photoFieldsMustBeNonBlankStrings() {
        for (field in listOf("id", "speciesKey", "imagePath")) {
            for (badValue in listOf("", " ", JSONObject.NULL, 12)) {
                assertTrue(field, loader.parsePhotos("[${photo().put(field, badValue)}]").isEmpty())
            }
            val missing = photo().apply { remove(field) }
            assertTrue(field, loader.parsePhotos("[$missing]").isEmpty())
        }
    }

    @Test
    fun externalAndEscapingImagePathsAreExcluded() {
        for (path in listOf("https://example.org/bird.png", "file:///tmp/bird.png",
            "content://photos/bird", "/learning/photos/valid.png", "C:/bird.png",
            "learning/valid.png", "learning/photos/../valid.png", "learning/photos/./valid.png",
            "learning/photos//valid.png", "learning/photos/%2e%2e/valid.png",
            "learning/photos\\valid.png", "learning/photos/valid.png?x=1")) {
            assertTrue(path, loader.parsePhotos("[${photo().put("imagePath", path)}]").isEmpty())
        }
    }

    @Test
    fun missingAndUndecodableImagesDoNotDiscardTheValidPhoto() {
        val entries = JSONArray().put(photo().put("imagePath", "learning/photos/missing.png"))
            .put(photo().put("imagePath", "learning/photos/unreadable.png"))
            .put(photo())
        assertEquals(listOf("PHOTO-001"), loader.parsePhotos(entries.toString()).map { it.id })
    }

    @Test
    fun firstValidPhotoWinsByIdentifierAndMultiplePhotosOfASpeciesRemainAvailable() {
        val entries = JSONArray().put(photo().put("speciesKey", "unknown_species"))
            .put(photo()).put(photo().put("speciesKey", "ardea_alba"))
            .put(photo("PHOTO-002")).put(JSONObject.NULL)
        val cards = loader.parsePhotos(entries.toString())
        assertEquals(listOf("PHOTO-001", "PHOTO-002"), cards.map { it.id })
        assertTrue(cards.all { it.speciesKey == "vanellus_chilensis" })
        val session = LearningSession(cards)
        session.startRound(LearningMode.SPECIES)
        assertEquals(1, session.state.availableCount)
        assertEquals(1, session.state.total)
    }

    @Test
    fun brokenPhotoDocumentDoesNotInvalidateQuestions() {
        for (path in listOf("learning/malformed.json", "learning/missing.json")) {
            val cards = loader.load("learning/valid_questions.json", path)
            assertEquals(1, cards.filterIsInstance<LearningCard.Question>().size)
            assertTrue(cards.filterIsInstance<LearningCard.Photo>().isEmpty())
        }
    }

    @Test
    fun brokenQuestionDocumentDoesNotInvalidatePhotos() {
        for (path in listOf("learning/malformed.json", "learning/missing.json")) {
            val cards = loader.load(path, "learning/valid_photos.json")
            assertEquals(3, cards.filterIsInstance<LearningCard.Photo>().size)
            assertTrue(cards.filterIsInstance<LearningCard.Question>().isEmpty())
        }
    }

    @Test
    fun loadedCatalogDistinguishesAvailableInsufficientAndUnavailablePerMode() {
        val packaged = LearningSession(LearningContentLoader(context).load())
        packaged.startRound(LearningMode.QUESTIONS)
        assertEquals(40, packaged.state.availableCount)
        assertEquals(10, packaged.state.total)
        assertNull(packaged.state.notice)
        val small = LearningSession(loader.load("learning/valid_questions.json", "learning/valid_photos.json"))
        small.startRound(LearningMode.QUESTIONS)
        assertEquals(1, small.state.availableCount)
        assertEquals(LearningNotice.INSUFFICIENT_CONTENT, small.state.notice)
        small.startRound(LearningMode.SPECIES)
        assertEquals(2, small.state.availableCount)
        assertEquals(2, small.state.total)
        assertEquals(LearningNotice.INSUFFICIENT_CONTENT, small.state.notice)
        val empty = LearningSession(loader.load("learning/missing.json", "learning/malformed.json"))
        for (mode in LearningMode.values()) {
            empty.startRound(mode)
            assertEquals(0, empty.state.availableCount)
            assertEquals(LearningScreen.SELECTOR, empty.state.screen)
            assertEquals(LearningNotice.UNAVAILABLE_CONTENT, empty.state.notice)
        }
    }

    private fun question(id: String = "AVE-001") = JSONObject()
        .put("id", id).put("topic", "Anatomía y plumaje").put("level", "Inicial")
        .put("question", "¿Qué es la muda?").put("answer", "El reemplazo de las plumas.")
        .put("sources", JSONArray().put("[F01]: https://academy.allaboutbirds.org/feathers-article/"))

    private fun photo(id: String = "PHOTO-001") = JSONObject()
        .put("id", id).put("speciesKey", "vanellus_chilensis")
        .put("imagePath", "learning/photos/valid.png")
}
