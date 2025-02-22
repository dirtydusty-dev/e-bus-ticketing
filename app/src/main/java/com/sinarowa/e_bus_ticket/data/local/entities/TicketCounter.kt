package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity

@Entity(tableName = "ticketcounter",primaryKeys = ["tripId"])
data class TicketCounter(
    val tripId: String,   // Unique ID for the trip
    val lastTicketNumber: Int
)
