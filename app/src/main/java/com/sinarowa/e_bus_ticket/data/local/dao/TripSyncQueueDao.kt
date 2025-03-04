package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinarowa.e_bus_ticket.data.local.entities.TripSyncQueue

@Dao
interface TripSyncQueueDao {

    // Insert a new trip sync queue item, replace if exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tripSyncQueue: TripSyncQueue)

    // Insert multiple trip sync queue items at once
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tripSyncQueues: List<TripSyncQueue>)

    // Get all the queued trips with pending status
    @Query("SELECT * FROM tripsyncqueue WHERE status = 'PENDING'")
    suspend fun getPendingTrips(): List<TripSyncQueue>

    // Mark a specific trip sync item as 'SENT'
    @Query("UPDATE tripsyncqueue SET status = 'SENT' WHERE tripSyncQueueId = :tripSyncQueueId")
    suspend fun markAsSent(tripSyncQueueId: Long)
}
