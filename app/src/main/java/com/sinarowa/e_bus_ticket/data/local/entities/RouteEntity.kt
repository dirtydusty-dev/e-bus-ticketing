package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val routeId: String,
    val name: String,
    val from: String,
    val to: String,
    val stops: String  // ✅ Store stops as comma-separated values
)
