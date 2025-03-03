package com.sinarowa.e_bus_ticket.domain.models

import androidx.room.Embedded
import androidx.room.Relation
import com.sinarowa.e_bus_ticket.data.local.entities.Bus
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.data.local.entities.Trip

data class TripWithRoute(
    @Embedded val trip: Trip, // Embedded Trip entity

    @Relation(
        parentColumn = "trip_routeId", // Matches the column name in Trip entity
        entityColumn = "routeId" // Matches the column name in RouteEntity
    )
    val route: RouteEntity, // Relationship to RouteEntity

    @Relation(
        parentColumn = "trip_busId", // Matches the column name in Trip entity
        entityColumn = "busId" // Matches the column name in Bus entity
    )
    val bus: Bus // Relationship to Bus
)