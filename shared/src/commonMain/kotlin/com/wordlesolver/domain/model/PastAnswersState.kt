package com.wordlesolver.domain.model

/**
 * In-memory representation of past-wordle-answers.txt:
 * - lastUpdateDate: "yy-mm-dd" string, the date the file was last synced up to
 * - answers: all past Wordle answers recorded so far, in chronological order
 */
data class PastAnswersState(
    val lastUpdateDate: String,
    val answers: List<String>,
    /**
     * Non-null when the last attempt to sync with the Wordle Hints API failed.
     * The locally persisted [answers] are still returned/kept in this case, so the
     * UI can keep showing whatever was already loaded from disk while surfacing the error.
     */
    val syncErrorMessage: String? = null
)
