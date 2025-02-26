package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "routes")
@TypeConverters(Converters::class)
data class Route(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeName: String,
    val stations: List<String>
)