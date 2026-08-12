package com.wordlesolver.domain.repository

/**
 * Port for accessing the word lists. Implemented in the data layer
 * (reading wordle-dictionary.txt / general-dictionary.txt).
 */
interface WordRepository {
    /** All words considered valid Wordle solutions (2340 words). */
    suspend fun getWordleDictionary(): List<String>

    /** All words in the broader dictionary (12930 words), superset of the Wordle dictionary. */
    suspend fun getGeneralDictionary(): List<String>
}
