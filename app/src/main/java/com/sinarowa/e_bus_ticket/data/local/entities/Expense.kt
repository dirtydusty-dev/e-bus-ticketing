package com.sinarowa.e_bus_ticket.data.local.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sinarowa.e_bus_ticket.utils.TimeUtils.getFormattedTimestamp

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val expenseId: String,
    val tripId: String,
    val type: String,
    val amount: Double,
    val description: String,
    val creationTime: String = getFormattedTimestamp(),
    val location: String
)
