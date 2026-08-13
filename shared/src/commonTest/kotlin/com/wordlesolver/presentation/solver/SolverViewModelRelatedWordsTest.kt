package com.wordlesolver.presentation.solver

import com.wordlesolver.domain.model.LetterRow
import com.wordlesolver.domain.model.LetterState
import com.wordlesolver.domain.model.PositionedLetter
import com.wordlesolver.domain.model.WordPattern
import com.wordlesolver.domain.repository.PastAnswersRepository
import com.wordlesolver.domain.repository.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakeWordRepository(
    private val general: List<String>
) : WordRepository {
    override suspend fun getWordleDictionary(): List<String> = emptyList()
    override suspend fun getGeneralDictionary(): List<String> = general
}

private class FakePastAnswersRepository : PastAnswersRepository {
    override suspend fun getPastAnswers() = throw NotImplementedError("unused in this test")
    override suspend fun syncPastAnswers() = throw NotImplementedError("unused in this test")
}

class SolverViewModelRelatedWordsTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun greenRow(position: Int, letter: Char): LetterRow =
        LetterRow((0..4).map {
            if (it == position) PositionedLetter(it, letter, LetterState.GREEN)
            else PositionedLetter(it, null, LetterState.GREEN)
        })

    @Test
    fun selectingWordWithActivePatternPopulatesModal() = runTest {
        val viewModel = SolverViewModel(
            wordRepository = FakeWordRepository(general = listOf("CRANE", "CRATE", "SLATE")),
            pastAnswersRepository = FakePastAnswersRepository()
        )
        // Give the first pattern a green 'C' at position 0 (an "active" pattern).
        val pattern = WordPattern(greenRow(0, 'C'), expectedMatchCount = 2)
        // Directly mutate state via the same mechanism the UI would (add + edit pattern).
        repeat(0) { } // no-op, patterns default list already has one empty pattern
        viewModel.onPatternLetterChanged(0, 0, 'C')
        viewModel.onPatternExpectedCountChanged(0, 2)

        viewModel.onResultWordSelected("CRANE")

        val modal = viewModel.uiState.value.relatedWordsModal
        requireNotNull(modal)
        assertEquals("CRANE", modal.selectedWord)
        assertEquals(setOf("CRANE", "CRATE"), modal.wordsByPattern.first().relatedWords.toSet())
    }

    @Test
    fun selectingWordWithNoActivePatternsDoesNothing() = runTest {
        val viewModel = SolverViewModel(
            wordRepository = FakeWordRepository(general = listOf("CRANE")),
            pastAnswersRepository = FakePastAnswersRepository()
        )

        viewModel.onResultWordSelected("CRANE")

        assertNull(viewModel.uiState.value.relatedWordsModal)
    }

    @Test
    fun dismissClearsModal() = runTest {
        val viewModel = SolverViewModel(
            wordRepository = FakeWordRepository(general = listOf("CRANE", "CRATE")),
            pastAnswersRepository = FakePastAnswersRepository()
        )
        viewModel.onPatternLetterChanged(0, 0, 'C')
        viewModel.onResultWordSelected("CRANE")

        viewModel.dismissRelatedWordsModal()

        assertNull(viewModel.uiState.value.relatedWordsModal)
    }
}
