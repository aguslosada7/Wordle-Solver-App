package com.wordlesolver.data.repository

import com.wordlesolver.data.DictionaryFiles
import com.wordlesolver.data.PastAnswersDateFormat
import com.wordlesolver.data.datasource.TextFileReader
import com.wordlesolver.data.remote.WordleHintsApiService
import com.wordlesolver.data.remote.dto.WordleAnswerResultDto
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeTextFileReader1(
    private val files: MutableMap<String, String> = mutableMapOf()
) : TextFileReader {
    override suspend fun readLines(fileName: String): List<String> =
        files[fileName]?.lineSequence()?.filter { it.isNotBlank() }?.toList() ?: emptyList()

    override suspend fun readText(fileName: String): String = files[fileName] ?: ""

    override suspend fun writeText(fileName: String, content: String) {
        files[fileName] = content
    }

    override suspend fun existsInWritableStorage(fileName: String): Boolean = files.containsKey(fileName)
}

private class FakeWordleHintsApiService(
    private val response: List<WordleAnswerResultDto>
) : WordleHintsApiService {
    var callCount = 0
        private set

    override suspend fun getAnswers(from: String, to: String): List<WordleAnswerResultDto> {
        callCount++
        return response
    }
}

class PastAnswersRepositoryImplTest {

    @Test
    fun syncAppendsNewAnswersChronologicallyAndUpdatesDate() = runTest {
        val yesterday = PastAnswersDateFormat.yesterday()
        val twoDaysAgo = PastAnswersDateFormat.toApiDate(
            kotlinx.datetime.LocalDate(yesterday.year, yesterday.monthNumber, yesterday.dayOfMonth)
        )
        val fakeReader = FakeTextFileReader1(
            mutableMapOf(DictionaryFiles.PAST_ANSWERS to "00-01-01\nOLDWORD")
        )
        // API returns newest-first, as documented in the spec's example response.
        val fakeApi = FakeWordleHintsApiService(
            listOf(
                WordleAnswerResultDto(2, PastAnswersDateFormat.toApiDate(yesterday), "day", "editor", "NEWER", 3.0),
                WordleAnswerResultDto(1, twoDaysAgo, "day", "editor", "OLDER", 3.0)
            )
        )
        val repository = PastAnswersRepositoryImpl(fakeReader, fakeApi)

        val result = repository.syncPastAnswers()

        assertEquals(listOf("OLDWORD", "OLDER", "NEWER"), result.answers)
        assertEquals(PastAnswersDateFormat.toFileDate(PastAnswersDateFormat.today()), result.lastUpdateDate)
        assertEquals(1, fakeApi.callCount)
    }

    @Test
    fun syncSkipsApiCallWhenAlreadyUpToDate() = runTest {
        // lastUpdateDate stores the date the sync last RAN (one day ahead of the last
        // date its answers actually cover), so "already up to date" means the stored
        // date is today, not yesterday.
        val today = PastAnswersDateFormat.toFileDate(PastAnswersDateFormat.today())
        val fakeReader = FakeTextFileReader1(
            mutableMapOf(DictionaryFiles.PAST_ANSWERS to "$today\nWORD1 WORD2")
        )
        val fakeApi = FakeWordleHintsApiService(emptyList())
        val repository = PastAnswersRepositoryImpl(fakeReader, fakeApi)

        val result = repository.syncPastAnswers()

        assertEquals(listOf("WORD1", "WORD2"), result.answers)
        assertEquals(0, fakeApi.callCount)
        assertTrue(fakeReader.existsInWritableStorage(DictionaryFiles.PAST_ANSWERS))
    }

    @Test
    fun syncFetchesYesterdaysAnswerWhenLastUpdateDateIsYesterday() = runTest {
        // Regression test: a file last synced yesterday (i.e. its answers only cover
        // through the day before yesterday) must still fetch yesterday's word today,
        // rather than being mistaken for "up to date".
        val yesterday = PastAnswersDateFormat.yesterday()
        val fakeReader = FakeTextFileReader1(
            mutableMapOf(DictionaryFiles.PAST_ANSWERS to "${PastAnswersDateFormat.toFileDate(yesterday)}\nWORD1")
        )
        val fakeApi = FakeWordleHintsApiService(
            listOf(WordleAnswerResultDto(1, PastAnswersDateFormat.toApiDate(yesterday), "day", "editor", "NEWER", 3.0))
        )
        val repository = PastAnswersRepositoryImpl(fakeReader, fakeApi)

        val result = repository.syncPastAnswers()

        assertEquals(1, fakeApi.callCount)
        assertEquals(listOf("WORD1", "NEWER"), result.answers)
        assertEquals(PastAnswersDateFormat.toFileDate(PastAnswersDateFormat.today()), result.lastUpdateDate)
    }
}