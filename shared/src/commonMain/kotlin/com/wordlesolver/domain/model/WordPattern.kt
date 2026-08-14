package com.wordlesolver.domain.model

/**
 * A single row of the "Patterns" section: just 5 box colors (green/yellow/gray),
 * plus the number of General-dictionary words the user expects to match it.
 *
 * Per CLAUDE.md, a Patterns row has NO letter inputs of its own — the user only
 * clicks each box to cycle its color. The letters are always supplied by whichever
 * word the pattern is being evaluated against (a candidate word while filtering the
 * results list, or the tapped word when showing the related-words modal); see
 * [com.wordlesolver.domain.matcher.WordleGrader].
 */
data class WordPattern(
    val colors: List<LetterState> = List(5) { LetterState.GRAY },
    val expectedMatchCount: Int = 0
) {
    init {
        require(colors.size == 5) { "A pattern must have exactly 5 boxes" }
        require(expectedMatchCount >= 0) { "expectedMatchCount cannot be negative" }
    }

    /**
     * A pattern only represents a real clue from the user once at least one box has
     * been marked green or yellow. A row left entirely gray (the default, untouched
     * look of a Patterns row) must not trigger pattern filtering.
     */
    val isActive: Boolean
        get() = colors.any { it == LetterState.GREEN || it == LetterState.YELLOW }

    companion object {
        fun empty(): WordPattern = WordPattern()
    }
}
