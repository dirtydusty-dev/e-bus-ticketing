package com.sinarowa.e_bus_ticket.data.local.entities
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sinarowa.e_bus_ticket.data.local.enums.TicketStatus

@Entity(
    tableName = "tickets",
    foreignKeys = [
        ForeignKey(entity = Trip::class, parentColumns = ["id"], childColumns = ["tripId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["tripId"])]
)
data class Ticket(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val startStation: String,
    val stopStation: String,
    val type: String,  // e.g. "Adult", "Child", "Senior"
    val creationTime: String, // Stored as String for easy sync
    val amount: Double,
    val status: TicketStatus
)
