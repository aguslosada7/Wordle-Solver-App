package com.wordlesolver.android.ui.previousanswers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordlesolver.presentation.previousanswers.PreviousAnswersViewModel

@Composable
fun PreviousAnswersScreen(viewModelFactory: ViewModelProvider.Factory) {
    val viewModel: PreviousAnswersViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon signaling whether the list is up to date through yesterday.
            Text(if (state.isUpToDateThroughYesterday) "✅" else "⏳")
            Text(
                text = if (state.isUpToDateThroughYesterday) "Up to date" else "Syncing...",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
        state.errorMessage?.let { Text(it, modifier = Modifier.padding(top = 8.dp)) }

        LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            items(state.answers) { word ->
                Text(text = word, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            }
        }
    }
}
