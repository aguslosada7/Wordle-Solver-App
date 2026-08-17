package com.wordlesolver.domain.matcher

import com.wordlesolver.domain.model.LetterState
import kotlin.test.Test
import kotlin.test.assertEquals

class WordleGraderTest {

    @Test
    fun identicalWordsAreAllGreen() {
        assertEquals(
            List(5) { LetterState.GREEN },
            WordleGrader.grade("CRANE", "CRANE")
        )
    }

    @Test
    fun handlesDuplicateLettersCorrectly_epodeAgainstGeode() {
        // guess EPODE against target GEODE:
        // target letters G,E,O,D,E. Greens at positions 2,3,4 (O,D,E) consume one O, D, E.
        // Remaining target pool for positions 0,1: {G, E}.
        // guess[0]='E': present in pool (and guess[0] != target[0]='G') -> YELLOW, consumes E.
        // guess[1]='P': not in remaining pool {G} -> GRAY.
        assertEquals(
            listOf(LetterState.YELLOW, LetterState.GRAY, LetterState.GREEN, LetterState.GREEN, LetterState.GREEN),
            WordleGrader.grade("EPODE", "GEODE")
        )
    }

    @Test
    fun abodeDoesNotProduceYellowGrayGreenGreenGreenAgainstGeode() {
        // guess ABODE against target GEODE: greens at 2,3,4 (O,D,E) as above.
        // Remaining target pool for positions 0,1: {G, E}.
        // guess[0]='A': not in pool -> GRAY (not yellow).
        // guess[1]='B': not in pool -> GRAY.
        assertEquals(
            listOf(LetterState.GRAY, LetterState.GRAY, LetterState.GREEN, LetterState.GREEN, LetterState.GREEN),
            WordleGrader.grade("ABODE", "GEODE")
        )
    }

    @Test
    fun secondSpecExample_yellowGrayGrayGreenGreen() {
        // should allow the first letter to be E or O, but never G.
        assertEquals(
            listOf(LetterState.YELLOW, LetterState.GRAY, LetterState.GRAY, LetterState.GREEN, LetterState.GREEN),
            WordleGrader.grade("ELIDE", "GEODE")
        )
        assertEquals(
            listOf(LetterState.YELLOW, LetterState.GRAY, LetterState.GRAY, LetterState.GREEN, LetterState.GREEN),
            WordleGrader.grade("OXIDE", "GEODE")
        )
    }
}
