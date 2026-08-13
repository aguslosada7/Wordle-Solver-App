package com.wordlesolver.domain.usecase

import com.wordlesolver.domain.model.LetterRow
import com.wordlesolver.domain.model.LetterState
import com.wordlesolver.domain.model.PositionedLetter
import com.wordlesolver.domain.model.WordPattern
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterWordsByPatternsUseCaseTest {

    private val useCase = FilterWordsByPatternsUseCase()
    private val generalDictionary = listOf("CRANE", "SLATE", "TRACE", "GRATE", "PLATE")

    private fun greenRowAt(position: Int, letter: Char): LetterRow =
        LetterRow((0..4).map {
            if (it == position) PositionedLetter(it, letter, LetterState.GREEN)
            else PositionedLetter(it, null, LetterState.GREEN)
        })

    @Test
    fun keepsCandidatesWhenExpectedCountMatches() {
        // Words ending in "ATE": SLATE, GRATE, PLATE -> 3 matches in the dictionary above
        val row = LetterRow(greenRowAt(4, 'E').letters.mapIndexed { index, boxed ->
            when (index) {
                1 -> PositionedLetter(1, 'L', LetterState.GREEN)
                2 -> PositionedLetter(2, 'A', LetterState.GREEN)
                3 -> PositionedLetter(3, 'T', LetterState.GREEN)
                else -> boxed
            }
        })
        val pattern = WordPattern(row, expectedMatchCount = 1) // only SLATE has S_LATE shape starting S
        // Adjust: we actually match any word with _LATE, dictionary has SLATE, GRATE(no), PLATE
        // GRATE doesn't match _LATE (G-R-A-T-E), PLATE matches (P-L-A-T-E), SLATE matches (S-L-A-T-E)
        val correctedPattern = pattern.copy(expectedMatchCount = 2)

        val result = useCase(
            candidateWords = listOf("SLATE", "PLATE", "CRANE"),
            patterns = listOf(correctedPattern),
            generalDictionary = generalDictionary
        )

        assertEquals(setOf("SLATE", "PLATE"), result.toSet())
    }

    @Test
    fun rejectsAllCandidatesWhenExpectedCountDoesNotMatch() {
        val row = greenRowAt(0, 'C')
        // Dictionary has 2 words starting with C (CRANE, ... only CRANE actually)
        val pattern = WordPattern(row, expectedMatchCount = 99) // deliberately wrong

        val result = useCase(
            candidateWords = listOf("CRANE"),
            patterns = listOf(pattern),
            generalDictionary = generalDictionary
        )

        assertEquals(emptyList(), result)
    }

    @Test
    fun noPatternsReturnsCandidatesUnchanged() {
        val result = useCase(
            candidateWords = listOf("CRANE", "SLATE"),
            patterns = emptyList(),
            generalDictionary = generalDictionary
        )

        assertEquals(listOf("CRANE", "SLATE"), result)
    }
}
