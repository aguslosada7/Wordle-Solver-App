package com.wordlesolver.domain.matcher

import com.wordlesolver.domain.model.LetterRow
import com.wordlesolver.domain.model.LetterState

/**
 * Shared per-box green/yellow/gray matching logic against a [LetterRow].
 * Each box carries its own [LetterState], so this works both for uniform rows
 * (the single "Correct letters" row, a "Misplaced letters" row) and for mixed
 * "Patterns" rows where green/yellow/gray boxes can coexist in the same row.
 */
object LetterMatcher {

    /** True if [word] (already uppercased) satisfies every non-empty box in [row]. */
    fun matchesRow(word: String, row: LetterRow): Boolean {
        val greenOrYellowLetters = row.letters
            .filter { it.state == LetterState.GREEN || it.state == LetterState.YELLOW }
            .mapNotNull { it.letter?.uppercaseChar() }
            .toSet()

        row.letters.forEach { boxed ->
            val expected = boxed.letter?.uppercaseChar() ?: return@forEach
            when (boxed.state) {
                LetterState.GREEN -> if (word[boxed.position] != expected) return false
                LetterState.YELLOW -> {
                    if (!word.contains(expected)) return false
                    if (word[boxed.position] == expected) return false
                }
                LetterState.GRAY -> {
                    // A gray letter that's also green/yellow elsewhere in the row just
                    // means "no further occurrences" isn't enforced here; we only
                    // exclude the word if the letter isn't accounted for elsewhere.
                    if (expected !in greenOrYellowLetters && word.contains(expected)) return false
                }
                LetterState.EMPTY -> Unit
            }
        }
        return true
    }
}
