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

    /**
     * A pattern only represents a real clue from the user once at least one of its
     * boxes has a letter AND has been marked green or yellow. A row left entirely
     * gray (the default look of an untouched Patterns row, even if letters were
     * typed into it) must not trigger pattern filtering.
     */
    val isActive: Boolean
        get() = row.letters.any { boxed ->
            boxed.letter != null && (boxed.state == LetterState.GREEN || boxed.state == LetterState.YELLOW)
        }

    companion object {
        fun empty(): WordPattern = WordPattern(LetterRow.empty(LetterState.GRAY), expectedMatchCount = 0)
    }
}