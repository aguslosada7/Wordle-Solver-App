package com.wordlesolver.domain.usecase

import com.wordlesolver.domain.matcher.WordleGrader
import com.wordlesolver.domain.model.WordPattern
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

/**
 * Applies "pattern filtering" on top of an already basic-filtered word list.
 *
 * A [WordPattern] carries no letters of its own — only colors. So for each remaining
 * candidate word, that candidate itself is treated as the *target* (the presumed
 * answer), and every pattern's colors are checked against how many General-dictionary
 * words, used as a *guess*, would produce that exact color feedback against it (real
 * Wordle duplicate-letter grading, via [WordleGrader]).
 *
 * A candidate is kept only if, for every active pattern, that count is at least the
 * pattern's [WordPattern.expectedMatchCount] (a floor, not an exact target — "1" means
 * "at least 1").
 */
class FilterWordsByPatternsUseCase {

    operator fun invoke(
        candidateWords: List<String>,
        patterns: List<WordPattern>,
        generalDictionary: List<String>
    ): List<String> {
        // An "inactive" pattern (no box marked green/yellow yet) isn't a real clue from
        // the user yet — it must not filter anything out, otherwise the always-present
        // default pattern row would wipe every result before the user ever touches it.
        val activePatterns = patterns.filter { it.isActive }
        if (activePatterns.isEmpty() || candidateWords.isEmpty()) return candidateWords

        // Naively this is candidateWords.size * generalDictionary.size * patterns.size
        // full grade() computations. When basic filtering hasn't narrowed candidateWords
        // down yet, candidateWords IS the ~12930-word General dictionary, so that's
        // ~167 million+ comparisons run synchronously on whatever thread called this
        // (previously the Main thread) — that's the freeze. Three things fix it:
        //
        // 1. Normalize the General dictionary to CharArrays ONCE for the whole call
        //    (was already outside the candidate loop, but every comparison still paid
        //    for String.uppercase()/indexing inside WordleGrader.grade() itself).
        // 2. Skip the scan entirely for a pattern whose expected count is 0 ("at least
        //    0 matches" is always true) — the count field's spec default — and stop
        //    scanning a pattern's dictionary as soon as its expected count is reached,
        //    since we only need a floor, not the exact total.
        // 3. Split the candidate list across several coroutines on Dispatchers.Default
        //    so the work runs off the calling thread and across CPU cores instead of
        //    as one single-threaded loop.
        val generalChars = Array(generalDictionary.size) { generalDictionary[it].uppercase().toCharArray() }

        val workerCount = 8.coerceAtMost(candidateWords.size)
        val chunkSize = (candidateWords.size + workerCount - 1) / workerCount
        val chunks = candidateWords.chunked(chunkSize.coerceAtLeast(1))

        return runBlocking(Dispatchers.Default) {
            chunks
                .map { chunk ->
                    async {
                        // One scratch buffer per coroutine: WordleGrader.matches clears
                        // and reuses it instead of allocating a HashMap per comparison.
                        val scratch = IntArray(26)
                        chunk.filter { candidate ->
                            val target = candidate.uppercase().toCharArray()
                            activePatterns.all { pattern ->
                                val expected = pattern.expectedMatchCount
                                if (expected <= 0) {
                                    true
                                } else {
                                    var actualMatchCount = 0
                                    for (guess in generalChars) {
                                        if (WordleGrader.matches(guess, target, pattern.colors, scratch)) {
                                            actualMatchCount++
                                            if (actualMatchCount >= expected) break
                                        }
                                    }
                                    actualMatchCount >= expected
                                }
                            }
                        }
                    }
                }
                .flatMap { it.await() }
        }
    }
}