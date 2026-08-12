package com.wordlesolver.data.repository

import com.wordlesolver.data.DictionaryFiles
import com.wordlesolver.data.datasource.TextFileReader
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeTextFileReader(
    private val files: MutableMap<String, String> = mutableMapOf()
) : TextFileReader {
    var readCount: Int = 0
        private set

    override suspend fun readLines(fileName: String): List<String> {
        readCount++
        return files[fileName]?.lineSequence()?.filter { it.isNotBlank() }?.toList() ?: emptyList()
    }

    override suspend fun readText(fileName: String): String = files[fileName] ?: ""

    override suspend fun writeText(fileName: String, content: String) {
        files[fileName] = content
    }

    override suspend fun existsInWritableStorage(fileName: String): Boolean = files.containsKey(fileName)
}

class WordRepositoryImplTest {

    @Test
    fun loadsAndUppercasesWordleDictionary() = runTest {
        val fakeReader = FakeTextFileReader(
            mutableMapOf(DictionaryFiles.WORDLE_DICTIONARY to "crane\nslate\nghost")
        )
        val repository = WordRepositoryImpl(fakeReader)

        val result = repository.getWordleDictionary()

        assertEquals(listOf("CRANE", "SLATE", "GHOST"), result)
    }

    @Test
    fun cachesDictionaryAfterFirstRead() = runTest {
        val fakeReader = FakeTextFileReader(
            mutableMapOf(DictionaryFiles.GENERAL_DICTIONARY to "apple\nbread")
        )
        val repository = WordRepositoryImpl(fakeReader)

        repository.getGeneralDictionary()
        repository.getGeneralDictionary()
        repository.getGeneralDictionary()

        assertEquals(1, fakeReader.readCount)
    }
}
