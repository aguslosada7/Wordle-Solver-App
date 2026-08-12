package com.wordlesolver.data.datasource

/**
 * Platform-agnostic contract for reading a bundled text file as a list of lines.
 * Implemented per-platform (Android: AssetManager; iOS would use NSBundle, not needed here).
 */
interface TextFileReader {
    /** Reads [fileName] and returns its raw lines (no trimming/filtering applied). */
    suspend fun readLines(fileName: String): List<String>

    /** Reads [fileName] and returns its full raw text content. */
    suspend fun readText(fileName: String): String

    /** Overwrites [fileName] with [content]. Used for past-wordle-answers.txt updates. */
    suspend fun writeText(fileName: String, content: String)

    /** True if the file exists in writable storage (as opposed to only bundled assets). */
    suspend fun existsInWritableStorage(fileName: String): Boolean
}
