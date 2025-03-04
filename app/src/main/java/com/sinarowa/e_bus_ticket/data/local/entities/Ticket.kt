package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sinarowa.e_bus_ticket.data.local.enums.SyncStatus
import com.sinarowa.e_bus_ticket.data.local.enums.TicketStatus

@Entity(
    tableName = "tickets",
    foreignKeys = [
        ForeignKey(entity = Trip::class, parentColumns = ["tripId"], childColumns = ["ticket_tripId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Price::class, parentColumns = ["priceId"], childColumns = ["ticket_priceId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["ticket_tripId"]), Index(value = ["ticket_priceId"])]
)
data class Ticket(
    @PrimaryKey val ticketId: String = "",
    @ColumnInfo(name = "ticket_tripId")val tripId: String = "",
    @ColumnInfo(name = "ticket_priceId")val priceId: String = "",
    val paymentCategory: String = "",  // e.g. "Adult", "Child", "Senior"
    val creationTime: String = "", // Stored as String for easy sync
    val amount: Double = 0.00,
    var status: TicketStatus = TicketStatus.VALID,
    var syncStatus: SyncStatus = SyncStatus.PENDING
)
