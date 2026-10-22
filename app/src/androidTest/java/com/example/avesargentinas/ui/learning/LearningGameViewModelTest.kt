package com.example.avesargentinas.ui.learning

import android.app.Application
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.avesargentinas.BirdLabelsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class LearningGameViewModelTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val application = instrumentation.targetContext.applicationContext as Application
    private val stores = mutableListOf<ViewModelStore>()

    @After
    fun clearStores() = onMain { stores.forEach { it.clear() } }

    @Test
    fun sameStoreRetainsRoundReviewAndSummaryWithoutReloading() {
        val loads = AtomicInteger()
        val store = newStore()
        val vm = obtain(store) { loads.incrementAndGet(); questions(10) }
        awaitReady(vm)
        onMain { vm.startRound(LearningMode.QUESTIONS) }
        val selected = vm.state.value.session.cards
        repeat(10) { index ->
            onMain { vm.flip(vm.state.value.session.cardToken!!) }
            assertRetained(store, vm)
            onMain {
                vm.assess(vm.state.value.session.cardToken!!,
                    if (index < 7) SelfAssessment.KNOWN else SelfAssessment.REVIEW)
            }
            assertRetained(store, vm)
        }
        assertEquals(LearningSummary(10, 7, 3), vm.state.value.session.summary)
        onMain { vm.startReview() }
        assertEquals(selected.takeLast(3), vm.state.value.session.cards)
        assertEquals(LearningPass.REVIEW, vm.state.value.session.pass)
        assertEquals(CardFace.FRONT, vm.state.value.session.face)
        assertTrue(vm.state.value.session.assessments.isEmpty())
        repeat(3) { index ->
            onMain { vm.flip(vm.state.value.session.cardToken!!) }
            assertRetained(store, vm)
            onMain {
                vm.assess(vm.state.value.session.cardToken!!,
                    if (index < 2) SelfAssessment.KNOWN else SelfAssessment.REVIEW)
            }
        }
        assertEquals(LearningSummary(3, 2, 1), vm.state.value.session.summary)
        assertRetained(store, vm)
        onMain { vm.startRound(LearningMode.QUESTIONS) }
        assertEquals(LearningPass.ROUND, vm.state.value.session.pass)
        assertEquals(10, vm.state.value.session.total)
        assertEquals(CardFace.FRONT, vm.state.value.session.face)
        assertTrue(vm.state.value.session.assessments.isEmpty())
        assertEquals(1, loads.get())
    }

    @Test
    fun newStoreStartsAtSelectorAfterPreviousStoreIsCleared() {
        val oldStore = newStore()
        val old = obtain(oldStore) { questions(1) }
        awaitReady(old)
        onMain {
            old.startRound(LearningMode.QUESTIONS)
            old.flip(old.state.value.session.cardToken!!)
            old.assess(old.state.value.session.cardToken!!, SelfAssessment.REVIEW)
        }
        assertEquals(LearningSummary(1, 0, 1), old.state.value.session.summary)
        onMain { oldStore.clear() }
        val fresh = obtain(newStore()) { questions(1) }
        awaitReady(fresh)
        assertNotSame(old, fresh)
        assertEquals(LearningState(), fresh.state.value.session)
    }

    @Test
    fun explicitClearRemovesMarksAndRejectsCallbacksFromTheAbandonedRound() {
        val vm = obtain(newStore()) { questions(2) }
        awaitReady(vm)
        onMain {
            vm.startRound(LearningMode.QUESTIONS)
            vm.flip(vm.state.value.session.cardToken!!)
            vm.assess(vm.state.value.session.cardToken!!, SelfAssessment.REVIEW)
        }
        val abandonedToken = vm.state.value.session.cardToken!!
        val availability = vm.state.value.availableCounts
        onMain { vm.clearSession() }
        assertEquals(LearningState(), vm.state.value.session)
        assertEquals(availability, vm.state.value.availableCounts)
        onMain { vm.startRound(LearningMode.QUESTIONS) }
        val fresh = vm.state.value
        onMain {
            vm.flip(abandonedToken)
            vm.assess(abandonedToken, SelfAssessment.KNOWN)
            vm.discardCard(abandonedToken)
        }
        assertEquals(fresh, vm.state.value)
        assertTrue(vm.state.value.session.assessments.isEmpty())
    }

    @Test
    fun loadRunsOffMainAndActionsWaitForObservableReadiness() {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val loadThread = AtomicReference<Looper?>()
        val vm = obtain(newStore()) {
            loadThread.set(Looper.myLooper())
            entered.countDown()
            check(release.await(10, TimeUnit.SECONDS))
            questions(1)
        }
        try {
            assertTrue(entered.await(10, TimeUnit.SECONDS))
            assertNotEquals(Looper.getMainLooper(), loadThread.get())
            assertTrue(vm.state.value.isLoading)
            onMain {
                vm.startRound(LearningMode.QUESTIONS)
                vm.startReview()
                vm.flip(1)
                vm.assess(1, SelfAssessment.REVIEW)
                vm.discardCard(1)
                vm.clearSession()
            }
            assertEquals(LearningState(), vm.state.value.session)
        } finally {
            release.countDown()
        }
        awaitReady(vm)
        assertEquals(LearningState(), vm.state.value.session)
        onMain { vm.startRound(LearningMode.QUESTIONS) }
        assertEquals(1, vm.state.value.session.total)
    }

    @Test
    fun applicationFactoryLoadsPackagedContentWithoutStartingAGame() {
        val store = newStore()
        val vm = onMain {
            ViewModelProvider(store, ViewModelProvider.AndroidViewModelFactory(application))
                .get(LearningGameViewModel::class.java)
        }
        val ready = awaitReady(vm)
        assertFalse(ready.loadFailed)
        assertEquals(mapOf(LearningMode.QUESTIONS to 40, LearningMode.SPECIES to 45), ready.availableCounts)
        assertEquals(LearningState(), ready.session)
        onMain { vm.startRound(LearningMode.SPECIES) }
        assertEquals(10, vm.state.value.session.total)
        onMain { vm.flip(vm.state.value.session.cardToken!!) }
        assertRetained(store, vm)
    }

    @Test
    fun clearingStoreDuringLoadPreventsLatePublicationOrAFakeLoadError() {
        assertClearedLoadIsNotPublished { questions(1) }
    }

    @Test
    fun clearingStoreDuringFailingLoadDoesNotPublishAnError() {
        assertClearedLoadIsNotPublished { throw IOException("Lectura fallida después de limpiar") }
    }

    private fun assertClearedLoadIsNotPublished(result: () -> List<LearningCard>) {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val store = newStore()
        val vm = obtain(store) {
            entered.countDown()
            check(release.await(10, TimeUnit.SECONDS))
            result()
        }
        try {
            assertTrue(entered.await(10, TimeUnit.SECONDS))
            val loading = onMain { vm.viewModelScope.coroutineContext[Job]!!.children.single() }
            onMain { store.clear() }
            release.countDown()
            runBlocking { withTimeout(10_000) { loading.join() } }
            assertTrue(vm.state.value.isLoading)
            assertFalse(vm.state.value.loadFailed)
            assertEquals(LearningState(), vm.state.value.session)
            assertTrue(vm.state.value.availableCounts.isEmpty())
        } finally {
            release.countDown()
        }
    }

    @Test
    fun insufficientContentReportsDistinctSpeciesAndStartsOnlyAvailableCards() {
        val vm = obtain(newStore()) { fixtureLoader().load("learning/valid_questions.json", "learning/valid_photos.json") }
        val ready = awaitReady(vm)
        assertEquals(mapOf(LearningMode.QUESTIONS to 1, LearningMode.SPECIES to 2), ready.availableCounts)
        onMain { vm.startRound(LearningMode.SPECIES) }
        assertEquals(2, vm.state.value.session.total)
        assertEquals(LearningNotice.INSUFFICIENT_CONTENT, vm.state.value.session.notice)
        onMain { vm.startRound(LearningMode.QUESTIONS) }
        assertEquals(1, vm.state.value.session.total)
        assertEquals(LearningNotice.INSUFFICIENT_CONTENT, vm.state.value.session.notice)
    }

    @Test
    fun brokenPhotoDocumentLeavesQuestionsPlayable() {
        val vm = obtain(newStore()) { fixtureLoader().load("learning/valid_questions.json", "learning/malformed.json") }
        awaitReady(vm)
        assertEquals(0, vm.state.value.availableCounts[LearningMode.SPECIES])
        onMain { vm.startRound(LearningMode.SPECIES) }
        assertEquals(LearningScreen.SELECTOR, vm.state.value.session.screen)
        assertEquals(LearningNotice.UNAVAILABLE_CONTENT, vm.state.value.session.notice)
        onMain { vm.startRound(LearningMode.QUESTIONS) }
        assertEquals(LearningScreen.CARD, vm.state.value.session.screen)
    }

    @Test
    fun unexpectedLoadFailurePublishesUnavailableContentAndAllowsClear() {
        val vm = obtain(newStore()) { throw IOException("No se puede leer el catálogo") }
        val ready = awaitReady(vm)
        assertTrue(ready.loadFailed)
        assertEquals(mapOf(LearningMode.QUESTIONS to 0, LearningMode.SPECIES to 0), ready.availableCounts)
        onMain { vm.startRound(LearningMode.QUESTIONS) }
        assertEquals(LearningScreen.SELECTOR, vm.state.value.session.screen)
        assertEquals(LearningNotice.UNAVAILABLE_CONTENT, vm.state.value.session.notice)
        onMain { vm.clearSession() }
        assertEquals(LearningState(), vm.state.value.session)
    }

    @Test
    fun lateAndDuplicateActionsDoNotEvaluateOrDiscardTheNextCard() {
        val vm = obtain(newStore()) { questions(2) }
        awaitReady(vm)
        onMain { vm.startRound(LearningMode.QUESTIONS) }
        val token = vm.state.value.session.cardToken!!
        onMain {
            vm.assess(token, SelfAssessment.KNOWN)
        }
        assertTrue(vm.state.value.session.assessments.isEmpty())
        onMain { vm.flip(token) }
        val revealed = vm.state.value.session
        onMain { vm.assess(token, SelfAssessment.REVIEW) }
        val next = vm.state.value
        assertEquals(CardFace.BACK, revealed.face)
        assertTrue(revealed.assessments.isEmpty())
        assertEquals(1, next.session.cardIndex)
        assertEquals(CardFace.FRONT, next.session.face)
        assertEquals(1, next.session.assessments.size)
        onMain {
            vm.assess(token, SelfAssessment.KNOWN)
            vm.flip(token)
            vm.discardCard(token)
        }
        assertEquals(next, vm.state.value)
    }

    @Test
    fun failedCardPublishesUnavailableWithoutInventingAnAssessment() {
        val vm = obtain(newStore()) { questions(1) }
        awaitReady(vm)
        onMain {
            vm.startRound(LearningMode.QUESTIONS)
            vm.discardCard(vm.state.value.session.cardToken!!)
        }
        val state = vm.state.value.session
        assertEquals(LearningScreen.SELECTOR, state.screen)
        assertEquals(LearningNotice.UNAVAILABLE_CONTENT, state.notice)
        assertTrue(state.assessments.isEmpty())
        assertNull(state.summary)
    }

    private fun assertRetained(store: ViewModelStore, vm: LearningGameViewModel) {
        val snapshot = vm.state.value
        val obtained = obtain(store) { error("El mismo store no debe recargar el contenido") }
        assertSame(vm, obtained)
        assertEquals(snapshot, obtained.state.value)
    }

    private fun newStore() = ViewModelStore().also { stores.add(it) }

    private fun obtain(store: ViewModelStore, load: () -> List<LearningCard>): LearningGameViewModel = onMain {
        ViewModelProvider(store, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                LearningGameViewModel(application, load) as T
        })[LearningGameViewModel::class.java]
    }

    private fun awaitReady(vm: LearningGameViewModel): LearningGameState = runBlocking {
        withTimeout(10_000) { vm.state.first { !it.isLoading } }
    }

    private fun fixtureLoader() = LearningContentLoader(
        instrumentation.context.assets, BirdLabelsManager.loadLabels(application)
    )

    private fun questions(count: Int) = (1..count).map {
        LearningCard.Question("q-$it", "Tema", "Inicial", "¿Pregunta $it?", "Respuesta $it")
    }

    private fun <T> onMain(action: () -> T): T {
        val result = AtomicReference<Result<T>>()
        instrumentation.runOnMainSync { result.set(runCatching(action)) }
        return result.get().getOrThrow()
    }
}
