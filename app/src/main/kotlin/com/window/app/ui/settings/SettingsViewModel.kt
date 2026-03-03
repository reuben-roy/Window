package com.window.app.ui.settings

import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.window.app.data.ai.GeminiRepository
import com.window.app.worker.PruneEventsWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isAccessibilityEnabled: Boolean = false,
    val isUsageStatsGranted: Boolean    = false,
    val isGeminiAvailable: Boolean      = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        _uiState.update {
            SettingsUiState(
                isAccessibilityEnabled = checkAccessibilityEnabled(),
                isUsageStatsGranted    = checkUsageStatsGranted(),
                isGeminiAvailable      = geminiRepository.isModelAvailable()
            )
        }
    }

    fun pruneNow() {
        viewModelScope.launch {
            val request = OneTimeWorkRequestBuilder<PruneEventsWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }

    // -------------------------------------------------------------------------
    // Permission checks
    // -------------------------------------------------------------------------

    private fun checkAccessibilityEnabled(): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(context.packageName, ignoreCase = true)
    }

    private fun checkUsageStatsGranted(): Boolean {
        val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = ops.unsafeCheckOpNoThrow(
            OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == MODE_ALLOWED
    }
}

