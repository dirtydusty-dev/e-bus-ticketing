package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sinarowa.e_bus_ticket.utils.TimeUtils.getFormattedTimestamp

@Entity(tableName = "sync_queue")
data class SyncQueue(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "TICKET" or "EXPENSE"
    val jsonData: String, // Store data as JSON string
    val isSynced: Boolean = false, // Flag to check sync status
    val createdAt: String = getFormattedTimestamp() // Timestamp
)

