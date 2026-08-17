package com.wordlesolver.data.repository

import com.wordlesolver.data.DictionaryFiles
import com.wordlesolver.data.PastAnswersDateFormat
import com.wordlesolver.data.datasource.TextFileReader
import com.wordlesolver.data.remote.WordleHintsApiService
import com.wordlesolver.domain.model.PastAnswersState
import com.wordlesolver.domain.repository.PastAnswersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [PastAnswersRepository] implementation backed by [TextFileReader] (for persistence) and
 * [WordleHintsApiService] (for fetching new answers).
 *
 * File format (past-wordle-answers.txt):
 *   line 1: "yy-mm-dd" last-update date
 *   line 2: all past answers, space-separated
 */
class PastAnswersRepositoryImpl(
    private val fileReader: TextFileReader,
    private val apiService: WordleHintsApiService
) : PastAnswersRepository {

    override suspend fun getPastAnswers(): PastAnswersState = readState()

    override suspend fun syncPastAnswers(): PastAnswersState {
        val current = readState()
        val today = PastAnswersDateFormat.today()
        val yesterday = PastAnswersDateFormat.yesterday()
        val lastUpdate = PastAnswersDateFormat.parseFileDate(current.lastUpdateDate)

        // lastUpdateDate stores the date the sync last RAN, not the date its answers go
        // up through: a sync that runs on day D only fetches through day D-1 (today's
        // word is never fetched, so it doesn't spoil the puzzle), but still stamps the
        // file with D. So "already up to date" means we already ran a sync today (or
        // later), i.e. lastUpdate >= today -- comparing against yesterday here would
        // incorrectly skip fetching yesterday's word every time this runs on a new day.
        // LocalDate is Comparable in kotlinx-datetime, so this compares chronologically.
        if (lastUpdate >= today) return current

        val fetched = try {
            apiService.getAnswers(
                from = PastAnswersDateFormat.toApiDate(lastUpdate),
                to = PastAnswersDateFormat.toApiDate(yesterday)
            )
        } catch (e: Exception) {
            // Network/API failure: keep whatever was already persisted on disk visible
            // to the user instead of throwing it away, but surface that the sync failed.
            return current.copy(
                syncErrorMessage = "Could not sync with WordleHints: ${e.message ?: "unknown error"}"
            )
        }

        // API returns newest-first (per the spec's example response); the file is
        // appended in chronological order, so sort ascending by date before appending.
        val newAnswersChronological = withContext(Dispatchers.Default) {
            fetched
                .sortedBy { it.date }
                .map { it.answer.uppercase() }
        }

        val updatedState = PastAnswersState(
            lastUpdateDate = PastAnswersDateFormat.toFileDate(today),
            answers = current.answers + newAnswersChronological,
            syncErrorMessage = null
        )

        persist(updatedState)
        return updatedState
    }

    private suspend fun readState(): PastAnswersState {
        val raw = fileReader.readText(DictionaryFiles.PAST_ANSWERS)
        if (raw.isBlank()) {
            // No file yet anywhere (fresh install, no bundled seed asset): start empty,
            // dated far enough in the past that the next sync fetches full history.
            return PastAnswersState(lastUpdateDate = "00-01-01", answers = emptyList())
        }
        return withContext(Dispatchers.Default) {
            val lines = raw.lines()
            val dateLine = lines.getOrNull(0)?.trim().orEmpty()
            val answersLine = lines.getOrNull(1)?.trim().orEmpty()
            val answers = if (answersLine.isEmpty()) emptyList() else answersLine.split(" ")
            PastAnswersState(lastUpdateDate = dateLine, answers = answers)
        }
    }

    private suspend fun persist(state: PastAnswersState) {
        val content = "${state.lastUpdateDate}\n${state.answers.joinToString(" ")}"
        fileReader.writeText(DictionaryFiles.PAST_ANSWERS, content)
    }
}