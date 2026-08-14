package com.wordlesolver.presentation.solver

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

    /** Clicks every box of pattern 0 once, cycling it from the default gray to all-green. */
    private fun SolverViewModel.makePatternAllGreen() {
        repeat(5) { position -> onPatternBoxColorCycled(0, position) }
    }

    @Test
    fun selectingWordWithActivePatternPopulatesModal() = runTest {
        val viewModel = SolverViewModel(
            wordRepository = FakeWordRepository(general = listOf("CRANE", "CRATE", "SLATE")),
            pastAnswersRepository = FakePastAnswersRepository()
        )
        // An all-green pattern only matches a guess identical to the target itself.
        viewModel.makePatternAllGreen()

        viewModel.onResultWordSelected("CRANE")

        val modal = viewModel.uiState.value.relatedWordsModal
        requireNotNull(modal)
        assertEquals("CRANE", modal.selectedWord)
        assertEquals(setOf("CRANE"), modal.wordsByPattern.first().relatedWords.toSet())
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
        viewModel.makePatternAllGreen()
        viewModel.onResultWordSelected("CRANE")

        viewModel.dismissRelatedWordsModal()

        assertNull(viewModel.uiState.value.relatedWordsModal)
    }
}
