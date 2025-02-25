package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.RouteDao
import com.sinarowa.e_bus_ticket.data.local.dao.TripDetailsDao
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RouteRepository @Inject constructor(
    private val routeDao: RouteDao,
    private val tripDao: TripDetailsDao
) {
    suspend fun getAllRoutes(): List<RouteEntity> = routeDao.getAllRoutes()

    suspend fun getRouteById(routeId: String): RouteEntity? = routeDao.getRouteById(routeId)

    suspend fun getStopsByRouteId(routeId: String): String? = routeDao.getStopsByRouteId(routeId)

    suspend fun insertRoutes(routes: List<RouteEntity>) = routeDao.insertRoutes(routes)

    suspend fun insertRoute(route: RouteEntity) = routeDao.insertRoute(route)


    /**
     * ✅ Retrieves the route details for a given trip ID.
     */
    suspend fun getRouteByTrip(tripId: String): RouteEntity? {
        return withContext(Dispatchers.IO) {
            val trip = tripDao.getTripById(tripId) ?: return@withContext null
            return@withContext routeDao.getRouteByName(trip.routeName)
        }
    }


}
