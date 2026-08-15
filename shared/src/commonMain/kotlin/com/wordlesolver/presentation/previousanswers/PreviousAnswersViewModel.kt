package com.wordlesolver.presentation.previousanswers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordlesolver.data.PastAnswersDateFormat
import com.wordlesolver.domain.usecase.SyncPastAnswersUseCase
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** How long to wait after the last keystroke before re-filtering the list. */
private val SEARCH_DEBOUNCE = 300.milliseconds

/**
 * Everything the "Previous Answers" screen needs: the word list plus whether the
 * file is up to date through yesterday (drives the icon above the list), the current
 * search query, and which words are duplicates (answers that came out more than once).
 */
data class PreviousAnswersUiState(
    /** All distinct answers, uppercase and sorted alphabetically. */
    val answers: List<String> = emptyList(),
    /** [answers] filtered by [searchQuery] (after debounce), what the UI should render. */
    val displayedAnswers: List<String> = emptyList(),
    /** Answers that appeared more than once, so the UI can give them a distinct background. */
    val duplicateAnswers: Set<String> = emptySet(),
    val searchQuery: String = "",
    /** Raw "last update" date string exactly as stored in past-wordle-answers.txt. */
    val lastUpdateDate: String = "",
    val isUpToDateThroughYesterday: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(FlowPreview::class)
class PreviousAnswersViewModel(
    private val syncPastAnswersUseCase: SyncPastAnswersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreviousAnswersUiState())
    val uiState: StateFlow<PreviousAnswersUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        refresh()
        viewModelScope.launch {
            searchQueryFlow
                .debounce(SEARCH_DEBOUNCE)
                .collectLatest { query -> applyFilter(query) }
        }
    }

    /** Called on every keystroke; updates the field immediately but the list re-filters
     *  only after [SEARCH_DEBOUNCE] of no further typing. */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }

    private fun applyFilter(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.answers
            } else {
                state.answers.filter { it.contains(query.trim().uppercase()) }
            }
            state.copy(displayedAnswers = filtered)
        }
    }

    /** Triggers a sync (network call only if needed) and updates the UI state. */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val state = syncPastAnswersUseCase()
                val yesterday = PastAnswersDateFormat.yesterday()
                val lastUpdate = PastAnswersDateFormat.parseFileDate(state.lastUpdateDate)

                // Words that show up more than once (an answer that repeated) get flagged
                // so the UI can color them differently, but only appear once in the list.
                val counts = state.answers.groupingBy { it.uppercase() }.eachCount()
                val duplicates = counts.filterValues { it > 1 }.keys
                val distinctSorted = counts.keys.sorted()

                _uiState.update { current ->
                    // Always show whatever answers we do have (even if the last sync
                    // attempt failed), so the words already on disk stay visible.
                    current.copy(
                        isLoading = false,
                        answers = distinctSorted,
                        duplicateAnswers = duplicates,
                        lastUpdateDate = state.lastUpdateDate,
                        isUpToDateThroughYesterday = lastUpdate >= yesterday && state.syncErrorMessage == null,
                        errorMessage = state.syncErrorMessage
                    )
                }
                applyFilter(_uiState.value.searchQuery)
            } catch (e: Exception) {
                // Unexpected failure reading the sync result itself: keep any answers
                // already in state rather than clearing the list.
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Unknown error") }
            }
        }
    }
}