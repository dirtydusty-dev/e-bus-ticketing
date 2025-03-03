/*
package com.sinarowa.e_bus_ticket.data.repository

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.sinarowa.e_bus_ticket.data.local.dao.LocationDao
import com.sinarowa.e_bus_ticket.data.local.dao.RouteDao
import com.sinarowa.e_bus_ticket.utils.Haversine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val context: Context,
    private val routeDao: RouteDao,
    private val locationDao: LocationDao
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    suspend fun getClosestStop(tripId: String): String {
        return withContext(Dispatchers.IO) {
            val route = routeDao.getRouteByTrip(tripId) ?: return@withContext "Unknown"

            val stopsWithCoordinates = route.stops.split(",").mapNotNull { stopName ->
                locationDao.getLocationByCity(stopName.trim())?.let { stopName to it }
            }

            val lastLocation = getLastKnownLocation() ?: return@withContext "Unknown"

            return@withContext stopsWithCoordinates.minByOrNull { (_, location) ->
                Haversine.calculate(lastLocation.latitude, lastLocation.longitude, location.latitude, location.longitude)
            }?.first ?: "Unknown"
        }
    }

    suspend fun getLastKnownLocation(): Location? {
        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            Log.e("LOCATION_PROCESS", "❌ Error getting location: ${e.message}")
            null
        }
    }
}
*/
