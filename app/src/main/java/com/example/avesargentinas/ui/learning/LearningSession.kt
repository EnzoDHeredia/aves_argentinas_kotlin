package com.example.avesargentinas.ui.learning

import kotlin.random.Random

class LearningSession(catalog: List<LearningCard>, private val random: Random = Random.Default) {
    private val catalog = catalog.toList()
    private val usedKeys = mutableSetOf<String>()
    private val failedContent = mutableSetOf<String>()
    // No reiniciar al limpiar: los callbacks de sesiones anteriores deben seguir siendo inválidos.
    private var lastToken = 0L

    var state = LearningState()
        private set

    fun availableCount(mode: LearningMode): Int = cardGroups(mode).size

    fun startReview(): Boolean {
        if (!state.canReview) return false
        val marked = state.cards.filter { state.assessments[it.id] == SelfAssessment.REVIEW }
        state = state.copy(
            screen = LearningScreen.CARD,
            pass = LearningPass.REVIEW,
            cards = marked,
            cardIndex = 0,
            face = CardFace.FRONT,
            assessments = emptyMap(),
            notice = null,
            cardToken = ++lastToken
        )
        return true
    }

    fun clear() {
        usedKeys.clear()
        failedContent.clear()
        state = LearningState()
    }

    fun discardCard(expectedToken: Long): Boolean {
        if (state.screen != LearningScreen.CARD || state.cardToken != expectedToken) return false
        failedContent.add(state.currentCard!!.contentKey())
        val groups = cardGroups(state.mode!!)
        val replacement = if (state.pass == LearningPass.ROUND) {
            groups.filter { it.first().selectionKey() !in usedKeys }
                .randomOrNull(random)?.random(random)
        } else null
        val cards = state.cards.toMutableList()
        if (replacement != null) {
            cards[state.cardIndex] = replacement
            usedKeys.add(replacement.selectionKey())
        } else {
            cards.removeAt(state.cardIndex)
        }
        val finished = state.cardIndex == cards.size
        state = if (finished && state.assessments.isEmpty()) {
            LearningState(
                mode = state.mode,
                notice = LearningNotice.UNAVAILABLE_CONTENT,
                availableCount = groups.size
            )
        } else state.copy(
            screen = if (finished) LearningScreen.SUMMARY else LearningScreen.CARD,
            cards = cards.toList(),
            face = CardFace.FRONT,
            notice = LearningNotice.CARD_UNAVAILABLE,
            availableCount = groups.size,
            cardToken = if (finished) null else ++lastToken
        )
        return true
    }

    fun startRound(mode: LearningMode) {
        clear()
        val groups = cardGroups(mode)
        val selected = groups.shuffled(random).take(10).map { it.random(random) }
        usedKeys.addAll(selected.map { it.selectionKey() })
        state = LearningState(
            screen = if (selected.isEmpty()) LearningScreen.SELECTOR else LearningScreen.CARD,
            mode = mode,
            pass = if (selected.isEmpty()) null else LearningPass.ROUND,
            cards = selected,
            notice = when {
                groups.isEmpty() -> LearningNotice.UNAVAILABLE_CONTENT
                groups.size < 10 -> LearningNotice.INSUFFICIENT_CONTENT
                else -> null
            },
            availableCount = groups.size,
            cardToken = if (selected.isEmpty()) null else ++lastToken
        )
    }

    fun flip(expectedToken: Long): Boolean {
        if (state.screen != LearningScreen.CARD || state.cardToken != expectedToken) return false
        state = state.copy(face = if (state.face == CardFace.FRONT) CardFace.BACK else CardFace.FRONT)
        return true
    }

    fun assess(expectedToken: Long, assessment: SelfAssessment): Boolean {
        if (state.cardToken != expectedToken || !state.canAssess) return false
        val assessments = state.assessments + (state.currentCard!!.id to assessment)
        val nextIndex = state.cardIndex + 1
        val finished = nextIndex == state.total
        state = state.copy(
            screen = if (finished) LearningScreen.SUMMARY else LearningScreen.CARD,
            cardIndex = nextIndex,
            face = CardFace.FRONT,
            assessments = assessments,
            cardToken = if (finished) null else ++lastToken
        )
        return true
    }

    private fun cardGroups(mode: LearningMode): List<List<LearningCard>> {
        val available = catalog.filter { it.contentKey() !in failedContent }
        return when (mode) {
            LearningMode.QUESTIONS -> available.filterIsInstance<LearningCard.Question>()
                .distinctBy { it.id }.map { listOf(it) }
            LearningMode.SPECIES -> available.filterIsInstance<LearningCard.Photo>()
                .groupBy { it.speciesKey }.values.toList()
        }
    }

    private fun LearningCard.selectionKey(): String = when (this) {
        is LearningCard.Question -> id
        is LearningCard.Photo -> speciesKey
    }

    private fun LearningCard.contentKey(): String = when (this) {
        is LearningCard.Question -> id
        is LearningCard.Photo -> imagePath
    }
}
