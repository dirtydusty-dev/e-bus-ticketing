package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val locationId: String,
    val cityName: String,
    val latitude: Double,
    val longitude: Double
)
