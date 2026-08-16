package com.wordlesolver.android.ui.dictionaries

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordlesolver.android.ui.common.WordGrid
import com.wordlesolver.android.ui.theme.WordleColors
import com.wordlesolver.presentation.dictionaries.DictionariesViewModel
import com.wordlesolver.presentation.dictionaries.DictionaryTab

@Composable
fun DictionariesScreen(viewModelFactory: ViewModelProvider.Factory, darkMode: Boolean) {
    val viewModel: DictionariesViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        val selectedIndex = if (state.selectedTab == DictionaryTab.WORDLE) 0 else 1
        SecondaryTabRow(selectedTabIndex = selectedIndex, tabs = {
            Tab(
                selected = selectedIndex == 0,
                onClick = { viewModel.onTabSelected(DictionaryTab.WORDLE) },
                text = { Text("Wordle (2341)") }
            )
            Tab(
                selected = selectedIndex == 1,
                onClick = { viewModel.onTabSelected(DictionaryTab.GENERAL) },
                text = { Text("General (12930)") }
            )
        })

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            placeholder = { Text("Search...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
        state.errorMessage?.let { Text(it, modifier = Modifier.padding(16.dp)) }

        WordGrid(
            words = state.displayedWords,
            uppercase = true,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            backgroundColorFor = { _ ->
                if (darkMode) WordleColors.ResultDarkBackground else WordleColors.ResultBackground
            },
            wordColor = if (darkMode) WordleColors.White else WordleColors.Black,
            borderColor = if (darkMode) WordleColors.TitleDark else WordleColors.WordCardBorder
        )
    }
}