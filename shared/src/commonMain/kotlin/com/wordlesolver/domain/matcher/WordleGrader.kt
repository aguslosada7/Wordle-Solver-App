package com.wordlesolver.domain.matcher

import com.wordlesolver.domain.model.LetterState

/**
 * Computes the real Wordle color feedback for a guess against a target word,
 * correctly handling repeated letters (the standard two-pass algorithm):
 *
 * 1. Green pass: every position where guess[ i ] == target[ i ] is GREEN. Those letter
 *    instances are removed from the pool of "unmatched" target letters.
 * 2. Yellow/gray pass: for every remaining (non-green) position, left to right, the
 *    guess letter is YELLOW if an unmatched instance of it still remains in the
 *    target pool (which is then consumed), otherwise GRAY.
 *
 * This is what makes a "Patterns" row meaningful without the user ever typing a
 * letter: the row's colors are always evaluated against a specific word (the
 * candidate being tested, or the word tapped for the related-words modal), and the
 * candidate/dictionary word supplies the letters.
 */
object WordleGrader {

    fun grade(guess: String, target: String): List<LetterState> {
        require(guess.length == 5) { "guess must be 5 letters" }
        require(target.length == 5) { "target must be 5 letters" }

        val g = guess.uppercase()
        val t = target.uppercase()
        val result = MutableList(5) { LetterState.GRAY }

        // Remaining, unmatched target letters available for the yellow pass.
        val remaining = mutableMapOf<Char, Int>()

        // Pass 1: greens.
        for (i in 0..4) {
            if (g[i] == t[i]) {
                result[i] = LetterState.GREEN
            } else {
                remaining[t[i]] = (remaining[t[i]] ?: 0) + 1
            }
        }

        // Pass 2: yellows/grays, left to right, over non-green positions only.
        for (i in 0..4) {
            if (result[i] == LetterState.GREEN) continue
            val letter = g[i]
            val count = remaining[letter] ?: 0
            if (count > 0) {
                result[i] = LetterState.YELLOW
                remaining[letter] = count - 1
            } else {
                result[i] = LetterState.GRAY
            }
        }

        return result
    }
}
