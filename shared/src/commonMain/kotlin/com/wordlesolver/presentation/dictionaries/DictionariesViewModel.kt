package com.wordlesolver.presentation.dictionaries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordlesolver.domain.repository.WordRepository
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

/** Which of the two dictionaries the screen is currently showing. */
enum class DictionaryTab {
    WORDLE,
    GENERAL
}

/**
 * Everything the "Dictionaries" screen needs: the selected tab, that
 * dictionary's word list (2340 Wordle words / 12930 General words per spec),
 * and the current search query.
 */
data class DictionariesUiState(
    val selectedTab: DictionaryTab = DictionaryTab.WORDLE,
    /** The full word list for the selected tab. */
    val words: List<String> = emptyList(),
    /** [words] filtered by [searchQuery] (after debounce), what the UI should render. */
    val displayedWords: List<String> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(FlowPreview::class)
class DictionariesViewModel(
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DictionariesUiState())
    val uiState: StateFlow<DictionariesUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        loadTab(DictionaryTab.WORDLE)
        viewModelScope.launch {
            searchQueryFlow
                .debounce(SEARCH_DEBOUNCE)
                .collectLatest { query -> applyFilter(query) }
        }
    }

    fun onTabSelected(tab: DictionaryTab) {
        if (tab == _uiState.value.selectedTab && _uiState.value.words.isNotEmpty()) return
        loadTab(tab)
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
                state.words
            } else {
                state.words.filter { it.contains(query.trim().uppercase()) }
            }
            state.copy(displayedWords = filtered)
        }
    }

    private fun loadTab(tab: DictionaryTab) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedTab = tab, isLoading = true, errorMessage = null) }
            try {
                val words = when (tab) {
                    DictionaryTab.WORDLE -> wordRepository.getWordleDictionary()
                    DictionaryTab.GENERAL -> wordRepository.getGeneralDictionary()
                }
                _uiState.update { it.copy(isLoading = false, words = words) }
                applyFilter(_uiState.value.searchQuery)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Unknown error") }
            }
        }
    }
}