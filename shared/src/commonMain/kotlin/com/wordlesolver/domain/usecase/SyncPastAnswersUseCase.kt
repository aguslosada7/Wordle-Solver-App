package com.wordlesolver.domain.usecase

import com.wordlesolver.domain.model.PastAnswersState
import com.wordlesolver.domain.repository.PastAnswersRepository

/** Thin use case wrapper so presentation layers don't depend on the repository directly. */
class SyncPastAnswersUseCase(
    private val repository: PastAnswersRepository
) {
    suspend operator fun invoke(): PastAnswersState = repository.syncPastAnswers()
}
