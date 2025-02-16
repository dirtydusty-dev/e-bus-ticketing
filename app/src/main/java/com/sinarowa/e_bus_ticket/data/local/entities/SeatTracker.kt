package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seat_tracker")
data class SeatTracker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: String,
    val seatNumber: Int,
    val fromStop: String,
    val toStop: String
)
