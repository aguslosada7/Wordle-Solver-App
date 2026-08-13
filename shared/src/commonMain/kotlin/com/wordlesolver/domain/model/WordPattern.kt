package com.wordlesolver.domain.model

/**
 * A single row of the "Patterns" section: a 5-box green/yellow/gray row plus the
 * number of General-dictionary words the user expects to match it.
 */
data class WordPattern(
    val row: LetterRow,
    val expectedMatchCount: Int
) {
    init {
        require(expectedMatchCount >= 0) { "expectedMatchCount cannot be negative" }
    }

    companion object {
        fun empty(): WordPattern = WordPattern(LetterRow.empty(LetterState.GRAY), expectedMatchCount = 0)
    }
}
