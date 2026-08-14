package com.wordlesolver.domain.usecase

import com.wordlesolver.domain.matcher.WordleGrader
import com.wordlesolver.domain.model.WordPattern

/**
 * For the modal shown when pattern filtering is enabled and the user taps a result
 * word: returns, per pattern, the General-dictionary words that would produce that
 * pattern's exact color feedback when used as a guess against the tapped word
 * (treated as the target), via [WordleGrader].
 */
class GetRelatedWordsByPatternUseCase {

    operator fun invoke(
        patterns: List<WordPattern>,
        targetWord: String,
        generalDictionary: List<String>
    ): Map<WordPattern, List<String>> {
        val target = targetWord.uppercase()
        val normalizedGeneral = generalDictionary.map { it.uppercase() }
        return patterns.associateWith { pattern ->
            normalizedGeneral.filter { guess -> WordleGrader.grade(guess, target) == pattern.colors }
        }
    }
}
