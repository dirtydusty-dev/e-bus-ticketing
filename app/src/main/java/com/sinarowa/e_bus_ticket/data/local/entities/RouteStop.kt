package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.*

@Entity(
    tableName = "routestop",
    primaryKeys = ["route_stop_route_id", "route_stop_station_id"],
    foreignKeys = [
        ForeignKey(
            entity = RouteEntity::class,
            parentColumns = ["routeId"],
            childColumns = ["route_stop_route_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StationEntity::class,
            parentColumns = ["stationId"],
            childColumns = ["route_stop_station_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["route_stop_route_id"]),
        Index(value = ["route_stop_station_id"])
    ]
)
data class RouteStop(
    @ColumnInfo(name = "route_stop_route_id") val routeId: String = "",
    @ColumnInfo(name = "route_stop_station_id") val stationId: String = "",
    @ColumnInfo(name = "stop_order") val stopOrder: Int = 0
) {
    constructor() : this("", "", 0)
}
