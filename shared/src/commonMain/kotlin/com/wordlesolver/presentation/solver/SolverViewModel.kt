package com.wordlesolver.presentation.solver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordlesolver.domain.model.FilterCriteria
import com.wordlesolver.domain.model.LetterRow
import com.wordlesolver.domain.model.LetterState
import com.wordlesolver.domain.model.WordPattern
import com.wordlesolver.domain.repository.PastAnswersRepository
import com.wordlesolver.domain.repository.WordRepository
import com.wordlesolver.domain.usecase.FilterWordsBasicUseCase
import com.wordlesolver.domain.usecase.FilterWordsByPatternsUseCase
import com.wordlesolver.domain.usecase.GetRelatedWordsByPatternUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Solver screen. Owns [SolverUiState] and drives the domain use cases;
 * views only dispatch events and observe [uiState].
 */
class SolverViewModel(
    private val wordRepository: WordRepository,
    private val pastAnswersRepository: PastAnswersRepository,
    private val filterWordsBasicUseCase: FilterWordsBasicUseCase = FilterWordsBasicUseCase(),
    private val filterWordsByPatternsUseCase: FilterWordsByPatternsUseCase = FilterWordsByPatternsUseCase(),
    private val getRelatedWordsByPatternUseCase: GetRelatedWordsByPatternUseCase = GetRelatedWordsByPatternUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SolverUiState())
    val uiState: StateFlow<SolverUiState> = _uiState.asStateFlow()

    init {
        // Show the full (switch-filtered) word list immediately, even before the user
        // types anything into any input.
        submit()
    }

    // --- Correct letters row -------------------------------------------------

    fun onCorrectLetterChanged(position: Int, letter: Char?) {
        _uiState.update { state ->
            state.copy(correctRow = state.correctRow.withLetter(position, letter))
        }
    }

    fun clearCorrectRow() {
        _uiState.update { it.copy(correctRow = LetterRow.empty(LetterState.GREEN)) }
    }

    // --- Misplaced letters rows ----------------------------------------------

    fun onMisplacedLetterChanged(rowIndex: Int, position: Int, letter: Char?) {
        _uiState.update { state ->
            state.copy(misplacedRows = state.misplacedRows.replaceAt(rowIndex) { it.withLetter(position, letter) })
        }
    }

    fun addMisplacedRow() {
        _uiState.update { state -> state.copy(misplacedRows = state.misplacedRows + LetterRow.empty(LetterState.YELLOW)) }
    }

    fun removeMisplacedRow() {
        _uiState.update { state ->
            if (state.misplacedRows.size <= 1) state
            else state.copy(misplacedRows = state.misplacedRows.dropLast(1))
        }
    }

    // --- Pattern rows ----------------------------------------------------------

    /** Cycles a pattern box's color: gray -> green -> yellow -> gray. Patterns have no
     * letter inputs of their own — this is the only way to edit a Patterns row. */
    fun onPatternBoxColorCycled(patternIndex: Int, position: Int) {
        _uiState.update { state ->
            state.copy(patterns = state.patterns.replaceAt(patternIndex) { pattern ->
                val current = pattern.colors[position]
                val next = when (current) {
                    LetterState.GRAY -> LetterState.GREEN
                    LetterState.GREEN -> LetterState.YELLOW
                    else -> LetterState.GRAY
                }
                pattern.copy(colors = pattern.colors.toMutableList().apply { this[position] = next })
            })
        }
    }

    fun onPatternExpectedCountChanged(patternIndex: Int, count: Int) {
        _uiState.update { state ->
            state.copy(patterns = state.patterns.replaceAt(patternIndex) { it.copy(expectedMatchCount = count) })
        }
    }

    fun addPatternRow() {
        _uiState.update { state -> state.copy(patterns = state.patterns + WordPattern.empty()) }
    }

    fun removePatternRow() {
        _uiState.update { state ->
            if (state.patterns.size <= 1) state
            else state.copy(patterns = state.patterns.dropLast(1))
        }
    }

    // --- Excluded letters + switches -------------------------------------------

    fun onExcludedLettersChanged(input: String) {
        _uiState.update { it.copy(excludedLettersInput = input.uppercase()) }
    }

    fun onShowOnlyWordleWordsToggled(enabled: Boolean) {
        _uiState.update { it.copy(showOnlyWordleWords = enabled) }
        submit()
    }

    fun onExcludePreviousAnswersToggled(enabled: Boolean) {
        _uiState.update { it.copy(excludePreviousAnswers = enabled) }
        submit()
    }

    // --- Related words modal (pattern filtering only) --------------------------

    /**
     * Per spec: "If filtering by patterns is enabled, when selecting a word from the
     * results, a modal is displayed" showing General-dictionary words matching each pattern.
     */
    fun onResultWordSelected(word: String) {
        val state = _uiState.value
        val activePatterns = state.patterns.filter { it.isActive }
        if (activePatterns.isEmpty()) return

        viewModelScope.launch {
            val generalDictionary = wordRepository.getGeneralDictionary()
            val relatedByPattern = getRelatedWordsByPatternUseCase(activePatterns, word, generalDictionary)
            val modalState = RelatedWordsModalState(
                selectedWord = word,
                wordsByPattern = activePatterns.map { pattern ->
                    PatternRelatedWords(pattern = pattern, relatedWords = relatedByPattern[pattern].orEmpty())
                }
            )
            _uiState.update { it.copy(relatedWordsModal = modalState) }
        }
    }

    fun dismissRelatedWordsModal() {
        _uiState.update { it.copy(relatedWordsModal = null) }
    }

    // --- Actions -----------------------------------------------------------------

    fun clearAll() {
        _uiState.value = SolverUiState()
        submit()
    }

    /** Runs basic filtering, then pattern filtering, then the two switches. */
    fun submit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val state = _uiState.value
                val generalDictionary = wordRepository.getGeneralDictionary()
                val wordleDictionary = wordRepository.getWordleDictionary().toSet()

                val criteria = FilterCriteria(
                    correctRow = state.correctRow,
                    misplacedRows = state.misplacedRows,
                    excludedLetters = state.excludedLettersInput.trim().uppercase().toSet(),
                    patterns = state.patterns
                )

                // filterWordsByPatternsUseCase does its own internal parallel work (it
                // calls runBlocking on Dispatchers.Default), which would block this
                // coroutine's thread until done. Running the whole basic+pattern
                // filtering step via withContext(Dispatchers.Default) keeps that
                // blocking off viewModelScope's Main thread, so the UI (loading
                // spinner, navigation, etc.) stays responsive while it runs.
                var candidates = withContext(Dispatchers.Default) {
                    val basicFiltered = filterWordsBasicUseCase(generalDictionary, criteria)
                    filterWordsByPatternsUseCase(basicFiltered, state.patterns, generalDictionary)
                }

                if (state.showOnlyWordleWords) {
                    candidates = candidates.filter { it in wordleDictionary }
                }

                if (state.excludePreviousAnswers) {
                    val previousAnswers = pastAnswersRepository.getPastAnswers().answers.toSet()
                    candidates = candidates.filterNot { it in previousAnswers }
                }

                val results = candidates.map { word ->
                    SolverResultWord(word = word, isWordleWord = word in wordleDictionary)
                }

                _uiState.update { it.copy(isLoading = false, results = results) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Unknown error") }
            }
        }
    }
}

// --- Small immutable-update helpers -------------------------------------------

private fun LetterRow.withLetter(position: Int, letter: Char?): LetterRow =
    LetterRow(letters.mapIndexed { index, boxed ->
        if (index == position) boxed.copy(letter = letter) else boxed
    })

private fun <T> List<T>.replaceAt(index: Int, transform: (T) -> T): List<T> =
    mapIndexed { i, item -> if (i == index) transform(item) else item }