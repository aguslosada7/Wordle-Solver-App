package com.wordlesolver.data.remote

import com.wordlesolver.data.remote.dto.WordleAnswerResultDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/** Port for the wordlehintstoday.org answers endpoint. */
interface WordleHintsApiService {
    /**
     * Returns every past answer with date in [from, to] (inclusive), handling pagination
     * transparently. Dates are "yyyy-MM-dd" strings, matching the endpoint's format.
     */
    suspend fun getAnswers(from: String, to: String): List<WordleAnswerResultDto>
}

class WordleHintsApiServiceImpl(
    private val httpClient: HttpClient
) : WordleHintsApiService {

    override suspend fun getAnswers(from: String, to: String): List<WordleAnswerResultDto> {
        val allResults = mutableListOf<WordleAnswerResultDto>()
        var page = 1
        while (true) {
            val response = httpClient.get(ENDPOINT) {
                parameter("from", from)
                parameter("to", to)
                parameter("page", page)
            }.body<com.wordlesolver.data.remote.dto.WordleAnswersResponseDto>()

            allResults += response.results
            if (!response.hasMore) break
            page++
        }
        return allResults
    }

    private companion object {
        const val ENDPOINT = "https://wordlehintstoday.org/api/v1/wordle/answers"
    }
}
