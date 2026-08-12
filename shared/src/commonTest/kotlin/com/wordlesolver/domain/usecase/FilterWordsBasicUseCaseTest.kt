package com.wordlesolver.domain.usecase

import com.wordlesolver.domain.model.FilterCriteria
import com.wordlesolver.domain.model.LetterRow
import com.wordlesolver.domain.model.LetterState
import com.wordlesolver.domain.model.PositionedLetter
import kotlin.test.*

class FilterWordsBasicUseCaseTest {

    private val useCase = FilterWordsBasicUseCase()
    private val words = listOf(
        "CRANE",
        "SLATE",
        "TRACE",
        "GHOST",
        "BLIND",
    )

    @Test
    fun filtersByGreenPosition() {
        // C at position 0 -> only CRANE
        val correctRow = LetterRow(
            (0..4).map {
                if (it == 0) PositionedLetter(0, 'C', LetterState.GREEN)
                else PositionedLetter(it, null, LetterState.GREEN)
            }
        )
        val criteria = FilterCriteria(correctRow = correctRow)

        val result = useCase(words, criteria)

        assertEquals(listOf("CRANE"), result)
    }

    @Test
    fun filtersByYellowPresentButWrongPosition() {
        // 'A' marked yellow at position 0 -> must contain A, but not at position 0
        val misplacedRow = LetterRow(
            (0..4).map {
                if (it == 0) PositionedLetter(0, 'A', LetterState.YELLOW)
                else PositionedLetter(it, null, LetterState.YELLOW)
            }
        )
        val criteria = FilterCriteria(misplacedRows = listOf(misplacedRow))

        val result = useCase(words, criteria)

        // CRANE, SLATE, TRACE all contain A not at position 0
        assertEquals(setOf("CRANE", "SLATE", "TRACE"), result.toSet())
    }

    @Test
    fun filtersByExcludedLetters() {
        val criteria = FilterCriteria(excludedLetters = setOf('S', 'B'))

        val result = useCase(words, criteria)

        assertEquals(setOf("CRANE", "TRACE"), result.toSet())
    }
}
