package com.sinarowa.e_bus_ticket.worker

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.sinarowa.e_bus_ticket.data.dto.CreateTripRequest
import com.sinarowa.e_bus_ticket.service.ApiService
import com.sinarowa.e_bus_ticket.data.local.dao.TripSyncQueueDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import javax.inject.Inject

@HiltWorker
class SyncTripWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: ApiService,
    private val tripSyncQueueDao: TripSyncQueueDao
) : CoroutineWorker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.M)
    override suspend fun doWork(): Result {
        Timber.d("SyncTripWorker: Starting sync work...")

        // Check network availability
        if (!isNetworkAvailable()) {
            Timber.w("No internet connection. Work will retry when network is available.")
            return Result.retry() // Retry when the network is available again
        }

        // Retrieve unsynced trips
        val unsyncedTrips = tripSyncQueueDao.getPendingTrips()
        if (unsyncedTrips.isEmpty()) {
            Timber.d("No unsynced trips found.")
            return Result.success() // No work to do
        }

        // Process each unsynced trip
        for (trip in unsyncedTrips) {
            try {
                val tripRequestJson = trip.tripRequestJson
                val syncResult = syncTripWithServer(tripRequestJson)

                if (syncResult) {
                    tripSyncQueueDao.markAsSent(trip.tripSyncQueueId)
                    Timber.d("Trip synced successfully.")
                } else {
                    Timber.e("Failed to sync trip: API error.")
                    // Leave the trip in the queue for retrying
                }
            } catch (e: Exception) {
                Timber.e("Error syncing trip: ${e.message}")
                // Leave the trip in the queue for retrying
            }
        }

        Timber.d("SyncTripWorker completed.")
        return Result.success() // Completion of work
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isNetworkAvailable(): Boolean {
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private suspend fun syncTripWithServer(tripRequestJson: String): Boolean {
        return try {
            val createTripRequest = Gson().fromJson(tripRequestJson, CreateTripRequest::class.java)
            val response = apiService.createTrip(createTripRequest)
            response.isSuccessful
        } catch (e: Exception) {
            Timber.e("Error during API sync: ${e.message}")
            false
        }
    }
}
