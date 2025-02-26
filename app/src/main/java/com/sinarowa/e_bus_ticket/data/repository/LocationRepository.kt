package com.sinarowa.e_bus_ticket.data.repository

import android.content.Context
import android.location.Location
import com.sinarowa.e_bus_ticket.data.location.LocationTracker
import com.sinarowa.e_bus_ticket.data.local.dao.RouteDao
import com.sinarowa.e_bus_ticket.data.local.dao.StationDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val context: Context,
    private val locationTracker: LocationTracker,
    private val routeDao: RouteDao,
    private val stationRepository: StationRepository
) {

    // Expose the latest location from LocationTracker
    val currentLocation = locationTracker.location

    // Fetch the closest stop based on the current location
    suspend fun getClosestStop(latitude: Double, longitude: Double, routeId: Long): String {
        return withContext(Dispatchers.IO) {
            // Get the route from the database
            val route = routeDao.getRouteById(routeId).firstOrNull() ?: return@withContext "Unknown"

            // Now we directly work with the stations list
            val stops = route.stations

            // Find the closest stop using Haversine distance
            val closestStop = stops.minByOrNull { stop ->
                // Fetch the coordinates of each stop using the StationRepository
                val stationCoordinates = stationRepository.getStationCoordinates(stop).firstOrNull()
                stationCoordinates?.let {
                    haversineDistance(latitude, longitude, it.latitude, it.longitude)
                } ?: Double.MAX_VALUE
            }

            closestStop ?: "Unknown"
        }
    }


    // Haversine distance calculation
    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Earth’s radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c // Distance in km
    }
}
