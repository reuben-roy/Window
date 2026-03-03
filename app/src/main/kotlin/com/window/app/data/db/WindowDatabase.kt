package com.window.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * WindowDatabase — single Room database for the Window app.
 *
 * Version history:
 *   1 — initial schema (AppUsageSession, WindowContentEvent)
 */
@Database(
    entities = [
        AppUsageSession::class,
        WindowContentEvent::class
    ],
    version = 1,
    exportSchema = true          // schemas written to app/schemas/ for migration tracking
)
abstract class WindowDatabase : RoomDatabase() {
    abstract fun appUsageSessionDao(): AppUsageSessionDao
    abstract fun windowContentEventDao(): WindowContentEventDao

    companion object {
        const val DATABASE_NAME = "window_db"
    }
}

