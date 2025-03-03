package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stops")
data class StationEntity(
    @PrimaryKey val stationId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)
