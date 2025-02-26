package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sinarowa.e_bus_ticket.data.local.enums.TripStatus
import java.sql.Date

// Trip Entity
@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(entity = Route::class, parentColumns = ["id"], childColumns = ["routeId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Bus::class, parentColumns = ["id"], childColumns = ["busId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["routeId"]), Index(value = ["busId"])]
)
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeId: Long,
    val busId: Long,
    val startTime: String,
    val stopTime: String?,
    val status: TripStatus
)