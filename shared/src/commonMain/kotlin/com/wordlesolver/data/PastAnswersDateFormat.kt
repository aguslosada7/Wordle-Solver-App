package com.wordlesolver.data

import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

/**
 * past-wordle-answers.txt stores dates as "yy-mm-dd".
 * The API expects/returns "yyyy-MM-dd". This object converts between the two and
 * resolves "today"/"yesterday" for the sync window.
 */
object PastAnswersDateFormat {

    fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    fun yesterday(): LocalDate = today().minus(DatePeriod(days = 1))

    /** "yy-mm-dd" (file format) -> LocalDate. Assumes 20xx for the 2-digit year. */
    fun parseFileDate(fileDate: String): LocalDate {
        val (yy, mm, dd) = fileDate.split("-").map { it.toInt() }
        return LocalDate(year = 2000 + yy, monthNumber = mm, dayOfMonth = dd)
    }

    /** LocalDate -> "yy-mm-dd" (file format). */
    fun toFileDate(date: LocalDate): String {
        val yy = (date.year % 100).toString().padStart(2, '0')
        val mm = date.monthNumber.toString().padStart(2, '0')
        val dd = date.dayOfMonth.toString().padStart(2, '0')
        return "$yy-$mm-$dd"
    }

    /** LocalDate -> "yyyy-MM-dd" (API format). */
    fun toApiDate(date: LocalDate): String = date.toString()
}
