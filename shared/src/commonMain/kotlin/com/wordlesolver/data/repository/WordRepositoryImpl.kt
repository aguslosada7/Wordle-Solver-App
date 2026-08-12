package com.wordlesolver.data.repository

import com.wordlesolver.data.DictionaryFiles
import com.wordlesolver.data.datasource.TextFileReader
import com.wordlesolver.domain.repository.WordRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * [WordRepository] implementation backed by [TextFileReader].
 * Dictionaries are read once and cached in memory (they never change at runtime),
 * guarded by a mutex to avoid duplicate concurrent reads on first access.
 */
class WordRepositoryImpl(
    private val fileReader: TextFileReader
) : WordRepository {

    private val mutex = Mutex()
    private var wordleDictionaryCache: List<String>? = null
    private var generalDictionaryCache: List<String>? = null

    override suspend fun getWordleDictionary(): List<String> = mutex.withLock {
        wordleDictionaryCache ?: loadDictionary(DictionaryFiles.WORDLE_DICTIONARY).also {
            wordleDictionaryCache = it
        }
    }

    override suspend fun getGeneralDictionary(): List<String> = mutex.withLock {
        generalDictionaryCache ?: loadDictionary(DictionaryFiles.GENERAL_DICTIONARY).also {
            generalDictionaryCache = it
        }
    }

    private suspend fun loadDictionary(fileName: String): List<String> =
        fileReader.readLines(fileName)
            .map { it.trim().uppercase() }
            .filter { it.isNotEmpty() }
}
