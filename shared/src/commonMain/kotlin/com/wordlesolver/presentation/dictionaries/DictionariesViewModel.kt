package com.wordlesolver.presentation.dictionaries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordlesolver.domain.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Which of the two dictionaries the screen is currently showing. */
enum class DictionaryTab {
    WORDLE,
    GENERAL
}

/**
 * Everything the "Dictionaries" screen needs: the selected tab and that
 * dictionary's word list (2340 Wordle words / 12930 General words per spec).
 */
data class DictionariesUiState(
    val selectedTab: DictionaryTab = DictionaryTab.WORDLE,
    val words: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DictionariesViewModel(
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DictionariesUiState())
    val uiState: StateFlow<DictionariesUiState> = _uiState.asStateFlow()

    init {
        loadTab(DictionaryTab.WORDLE)
    }

    fun onTabSelected(tab: DictionaryTab) {
        if (tab == _uiState.value.selectedTab && _uiState.value.words.isNotEmpty()) return
        loadTab(tab)
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
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Unknown error") }
            }
        }
    }
}
