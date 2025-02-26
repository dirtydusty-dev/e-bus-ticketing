package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.RouteDao
import com.sinarowa.e_bus_ticket.data.local.entities.Route
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RouteRepository @Inject constructor(private val routeDao: RouteDao) {

    suspend fun insertRoute(route: Route) {
        routeDao.insertRoute(route)
    }

    suspend fun deleteRoute(route: Route) {
        routeDao.deleteRoute(route)
    }

    fun getRouteById(routeId: Long): Flow<Route?> {
        return routeDao.getRouteById(routeId)
    }

    fun getAllRoutes(): Flow<List<Route>> {
        return routeDao.getAllRoutes()
    }
}
