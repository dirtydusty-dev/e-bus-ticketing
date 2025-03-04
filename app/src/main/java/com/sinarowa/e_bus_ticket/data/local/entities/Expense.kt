package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sinarowa.e_bus_ticket.data.local.enums.SyncStatus
import com.sinarowa.e_bus_ticket.utils.TimeUtils.getFormattedTimestamp

@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(entity = Trip::class, parentColumns = ["tripId"], childColumns = ["expense_tripId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["expense_tripId"])]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "expense_tripId")val tripId: String,
    val expenseType: String, // e.g. "Fuel", "Food", "Repair"
    val description: String,
    val amount: Double,
    val creationTime: String,
    var syncStatus: SyncStatus = SyncStatus.PENDING
)
