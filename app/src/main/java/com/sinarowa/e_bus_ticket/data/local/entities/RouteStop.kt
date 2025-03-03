package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity

@Entity(tableName = "routestop", primaryKeys = ["routeId", "stationId"])
data class RouteStop(
    val routeId: String,
    val stationId: String,
    val stopOrder: Int  // Field to track the order of stops
)
