package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sinarowa.e_bus_ticket.data.local.enums.TicketStatus

@Entity(
    tableName = "tickets",
    foreignKeys = [
        ForeignKey(entity = Trip::class, parentColumns = ["tripId"], childColumns = ["tripId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Price::class, parentColumns = ["priceId"], childColumns = ["priceId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["tripId"]), Index(value = ["priceId"])]
)
data class Ticket(
    @PrimaryKey val ticketId: String,
    val tripId: String,
    val priceId: String,
    val paymentCategory: String,  // e.g. "Adult", "Child", "Senior"
    val creationTime: String, // Stored as String for easy sync
    val amount: Double,
    val status: TicketStatus
)
