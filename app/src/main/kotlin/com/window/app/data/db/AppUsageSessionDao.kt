package com.window.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageSessionDao {

    // -------------------------------------------------------------------------
    // Writes
    // -------------------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: AppUsageSession): Long

    @Update
    suspend fun update(session: AppUsageSession)

    /** Close the open session: set endTime, durationMs */
    @Query("""
        UPDATE app_usage_sessions
        SET    endTime    = :endTime,
               durationMs = :durationMs
        WHERE  id         = :id
    """)
    suspend fun closeSession(id: Long, endTime: Long, durationMs: Long)

    // -------------------------------------------------------------------------
    // Reads — reactive (Flow)
    // -------------------------------------------------------------------------

    /** Observe all sessions for a given date, newest first */
    @Query("SELECT * FROM app_usage_sessions WHERE date = :date ORDER BY startTime DESC")
    fun observeSessionsByDate(date: String): Flow<List<AppUsageSession>>

    /** Aggregate total time (ms) per app for a date — for dashboard pie chart */
    @Query("""
        SELECT packageName, appLabel, SUM(durationMs) AS totalMs
        FROM   app_usage_sessions
        WHERE  date = :date
        GROUP  BY packageName
        ORDER  BY totalMs DESC
    """)
    fun observeDailyTotals(date: String): Flow<List<AppDailyTotal>>

    // -------------------------------------------------------------------------
    // Reads — one-shot
    // -------------------------------------------------------------------------

    @Query("SELECT * FROM app_usage_sessions WHERE id = :id")
    suspend fun getById(id: Long): AppUsageSession?

    /** Total screen time (ms) across all apps for a date */
    @Query("SELECT COALESCE(SUM(durationMs), 0) FROM app_usage_sessions WHERE date = :date")
    suspend fun getTotalDurationForDate(date: String): Long

    // -------------------------------------------------------------------------
    // Maintenance
    // -------------------------------------------------------------------------

    @Query("DELETE FROM app_usage_sessions WHERE date < :beforeDate")
    suspend fun deleteOlderThan(beforeDate: String): Int
}

/** Lightweight projection for daily-total queries */
data class AppDailyTotal(
    val packageName: String,
    val appLabel: String,
    val totalMs: Long
)

