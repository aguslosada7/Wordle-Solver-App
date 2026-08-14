package com.wordlesolver.domain.usecase

import com.wordlesolver.domain.matcher.WordleGrader
import com.wordlesolver.domain.model.WordPattern

/**
 * Applies "pattern filtering" on top of an already basic-filtered word list, per CLAUDE.md.
 *
 * A [WordPattern] carries no letters of its own — only colors. So for each remaining
 * candidate word, that candidate itself is treated as the *target* (the presumed
 * answer), and every pattern's colors are checked against how many General-dictionary
 * words, used as a *guess*, would produce that exact color feedback against it (real
 * Wordle duplicate-letter grading, via [WordleGrader]).
 *
 * A candidate is kept only if, for every active pattern, that count is at least the
 * pattern's [WordPattern.expectedMatchCount] (a floor, not an exact target — "1" means
 * "at least 1").
 */
class FilterWordsByPatternsUseCase {

    operator fun invoke(
        candidateWords: List<String>,
        patterns: List<WordPattern>,
        generalDictionary: List<String>
    ): List<String> {
        // An "inactive" pattern (no box marked green/yellow yet) isn't a real clue from
        // the user yet — it must not filter anything out, otherwise the always-present
        // default pattern row would wipe every result before the user ever touches it.
        val activePatterns = patterns.filter { it.isActive }
        if (activePatterns.isEmpty()) return candidateWords

        val normalizedGeneral = generalDictionary.map { it.uppercase() }

        return candidateWords.filter { candidate ->
            val target = candidate.uppercase()
            activePatterns.all { pattern ->
                val actualMatchCount = normalizedGeneral.count { guess ->
                    WordleGrader.grade(guess, target) == pattern.colors
                }
                actualMatchCount >= pattern.expectedMatchCount
            }
        }
    }
}
