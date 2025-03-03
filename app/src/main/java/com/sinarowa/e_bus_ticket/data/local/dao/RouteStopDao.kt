package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.data.local.entities.RouteStop
import com.sinarowa.e_bus_ticket.data.local.entities.StationEntity

@Dao
interface RouteStopDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routeStop: RouteStop)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routeStops: List<RouteStop>)


    // Get route stops for a specific route
    @Query("SELECT * FROM routestop WHERE routeId = :routeId")
    suspend fun getRouteStopsByRouteId(routeId: String): List<RouteStop>

    // Get a specific route stop by routeId and stationId
    @Query("SELECT * FROM routestop WHERE routeId = :routeId AND stationId = :stationId LIMIT 1")
    suspend fun getRouteStopByRouteIdAndStationId(routeId: String, stationId: String): RouteStop?

    // Insert multiple route stops at once
    @Insert
    suspend fun insertMultipleRouteStops(routeStops: List<RouteStop>)


}


