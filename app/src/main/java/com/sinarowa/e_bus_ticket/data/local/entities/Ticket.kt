package com.sinarowa.e_bus_ticket.data.local.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sinarowa.e_bus_ticket.utils.TimeUtils.getFormattedTimestamp

@Entity(tableName = "tickets", primaryKeys = ["tripId", "ticketId"])
data class Ticket(
    val tripId: String,  // Primary Key (Part 1)
    val ticketId: Int, // Primary Key (Part 2)
    val fromStop: String,
    val toStop: String,
    val price: Double,
    val ticketType: String,
    val luggage: String? = null,
    val status: Int = 0,
    val creationTime: String = getFormattedTimestamp(),
    val isCancelled: Int = 0, // 0 = Not Cancelled, 1 = Cancelled
    val cancelReason: String? = null,
    val cancelTime: Long? = null
)
