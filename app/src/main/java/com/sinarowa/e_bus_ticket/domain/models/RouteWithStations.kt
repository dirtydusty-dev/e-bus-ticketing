package com.sinarowa.e_bus_ticket.domain.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.data.local.entities.RouteStop
import com.sinarowa.e_bus_ticket.data.local.entities.StationEntity

data class RouteWithStations(
    @Embedded var route: RouteEntity,

    @Relation(
        parentColumn = "routeId",           // From RouteEntity
        entityColumn = "route_stop_route_id", // From RouteStop
        entity = RouteStop::class
    )
    var stops: List<RouteStop>,

    @Relation(
        parentColumn = "routeId",           // From RouteEntity
        entityColumn = "stationId",         // From StationEntity
        associateBy = Junction(
            value = RouteStop::class,
            parentColumn = "route_stop_route_id",
            entityColumn = "route_stop_station_id"
        )
    )
    var stationDetails: List<StationEntity>
) {
    constructor() : this(RouteEntity(), emptyList(), emptyList())
}