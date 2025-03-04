package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.RouteDao
import com.sinarowa.e_bus_ticket.data.local.dao.RouteStopDao
import com.sinarowa.e_bus_ticket.data.local.dao.StationDao
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RouteRepository @Inject constructor(
    private val routeDao: RouteDao,
) {
    suspend fun getAllRoutes(): List<RouteEntity> {
        return routeDao.getAllRoutes()
    }

    // Get a route by its name
    suspend fun getRouteByName(routeName: String): List<RouteEntity> {
        return routeDao.getRouteByName(routeName)
    }

    // Get a route by its ID
    suspend fun getRouteById(routeId: String): RouteEntity {
        return routeDao.getRouteById(routeId)
    }
}
