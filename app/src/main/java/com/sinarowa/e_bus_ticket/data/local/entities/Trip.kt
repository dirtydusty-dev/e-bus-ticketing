package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sinarowa.e_bus_ticket.data.local.enums.SyncStatus
import com.sinarowa.e_bus_ticket.data.local.enums.TripStatus

@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(
            entity = RouteEntity::class,
            parentColumns = ["routeId"],
            childColumns = ["trip_routeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Bus::class,
            parentColumns = ["busId"],
            childColumns = ["trip_busId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["trip_routeId"]), Index(value = ["trip_busId"])]
)
data class Trip(
    @PrimaryKey val tripId: String = "",
    @ColumnInfo(name = "trip_routeId") val routeId: String = "",
    @ColumnInfo(name = "trip_busId") val busId: String = "",
    val startTime: String = "",
    var endTime: String? = null,
    var status: TripStatus = TripStatus.IN_PROGRESS,
    var syncStatus: SyncStatus = SyncStatus.PENDING
) {
    // No-argument constructor for Room
    constructor() : this("", "", "", "", null, TripStatus.IN_PROGRESS, SyncStatus.PENDING)
}