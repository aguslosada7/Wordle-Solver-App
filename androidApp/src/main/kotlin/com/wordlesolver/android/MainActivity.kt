package com.wordlesolver.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.wordlesolver.android.di.DependencyContainer
import com.wordlesolver.android.navigation.WordleSolverApp

class MainActivity : ComponentActivity() {

    private val dependencyContainer by lazy { DependencyContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WordleSolverApp(viewModelFactory = dependencyContainer.viewModelFactory)
                }
            }
        }
    }
}
