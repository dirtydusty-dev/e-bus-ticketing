package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prices")
data class Price(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startStation: String,
    val stopStation: String,
    val amount: Double
)