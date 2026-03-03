package com.window.app.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtil {
    private val ISO_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    /** Returns today's date as "YYYY-MM-DD" */
    fun todayIso(): String = ISO_FORMAT.format(Date())

    /** Returns the ISO date string for [daysAgo] days before today */
    fun daysAgoIso(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return ISO_FORMAT.format(cal.time)
    }

    /**
     * Formats a duration in milliseconds to a human-readable string.
     * e.g. 3_780_000ms → "1h 3m"
     */
    fun formatDuration(durationMs: Long): String {
        val hours   = TimeUnit.MILLISECONDS.toHours(durationMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
        return when {
            hours   > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else        -> "${seconds}s"
        }
    }
}

