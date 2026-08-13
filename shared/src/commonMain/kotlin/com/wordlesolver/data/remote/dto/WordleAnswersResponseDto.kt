package com.wordlesolver.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WordleAnswersResponseDto(
    val source: String,
    val version: String,
    val count: Int,
    val total: Int,
    val page: Int,
    @SerialName("per_page") val perPage: Int,
    @SerialName("has_more") val hasMore: Boolean,
    val results: List<WordleAnswerResultDto>
)

@Serializable
data class WordleAnswerResultDto(
    val game: Int,
    val date: String, // yyyy-MM-dd
    @SerialName("day_name") val dayName: String,
    val editor: String,
    val answer: String,
    val difficulty: Double
)
