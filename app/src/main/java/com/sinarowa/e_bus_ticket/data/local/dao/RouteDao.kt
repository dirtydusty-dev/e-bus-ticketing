package com.sinarowa.e_bus_ticket.data.local.dao


import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Route
import kotlinx.coroutines.flow.Flow


@Dao
interface RouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: Route)

    @Query("SELECT COUNT(*) FROM routes")
    suspend fun getRouteCount(): Int

    @Query("SELECT * FROM routes WHERE routeName = :routeName")
    suspend fun getRouteByName(routeName: String): List<Route>


    @Delete
    suspend fun deleteRoute(route: Route)

    @Query("SELECT * FROM routes WHERE id = :routeId")
    fun getRouteById(routeId: Long): Flow<Route?>

    @Query("SELECT * FROM routes")
    fun getAllRoutes(): Flow<List<Route>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) // ✅ Ensures no duplicates
    suspend fun insertAll(routes: List<Route>)
}
