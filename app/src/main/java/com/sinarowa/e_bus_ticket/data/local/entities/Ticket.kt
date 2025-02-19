package com.sinarowa.e_bus_ticket.data.local.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sinarowa.e_bus_ticket.utils.TimeUtils.getFormattedTimestamp

@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey val ticketId: String,
    val tripId: String,
    //val seatNumber: Int,
    val fromStop: String,
    val toStop: String,
    val price: Double,
    val ticketType: String,
    val luggage: String? = null,
    val creationTime: String = getFormattedTimestamp(),
    val isCancelled: Int = 0, // 0 = Not Cancelled, 1 = Cancelled
    val cancelReason: String? = null,
    val cancelTime: Long? = null
)

