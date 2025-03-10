package com.sinarowa.e_bus_ticket.worker

import android.content.Context
import androidx.work.*
import androidx.work.WorkInfo.State
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkerScheduler @Inject constructor(
    private val workManager: WorkManager
) {

    private val WORK_NAME = "SyncTripWorker"  // unique name for this work

    /** Schedule the SyncTripWorker to run every 5 minutes (if not already running). */
    fun scheduleSyncTripWorker(context: Context) {
        val syncRequest = PeriodicWorkRequestBuilder<SyncTripWorker>(15, TimeUnit.MINUTES)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES) // Exponential backoff with 10-minute delay
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // Only run when connected to the network
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep the existing worker if it's already scheduled
            syncRequest
        )
        Timber.d("Scheduled SyncTripWorker (every 15 minutes).")

        // (Optional) Observe worker state changes for debugging/logging
        workManager.getWorkInfosForUniqueWorkLiveData(WORK_NAME).observeForever { workInfoList ->
            // There should be at most one WorkInfo for this unique work (the latest instance)
            val info = workInfoList.firstOrNull()
            if (info != null) {
                when (info.state) {
                    State.ENQUEUED -> Timber.d("SyncTripWorker is enqueued (awaiting execution or next interval).")
                    State.RUNNING -> Timber.d("SyncTripWorker is currently running.")
                    else -> Timber.d("SyncTripWorker finished with state: ${info.state}.")
                }
            }
        }
    }


    private fun scheduleServiceMonitor(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<LocationServiceMonitorWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "LocationServiceMonitor",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

}
