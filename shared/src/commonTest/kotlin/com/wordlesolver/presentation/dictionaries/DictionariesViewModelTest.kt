package com.wordlesolver.presentation.dictionaries

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

private class FakeWordRepository(
    private val wordle: List<String>,
    private val general: List<String>
) : WordRepository {
    override suspend fun getWordleDictionary(): List<String> = wordle
    override suspend fun getGeneralDictionary(): List<String> = general
}

class DictionariesViewModelTest {

    // viewModelScope uses Dispatchers.Main.immediate; on non-Android JVM/native test
    // targets there's no default Main dispatcher, so we install a test one here.
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsWordleDictionaryByDefault() = runTest {
        val viewModel = DictionariesViewModel(
            FakeWordRepository(wordle = listOf("CRANE", "SLATE"), general = listOf("CRANE", "SLATE", "CRANES"))
        )

        val state = viewModel.uiState.value
        assertEquals(DictionaryTab.WORDLE, state.selectedTab)
        assertEquals(listOf("CRANE", "SLATE"), state.words)
    }

    @Test
    fun switchingTabLoadsGeneralDictionary() = runTest {
        val viewModel = DictionariesViewModel(
            FakeWordRepository(wordle = listOf("CRANE"), general = listOf("CRANE", "CRANES"))
        )

        viewModel.onTabSelected(DictionaryTab.GENERAL)

        val state = viewModel.uiState.value
        assertEquals(DictionaryTab.GENERAL, state.selectedTab)
        assertEquals(listOf("CRANE", "CRANES"), state.words)
    }
}
