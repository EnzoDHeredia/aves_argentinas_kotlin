package com.example.avesargentinas.ui.learning

enum class LearningMode { QUESTIONS, SPECIES }

sealed class LearningCard {
    abstract val id: String

    data class Question(
        override val id: String,
        val topic: String,
        val level: String,
        val question: String,
        val answer: String,
        val clarification: String? = null,
        val sources: List<String> = emptyList()
    ) : LearningCard()

    data class Photo(
        override val id: String,
        val speciesKey: String,
        val commonName: String,
        val scientificName: String,
        val imagePath: String
    ) : LearningCard()
}

enum class SelfAssessment { KNOWN, REVIEW }
enum class LearningScreen { SELECTOR, CARD, SUMMARY }
enum class LearningPass { ROUND, REVIEW }
enum class CardFace { FRONT, BACK }
enum class LearningNotice { INSUFFICIENT_CONTENT, UNAVAILABLE_CONTENT, CARD_UNAVAILABLE }

data class LearningSummary(val total: Int, val known: Int, val review: Int)

data class LearningState(
    val screen: LearningScreen = LearningScreen.SELECTOR,
    val mode: LearningMode? = null,
    val pass: LearningPass? = null,
    val cards: List<LearningCard> = emptyList(),
    val cardIndex: Int = 0,
    val face: CardFace = CardFace.FRONT,
    val assessments: Map<String, SelfAssessment> = emptyMap(),
    val notice: LearningNotice? = null,
    val availableCount: Int = 0,
    val cardToken: Long? = null
) {
    val total: Int get() = cards.size
    val currentCard: LearningCard?
        get() = if (screen == LearningScreen.CARD) cards.getOrNull(cardIndex) else null
    val position: Int get() = if (currentCard != null) cardIndex + 1 else 0
    val canAssess: Boolean
        get() = currentCard?.let { face == CardFace.BACK && it.id !in assessments } == true
    val canReview: Boolean get() = summary?.review?.let { it > 0 } == true
    val summary: LearningSummary?
        get() = if (screen == LearningScreen.SUMMARY) LearningSummary(
            total = assessments.size,
            known = assessments.values.count { it == SelfAssessment.KNOWN },
            review = assessments.values.count { it == SelfAssessment.REVIEW }
        ) else null
}
