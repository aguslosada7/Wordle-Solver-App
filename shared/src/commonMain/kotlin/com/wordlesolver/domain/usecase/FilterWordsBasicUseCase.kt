package com.wordlesolver.domain.usecase

import com.wordlesolver.domain.model.FilterCriteria
import com.wordlesolver.domain.model.LetterRow

/**
 * Applies "basic filtering" (green + yellow + excluded letters) to a candidate word list.
 *
 * Rules:
 * - Green letters: word must have that exact letter at that exact position.
 * - Yellow letters: word must contain the letter somewhere, but NOT at the yellow position.
 * - Excluded letters ("Letters NOT in the word"): word must not contain them in any position,
 *   UNLESS that same letter is also marked green/yellow elsewhere (handled by set difference
 *   at the caller/UI level - here we simply treat excludedLetters as a hard exclusion set).
 */
class FilterWordsBasicUseCase {

    operator fun invoke(words: List<String>, criteria: FilterCriteria): List<String> {
        return words.filter { word ->
            val normalized = word.uppercase()
            matchesGreen(normalized, criteria.correctRow) &&
                matchesAllYellowRows(normalized, criteria.misplacedRows) &&
                matchesExcluded(normalized, criteria.excludedLetters)
        }
    }

    private fun matchesGreen(word: String, correctRow: LetterRow): Boolean {
        correctRow.letters.forEach { boxed ->
            val expected = boxed.letter ?: return@forEach
            if (word[boxed.position] != expected.uppercaseChar()) return false
        }
        return true
    }

    private fun matchesAllYellowRows(word: String, misplacedRows: List<LetterRow>): Boolean {
        return misplacedRows.all { row -> matchesYellowRow(word, row) }
    }

    private fun matchesYellowRow(word: String, row: LetterRow): Boolean {
        row.letters.forEach { boxed ->
            val expected = boxed.letter?.uppercaseChar() ?: return@forEach
            // Must be present in the word somewhere...
            if (!word.contains(expected)) return false
            // ...but not at this exact position.
            if (word[boxed.position] == expected) return false
        }
        return true
    }

    private fun matchesExcluded(word: String, excludedLetters: Set<Char>): Boolean {
        if (excludedLetters.isEmpty()) return true
        val upperExcluded = excludedLetters.mapTo(mutableSetOf()) { it.uppercaseChar() }
        return word.none { it in upperExcluded }
    }
}
