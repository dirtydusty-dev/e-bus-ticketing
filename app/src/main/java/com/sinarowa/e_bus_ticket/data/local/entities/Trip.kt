package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sinarowa.e_bus_ticket.data.local.enums.TripStatus

@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(
            entity = RouteEntity::class,
            parentColumns = ["routeId"], // Parent column in RouteEntity
            childColumns = ["trip_routeId"], // Child column in Trip
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Bus::class,
            parentColumns = ["busId"], // Parent column in Bus
            childColumns = ["trip_busId"], // Child column in Trip
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["trip_routeId"]), Index(value = ["trip_busId"])]
)
data class Trip(
    @PrimaryKey val tripId: String,
    @ColumnInfo(name = "trip_routeId") val routeId: String, // Matches parentColumn in @Relation
    @ColumnInfo(name = "trip_busId") val busId: String, // Matches parentColumn in @Relation
    val startTime: String,
    val endTime: String?,
    val status: TripStatus
)