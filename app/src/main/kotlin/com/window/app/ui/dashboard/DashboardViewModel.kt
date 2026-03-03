package com.window.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.window.app.data.db.AppDailyTotal
import com.window.app.data.db.AppUsageSessionDao
import com.window.app.data.db.WindowContentEventDao
import com.window.app.util.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val todayTotals: List<AppDailyTotal> = emptyList(),
    val totalScreenTimeMs: Long = 0L,
    val latestAiSummary: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sessionDao: AppUsageSessionDao,
    private val eventDao: WindowContentEventDao
) : ViewModel() {

    private val today = DateUtil.todayIso()

    val uiState: StateFlow<DashboardUiState> = sessionDao
        .observeDailyTotals(today)
        .map { totals ->
            val totalMs    = totals.sumOf { it.totalMs }
            val aiSummary  = eventDao
                .getRecentEvents(limit = 1)
                .firstOrNull()
                ?.aiSummary
            DashboardUiState(
                todayTotals      = totals,
                totalScreenTimeMs = totalMs,
                latestAiSummary  = aiSummary,
                isLoading        = false
            )
        }
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5_000),
            initialValue  = DashboardUiState()
        )
}

