package com.sinarowa.e_bus_ticket.domain.models

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.sinarowa.e_bus_ticket.data.local.entities.*

data class TripWithRoute(
    @Embedded var trip: Trip,

    @Relation(
        parentColumn = "trip_routeId", // From Trip
        entityColumn = "routeId",      // From RouteEntity (inside RouteWithStations)
        entity = RouteEntity::class    // Specify the entity
    )
    var route: RouteWithStations,

    @Relation(
        parentColumn = "trip_busId",
        entityColumn = "busId"
    )
    var bus: Bus,

    /*@Relation(
        parentColumn = "trip_routeId",
        entityColumn = "startStationId"
    )
    var prices: List<Price>,*/

    @Relation(
        parentColumn = "tripId",
        entityColumn = "ticket_tripId"
    )
    var tickets: List<Ticket>,

    @Relation(
        parentColumn = "tripId",
        entityColumn = "expense_tripId"
    )
    var expenses: List<Expense>
) {
    constructor() : this(Trip(), RouteWithStations(), Bus(), emptyList(), emptyList())
}