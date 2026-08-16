package com.wordlesolver.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.wordlesolver.android.di.DependencyContainer
import com.wordlesolver.android.navigation.WordleSolverApp

class MainActivity : ComponentActivity() {

    private val dependencyContainer by lazy { DependencyContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings = dependencyContainer.appSettingsRepository

            var colorblindMode by remember { mutableStateOf(settings.colorblindMode) }
            var darkMode by remember { mutableStateOf(settings.darkMode) }

            MaterialTheme(colorScheme = if (darkMode) darkColorScheme() else lightColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WordleSolverApp(
                        viewModelFactory = dependencyContainer.viewModelFactory,
                        colorblindMode = colorblindMode,
                        onColorblindModeChanged = { enabled ->
                            colorblindMode = enabled
                            settings.colorblindMode = enabled
                        },
                        darkMode = darkMode,
                        onDarkModeChanged = { enabled ->
                            darkMode = enabled
                            settings.darkMode = enabled
                        }
                    )
                }
            }
        }
    }
}