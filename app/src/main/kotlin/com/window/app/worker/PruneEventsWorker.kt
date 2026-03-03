package com.window.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.window.app.data.db.WindowContentEventDao
import com.window.app.data.db.AppUsageSessionDao
import com.window.app.util.DateUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * PruneEventsWorker
 *
 * Runs once per day to delete WindowContentEvent and AppUsageSession rows
 * older than RETENTION_DAYS. This keeps the Room database lean without
 * manual user intervention.
 */
@HiltWorker
class PruneEventsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val eventDao: WindowContentEventDao,
    private val sessionDao: AppUsageSessionDao
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME     = "prune_events_worker"
        const val RETENTION_DAYS = 30
        private const val TAG   = "PruneEventsWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val cutoffDate  = DateUtil.daysAgoIso(RETENTION_DAYS)
            val cutoffEpoch = System.currentTimeMillis() - (RETENTION_DAYS.toLong() * 86_400_000L)

            val deletedEvents   = eventDao.deleteOlderThan(cutoffEpoch)
            val deletedSessions = sessionDao.deleteOlderThan(cutoffDate)

            Log.i(TAG, "Pruned $deletedEvents events and $deletedSessions sessions older than $cutoffDate")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Prune failed: ${e.message}")
            Result.retry()
        }
    }
}

