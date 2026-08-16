package com.wordlesolver.android.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.wordlesolver.android.ui.dictionaries.DictionariesScreen
import com.wordlesolver.android.ui.previousanswers.PreviousAnswersScreen
import com.wordlesolver.android.ui.solver.SolverScreen

enum class WordleSolverDestination(val label: String) {
    SOLVER("Solver"),
    PREVIOUS_ANSWERS("Previous answers"),
    DICTIONARIES("Dictionaries")
}

@Composable
fun WordleSolverApp(
    viewModelFactory: ViewModelProvider.Factory,
    colorblindMode: Boolean,
    onColorblindModeChanged: (Boolean) -> Unit,
    darkMode: Boolean,
    onDarkModeChanged: (Boolean) -> Unit
) {
    var destination by remember { mutableStateOf(WordleSolverDestination.SOLVER) }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Column(modifier = Modifier.weight(1f)) {
            when (destination) {
                WordleSolverDestination.SOLVER -> SolverScreen(
                    viewModelFactory = viewModelFactory,
                    colorblindMode = colorblindMode,
                    onColorblindModeChanged = onColorblindModeChanged,
                    darkMode = darkMode,
                    onDarkModeChanged = onDarkModeChanged
                )
                WordleSolverDestination.PREVIOUS_ANSWERS -> PreviousAnswersScreen(viewModelFactory, darkMode)
                WordleSolverDestination.DICTIONARIES -> DictionariesScreen(viewModelFactory, darkMode)
            }
        }
        NavigationBar {
            WordleSolverDestination.entries.forEach { dest ->
                NavigationBarItem(
                    selected = destination == dest,
                    onClick = { destination = dest },
                    icon = {},
                    label = { Text(dest.label) }
                )
            }
        }
    }
}