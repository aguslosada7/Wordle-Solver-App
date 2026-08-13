package com.wordlesolver.domain.usecase

import com.wordlesolver.domain.matcher.LetterMatcher
import com.wordlesolver.domain.model.WordPattern

/**
 * For the modal shown when pattern filtering is enabled and the user taps a result word:
 * returns, per pattern, the General-dictionary words that satisfy that pattern.
 */
class GetRelatedWordsByPatternUseCase {

    operator fun invoke(
        patterns: List<WordPattern>,
        generalDictionary: List<String>
    ): Map<WordPattern, List<String>> {
        val normalizedGeneral = generalDictionary.map { it.uppercase() }
        return patterns.associateWith { pattern ->
            normalizedGeneral.filter { LetterMatcher.matchesRow(it, pattern.row) }
        }
    }
}