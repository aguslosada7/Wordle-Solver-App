package com.wordlesolver.data.datasource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import java.io.File

/**
 * Android implementation of [TextFileReader].
 *
 * Read-only bundled files (wordle-dictionary.txt, general-dictionary.txt) are shipped as
 * assets and read directly from there.
 *
 * Mutable files (past-wordle-answers.txt) are first copied from assets into the app's
 * internal filesDir on first access, then read/written from there, since assets are
 * not writable at runtime.
 */
class AndroidTextFileReader(private val context: Context) : TextFileReader {

    override suspend fun readLines(fileName: String): List<String> = withContext(Dispatchers.IO) {
        readText(fileName).lineSequence().filter { it.isNotBlank() }.toList()
    }

    override suspend fun readText(fileName: String): String = withContext(Dispatchers.IO) {
        val internalFile = internalFile(fileName)
        if (internalFile.exists()) {
            internalFile.readText()
        } else {
            try {
                context.assets.open(fileName).bufferedReader().use { it.readText() }
            } catch (e: java.io.FileNotFoundException) {
                // Android's AssetManager throws FileNotFoundException with just the file
                // name as its message, which is confusing if it bubbles up to the UI as-is.
                throw java.io.IOException(
                    "Could not find bundled asset '$fileName'. Make sure Android assets " +
                            "are enabled/packaged for the shared module.",
                    e
                )
            }
        }
    }

    override suspend fun writeText(fileName: String, content: String) =
        withContext(Dispatchers.IO) {
            internalFile(fileName).writeText(content)
        }

    override suspend fun existsInWritableStorage(fileName: String): Boolean =
        withContext(Dispatchers.IO) {
            internalFile(fileName).exists()
        }

    private fun internalFile(fileName: String): File = File(context.filesDir, fileName)
}