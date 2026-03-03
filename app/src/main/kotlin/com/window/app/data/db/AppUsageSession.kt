package com.window.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a single foreground session for one app.
 *
 * A session starts when the app becomes foreground (typeWindowStateChanged) and
 * ends when a different app takes foreground or the device screen turns off.
 */
@Entity(
    tableName = "app_usage_sessions",
    indices = [
        Index(value = ["packageName"]),
        Index(value = ["date"])
    ]
)
data class AppUsageSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** e.g. "com.amazon.mShop.android.shopping" */
    val packageName: String,

    /** Human-readable label resolved from PackageManager, cached at insert time */
    val appLabel: String,

    /** Epoch millis when the app came to foreground */
    val startTime: Long,

    /** Epoch millis when the app left foreground; null while session is open */
    val endTime: Long? = null,

    /** Derived: endTime - startTime in milliseconds; 0 while session is open */
    val durationMs: Long = 0L,

    /** ISO-8601 date string "YYYY-MM-DD" for easy daily aggregation */
    val date: String
)

