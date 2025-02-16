package com.sinarowa.e_bus_ticket.data.local.entities
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey val ticketId: String,
    val tripId: String,
    //val seatNumber: Int,
    val fromStop: String,
    val toStop: String,
    val price: Double,
    val timestamp: Long
)
