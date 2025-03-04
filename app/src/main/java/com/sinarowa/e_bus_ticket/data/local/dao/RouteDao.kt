package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.data.local.entities.StationEntity

@Dao
interface RouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: RouteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routes: List<RouteEntity>)

    @Query("SELECT * FROM routes")
    suspend fun getAllRoutes(): List<RouteEntity>

    // Get a route by its name
    @Query("SELECT * FROM routes WHERE routeName = :routeName LIMIT 1")
    suspend fun getRouteByName(routeName: String): List<RouteEntity>

    // Get route by id
    @Query("SELECT * FROM routes WHERE routeId = :routeId LIMIT 1")
    suspend fun getRouteById(routeId: String): RouteEntity

}


