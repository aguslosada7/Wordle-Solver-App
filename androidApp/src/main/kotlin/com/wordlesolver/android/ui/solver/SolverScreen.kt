package com.wordlesolver.android.ui.solver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

    // A single scrollable LazyColumn hosts the whole screen (inputs + results), so the
    // user can always scroll up/down no matter how many rows are added, and the
    // Add/Remove Row buttons never get pushed off-screen.
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // --- Correct letters ---------------------------------------------------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Correct letters")
                TextButton(onClick = { viewModel.clearAll() }) {
                    Text("🗑 Clear All")
                }
            }
        }
        item {
            LetterBoxRow(
                row = state.correctRow,
                boxColorFor = { WordleColors.Green },
                onLetterChanged = viewModel::onCorrectLetterChanged,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // --- Misplaced letters ---------------------------------------------------
        item { Text("Misplaced letters", modifier = Modifier.padding(top = 16.dp)) }
        items(state.misplacedRows.size) { rowIndex ->
            LetterBoxRow(
                row = state.misplacedRows[rowIndex],
                boxColorFor = { WordleColors.Yellow },
                onLetterChanged = { position, letter ->
                    viewModel.onMisplacedLetterChanged(rowIndex, position, letter)
                },
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                Button(onClick = { viewModel.addMisplacedRow() }) { Text("Add Row") }
                Button(onClick = { viewModel.removeMisplacedRow() }) { Text("Remove Row") }
            }
        }

        // --- Patterns ---------------------------------------------------------------
        item { Text("Patterns", modifier = Modifier.padding(top = 16.dp)) }
        items(state.patterns.size) { patternIndex ->
            val pattern = state.patterns[patternIndex]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                PatternColorRow(
                    colors = pattern.colors,
                    boxColorFor = { boxState ->
                        when (boxState) {
                            LetterState.GREEN -> WordleColors.Green
                            LetterState.YELLOW -> WordleColors.Yellow
                            else -> WordleColors.Gray
                        }
                    },
                    onBoxTapped = { position ->
                        viewModel.onPatternBoxColorCycled(patternIndex, position)
                    },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = pattern.expectedMatchCount.toString(),
                    onValueChange = { text ->
                        // The field always starts at "0" (the default). Typing a new
                        // digit should replace that default, not get glued to it (e.g.
                        // typing "1" must produce 1, never "10"), so strip any leading
                        // zeros left over once there's more than one digit; an empty or
                        // all-zero result falls back to the "0" default.
                        val digitsOnly = text.filter(Char::isDigit)
                        val normalized = digitsOnly.trimStart('0')
                        val newCount = normalized.toIntOrNull() ?: 0
                        viewModel.onPatternExpectedCountChanged(patternIndex, newCount)
                    },
                    modifier = Modifier.width(72.dp).padding(start = 4.dp)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                Button(onClick = { viewModel.addPatternRow() }) { Text("Add Row") }
                Button(onClick = { viewModel.removePatternRow() }) { Text("Remove Row") }
            }
        }

        // --- Letters NOT in the word ---------------------------------------------
        item {
            Text("Letters NOT in the word", modifier = Modifier.padding(top = 16.dp))
            OutlinedTextField(
                value = state.excludedLettersInput,
                onValueChange = viewModel::onExcludedLettersChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .background(WordleColors.White)
            )
        }

        item {
            Button(onClick = { viewModel.submit() }, modifier = Modifier.padding(top = 16.dp)) {
                Text("Filter")
            }
        }

        // --- Switches -----------------------------------------------------------------
        // Each switch is paired with its label in the same Row so they can never drift
        // apart or overlap another switch, and the label wraps instead of being clipped.
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clickable { viewModel.onShowOnlyWordleWordsToggled(!state.showOnlyWordleWords) }
            ) {
                Switch(checked = state.showOnlyWordleWords, onCheckedChange = viewModel::onShowOnlyWordleWordsToggled)
                Text(
                    "Show only Wordle words",
                    modifier = Modifier.padding(start = 8.dp).weight(1f)
                )
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clickable { viewModel.onExcludePreviousAnswersToggled(!state.excludePreviousAnswers) }
            ) {
                Switch(checked = state.excludePreviousAnswers, onCheckedChange = viewModel::onExcludePreviousAnswersToggled)
                Text(
                    "Exclude previous answers",
                    modifier = Modifier.padding(start = 8.dp).weight(1f)
                )
            }
        }

        // --- Results --------------------------------------------------------------------
        if (state.isLoading) {
            item { CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp)) }
        }
        state.errorMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

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
                    .padding(top = 8.dp)
                    .background(background)
                    .clickable { viewModel.onResultWordSelected(result.word) }
                    .padding(12.dp)
            )
        }
    }

    state.relatedWordsModal?.let { modal ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissRelatedWordsModal() },
            confirmButton = {
                Button(onClick = { viewModel.dismissRelatedWordsModal() }) { Text("Close") }
            },
            title = { Text("Words related to ${modal.selectedWord}") },
            text = {
                Column(modifier = Modifier.heightIn(max = 400.dp)) {
                    LazyColumn {
                        modal.wordsByPattern.forEach { patternWords ->
                            item {
                                val emojis = patternWords.pattern.colors.joinToString("") { color ->
                                    when (color) {
                                        LetterState.GREEN -> "🟩"
                                        LetterState.YELLOW -> "🟨"
                                        else -> "⬜"
                                    }
                                }
                                Text(
                                    text = "$emojis (${patternWords.relatedWords.size} words)",
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            }
                            items(patternWords.relatedWords) { word ->
                                Text(text = word, modifier = Modifier.padding(start = 8.dp, top = 2.dp))
                            }
                        }
                    }
                }
            }
        )
    }
}