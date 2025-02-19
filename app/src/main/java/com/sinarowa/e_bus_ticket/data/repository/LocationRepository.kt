package com.sinarowa.e_bus_ticket.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.sinarowa.e_bus_ticket.data.local.dao.LocationDao
import com.sinarowa.e_bus_ticket.data.local.dao.RouteDao
import com.sinarowa.e_bus_ticket.data.local.dao.TripDetailsDao
import com.sinarowa.e_bus_ticket.data.local.entities.TripDetails
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tripDao: TripDetailsDao,
    private val routeDao: RouteDao,
    private val locationDao: LocationDao
) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
    }

    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = 10000  // 10 seconds update interval
        fastestInterval = 5000
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.lastOrNull()?.let { location ->
                Log.d("LOCATION_PROCESS", "✅ Fresh Location: ${location.latitude}, ${location.longitude}")
                cacheLocation(location)  // ✅ Save location when it changes significantly
            }
        }
    }

    /** ✅ Checks if location permissions are granted */
    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * ✅ Starts tracking location, but only if permissions are granted
     */
    @SuppressLint("MissingPermission")
    fun startTrackingLocation() {
        if (!hasLocationPermissions()) {
            Log.w("LOCATION_PROCESS", "⚠️ Location permissions NOT granted. Cannot start tracking.")
            return
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d("LOCATION_PROCESS", "✅ Started location tracking.")
        } catch (e: SecurityException) {
            Log.e("LOCATION_PROCESS", "❌ SecurityException: ${e.message}")
        }
    }

    /**
     * ✅ Stops location tracking
     */
    fun stopTrackingLocation() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("LOCATION_PROCESS", "✅ Stopped location tracking.")
    }

    /**
     * ✅ Caches the location only if the change is significant
     */
    fun cacheLocation(newLocation: Location) {
        val lastLatitude = sharedPreferences.getFloat("last_latitude", 0.0f).toDouble()
        val lastLongitude = sharedPreferences.getFloat("last_longitude", 0.0f).toDouble()

        if (isSignificantChange(lastLatitude, lastLongitude, newLocation.latitude, newLocation.longitude)) {
            sharedPreferences.edit()
                .putFloat("last_latitude", newLocation.latitude.toFloat())
                .putFloat("last_longitude", newLocation.longitude.toFloat())
                .apply()

            Log.d("LOCATION_PROCESS", "📌 Cached new location: ${newLocation.latitude}, ${newLocation.longitude}")
        } else {
            Log.d("LOCATION_PROCESS", "ℹ️ Location not significantly different. Skipping cache update.")
        }
    }

    /**
     * ✅ Determines if the new location is significantly different from the last one
     */
    private fun isSignificantChange(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Boolean {
        val distance = haversineDistance(lat1, lon1, lat2, lon2)
        return distance > 3.0 // Change threshold: 3 km
    }

    /**
     * ✅ Haversine formula to calculate distance
     */
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

    /**
     * ✅ Retrieves last cached location
     */
    fun getCachedLocation(): Location? {
        val lat = sharedPreferences.getFloat("last_latitude", 0.0f).toDouble()
        val lon = sharedPreferences.getFloat("last_longitude", 0.0f).toDouble()

        return if (lat != 0.0 && lon != 0.0) {
            Location("").apply {
                latitude = lat
                longitude = lon
            }
        } else {
            null
        }
    }


    /**
     * ✅ Get city name based on closest known locations
     */
    suspend fun getCityNameWithFallback(tripId: String): String {
        val location = getBestAvailableLocation()
        return if (location != null) {
            getClosestStop(location.latitude, location.longitude, tripId)
        } else {
            "Unknown"
        }
    }

    /**
     * ✅ Get fresh GPS location (high accuracy)
     */
    @SuppressLint("MissingPermission")
    suspend fun getFreshGPSLocation(): Location? {
        if (!hasLocationPermissions()) {
            Log.w("LOCATION_PROCESS", "⚠️ Cannot get fresh GPS. Permissions missing.")
            return null
        }

        return try {
            val locationTask = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            if (locationTask != null) {
                Log.d("LOCATION_PROCESS", "✅ Fresh GPS Location: ${locationTask.latitude}, ${locationTask.longitude}")
            } else {
                Log.w("LOCATION_PROCESS", "⚠️ No fresh GPS location available.")
            }
            locationTask
        } catch (e: Exception) {
            Log.e("LOCATION_PROCESS", "❌ Error getting fresh GPS: ${e.message}")
            null
        }
    }



    /**
     * ✅ Get last known location from Google API
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): Location? {
        if (!hasLocationPermissions()) {
            Log.w("LOCATION_PROCESS", "⚠️ Cannot get last known location. Permissions missing.")
            return null
        }

        return try {
            val location = fusedLocationClient.lastLocation.await()
            if (location != null) {
                Log.d("LOCATION_PROCESS", "✅ Google API Last Location: ${location.latitude}, ${location.longitude}")
            } else {
                Log.w("LOCATION_PROCESS", "⚠️ Google API returned null location.")
            }
            location
        } catch (e: Exception) {
            Log.e("LOCATION_PROCESS", "❌ Error getting last known location: ${e.message}")
            null
        }
    }

    private suspend fun getBestAvailableLocation(): Location? {
        return getFreshGPSLocation() ?: getLastKnownLocation() ?: getGPSNetworkLocation()?: getCachedLocation()
    }


    @SuppressLint("MissingPermission")
    private fun getGPSNetworkLocation(): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val gpsLocation: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val networkLocation: Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        return gpsLocation ?: networkLocation
    }

    /**
     * ✅ Gets the nearest city from stored locations
     */

    suspend fun getClosestStop(latitude: Double, longitude: Double, tripId: String): String {
        return withContext(Dispatchers.IO) { // ✅ Runs in IO thread
            val trip = tripDao.getTripById(tripId) ?: return@withContext "Unknown"
            val route = routeDao.getRouteByName(trip.routeName) ?: return@withContext "Unknown"

            val stopsList = route.stops.split(",").map { it.trim() } // ✅ Convert CSV stops to list
            Log.d("LOCATION_PROCESS", "📍 Checking ${stopsList.size} stops for closest match...")

            val stopsWithCoordinates = stopsList.mapNotNull { stopName ->
                val stopLocation = locationDao.getLocationByCity(stopName) // ✅ Fetch stop details from LocationEntity
                stopLocation?.let { stop -> stop.cityName to Pair(stop.latitude, stop.longitude) }
            }

            val closestStop = stopsWithCoordinates.minByOrNull { (_, stopCoordinates) ->
                val (stopLat, stopLon) = stopCoordinates
                val distance = haversineDistance(latitude, longitude, stopLat, stopLon)
                Log.d("LOCATION_PROCESS", "➡️ Distance to $stopLat, $stopLon: $distance km")
                distance
            }

            val stopName = closestStop?.first ?: "Unknown"
            Log.d("LOCATION_PROCESS", "🏆 Closest stop selected: $stopName")
            return@withContext stopName
        }
    }

}
