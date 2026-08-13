package com.wordlesolver.domain.model

/**
 * In-memory representation of past-wordle-answers.txt:
 * - lastUpdateDate: "yy-mm-dd" string, the date the file was last synced up to
 * - answers: all past Wordle answers recorded so far, in chronological order
 */
data class PastAnswersState(
    val lastUpdateDate: String,
    val answers: List<String>
)
