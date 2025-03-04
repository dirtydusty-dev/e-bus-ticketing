package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "prices",
    foreignKeys = [
        ForeignKey(
            entity = StationEntity::class,
            parentColumns = ["stationId"],
            childColumns = ["startStationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StationEntity::class,
            parentColumns = ["stationId"],
            childColumns = ["destinationStationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["startStationId"]), Index(value = ["destinationStationId"])]
)
data class Price(
    @PrimaryKey val priceId: String = "",
    val startStationId: String = "",  // Linked to StopEntity
    val destinationStationId: String = "",  // Linked to StopEntity
    val amount: Double = 0.00
)

