package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.TripSyncQueueDao
import com.sinarowa.e_bus_ticket.data.local.entities.TripSyncQueue
import javax.inject.Inject

class TripSyncQueueRepository @Inject constructor(
    private val tripSyncQueueDao: TripSyncQueueDao
) {

    // Insert a new trip sync queue
    suspend fun insertTripSyncQueue(tripSyncQueue: TripSyncQueue) {
        tripSyncQueueDao.insert(tripSyncQueue)
    }

    // Get all pending trip syncs
    suspend fun getPendingTrips(): List<TripSyncQueue> {
        return tripSyncQueueDao.getPendingTrips()
    }

    // Mark a specific trip as sent after successful sync
    suspend fun markTripAsSent(tripSyncQueueId: Long) {
        tripSyncQueueDao.markAsSent(tripSyncQueueId)
    }
}
