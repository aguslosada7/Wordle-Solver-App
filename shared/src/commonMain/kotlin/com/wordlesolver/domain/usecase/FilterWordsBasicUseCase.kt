package com.wordlesolver.domain.usecase

import com.wordlesolver.domain.matcher.LetterMatcher
import com.wordlesolver.domain.model.FilterCriteria

/**
 * Applies "basic filtering" (green + yellow + excluded letters) to a candidate word list.
 *
 * Rules:
 * - Green letters: word must have that exact letter at that exact position.
 * - Yellow letters: word must contain the letter somewhere, but NOT at the yellow position.
 * - Excluded letters ("Letters NOT in the word"): word must not contain them in any position.
 */
class FilterWordsBasicUseCase {

    operator fun invoke(words: List<String>, criteria: FilterCriteria): List<String> {
        return words.filter { word ->
            val normalized = word.uppercase()
            LetterMatcher.matchesRow(normalized, criteria.correctRow) &&
                criteria.misplacedRows.all { row -> LetterMatcher.matchesRow(normalized, row) } &&
                matchesExcluded(normalized, criteria.excludedLetters)
        }
    }

    private fun matchesExcluded(word: String, excludedLetters: Set<Char>): Boolean {
        if (excludedLetters.isEmpty()) return true
        val upperExcluded = excludedLetters.map { it.uppercaseChar() }.toSet()
        return word.none { it in upperExcluded }
    }
}
