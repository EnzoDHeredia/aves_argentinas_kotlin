package com.example.avesargentinas.ui.learning

import android.app.Application
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LearningGameState(
    val isLoading: Boolean = true,
    val availableCounts: Map<LearningMode, Int> = emptyMap(),
    val session: LearningState = LearningState(),
    val loadFailed: Boolean = false
)

@MainThread
class LearningGameViewModel internal constructor(
    application: Application,
    loadContent: () -> List<LearningCard>
) : AndroidViewModel(application) {
    constructor(application: Application) : this(application, { LearningContentLoader(application).load() })

    private val mutableState = MutableStateFlow(LearningGameState())
    val state: StateFlow<LearningGameState> = mutableState.asStateFlow()
    private var session: LearningSession? = null

    init {
        viewModelScope.launch {
            var loadFailed = false
            val catalog = try {
                withContext(Dispatchers.IO) { loadContent() }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.d("LearningGameViewModel", "Contenido de aprendizaje no disponible", error)
                loadFailed = true
                emptyList()
            }
            coroutineContext.ensureActive()
            val loadedSession = LearningSession(catalog)
            session = loadedSession
            mutableState.value = LearningGameState(
                isLoading = false,
                availableCounts = LearningMode.values().associateWith(loadedSession::availableCount),
                session = loadedSession.state,
                loadFailed = loadFailed
            )
        }
    }

    fun startRound(mode: LearningMode) = updateSession { it.startRound(mode) }
    fun flip(expectedToken: Long) = updateSession { it.flip(expectedToken) }
    fun assess(expectedToken: Long, assessment: SelfAssessment) =
        updateSession { it.assess(expectedToken, assessment) }
    fun startReview() = updateSession { it.startReview() }
    fun discardCard(expectedToken: Long) = updateSession { it.discardCard(expectedToken) }
    fun clearSession() = updateSession { it.clear() }

    private fun updateSession(action: (LearningSession) -> Unit) {
        val current = session ?: return
        action(current)
        mutableState.value = mutableState.value.copy(session = current.state)
    }

    override fun onCleared() {
        clearSession()
        session = null
        super.onCleared()
    }
}
