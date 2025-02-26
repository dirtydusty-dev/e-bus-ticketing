package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buses")
data class Bus(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val busName: String,
    val busNumber: String,
    val capacity: Int
)