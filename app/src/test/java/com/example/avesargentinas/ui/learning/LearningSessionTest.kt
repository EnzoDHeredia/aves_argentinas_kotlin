package com.example.avesargentinas.ui.learning

import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class LearningSessionTest {
    @Test
    fun fortyQuestionsProduceTenDistinctQuestionsWithControlledRandomness() {
        val catalog = questions(40)
        for (seed in 0..20) {
            val session = LearningSession(catalog, Random(seed))
            session.startRound(LearningMode.QUESTIONS)
            val state = session.state
            assertEquals(10, state.total)
            assertEquals(10, state.cards.map { it.id }.distinct().size)
            assertTrue(catalog.containsAll(state.cards))
            assertNull(state.notice)
            val repeat = LearningSession(catalog, Random(seed))
            repeat.startRound(LearningMode.QUESTIONS)
            assertEquals(state.cards, repeat.state.cards)
        }
    }

    @Test
    fun duplicateQuestionIdsDoNotFillTheRound() {
        val question = questions(1).single()
        val session = LearningSession(listOf(question, question.copy(answer = "Otra respuesta")))
        session.startRound(LearningMode.QUESTIONS)
        assertEquals(listOf(question), session.state.cards)
        assertEquals(1, session.state.availableCount)
    }

    @Test
    fun photosAreChosenBySpeciesBeforeChoosingAnImage() {
        val catalog = (1..15).map { photo("species-$it", "photo-$it") } +
            (1..40).map { photo("species-1", "female-$it") }
        for (seed in 0..20) {
            val session = LearningSession(catalog, Random(seed))
            session.startRound(LearningMode.SPECIES)
            assertEquals(15, session.state.availableCount)
            assertEquals(10, session.state.total)
            assertEquals(10, session.state.cards.filterIsInstance<LearningCard.Photo>()
                .map { it.speciesKey }.distinct().size)
            assertTrue(catalog.containsAll(session.state.cards))
        }
    }

    @Test
    fun multiplePhotosOfOneSpeciesStillProduceOneCard() {
        val session = LearningSession(listOf(photo("same", "male"), photo("same", "female")))
        session.startRound(LearningMode.SPECIES)
        assertEquals(1, session.state.total)
        assertEquals(1, session.state.availableCount)
        assertEquals(LearningNotice.INSUFFICIENT_CONTENT, session.state.notice)
    }

    @Test
    fun eachModeUsesOnlyItsOwnCatalog() {
        val session = LearningSession(questions(1) + photo("species", "photo"))
        session.startRound(LearningMode.QUESTIONS)
        assertTrue(session.state.currentCard is LearningCard.Question)
        session.startRound(LearningMode.SPECIES)
        assertTrue(session.state.currentCard is LearningCard.Photo)
    }

    @Test
    fun newSessionIsAnEmptySelector() {
        val state = LearningSession(questions(40)).state
        assertEquals(LearningScreen.SELECTOR, state.screen)
        assertNull(state.mode)
        assertTrue(state.cards.isEmpty())
        assertTrue(state.assessments.isEmpty())
        assertNull(state.cardToken)
        assertNull(state.summary)
    }

    @Test
    fun emptyCatalogDoesNotStartOrInventASummary() {
        val session = LearningSession(emptyList())
        for (mode in LearningMode.values()) {
            session.startRound(mode)
            assertEquals(LearningScreen.SELECTOR, session.state.screen)
            assertEquals(mode, session.state.mode)
            assertEquals(LearningNotice.UNAVAILABLE_CONTENT, session.state.notice)
            assertEquals(0, session.state.availableCount)
            assertEquals(0, session.state.total)
            assertNull(session.state.currentCard)
            assertNull(session.state.cardToken)
            assertNull(session.state.summary)
            val before = session.state
            assertFalse(session.flip(1L))
            assertFalse(session.assess(1L, SelfAssessment.KNOWN))
            assertEquals(before, session.state)
        }
    }

    @Test
    fun oneAndNineQuestionsStartShortRoundsWithoutRepeats() {
        for (count in listOf(1, 9)) {
            val session = LearningSession(questions(count), Random(5))
            session.startRound(LearningMode.QUESTIONS)
            assertEquals(LearningScreen.CARD, session.state.screen)
            assertEquals(count, session.state.availableCount)
            assertEquals(count, session.state.total)
            assertEquals(count, session.state.cards.map { it.id }.distinct().size)
            assertEquals(LearningNotice.INSUFFICIENT_CONTENT, session.state.notice)
            assertEquals(1, session.state.position)
            assertEquals(CardFace.FRONT, session.state.face)
            assertNotNull(session.state.cardToken)
            repeat(count) { evaluate(session, SelfAssessment.KNOWN) }
            assertEquals(LearningSummary(count, count, 0), session.state.summary)
        }
    }

    @Test
    fun flipReturnsToFrontWithoutChangingProgressOrEvaluations() {
        val session = started()
        val initial = session.state
        val token = initial.cardToken!!
        assertEquals(CardFace.FRONT, initial.face)
        assertEquals(1, initial.position)
        assertFalse(initial.canAssess)
        assertTrue(session.flip(token))
        assertEquals(CardFace.BACK, session.state.face)
        assertTrue(session.state.canAssess)
        assertEquals(initial.currentCard, session.state.currentCard)
        assertEquals(token, session.state.cardToken)
        assertTrue(session.state.assessments.isEmpty())
        assertTrue(session.flip(token))
        assertEquals(initial, session.state)
    }

    @Test
    fun assessmentFromFrontIsRejectedEvenAfterReturningFromBack() {
        val session = started()
        val token = session.state.cardToken!!
        val initial = session.state
        assertFalse(session.assess(token, SelfAssessment.KNOWN))
        assertEquals(initial, session.state)
        session.flip(token)
        session.flip(token)
        assertFalse(session.assess(token, SelfAssessment.REVIEW))
        assertEquals(initial, session.state)
    }

    @Test
    fun assessmentAdvancesOnceToANewFrontAndPreservesPreviousSnapshot() {
        val session = started()
        val initial = session.state
        val token = initial.cardToken!!
        session.flip(token)
        assertTrue(session.assess(token, SelfAssessment.REVIEW))
        assertEquals(2, session.state.position)
        assertEquals(CardFace.FRONT, session.state.face)
        assertFalse(session.state.canAssess)
        assertEquals(mapOf(initial.currentCard!!.id to SelfAssessment.REVIEW), session.state.assessments)
        assertNotEquals(token, session.state.cardToken)
        assertTrue(initial.assessments.isEmpty())
        assertEquals(1, initial.position)
        val after = session.state
        assertFalse(session.assess(token, SelfAssessment.KNOWN))
        assertFalse(session.flip(token))
        assertEquals(after, session.state)
    }

    @Test
    fun staleTokenCannotAssessTheNextCardEvenWhenItsBackIsVisible() {
        val session = started()
        val oldToken = session.state.cardToken!!
        evaluate(session, SelfAssessment.KNOWN)
        session.flip(session.state.cardToken!!)
        val before = session.state
        assertFalse(session.assess(oldToken, SelfAssessment.REVIEW))
        assertFalse(session.flip(oldToken))
        assertEquals(before, session.state)
    }

    @Test
    fun sevenKnownAndThreeReviewProduceOnlyTheCurrentPassSummary() {
        val session = started()
        val chosen = session.state.cards
        repeat(10) { index ->
            assertEquals(index + 1, session.state.position)
            assertEquals(CardFace.FRONT, session.state.face)
            assertNull(session.state.summary)
            evaluate(session, if (index < 7) SelfAssessment.KNOWN else SelfAssessment.REVIEW)
        }
        val state = session.state
        assertEquals(LearningScreen.SUMMARY, state.screen)
        assertEquals(LearningSummary(10, 7, 3), state.summary)
        assertEquals(chosen, state.cards)
        assertEquals(chosen.take(7).map { it.id }.toSet(),
            state.assessments.filterValues { it == SelfAssessment.KNOWN }.keys)
        assertEquals(chosen.takeLast(3).map { it.id }.toSet(),
            state.assessments.filterValues { it == SelfAssessment.REVIEW }.keys)
        assertNull(state.currentCard)
        assertNull(state.cardToken)
        assertFalse(state.canAssess)
    }

    @Test
    fun lastCardCannotBeEvaluatedOrFlippedAfterSummary() {
        val session = LearningSession(questions(1))
        session.startRound(LearningMode.QUESTIONS)
        val token = session.state.cardToken!!
        evaluate(session, SelfAssessment.REVIEW)
        val summary = session.state
        assertFalse(session.assess(token, SelfAssessment.KNOWN))
        assertFalse(session.flip(token))
        assertEquals(summary, session.state)
    }

    @Test
    fun newRoundDropsCountsAndRejectsOldTokenEvenForTheSameCard() {
        val session = LearningSession(questions(1), Random(8))
        session.startRound(LearningMode.QUESTIONS)
        val first = session.state
        evaluate(session, SelfAssessment.REVIEW)
        assertEquals(LearningSummary(1, 0, 1), session.state.summary)
        session.startRound(LearningMode.QUESTIONS)
        assertEquals(first.cards, session.state.cards)
        assertEquals(LearningMode.QUESTIONS, session.state.mode)
        assertEquals(LearningPass.ROUND, session.state.pass)
        assertEquals(1, session.state.position)
        assertEquals(CardFace.FRONT, session.state.face)
        assertTrue(session.state.assessments.isEmpty())
        assertNull(session.state.summary)
        assertNotEquals(first.cardToken, session.state.cardToken)
        session.flip(session.state.cardToken!!)
        val before = session.state
        assertFalse(session.assess(first.cardToken!!, SelfAssessment.KNOWN))
        assertFalse(session.flip(first.cardToken))
        assertEquals(before, session.state)
        assertTrue(session.assess(before.cardToken!!, SelfAssessment.KNOWN))
        assertEquals(LearningSummary(1, 1, 0), session.state.summary)
    }

    @Test
    fun reviewShrinksFromTenSevenThreeToThreeTwoOneToOneWithoutAccumulating() {
        val session = started()
        val round = session.state.cards
        assertFalse(session.startReview())
        repeat(10) { evaluate(session, if (it < 7) SelfAssessment.KNOWN else SelfAssessment.REVIEW) }
        val summary = session.state
        assertEquals(LearningSummary(10, 7, 3), summary.summary)
        assertTrue(summary.canReview)
        assertTrue(session.startReview())
        assertEquals(round.takeLast(3), session.state.cards)
        assertEquals(LearningMode.QUESTIONS, session.state.mode)
        assertEquals(LearningPass.REVIEW, session.state.pass)
        assertEquals(1, session.state.position)
        assertEquals(CardFace.FRONT, session.state.face)
        assertTrue(session.state.assessments.isEmpty())
        assertNull(session.state.notice)
        assertFalse(session.state.canReview)
        val reviewing = session.state
        assertFalse(session.startReview())
        assertEquals(reviewing, session.state)
        repeat(3) { evaluate(session, if (it < 2) SelfAssessment.KNOWN else SelfAssessment.REVIEW) }
        assertEquals(LearningSummary(3, 2, 1), session.state.summary)
        assertTrue(session.startReview())
        assertEquals(listOf(round.last()), session.state.cards)
        assertEquals(1, session.state.total)
        assertEquals(1, session.state.position)
        assertEquals(CardFace.FRONT, session.state.face)
        assertTrue(session.state.assessments.isEmpty())
        evaluate(session, SelfAssessment.KNOWN)
        assertEquals(LearningSummary(1, 1, 0), session.state.summary)
        assertFalse(session.state.canReview)
        val finished = session.state
        assertFalse(session.startReview())
        assertEquals(finished, session.state)
        assertEquals(LearningSummary(10, 7, 3), summary.summary)
    }

    @Test
    fun reviewKeepsTheExactPhotoEvenWhenTheSpeciesHasOtherPhotos() {
        val session = LearningSession((1..12).flatMap {
            listOf(photo("species-$it", "male-$it"), photo("species-$it", "female-$it"))
        }, Random(8))
        session.startRound(LearningMode.SPECIES)
        val selected = session.state.cards
        repeat(10) { evaluate(session, if (it < 7) SelfAssessment.KNOWN else SelfAssessment.REVIEW) }
        assertTrue(session.startReview())
        assertEquals(selected.takeLast(3), session.state.cards)
        repeat(3) { evaluate(session, SelfAssessment.REVIEW) }
        assertTrue(session.startReview())
        assertEquals(selected.takeLast(3), session.state.cards)
    }

    @Test
    fun clearDropsRoundReviewAndSummaryAndNeverReusesPresentationTokens() {
        for (stage in 0..2) {
            val session = LearningSession(questions(1))
            session.startRound(LearningMode.QUESTIONS)
            if (stage > 0) evaluate(session, SelfAssessment.REVIEW)
            if (stage == 2) assertTrue(session.startReview())
            if (stage != 1) assertTrue(session.flip(session.state.cardToken!!))
            val oldToken = session.state.cardToken
            session.clear()
            assertEquals(LearningState(), session.state)
            assertFalse(session.startReview())
            session.clear()
            session.startRound(LearningMode.QUESTIONS)
            assertEquals(1, session.state.position)
            assertEquals(CardFace.FRONT, session.state.face)
            assertTrue(session.state.assessments.isEmpty())
            if (oldToken != null) assertStaleCallbacksIgnored(session, oldToken)
            evaluate(session, SelfAssessment.KNOWN)
            assertEquals(LearningSummary(1, 1, 0), session.state.summary)
        }
    }

    @Test
    fun changingModeAndStartingANewRoundDropReviewMarks() {
        val session = LearningSession(questions(1) + photo("species", "photo"))
        assertFalse(session.startReview())
        session.startRound(LearningMode.QUESTIONS)
        evaluate(session, SelfAssessment.REVIEW)
        assertTrue(session.startReview())
        val oldToken = session.state.cardToken!!
        session.startRound(LearningMode.SPECIES)
        assertEquals(LearningMode.SPECIES, session.state.mode)
        assertEquals(LearningPass.ROUND, session.state.pass)
        assertEquals(CardFace.FRONT, session.state.face)
        assertTrue(session.state.assessments.isEmpty())
        assertNull(session.state.summary)
        assertStaleCallbacksIgnored(session, oldToken)
        evaluate(session, SelfAssessment.REVIEW)
        assertTrue(session.startReview())
        session.startRound(LearningMode.SPECIES)
        assertEquals(LearningPass.ROUND, session.state.pass)
        assertTrue(session.state.assessments.isEmpty())
        evaluate(session, SelfAssessment.KNOWN)
        assertEquals(LearningSummary(1, 1, 0), session.state.summary)
        assertEquals(LearningState(), LearningSession(questions(1)).state)
    }

    @Test
    fun previousPassCallbacksCannotChangeAReviewOfTheSameCard() {
        val session = LearningSession(questions(1))
        session.startRound(LearningMode.QUESTIONS)
        val roundToken = session.state.cardToken!!
        evaluate(session, SelfAssessment.REVIEW)
        assertStaleCallbacksIgnored(session, roundToken)
        assertTrue(session.startReview())
        val reviewToken = session.state.cardToken!!
        assertNotEquals(roundToken, reviewToken)
        assertTrue(session.flip(reviewToken))
        assertStaleCallbacksIgnored(session, roundToken)
        assertTrue(session.assess(reviewToken, SelfAssessment.REVIEW))
        assertTrue(session.startReview())
        assertStaleCallbacksIgnored(session, reviewToken)
        assertStaleCallbacksIgnored(session, roundToken)
        evaluate(session, SelfAssessment.KNOWN)
        assertEquals(LearningSummary(1, 1, 0), session.state.summary)
    }

    @Test
    fun questionReplacementsNeverRepeatSelectedOrPreviouslyFailedQuestions() {
        val catalog = questions(12)
        val session = LearningSession(catalog + photo("other-mode", "photo"), Random(3))
        session.startRound(LearningMode.QUESTIONS)
        val reserved = session.state.cards.toMutableSet()
        repeat(2) {
            val old = session.state
            assertTrue(session.discardCard(old.cardToken!!))
            assertEquals(10, session.state.total)
            assertEquals(1, session.state.position)
            assertTrue(session.state.currentCard in catalog)
            assertTrue(reserved.add(session.state.currentCard!!))
            assertTrue(session.state.assessments.isEmpty())
            assertEquals(LearningNotice.CARD_UNAVAILABLE, session.state.notice)
            assertStaleCallbacksIgnored(session, old.cardToken)
        }
        assertTrue(session.discardCard(session.state.cardToken!!))
        assertEquals(9, session.state.total)
        repeat(9) { evaluate(session, SelfAssessment.KNOWN) }
        assertEquals(LearningSummary(9, 9, 0), session.state.summary)
    }

    @Test
    fun photoReplacementsExcludeSeenFailedAndReservedSpeciesAcrossSeeds() {
        val catalog = (1..14).flatMap {
            listOf(photo("species-$it", "male-$it"), photo("species-$it", "female-$it"))
        }
        for (seed in 0..20) {
            val session = LearningSession(catalog, Random(seed))
            session.startRound(LearningMode.SPECIES)
            val reserved = session.state.cards.filterIsInstance<LearningCard.Photo>()
                .map { it.speciesKey }.toMutableSet()
            evaluate(session, SelfAssessment.KNOWN)
            evaluate(session, SelfAssessment.REVIEW)
            repeat(4) {
                val previous = session.state
                assertTrue(session.flip(previous.cardToken!!))
                assertTrue(session.discardCard(previous.cardToken))
                val state = session.state
                assertEquals(10, state.total)
                assertEquals(3, state.position)
                assertEquals(CardFace.FRONT, state.face)
                assertEquals(previous.assessments, state.assessments)
                assertTrue(reserved.add((state.currentCard as LearningCard.Photo).speciesKey))
                assertEquals(10, state.cards.filterIsInstance<LearningCard.Photo>()
                    .map { it.speciesKey }.distinct().size)
                assertStaleCallbacksIgnored(session, previous.cardToken)
            }
            assertTrue(session.discardCard(session.state.cardToken!!))
            assertEquals(9, session.state.total)
            assertEquals(3, session.state.position)
            repeat(7) { evaluate(session, SelfAssessment.KNOWN) }
            assertEquals(LearningSummary(9, 8, 1), session.state.summary)
        }
    }

    @Test
    fun failureWithoutReplacementAdjustsProgressAndSummaryWithoutAnAssessment() {
        val session = LearningSession(questions(9), Random(7))
        session.startRound(LearningMode.QUESTIONS)
        evaluate(session, SelfAssessment.REVIEW)
        val before = session.state
        val omitted = before.currentCard!!
        assertTrue(session.discardCard(before.cardToken!!))
        assertEquals(8, session.state.total)
        assertEquals(2, session.state.position)
        assertEquals(before.cards[2], session.state.currentCard)
        assertEquals(before.assessments, session.state.assessments)
        assertFalse(session.state.cards.contains(omitted))
        assertEquals(9, before.total)
        repeat(7) { evaluate(session, SelfAssessment.KNOWN) }
        assertEquals(LearningSummary(8, 7, 1), session.state.summary)
        assertTrue(session.startReview())
        assertEquals(listOf(before.cards.first()), session.state.cards)
    }

    @Test
    fun reviewFailureOmitsThePhotoWithoutUsingAnotherPhotoOrUnmarkedCard() {
        val catalog = (1..15).flatMap {
            listOf(photo("species-$it", "male-$it"), photo("species-$it", "female-$it"))
        }
        val session = LearningSession(catalog, Random(9))
        session.startRound(LearningMode.SPECIES)
        val marked = session.state.cards.takeLast(3)
        repeat(10) { evaluate(session, if (it < 7) SelfAssessment.KNOWN else SelfAssessment.REVIEW) }
        assertTrue(session.startReview())
        val failedToken = session.state.cardToken!!
        assertTrue(session.discardCard(failedToken))
        assertEquals(marked.drop(1), session.state.cards)
        assertEquals(2, session.state.total)
        assertEquals(1, session.state.position)
        assertEquals(CardFace.FRONT, session.state.face)
        assertTrue(session.state.assessments.isEmpty())
        assertEquals(LearningNotice.CARD_UNAVAILABLE, session.state.notice)
        assertStaleCallbacksIgnored(session, failedToken)
        evaluate(session, SelfAssessment.REVIEW)
        assertTrue(session.discardCard(session.state.cardToken!!))
        assertEquals(LearningSummary(1, 0, 1), session.state.summary)
        assertTrue(session.startReview())
        assertEquals(listOf(marked[1]), session.state.cards)
        assertEquals(1, session.state.total)
    }

    @Test
    fun allPhotosFailWithoutEvaluationsAndNoEmptySummaryIsCreated() {
        val session = LearningSession((1..12).map { photo("species-$it", "photo-$it") }, Random(4))
        session.startRound(LearningMode.SPECIES)
        val failedIds = mutableSetOf<String>()
        repeat(12) {
            assertEquals(LearningScreen.CARD, session.state.screen)
            assertTrue(failedIds.add(session.state.currentCard!!.id))
            val token = session.state.cardToken!!
            assertTrue(session.discardCard(token))
            assertTrue(session.state.assessments.isEmpty())
            assertNull(session.state.summary)
            assertStaleCallbacksIgnored(session, token)
        }
        assertEquals(LearningScreen.SELECTOR, session.state.screen)
        assertEquals(LearningNotice.UNAVAILABLE_CONTENT, session.state.notice)
        assertEquals(0, session.state.availableCount)
        assertEquals(0, session.state.total)
        assertNull(session.state.pass)
        assertNull(session.state.cardToken)
        assertFalse(session.startReview())
        session.startRound(LearningMode.SPECIES)
        assertEquals(10, session.state.total)
        assertEquals(12, session.state.availableCount)
        assertTrue(session.discardCard(session.state.cardToken!!))
        assertEquals(10, session.state.total)
    }

    @Test
    fun allRemainingPhotosFailAndOnlyActualEvaluationsReachTheSummary() {
        val session = LearningSession((1..12).map { photo("species-$it", "photo-$it") }, Random(4))
        session.startRound(LearningMode.SPECIES)
        val reviewed = session.state.cards[1]
        evaluate(session, SelfAssessment.KNOWN)
        evaluate(session, SelfAssessment.REVIEW)
        repeat(10) { assertTrue(session.discardCard(session.state.cardToken!!)) }
        assertEquals(LearningScreen.SUMMARY, session.state.screen)
        assertEquals(2, session.state.total)
        assertEquals(LearningSummary(2, 1, 1), session.state.summary)
        assertTrue(session.startReview())
        assertEquals(listOf(reviewed), session.state.cards)
        assertTrue(session.discardCard(session.state.cardToken!!))
        assertEquals(LearningScreen.SELECTOR, session.state.screen)
        assertEquals(LearningNotice.UNAVAILABLE_CONTENT, session.state.notice)
        assertNull(session.state.summary)
        assertTrue(session.state.assessments.isEmpty())
    }

    @Test
    fun allReviewPhotosFailWithoutImportingEvaluationsFromTheRound() {
        val session = LearningSession((1..12).map { photo("species-$it", "photo-$it") }, Random(2))
        session.startRound(LearningMode.SPECIES)
        repeat(10) { evaluate(session, if (it < 7) SelfAssessment.KNOWN else SelfAssessment.REVIEW) }
        assertTrue(session.startReview())
        repeat(3) { assertTrue(session.discardCard(session.state.cardToken!!)) }
        assertEquals(LearningScreen.SELECTOR, session.state.screen)
        assertEquals(LearningNotice.UNAVAILABLE_CONTENT, session.state.notice)
        assertNull(session.state.summary)
        assertEquals(0, session.state.total)
        assertTrue(session.state.assessments.isEmpty())
    }

    @Test
    fun failedImagePathsAreNotChosenAgainUnderAnotherCardId() {
        val session = LearningSession((1..12).map {
            photo("species-$it", "photo-$it").copy(imagePath = "learning/photos/broken.webp")
        }, Random(6))
        session.startRound(LearningMode.SPECIES)
        assertTrue(session.discardCard(session.state.cardToken!!))
        assertEquals(9, session.state.total)
        assertEquals(0, session.state.availableCount)
        assertTrue(session.state.assessments.isEmpty())
    }

    @Test
    fun newRoundAndClearForgetFailedContentAndKeepRejectingOldCallbacks() {
        val card = photo("species", "photo")
        val session = LearningSession(listOf(card) + questions(1))
        session.startRound(LearningMode.SPECIES)
        val firstToken = session.state.cardToken!!
        assertTrue(session.discardCard(firstToken))
        session.startRound(LearningMode.SPECIES)
        assertEquals(listOf(card), session.state.cards)
        assertEquals(1, session.state.availableCount)
        assertStaleCallbacksIgnored(session, firstToken)
        assertTrue(session.discardCard(session.state.cardToken!!))
        session.clear()
        assertEquals(LearningState(), session.state)
        session.startRound(LearningMode.SPECIES)
        assertEquals(listOf(card), session.state.cards)
        val oldToken = session.state.cardToken!!
        session.startRound(LearningMode.QUESTIONS)
        assertStaleCallbacksIgnored(session, oldToken)
        evaluate(session, SelfAssessment.KNOWN)
        assertEquals(LearningSummary(1, 1, 0), session.state.summary)
    }

    private fun assertStaleCallbacksIgnored(session: LearningSession, token: Long) {
        val before = session.state
        assertFalse(session.flip(token))
        assertFalse(session.assess(token, SelfAssessment.REVIEW))
        assertFalse(session.discardCard(token))
        assertEquals(before, session.state)
    }

    private fun started(): LearningSession = LearningSession(questions(40), Random(42)).also {
        it.startRound(LearningMode.QUESTIONS)
    }

    private fun evaluate(session: LearningSession, assessment: SelfAssessment) {
        val token = session.state.cardToken!!
        assertTrue(session.flip(token))
        assertTrue(session.assess(token, assessment))
    }

    private fun questions(count: Int) = (1..count).map {
        LearningCard.Question(
            id = "AVE-$it", topic = "Anatomía", level = "Inicial",
            question = "Pregunta $it", answer = "Respuesta $it",
            sources = listOf("Fuente de prueba")
        )
    }

    private fun photo(species: String, id: String) = LearningCard.Photo(
        id = id, speciesKey = species, commonName = "Nombre común",
        scientificName = "Nombre científico", imagePath = "learning/photos/$id.webp"
    )
}
