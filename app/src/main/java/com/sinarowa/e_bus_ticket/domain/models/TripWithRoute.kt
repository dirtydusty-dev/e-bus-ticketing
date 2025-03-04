package com.sinarowa.e_bus_ticket.domain.models

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.sinarowa.e_bus_ticket.data.local.entities.Bus
import com.sinarowa.e_bus_ticket.data.local.entities.Expense
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.entities.Trip
import com.sinarowa.e_bus_ticket.data.local.enums.TripStatus

data class TripWithRoute(
    @Embedded var trip: Trip, // Embedded Trip entity

    @Relation(
        parentColumn = "trip_routeId", // Matches the column name in Trip entity
        entityColumn = "routeId" // Matches the column name in RouteEntity
    )
    var route: RouteEntity, // Relationship to RouteEntity


    @Relation(
        parentColumn = "trip_busId", // Matches the column name in Trip entity
        entityColumn = "busId" // Matches the column name in Bus entity
    )
    var bus: Bus, // Relationship to Bus


    @Ignore
    val tickets: List<PriceWithTicket>,

    @Ignore
    val expenses: List<Expense>

){
    // No-argument constructor for Room
    constructor() : this(Trip(), RouteEntity(), Bus(), emptyList(), emptyList())
}

