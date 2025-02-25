package com.sinarowa.e_bus_ticket.data.repository

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.sinarowa.e_bus_ticket.data.local.dao.LocationDao
import com.sinarowa.e_bus_ticket.data.local.dao.RouteDao
import com.sinarowa.e_bus_ticket.data.local.dao.TripDetailsDao
import com.sinarowa.e_bus_ticket.services.LocationTrackingService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.*

class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tripDao: TripDetailsDao,
    private val routeDao: RouteDao,
    private val locationDao: LocationDao,
    private val routeRepository: RouteRepository

) {
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(context) }
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
    }


    suspend fun getOrderedStops(tripId: String): List<String> {
        return withContext(Dispatchers.IO) {
            val route = routeRepository.getRouteByTrip(tripId) ?: return@withContext emptyList()
            return@withContext route.stops.split(",").map { it.trim() } // ✅ Ensures proper formatting
        }
    }


    /**
     * ✅ Starts tracking when trip begins.
     */
    fun startTrackingLocation(tripId: String) {
        if (!hasLocationPermissions()) {
            Log.w("LOCATION_SERVICE", "⚠️ Missing location permissions.")
            return
        }

        sharedPreferences.edit().putString("current_trip_id", tripId).apply()

        val serviceIntent = Intent(context, LocationTrackingService::class.java).apply {
            putExtra("TRIP_ID", tripId)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
        Log.d("LOCATION_SERVICE", "✅ Tracking started for trip: $tripId")
    }


    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * ✅ Stops tracking location when trip ends.
     */
    fun stopTrackingLocation() {
        val serviceIntent = Intent(context, LocationTrackingService::class.java)
        context.stopService(serviceIntent)
        Log.d("LOCATION_SERVICE", "✅ Tracking stopped.")
    }

    /**
     * ✅ Gets the closest stop **based on the active trip's route**.
     * Uses **last known location as fallback** in case of weak GPS.
     */
    suspend fun getClosestStop(tripId: String): String {
        return withContext(Dispatchers.IO) {
            val trip = tripDao.getTripById(tripId) ?: return@withContext "Unknown"
            val route = routeDao.getRouteByName(trip.routeName) ?: return@withContext "Unknown"

            val stopsWithCoordinates = route.stops.split(",").mapNotNull { stopName ->
                locationDao.getLocationByCity(stopName.trim())?.let { stopName to it }
            }

            val lastLocation = getLastKnownLocation() ?: return@withContext "Unknown"

            return@withContext stopsWithCoordinates.minByOrNull { (_, location) ->
                haversineDistance(lastLocation.latitude, lastLocation.longitude, location.latitude, location.longitude)
            }?.first ?: "Unknown"
        }
    }

    /**
     * ✅ Gets the last known **GPS location** (fallback for bad GPS signals).
     */
    @Suppress("MissingPermission")
    private suspend fun getLastKnownLocation(): Location? {
        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            Log.e("LOCATION_PROCESS", "❌ Error getting location: ${e.message}")
            null
        }
    }

    /**
     * ✅ Uses **Haversine formula** to compute distance.
     */
    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }






}





