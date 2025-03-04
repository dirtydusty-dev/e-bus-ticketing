package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tripsyncqueue")
data class TripSyncQueue(
    @PrimaryKey(autoGenerate = true)
    val tripSyncQueueId: Long = 0,
    val tripRequestJson: String, // Store the CreateTripRequest as a JSON string
    val status: String // Keep track of whether the request is pending, successful, or failed
)
