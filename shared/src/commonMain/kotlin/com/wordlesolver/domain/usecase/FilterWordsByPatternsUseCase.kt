package com.wordlesolver.domain.usecase

import com.wordlesolver.domain.matcher.LetterMatcher
import com.wordlesolver.domain.model.WordPattern

/**
 * Applies "pattern filtering" on top of an already basic-filtered word list, per CLAUDE.md:
 *
 * For each [com.wordlesolver.domain.model.WordPattern] the user entered:
 * 1. Count how many words in [generalDictionary] match that pattern's row.
 * 2. If that count doesn't equal the pattern's [com.wordlesolver.domain.model.WordPattern.expectedMatchCount], the pattern
 *    is treated as unsatisfied and no candidate can pass it (acts as a validation guard against
 *    a mistyped clue-count).
 * 3. Otherwise, keep only candidate words that also match the pattern's row themselves.
 *
 * A word must satisfy every pattern to remain in the result.
 */
class FilterWordsByPatternsUseCase {

    operator fun invoke(
        candidateWords: List<String>,
        patterns: List<WordPattern>,
        generalDictionary: List<String>
    ): List<String> {
        if (patterns.isEmpty()) return candidateWords

        val normalizedGeneral = generalDictionary.map { it.uppercase() }

        return patterns.fold(candidateWords) { remaining, pattern ->
            val actualMatchCount = normalizedGeneral.count { LetterMatcher.matchesRow(it, pattern.row) }
            if (actualMatchCount != pattern.expectedMatchCount) {
                emptyList()
            } else {
                remaining.filter { LetterMatcher.matchesRow(it.uppercase(), pattern.row) }
            }
        }
    }
}