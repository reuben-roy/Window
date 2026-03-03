package com.window.app

import android.app.Application
import androidx.work.*
import com.window.app.worker.PruneEventsWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

/**
 * WindowApplication
 *
 * @HiltAndroidApp triggers Hilt's code generation and initialises the
 * application-level dependency graph.
 *
 * Also schedules the nightly data-retention WorkManager task.
 */
@HiltAndroidApp
class WindowApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        schedulePruneWorker()
    }

    /**
     * Schedule a daily job to delete events older than 30 days.
     * Uses KEEP policy so a running job isn't replaced prematurely.
     */
    private fun schedulePruneWorker() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<PruneEventsWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            PruneEventsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}

