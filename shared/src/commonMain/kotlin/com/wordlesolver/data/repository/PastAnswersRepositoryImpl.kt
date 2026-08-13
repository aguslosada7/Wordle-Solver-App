package com.wordlesolver.data.repository

import com.wordlesolver.data.DictionaryFiles
import com.wordlesolver.data.PastAnswersDateFormat
import com.wordlesolver.data.datasource.TextFileReader
import com.wordlesolver.data.remote.WordleHintsApiService
import com.wordlesolver.domain.model.PastAnswersState
import com.wordlesolver.domain.repository.PastAnswersRepository

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
        val yesterday = PastAnswersDateFormat.yesterday()
        val lastUpdate = PastAnswersDateFormat.parseFileDate(current.lastUpdateDate)

        // Already up to date through yesterday: skip the network call entirely.
        // LocalDate is Comparable in kotlinx-datetime, so this compares chronologically.
        if (lastUpdate >= yesterday) return current

        val fetched = apiService.getAnswers(
            from = PastAnswersDateFormat.toApiDate(lastUpdate),
            to = PastAnswersDateFormat.toApiDate(yesterday)
        )

        // API returns newest-first (per the spec's example response); the file is
        // appended in chronological order, so sort ascending by date before appending.
        val newAnswersChronological = fetched
            .sortedBy { it.date }
            .map { it.answer.uppercase() }

        val updatedState = PastAnswersState(
            lastUpdateDate = PastAnswersDateFormat.toFileDate(PastAnswersDateFormat.today()),
            answers = current.answers + newAnswersChronological
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
        val lines = raw.lines()
        val dateLine = lines.getOrNull(0)?.trim().orEmpty()
        val answersLine = lines.getOrNull(1)?.trim().orEmpty()
        val answers = if (answersLine.isEmpty()) emptyList() else answersLine.split(" ")
        return PastAnswersState(lastUpdateDate = dateLine, answers = answers)
    }

    private suspend fun persist(state: PastAnswersState) {
        val content = "${state.lastUpdateDate}\n${state.answers.joinToString(" ")}"
        fileReader.writeText(DictionaryFiles.PAST_ANSWERS, content)
    }
}
