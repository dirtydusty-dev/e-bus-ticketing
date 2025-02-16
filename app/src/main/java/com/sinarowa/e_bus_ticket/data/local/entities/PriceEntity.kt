package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prices")
data class PriceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "from") val fromCity: String,  // ✅ Renamed in Kotlin
    @ColumnInfo(name = "to") val toCity: String,
    val adultPrice: Double,
    val childPrice: Double,
    val luggagePrice: Double
)

