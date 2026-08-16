package com.wordlesolver.android.ui.solver

import android.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordlesolver.android.ui.theme.WordleColors
import com.wordlesolver.domain.model.LetterState
import com.wordlesolver.presentation.solver.SolverResultWord
import com.wordlesolver.presentation.solver.SolverViewModel

@Composable
fun SolverScreen(
    viewModelFactory: ViewModelProvider.Factory,
    colorblindMode: Boolean,
    onColorblindModeChanged: (Boolean) -> Unit
) {
    val viewModel: SolverViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.uiState.collectAsState()

    // Settings visibility is fine to keep local: it's only relevant while this
    // screen is on-screen, unlike colorblindMode (see WordleSolverApp) which must
    // survive switching to other tabs and back.
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(
            colorblindMode = colorblindMode,
            onColorblindModeChanged = onColorblindModeChanged,
            onBack = { showSettings = false }
        )
        return
    }

    val correctColor = if (colorblindMode) WordleColors.ColorblindCorrect else WordleColors.Green
    val misplacedColor = if (colorblindMode) WordleColors.ColorblindMisplaced else WordleColors.Yellow

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // --- Screen title -------------------------------------------------------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wordle Solver",
                    fontSize = 33.sp,
                    fontWeight = FontWeight.Bold,
                    color = WordleColors.TitleDark
                )
                IconButton(onClick = { showSettings = true }) {
                    Text("⚙", fontSize = 33.sp, color = WordleColors.TitleDark)
                }
            }
        }

        // --- Correct letters ---------------------------------------------------
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("Correct letters")
                Box(
                    modifier = Modifier
                        .background(WordleColors.ResultBackground, RoundedCornerShape(10.dp))
                        .border(BorderStroke(1.dp, WordleColors.WordCardBorder), RoundedCornerShape(10.dp))
                        .clickable { viewModel.clearAll() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("🗑 Clear All", color = WordleColors.WordCardText)
                }
            }
        }
        item {
            LetterBoxRow(
                row = state.correctRow,
                boxColorFor = { correctColor },
                onLetterChanged = viewModel::onCorrectLetterChanged,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // --- Misplaced letters ---------------------------------------------------
        item { SectionTitle("Misplaced letters", modifier = Modifier.padding(top = 16.dp)) }
        items(state.misplacedRows.size) { rowIndex ->
            LetterBoxRow(
                row = state.misplacedRows[rowIndex],
                boxColorFor = { misplacedColor },
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
        item { SectionTitle("Patterns", modifier = Modifier.padding(top = 16.dp)) }
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
                            LetterState.GREEN -> correctColor
                            LetterState.YELLOW -> misplacedColor
                            else -> WordleColors.Gray
                        }
                    },
                    onBoxTapped = { position ->
                        viewModel.onPatternBoxColorCycled(patternIndex, position)
                    },
                    modifier = Modifier.weight(1f)
                )
                var countFieldValue by remember(patternIndex) {
                    mutableStateOf(TextFieldValue(pattern.expectedMatchCount.toString()))
                }
                // Keep the field in sync if the count changes from outside this field
                // (e.g. "Clear All"), without clobbering what the user is mid-typing.
                LaunchedEffect(pattern.expectedMatchCount) {
                    if (countFieldValue.text.toIntOrNull() != pattern.expectedMatchCount) {
                        countFieldValue = TextFieldValue(pattern.expectedMatchCount.toString())
                    }
                }
                OutlinedTextField(
                    value = countFieldValue,
                    onValueChange = { newValue ->
                        val digitsOnly = newValue.text.filter(Char::isDigit)
                        countFieldValue = newValue.copy(text = digitsOnly)
                        viewModel.onPatternExpectedCountChanged(patternIndex, digitsOnly.toIntOrNull() ?: 0)
                    },
                    modifier = Modifier
                        .width(72.dp)
                        .padding(start = 4.dp)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                countFieldValue = countFieldValue.copy(
                                    selection = TextRange(0, countFieldValue.text.length)
                                )
                            }
                        }
                )
                Column(
                    modifier = Modifier.padding(start = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .background(WordleColors.Gray, RoundedCornerShape(4.dp))
                            .clickable {
                                viewModel.onPatternExpectedCountChanged(patternIndex, pattern.expectedMatchCount + 1)
                            }
                    ) {
                        Text("+", color = WordleColors.White, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .background(WordleColors.Gray, RoundedCornerShape(4.dp))
                            .clickable {
                                val newCount = (pattern.expectedMatchCount - 1).coerceAtLeast(0)
                                viewModel.onPatternExpectedCountChanged(patternIndex, newCount)
                            }
                    ) {
                        Text("−", color = WordleColors.White, fontWeight = FontWeight.Bold)
                    }
                }
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
            SectionTitle("Letters NOT in the word", modifier = Modifier.padding(top = 16.dp))
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

        // --- Results count ----------------------------------------------------------
        if (!state.isLoading && state.errorMessage == null) {
            item {
                SectionTitle(
                    text = "We found ${state.results.size} words",
                    modifier = Modifier.padding(top = 16.dp)
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

/**
 * Section title used throughout this screen ("Correct letters", "Patterns", etc.).
 * Bold and larger than body text, using the same accent color as the switches and buttons
 * (MaterialTheme's primary color).
 */
@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 25.sp,
        fontWeight = FontWeight.Bold,
        color = WordleColors.TitleDark,
        modifier = modifier
    )
}

@Composable
private fun SettingsScreen(
    colorblindMode: Boolean,
    onColorblindModeChanged: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Text("◀", fontSize = 30.sp, color = WordleColors.TitleDark)
            }
            Text(
                text = "Settings ⚙",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = WordleColors.TitleDark,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .clickable { onColorblindModeChanged(!colorblindMode) }
        ) {
            Switch(checked = colorblindMode, onCheckedChange = onColorblindModeChanged)
            Text(
                "Colorblind mode",
                modifier = Modifier.padding(start = 8.dp).weight(1f)
            )
        }
    }
}