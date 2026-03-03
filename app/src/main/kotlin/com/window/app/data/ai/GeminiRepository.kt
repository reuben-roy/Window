package com.window.app.data.ai

import android.content.Context
import android.util.Log
// TODO: Re-enable when correct Google AI Edge library version is available
// import com.google.ai.edge.localai.GenerativeModel
// import com.google.ai.edge.localai.GenerativeModelFutures
// import com.google.ai.edge.localai.common.GenerateContentResponse
// import com.google.ai.edge.localai.common.content
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GeminiRepository
 *
 * Manages all interactions with the on-device Gemini Nano model via the
 * Google AI Edge LocalAI SDK. All inference runs on-device — no network
 * calls are made.
 *
 * Model availability: Gemini Nano is bundled by AICore on Pixel 8+ / Android 14+.
 * On unsupported devices [isModelAvailable] returns false and callers should
 * degrade gracefully (show a "Not supported on this device" message).
 */
@Singleton
class GeminiRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "GeminiRepository"
        private const val MODEL_NAME = "gemini-nano"
        private const val MAX_OUTPUT_TOKENS = 128
    }

    // Lazy so we don't crash on devices where AICore is absent
    // TODO: Re-enable when Google AI Edge library is available
    private val model: Any? by lazy {
        Log.w(TAG, "Gemini Nano library not available - AI features disabled")
        null
        /*
        runCatching {
            GenerativeModel(
                modelName = MODEL_NAME,
                context   = context
            )
        }.onFailure { e ->
            Log.e(TAG, "Gemini Nano not available on this device: ${e.message}")
        }.getOrNull()
        */
    }

    /**
     * Returns true if Gemini Nano is available and ready on this device.
     * Call this before showing AI-driven UI to decide whether to render or hide it.
     */
    fun isModelAvailable(): Boolean = model != null

    /**
     * Generate a one-sentence behavioural summary from a list of scraped UI text strings.
     *
     * @param recentNodeTexts  Up to 100 pipe-delimited captured-text entries from Room,
     *                         most-recent first.
     * @param packageName      The currently active package (for context in the prompt).
     * @return                 A single sentence describing the user's current focus,
     *                         or null if inference failed / model unavailable.
     */
    suspend fun summarizeActivity(
        recentNodeTexts: List<String>,
        packageName: String
    ): String? = withContext(Dispatchers.Default) {
        // TODO: Re-enable when Google AI Edge library is available
        Log.w(TAG, "summarizeActivity called but AI library not available")
        return@withContext null

        /*
        val localModel = model ?: run {
            Log.w(TAG, "summarizeActivity called but model is unavailable")
            return@withContext null
        }

        val contextBlock = recentNodeTexts
            .take(100)
            .joinToString(separator = "\n") { "- $it" }

        val prompt = """
            You are a behaviour-summarisation assistant running entirely on-device.
            The user is currently using the app: $packageName.

            Below is a chronological list of UI text and interactions captured from the screen
            (most-recent first).  Each line represents one screen-event.

            $contextBlock

            Based only on the information above, write a single sentence (≤ 20 words) that
            describes what the user is currently focused on.  Be specific.
            Do NOT include app names, package names, or any personally identifiable information.
            Respond with only the summary sentence — no preamble, no punctuation beyond a period.
        """.trimIndent()

        return@withContext runCatching {
            val response: GenerateContentResponse = localModel.generateContent(
                content { text(prompt) }
            )
            response.text?.trim()
        }.onFailure { e ->
            Log.e(TAG, "Inference failed: ${e.message}")
        }.getOrNull()
        */
    }

    /**
     * Warm up the model so the first real inference call is fast.
     * Call from Application.onCreate() or the first Compose screen.
     */
    suspend fun warmUp() = withContext(Dispatchers.Default) {
        // TODO: Re-enable when Google AI Edge library is available
        Log.w(TAG, "warmUp called but AI library not available")
        /*
        model ?: return@withContext
        runCatching {
            model?.generateContent(content { text("Hello") })
        }.onFailure { Log.w(TAG, "Warm-up failed: ${it.message}") }
        */
    }
}

