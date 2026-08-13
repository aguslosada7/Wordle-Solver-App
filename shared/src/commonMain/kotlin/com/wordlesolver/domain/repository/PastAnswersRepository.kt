package com.wordlesolver.domain.repository

import com.wordlesolver.domain.model.PastAnswersState

/**
 * Port for reading and syncing past-wordle-answers.txt against the Wordle Hints API.
 */
interface PastAnswersRepository {
    /** Current on-disk state, without triggering a network sync. */
    suspend fun getPastAnswers(): PastAnswersState

    /**
     * Fetches any missing answers (from the last recorded update date up to yesterday),
     * appends them, and persists the updated file. If yesterday's answer is already
     * recorded, no network call is made and the current state is returned unchanged.
     */
    suspend fun syncPastAnswers(): PastAnswersState
}
