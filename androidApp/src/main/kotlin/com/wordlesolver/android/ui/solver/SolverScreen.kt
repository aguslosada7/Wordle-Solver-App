package com.wordlesolver.android.ui.solver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordlesolver.android.ui.theme.WordleColors
import com.wordlesolver.domain.model.LetterState
import com.wordlesolver.presentation.solver.SolverResultWord
import com.wordlesolver.presentation.solver.SolverViewModel

@Composable
fun SolverScreen(viewModelFactory: ViewModelProvider.Factory) {
    val viewModel: SolverViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // --- Correct letters ---------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Correct letters")
            IconButton(onClick = { viewModel.clearAll() }) {
                Text("🗑 Clear All")
            }
        }
        LetterBoxRow(
            row = state.correctRow,
            boxColorFor = { WordleColors.Green },
            onLetterChanged = viewModel::onCorrectLetterChanged
        )

        // --- Misplaced letters ---------------------------------------------------
        Text("Misplaced letters", modifier = Modifier.padding(top = 16.dp))
        state.misplacedRows.forEachIndexed { rowIndex, row ->
            LetterBoxRow(
                row = row,
                boxColorFor = { WordleColors.Yellow },
                onLetterChanged = { position, letter ->
                    viewModel.onMisplacedLetterChanged(rowIndex, position, letter)
                },
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            Button(onClick = { viewModel.addMisplacedRow() }) { Text("Add Row") }
            Button(onClick = { viewModel.removeMisplacedRow() }) { Text("Remove Row") }
        }

        // --- Patterns ---------------------------------------------------------------
        Text("Patterns", modifier = Modifier.padding(top = 16.dp))
        state.patterns.forEachIndexed { patternIndex, pattern ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                LetterBoxRow(
                    row = pattern.row,
                    boxColorFor = { boxState ->
                        when (boxState) {
                            LetterState.GREEN -> WordleColors.Green
                            LetterState.YELLOW -> WordleColors.Yellow
                            else -> WordleColors.Gray
                        }
                    },
                    onLetterChanged = { position, letter ->
                        viewModel.onPatternLetterChanged(patternIndex, position, letter)
                    },
                    onBoxTapped = { position ->
                        viewModel.onPatternBoxColorCycled(patternIndex, position)
                    }
                )
                OutlinedTextField(
                    value = pattern.expectedMatchCount.toString(),
                    onValueChange = { text ->
                        viewModel.onPatternExpectedCountChanged(patternIndex, text.toIntOrNull() ?: 0)
                    },
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            Button(onClick = { viewModel.addPatternRow() }) { Text("Add Row") }
            Button(onClick = { viewModel.removePatternRow() }) { Text("Remove Row") }
        }

        // --- Letters NOT in the word ---------------------------------------------
        Text("Letters NOT in the word", modifier = Modifier.padding(top = 16.dp))
        OutlinedTextField(
            value = state.excludedLettersInput,
            onValueChange = viewModel::onExcludedLettersChanged,
            modifier = Modifier
                .fillMaxWidth()
                .background(WordleColors.White)
        )

        Button(onClick = { viewModel.submit() }, modifier = Modifier.padding(top = 16.dp)) {
            Text("Filter")
        }

        // --- Switches -----------------------------------------------------------------
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
            Switch(checked = state.showOnlyWordleWords, onCheckedChange = viewModel::onShowOnlyWordleWordsToggled)
            Text("Show only Wordle words", modifier = Modifier.padding(start = 8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = state.excludePreviousAnswers, onCheckedChange = viewModel::onExcludePreviousAnswersToggled)
            Text("Exclude previous answers", modifier = Modifier.padding(start = 8.dp))
        }

        // --- Results --------------------------------------------------------------------
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
        state.errorMessage?.let { Text(it, modifier = Modifier.padding(top = 8.dp)) }

        LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            items(state.results) { result: SolverResultWord ->
                val background = if (!state.showOnlyWordleWords && result.isWordleWord) {
                    WordleColors.WordleWordBackground
                } else {
                    WordleColors.ResultBackground
                }
                Text(
                    text = result.word,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(background)
                        .padding(12.dp)
                )
            }
        }
    }
}
