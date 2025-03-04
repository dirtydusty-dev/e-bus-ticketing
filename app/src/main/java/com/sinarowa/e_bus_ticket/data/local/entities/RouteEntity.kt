package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val routeId: String = "",
    val routeName: String = ""
) {
    // No-argument constructor for Room
    constructor() : this("", "")
}
