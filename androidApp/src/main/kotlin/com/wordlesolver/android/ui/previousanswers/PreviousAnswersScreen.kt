package com.wordlesolver.android.ui.previousanswers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordlesolver.android.ui.common.WordGrid
import com.wordlesolver.android.ui.theme.WordleColors
import com.wordlesolver.presentation.previousanswers.PreviousAnswersViewModel

@Composable
fun PreviousAnswersScreen(viewModelFactory: ViewModelProvider.Factory, darkMode: Boolean) {
    val viewModel: PreviousAnswersViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when {
                state.isLoading -> {
                    // Spinning wheel while we sync with the WordleHints API.
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Syncing...", modifier = Modifier.padding(start = 8.dp))
                }
                state.errorMessage != null -> {
                    Text("⚠️")
                    Text(
                        text = "Sync failed: ${state.errorMessage}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                state.isUpToDateThroughYesterday -> {
                    Text("✅")
                    Text("Up to date", modifier = Modifier.padding(start = 8.dp))
                }
                else -> {
                    Text("⏳")
                    Text("Syncing...", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        if (state.lastUpdateDate.isNotBlank()) {
            Text(
                text = "Last update: ${state.lastUpdateDate}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            placeholder = { Text("Search...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        // The list of answers already loaded from disk stays visible regardless of
        // whether the most recent sync attempt succeeded or failed.
        WordGrid(
            words = state.displayedAnswers,
            uppercase = true,
            backgroundColorFor = { word ->
                if (word in state.duplicateAnswers)
                    if (darkMode) WordleColors.RepeatedAnswerBackgroundDark else WordleColors.RepeatedAnswerBackground
                else
                    if (darkMode) WordleColors.ResultDarkBackground else WordleColors.ResultBackground
            },
            modifier = Modifier.fillMaxSize().padding(top = 16.dp),
            wordColor = if (darkMode) WordleColors.White else WordleColors.Black,
            borderColor = if (darkMode) WordleColors.TitleDark else WordleColors.WordCardBorder
        )
    }
}