package com.wordlesolver.presentation.solver

import com.wordlesolver.domain.model.LetterRow
import com.wordlesolver.domain.model.LetterState
import com.wordlesolver.domain.model.WordPattern

/**
 * Everything the Solver screen needs to render, mirroring the CLAUDE.md layout:
 * correct row, misplaced rows, pattern rows, excluded letters, the two switches,
 * and the resulting word list.
 */
data class SolverUiState(
    val correctRow: LetterRow = LetterRow.empty(LetterState.GREEN),
    val misplacedRows: List<LetterRow> = listOf(LetterRow.empty(LetterState.YELLOW)),
    val patterns: List<WordPattern> = listOf(WordPattern.empty()),
    val excludedLettersInput: String = "",
    val showOnlyWordleWords: Boolean = true,
    val excludePreviousAnswers: Boolean = false,
    val isLoading: Boolean = false,
    val results: List<SolverResultWord> = emptyList(),
    val errorMessage: String? = null
)

/**
 * A single result word plus whether it belongs to the Wordle dictionary, so the UI
 * can apply the alternate background color (#b9b0d4) when "Show only Wordle words" is off.
 */
data class SolverResultWord(
    val word: String,
    val isWordleWord: Boolean
)
