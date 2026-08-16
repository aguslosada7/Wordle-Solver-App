package com.wordlesolver.android.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.wordlesolver.data.datasource.AndroidTextFileReader
import com.wordlesolver.data.remote.WordleHintsApiServiceImpl
import com.wordlesolver.data.remote.createHttpClient
import com.wordlesolver.data.repository.PastAnswersRepositoryImpl
import com.wordlesolver.data.repository.WordRepositoryImpl
import com.wordlesolver.domain.repository.PastAnswersRepository
import com.wordlesolver.domain.repository.WordRepository
import com.wordlesolver.domain.usecase.SyncPastAnswersUseCase
import com.wordlesolver.presentation.dictionaries.DictionariesViewModel
import com.wordlesolver.presentation.previousanswers.PreviousAnswersViewModel
import com.wordlesolver.presentation.solver.SolverViewModel

class DependencyContainer(context: Context) {

    private val textFileReader = AndroidTextFileReader(context.applicationContext)
    private val httpClient = createHttpClient()
    private val apiService = WordleHintsApiServiceImpl(httpClient)

    val wordRepository: WordRepository = WordRepositoryImpl(textFileReader)
    val pastAnswersRepository: PastAnswersRepository = PastAnswersRepositoryImpl(textFileReader, apiService)
    private val syncPastAnswersUseCase = SyncPastAnswersUseCase(pastAnswersRepository)

    /** A [ViewModelProvider.Factory] that knows how to build every screen's ViewModel. */
    val viewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T = when {
            modelClass.isAssignableFrom(SolverViewModel::class.java) ->
                SolverViewModel(wordRepository, pastAnswersRepository) as T
            modelClass.isAssignableFrom(PreviousAnswersViewModel::class.java) ->
                PreviousAnswersViewModel(syncPastAnswersUseCase) as T
            modelClass.isAssignableFrom(DictionariesViewModel::class.java) ->
                DictionariesViewModel(wordRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}
