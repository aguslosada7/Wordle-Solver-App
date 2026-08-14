package com.wordlesolver.domain.usecase

import com.wordlesolver.domain.model.LetterState
import com.wordlesolver.domain.model.WordPattern
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterWordsByPatternsUseCaseTest {

    private val useCase = FilterWordsByPatternsUseCase()

    private fun colors(vararg states: LetterState): List<LetterState> = states.toList()

    @Test
    fun keepsCandidatesWhenExpectedCountIsMet() {
        // Pattern: yellow, gray, green, green, green — evaluated with each candidate as
        // the target. Against target CRANE, general-dict guesses producing that exact
        // pattern are counted; CRATE and CRANE itself grade differently, so we pick a
        // dictionary/candidate pair with a known, simple match: an all-gray pattern
        // (nothing green/yellow) means "no guess shares any letter with the target".
        val allGray = colors(LetterState.GRAY, LetterState.GRAY, LetterState.GRAY, LetterState.GRAY, LetterState.GRAY)
        val pattern = WordPattern(allGray, expectedMatchCount = 1)

        // Against target "CRANE", "SLIMY" shares no letters -> counts as a match (all gray).
        val result = useCase(
            candidateWords = listOf("CRANE", "GRAPE"),
            patterns = listOf(pattern),
            generalDictionary = listOf("SLIMY", "CRATE")
        )

        // CRANE: SLIMY shares no letters with CRANE -> all gray -> 1 match -> kept.
        // GRAPE: SLIMY shares no letters with GRAPE either -> also kept.
        assertEquals(setOf("CRANE", "GRAPE"), result.toSet())
    }

    @Test
    fun rejectsCandidatesWhenExpectedCountIsNotMet() {
        val allGreen = colors(LetterState.GREEN, LetterState.GREEN, LetterState.GREEN, LetterState.GREEN, LetterState.GREEN)
        // Only a guess identical to the candidate itself can be all-green; asking for 2
        // is impossible since the general dictionary here has no duplicate of "CRANE".
        val pattern = WordPattern(allGreen, expectedMatchCount = 2)

        val result = useCase(
            candidateWords = listOf("CRANE"),
            patterns = listOf(pattern),
            generalDictionary = listOf("CRANE", "SLATE")
        )

        assertEquals(emptyList(), result)
    }

    @Test
    fun noActivePatternsReturnsCandidatesUnchanged() {
        val result = useCase(
            candidateWords = listOf("CRANE", "SLATE"),
            patterns = listOf(WordPattern.empty()),
            generalDictionary = listOf("CRANE", "SLATE")
        )

        assertEquals(listOf("CRANE", "SLATE"), result)
    }

    @Test
    fun geodeExampleFromSpec() {
        // Word: GEODE, pattern yellow-gray-green-green-green.
        // Per the correct duplicate-letter-aware grading, the only General-dictionary
        // guesses that produce this exact pattern against target GEODE are words of the
        // shape E?ODE where the first letter is forced to E (GEODE's own remaining
        // letter after the greens consume O/D/E), and the second letter is anything
        // except G or E.
        val pattern = WordPattern(
            colors = colors(LetterState.YELLOW, LetterState.GRAY, LetterState.GREEN, LetterState.GREEN, LetterState.GREEN),
            expectedMatchCount = 3
        )

        val result = useCase(
            candidateWords = listOf("GEODE"),
            patterns = listOf(pattern),
            generalDictionary = listOf("EPODE", "ERODE", "EXODE", "ABODE", "GEODE")
        )

        // EPODE, ERODE, EXODE match; ABODE and GEODE itself do not.
        assertEquals(listOf("GEODE"), result)
    }
}
