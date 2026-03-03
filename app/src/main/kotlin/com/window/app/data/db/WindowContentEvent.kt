package com.window.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single scraped UI event captured by the AccessibilityService.
 *
 * Each row corresponds to one accessibility event (window content change,
 * view click, etc.) and stores the extracted text tree as a single
 * pipe-delimited string for compact storage.
 */
@Entity(
    tableName = "window_content_events",
    foreignKeys = [
        ForeignKey(
            entity        = AppUsageSession::class,
            parentColumns = ["id"],
            childColumns  = ["sessionId"],
            onDelete      = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["packageName"]),
        Index(value = ["timestamp"])
    ]
)
data class WindowContentEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** FK → AppUsageSession.id */
    val sessionId: Long,

    /** e.g. "com.amazon.mShop.android.shopping" */
    val packageName: String,

    /**
     * Pipe-delimited visible text extracted from the node tree.
     * e.g. "Search|Buy Now|Add to Cart|Price: $19.99"
     */
    val capturedText: String,

    /**
     * Describes the interaction type:
     * "WINDOW_CHANGED" | "CONTENT_CHANGED" | "VIEW_CLICKED" | "VIEW_FOCUSED"
     */
    val interactionType: String,

    /** Resource ID of the focused/clicked view, if available */
    val viewResourceId: String? = null,

    /** Epoch millis */
    val timestamp: Long,

    /**
     * Optional Gemini Nano one-sentence summary — populated asynchronously
     * by GeminiRepository after a batch of events has been collected.
     */
    val aiSummary: String? = null
)

