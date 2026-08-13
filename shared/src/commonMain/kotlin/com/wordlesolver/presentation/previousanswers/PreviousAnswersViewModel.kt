package com.wordlesolver.presentation.previousanswers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordlesolver.data.PastAnswersDateFormat
import com.wordlesolver.domain.usecase.SyncPastAnswersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Everything the "Previous Answers" screen needs: the word list plus whether the
 * file is up to date through yesterday (drives the icon above the list).
 */
data class PreviousAnswersUiState(
    val answers: List<String> = emptyList(),
    val isUpToDateThroughYesterday: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PreviousAnswersViewModel(
    private val syncPastAnswersUseCase: SyncPastAnswersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreviousAnswersUiState())
    val uiState: StateFlow<PreviousAnswersUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /** Triggers a sync (network call only if needed) and updates the UI state. */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val state = syncPastAnswersUseCase()
                val yesterday = PastAnswersDateFormat.yesterday()
                val lastUpdate = PastAnswersDateFormat.parseFileDate(state.lastUpdateDate)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        answers = state.answers,
                        isUpToDateThroughYesterday = lastUpdate >= yesterday
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Unknown error") }
            }
        }
    }
}
