package com.wordlesolver.domain.model

/**
 * Aggregates the "basic filtering" inputs from the Solver screen:
 * - correctRow: the single green row ("Correct letters")
 * - misplacedRows: one or more yellow rows ("Misplaced letters")
 * - excludedLetters: letters known NOT to be in the word, position-independent
 */
data class FilterCriteria(
    val correctRow: LetterRow = LetterRow.empty(LetterState.GREEN),
    val misplacedRows: List<LetterRow> = listOf(LetterRow.empty(LetterState.YELLOW)),
    val excludedLetters: Set<Char> = emptySet(),
)
