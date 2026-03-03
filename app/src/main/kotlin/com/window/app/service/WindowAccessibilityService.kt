package com.window.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import com.window.app.MainActivity
import com.window.app.R
import com.window.app.data.ai.GeminiRepository
import com.window.app.data.db.AppUsageSession
import com.window.app.data.db.AppUsageSessionDao
import com.window.app.data.db.WindowContentEvent
import com.window.app.data.db.WindowContentEventDao
import com.window.app.util.DateUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

/**
 * WindowAccessibilityService
 *
 * The primary monitoring engine for the Window app.
 *
 * Responsibilities:
 *  1. Track foreground app changes → write AppUsageSession rows.
 *  2. Scrape visible UI text on every meaningful event → write WindowContentEvent rows.
 *  3. Periodically trigger Gemini Nano inference on the latest event batch.
 *
 * Permission required: android.permission.BIND_ACCESSIBILITY_SERVICE
 * The user must enable this service in Settings > Accessibility > Window.
 */
@AndroidEntryPoint
class WindowAccessibilityService : AccessibilityService() {

    @Inject lateinit var sessionDao: AppUsageSessionDao
    @Inject lateinit var eventDao: WindowContentEventDao
    @Inject lateinit var geminiRepository: GeminiRepository

    // -------------------------------------------------------------------------
    // Coroutine scope — cancelled in onUnbind
    // -------------------------------------------------------------------------
    private val serviceJob   = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.IO)

    // -------------------------------------------------------------------------
    // Session tracking state
    // -------------------------------------------------------------------------
    private var currentPackage: String = ""
    private var currentSessionId: Long = -1L
    private val sessionStartTime = AtomicLong(0L)

    // AI inference debounce — run inference at most once per 30 seconds
    private var lastInferenceTime = 0L
    private val INFERENCE_INTERVAL_MS = 30_000L

    // Max nodes to scrape per event (prevents runaway traversal on complex UIs)
    private val MAX_NODES = 200

    companion object {
        private const val TAG              = "WindowA11yService"
        private const val NOTIF_CHANNEL_ID = "window_service_channel"
        private const val NOTIF_ID         = 1001
    }

    // =========================================================================
    // Lifecycle
    // =========================================================================

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "Service connected")

        // Ensure the service info is configured even if XML wasn't loaded
        serviceInfo = serviceInfo.apply {
            eventTypes = (
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_VIEW_CLICKED or
                AccessibilityEvent.TYPE_VIEW_FOCUSED
            )
            feedbackType          = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags                 = (
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
            )
            notificationTimeout   = 100L
        }

        startForegroundWithNotification()

        // Warm up Gemini Nano in the background
        serviceScope.launch { geminiRepository.warmUp() }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        val pkg = event.packageName?.toString() ?: return

        // Skip our own package to avoid feedback loops
        if (pkg == packageName) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> handleAppSwitch(pkg, event)
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_VIEW_FOCUSED         -> handleContentEvent(pkg, event)
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Service interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        closeCurrentSession()
        serviceJob.cancel()
        Log.i(TAG, "Service unbound — scope cancelled")
        return super.onUnbind(intent)
    }

    // =========================================================================
    // App-switch handling
    // =========================================================================

    private fun handleAppSwitch(newPackage: String, event: AccessibilityEvent) {
        if (newPackage == currentPackage) return

        val now = System.currentTimeMillis()

        // Close the previous session
        if (currentSessionId != -1L) {
            val start    = sessionStartTime.get()
            val duration = now - start
            serviceScope.launch {
                sessionDao.closeSession(
                    id         = currentSessionId,
                    endTime    = now,
                    durationMs = duration
                )
                Log.d(TAG, "Closed session $currentSessionId for $currentPackage (${duration}ms)")
            }
        }

        // Open a new session
        currentPackage = newPackage
        sessionStartTime.set(now)

        serviceScope.launch {
            val label  = resolveAppLabel(newPackage)
            val id     = sessionDao.insert(
                AppUsageSession(
                    packageName = newPackage,
                    appLabel    = label,
                    startTime   = now,
                    date        = DateUtil.todayIso()
                )
            )
            currentSessionId = id
            Log.d(TAG, "Opened session $id for $newPackage ($label)")
        }
    }

    // =========================================================================
    // Content scraping
    // =========================================================================

    private fun handleContentEvent(pkg: String, event: AccessibilityEvent) {
        val sessionId = currentSessionId
        if (sessionId == -1L) return

        val interactionType = when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED         -> "VIEW_CLICKED"
            AccessibilityEvent.TYPE_VIEW_FOCUSED         -> "VIEW_FOCUSED"
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> "CONTENT_CHANGED"
            else                                         -> "OTHER"
        }

        // Scrape the root node tree
        val rootNode   = rootInActiveWindow ?: return
        val textBuffer = mutableListOf<String>()
        scrapeNodeTree(rootNode, textBuffer, depth = 0)
        rootNode.recycle()

        if (textBuffer.isEmpty()) return

        val capturedText  = textBuffer.joinToString(separator = "|")
        val viewResourceId = event.source?.viewIdResourceName
        event.source?.recycle()

        val now = System.currentTimeMillis()

        serviceScope.launch {
            val eventId = eventDao.insert(
                WindowContentEvent(
                    sessionId      = sessionId,
                    packageName    = pkg,
                    capturedText   = capturedText,
                    interactionType = interactionType,
                    viewResourceId = viewResourceId,
                    timestamp      = now
                )
            )

            // Debounced AI inference
            if (now - lastInferenceTime > INFERENCE_INTERVAL_MS) {
                lastInferenceTime = now
                triggerInference(pkg, eventId)
            }
        }
    }

    /**
     * Recursively traverse the AccessibilityNodeInfo tree, collecting
     * visible text, content descriptions, and button labels.
     *
     * Stops at [MAX_NODES] to bound execution time on dense UIs.
     */
    private fun scrapeNodeTree(
        node: AccessibilityNodeInfo?,
        buffer: MutableList<String>,
        depth: Int
    ) {
        if (node == null || buffer.size >= MAX_NODES) return

        // Extract text content
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let { buffer.add(it.trim()) }
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }
            ?.let { buffer.add("[desc: ${it.trim()}]") }
        node.hintText?.toString()?.takeIf { it.isNotBlank() }
            ?.let { buffer.add("[hint: ${it.trim()}]") }

        // Recurse into children
        for (i in 0 until node.childCount) {
            scrapeNodeTree(node.getChild(i), buffer, depth + 1)
            if (buffer.size >= MAX_NODES) break
        }
    }

    // =========================================================================
    // Gemini Nano inference
    // =========================================================================

    private suspend fun triggerInference(packageName: String, latestEventId: Long) {
        val recentEvents = eventDao.getRecentEventsForPackage(packageName, limit = 50)
        if (recentEvents.isEmpty()) return

        val nodeTexts = recentEvents.map { it.capturedText }

        val summary = geminiRepository.summarizeActivity(
            recentNodeTexts = nodeTexts,
            packageName     = packageName
        ) ?: return

        // Attach the summary to the most recent event
        eventDao.updateSummary(id = latestEventId, summary = summary)
        Log.d(TAG, "AI summary for $packageName: $summary")
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private fun closeCurrentSession() {
        if (currentSessionId == -1L) return
        val now      = System.currentTimeMillis()
        val duration = now - sessionStartTime.get()
        val id       = currentSessionId
        // Use GlobalScope here because serviceScope is being cancelled
        GlobalScope.launch(Dispatchers.IO) {
            sessionDao.closeSession(id = id, endTime = now, durationMs = duration)
        }
    }

    private fun resolveAppLabel(packageName: String): String {
        return try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    // =========================================================================
    // Foreground notification (keeps service alive on Android 8+)
    // =========================================================================

    private fun startForegroundWithNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(NOTIF_CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                NOTIF_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_MIN
            ).apply { description = getString(R.string.notification_channel_desc) }
            manager.createNotificationChannel(channel)
        }

        val tapIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setContentIntent(tapIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        startForeground(NOTIF_ID, notification)
    }
}

