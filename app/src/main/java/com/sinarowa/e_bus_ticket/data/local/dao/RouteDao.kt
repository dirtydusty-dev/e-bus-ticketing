package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity

@Dao
interface RouteDao {

    @Query("SELECT * FROM routes")
    fun getAllRoutes(): List<RouteEntity>

    @Query("SELECT * FROM routes WHERE routeId = :routeId LIMIT 1")
    suspend fun getRouteById(routeId: String): RouteEntity?  // ✅ Fetch single route by ID

    @Query("SELECT * FROM routes WHERE name = :name")
    suspend fun getRouteByName(name: String): RouteEntity?

    @Query("SELECT stops FROM routes WHERE routeId = :routeId LIMIT 1")
    suspend fun getStopsByRouteId(routeId: String): String?  // ✅ Fetch stops only

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<RouteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)  // ✅ Insert single route
}
