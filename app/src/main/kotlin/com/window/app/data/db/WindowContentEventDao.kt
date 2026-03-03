package com.window.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WindowContentEventDao {

    // -------------------------------------------------------------------------
    // Writes
    // -------------------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: WindowContentEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<WindowContentEvent>)

    /** Patch the aiSummary column once Gemini Nano responds */
    @Query("UPDATE window_content_events SET aiSummary = :summary WHERE id = :id")
    suspend fun updateSummary(id: Long, summary: String)

    // -------------------------------------------------------------------------
    // Reads — for AI context injection
    // -------------------------------------------------------------------------

    /**
     * Retrieve the last [limit] events for a specific package.
     * Used to build the prompt context fed to Gemini Nano.
     */
    @Query("""
        SELECT * FROM window_content_events
        WHERE  packageName = :packageName
        ORDER  BY timestamp DESC
        LIMIT  :limit
    """)
    suspend fun getRecentEventsForPackage(packageName: String, limit: Int = 50): List<WindowContentEvent>

    /**
     * Retrieve the last [limit] events across ALL packages.
     * Used for a global "what is the user doing right now" inference call.
     */
    @Query("""
        SELECT * FROM window_content_events
        ORDER  BY timestamp DESC
        LIMIT  :limit
    """)
    suspend fun getRecentEvents(limit: Int = 100): List<WindowContentEvent>

    // -------------------------------------------------------------------------
    // Reads — reactive
    // -------------------------------------------------------------------------

    @Query("""
        SELECT * FROM window_content_events
        WHERE  sessionId = :sessionId
        ORDER  BY timestamp ASC
    """)
    fun observeEventsForSession(sessionId: Long): Flow<List<WindowContentEvent>>

    // -------------------------------------------------------------------------
    // Maintenance
    // -------------------------------------------------------------------------

    @Query("DELETE FROM window_content_events WHERE timestamp < :beforeEpochMs")
    suspend fun deleteOlderThan(beforeEpochMs: Long): Int

    @Query("SELECT COUNT(*) FROM window_content_events")
    suspend fun count(): Int
}

