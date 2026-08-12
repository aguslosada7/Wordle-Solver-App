package com.wordlesolver.domain.model

/**
 * A single letter at a fixed position (0..4) with an associated state.
 * Used to represent one box in the Correct/Misplaced/Pattern rows.
 */
data class PositionedLetter(
    val position: Int,
    val letter: Char?,
    val state: LetterState,
) {
    init {
        require(position in (0..4)) { "position must be between 0 and 4" }
    }
}

/**
 * A full 5-box row, e.g. one "Misplaced letters" row or the single "Correct letters" row.
 */
data class LetterRow(
    val letters: List<PositionedLetter>
) {
    init {
        require(letters.size == 5) { "A row must contain exactly 5 letters" }
    }

    companion object {
        fun empty(state: LetterState): LetterRow =
            LetterRow((0..4).map { PositionedLetter(it, null, state) })
    }
}
