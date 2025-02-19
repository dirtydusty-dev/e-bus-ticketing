package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinarowa.e_bus_ticket.data.local.entities.SyncQueue

@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(syncItem: SyncQueue)

    @Query("SELECT * FROM sync_queue WHERE isSynced = 0 ORDER BY createdAt ASC")
    suspend fun getPendingSyncItems(): List<SyncQueue>

    @Query("UPDATE sync_queue SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)

    @Query("DELETE FROM sync_queue WHERE isSynced = 1")
    suspend fun deleteSyncedItems()
}
