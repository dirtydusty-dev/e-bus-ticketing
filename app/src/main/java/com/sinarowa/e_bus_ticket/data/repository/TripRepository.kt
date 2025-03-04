package com.sinarowa.e_bus_ticket.data.repository

import android.util.Log
import com.sinarowa.e_bus_ticket.data.local.dao.BusDao
import com.sinarowa.e_bus_ticket.data.local.dao.RouteDao
import com.sinarowa.e_bus_ticket.data.local.dao.TripDao
import com.sinarowa.e_bus_ticket.data.local.entities.Bus
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.data.local.entities.Trip
import com.sinarowa.e_bus_ticket.data.local.enums.SyncStatus
import com.sinarowa.e_bus_ticket.data.local.enums.TripStatus
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TripRepository @Inject constructor(
    private val tripDao: TripDao,
    private val routeDao: RouteDao,   // Assuming you have this
    private val busDao: BusDao
) {

    suspend fun createTrip(tripWithRoute: TripWithRoute) {
        // Insert the route if it doesn't exist
        val routeId = insertRouteIfNotExists(tripWithRoute.route)
        val busId = insertBusIfNotExists(tripWithRoute.bus)

        // Create the trip
        val trip = tripWithRoute.trip.copy(routeId = routeId, busId = busId)
        tripDao.insertTrip(trip)
    }

    // Insert route if not already exists
    private suspend fun insertRouteIfNotExists(route: RouteEntity): String {
        // Check if route already exists
        val existingRoute = routeDao.getRouteByName(route.routeName)
        return if (existingRoute.isEmpty()) {
            route.routeId
        } else {
            existingRoute.first().routeId // Return the existing routeId
        }
    }

    // Insert bus if not already exists
    private suspend fun insertBusIfNotExists(bus: Bus): String {
        // Check if bus already exists
        val existingBus = busDao.getBusByRegistrationNumber(bus.busNumber)
        return if (existingBus == null) {
            bus.busId
        } else {
            existingBus.busId
        }
    }

    suspend fun hasActiveTrip(): Boolean {
        return tripDao.getActiveTrip() != null
    }

    suspend fun getActiveTrip(): Trip? {
        return tripDao.getActiveTrip()
    }

    // Get active trip with its route
    suspend fun getActiveTripWithRoute(): TripWithRoute? {
        return tripDao.getActiveTripWithRoute(TripStatus.IN_PROGRESS)
    }

    suspend fun updateTripSyncStatus(tripId: String){
        tripDao.updateSyncStatus(tripId,SyncStatus.SYNCED)
    }


}
