package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prices")
data class Price(
    @PrimaryKey val priceId: String,
    val startStationId: String,  // Linked to StopEntity
    val destinationStationId: String,  // Linked to StopEntity
    val amount: Double
)
