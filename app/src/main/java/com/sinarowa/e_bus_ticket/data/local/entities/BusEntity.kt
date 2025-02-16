package com.sinarowa.e_bus_ticket.data.local.entities
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buses")
data class BusEntity(
    @PrimaryKey val busId: String,
    val busName: String,
    val busNumber: String,
    val totalSeats: Int
)
