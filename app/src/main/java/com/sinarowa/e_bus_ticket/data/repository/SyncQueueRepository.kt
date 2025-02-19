package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.SyncQueueDao
import com.sinarowa.e_bus_ticket.data.local.entities.SyncQueue
import javax.inject.Inject

class SyncQueueRepository @Inject constructor(private val syncQueueDao: SyncQueueDao) {

    suspend fun addToQueue(type: String, jsonData: String) {
        val syncItem = SyncQueue(type = type, jsonData = jsonData)
        syncQueueDao.insert(syncItem)
    }

    suspend fun getPendingItems(): List<SyncQueue> = syncQueueDao.getPendingSyncItems()

    suspend fun markItemAsSynced(id: Int) {
        syncQueueDao.markAsSynced(id)
    }

    suspend fun cleanUpSyncedItems() {
        syncQueueDao.deleteSyncedItems()
    }
}
