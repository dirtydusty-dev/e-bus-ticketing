package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "station_coordinates")
data class StationCoordinates(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val station: String,
    val latitude: Double,
    val longitude: Double
)