package com.sinarowa.e_bus_ticket.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import com.sinarowa.e_bus_ticket.data.local.dao.TicketDao
import com.sinarowa.e_bus_ticket.data.local.dao.TripDetailsDao
import com.sinarowa.e_bus_ticket.services.LocationTrackingService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tripDao: TripDetailsDao,
    private val routeDao: RouteDao,
    private val locationDao: LocationDao,
    private val ticketDao: TicketDao
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



    /** ✅ Checks if location permissions are granted */
    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * ✅ Starts tracking location in the background
     */
    fun startTrackingLocation(tripId: String) {
        if (!hasLocationPermissions()) {
            Log.w("LOCATION_SERVICE", "⚠️ Location permissions missing, cannot start tracking.")
            return
        }

        if (!isServiceRunning()) {
            val serviceIntent = Intent(context, LocationTrackingService::class.java).apply {
                putExtra("TRIP_ID", tripId) // ✅ Pass tripId for tracking
            }
            ContextCompat.startForegroundService(context, serviceIntent)
            Log.d("LOCATION_SERVICE", "✅ Foreground location service started for trip: $tripId")
        } else {
            Log.d("LOCATION_SERVICE", "ℹ️ Foreground service is already running.")
        }
    }




    /** ✅ Stops location tracking */
    fun stopTrackingLocation() {
        if (isServiceRunning()) {
            val serviceIntent = Intent(context, LocationTrackingService::class.java)
            context.stopService(serviceIntent)
            Log.d("LOCATION_SERVICE", "✅ Foreground location service stopped.")
        } else {
            Log.d("LOCATION_SERVICE", "ℹ️ Foreground service was not running.")
        }
    }



    private fun isServiceRunning(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        return activityManager.getRunningServices(Integer.MAX_VALUE).any {
            it.service.className == LocationTrackingService::class.java.name
        }
    }



    /** ✅ Caches location if movement is significant */
    fun cacheLocation(newLocation: Location) {
        val lastLatitude = sharedPreferences.getFloat("last_latitude", 0.0f).toDouble()
        val lastLongitude = sharedPreferences.getFloat("last_longitude", 0.0f).toDouble()

        val isSignificant = isSignificantChange(lastLatitude, lastLongitude, newLocation.latitude, newLocation.longitude)

        sharedPreferences.edit()
            .putFloat("last_latitude", newLocation.latitude.toFloat())
            .putFloat("last_longitude", newLocation.longitude.toFloat())
            .putLong("last_location_time", System.currentTimeMillis()) // ✅ Always update timestamp
            .apply()

        if (isSignificant) {
            Log.d("LOCATION_PROCESS", "📌 Cached new location: ${newLocation.latitude}, ${newLocation.longitude}")
        } else {
            Log.d("LOCATION_PROCESS", "ℹ️ Location not significantly different, but timestamp updated.")
        }

    }


    fun updateDepartedCustomers(location: Location, tripId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            //val tripId = sharedPreferences.getString("current_trip_id", null) ?: return@launch

            // ✅ Get the closest stop based on the new location
            val closestStop = getClosestStop(location.latitude, location.longitude, tripId)

            if (closestStop != "Unknown") {
                Log.d("LOCATION_TRACKING", "🏁 Closest stop: $closestStop, checking tickets...")

                // ✅ Update ticket status where destination (`toStop`) matches the closest stop
                val updatedCount = ticketDao.updateDepartedTickets(tripId, closestStop)

                Log.d("LOCATION_TRACKING", "✅ Updated $updatedCount tickets as 'Departed' for stop: $closestStop")
            }
        }
    }






    /** ✅ Determines if location change is significant (500m threshold) */
    fun isSignificantChange(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Boolean {
        val distance = haversineDistance(lat1, lon1, lat2, lon2)
        return distance > 0.1 // ✅ Adjusted for better accuracy
    }

    /** ✅ Haversine formula for accurate distance calculation */
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

    /** ✅ Retrieves last cached location */
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

    /** ✅ Gets the best available location (without cached checks) */
    private suspend fun getBestAvailableLocation(): Location? {
        return getLastKnownLocation()
            ?: getGPSNetworkLocation()
            ?: getFreshGPSLocation()
    }

    /** ✅ Optimized best location fetching for tickets */
    suspend fun getBestTicketLocation(tripId: String): String {
        val cachedLocation = getCachedLocation()
        val lastTimestamp = sharedPreferences.getLong("last_location_time", 0L)
        val timeElapsed = (System.currentTimeMillis() - lastTimestamp) / 1000 // Seconds

        // ✅ If cached location is recent (within 1 min), return closest stop immediately
        if (timeElapsed < 60 && cachedLocation != null) {
            return getClosestStop(cachedLocation.latitude, cachedLocation.longitude, tripId)
        }

        // ✅ Otherwise, fetch best available location
        val location = getBestAvailableLocation() ?: return "Unknown"

        // ✅ Cache location & return closest stop
        cacheLocation(location)
        return getClosestStop(location.latitude, location.longitude, tripId)
    }

    @SuppressLint("MissingPermission")
    private fun getGPSNetworkLocation(): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        val currentTime = System.currentTimeMillis()

        val validGpsLocation = gpsLocation?.let {
            val timeElapsed = (currentTime - it.time) / 1000
            if (timeElapsed < 300) it else null // ✅ Only use if less than 5 min old
        }

        val validNetworkLocation = networkLocation?.let {
            val timeElapsed = (currentTime - it.time) / 1000
            if (timeElapsed < 300) it else null // ✅ Only use if less than 5 min old
        }

        return validGpsLocation ?: validNetworkLocation
    }


    /** ✅ Ensures last known location is recent and accurate */
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): Location? {
        if (!hasLocationPermissions()) return null

        return try {
            val location = fusedLocationClient.lastLocation.await() ?: return null
            val timeElapsed = (System.currentTimeMillis() - location.time) / 1000

            if (timeElapsed > 300 || (location.hasAccuracy() && location.accuracy > 50)) {
                Log.w("LOCATION_PROCESS", "⚠️ Last known location is too old or inaccurate. Ignoring.")
                return null
            }

            location
        } catch (e: Exception) {
            Log.e("LOCATION_PROCESS", "❌ Error getting last known location: ${e.message}")
            null
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getFreshGPSLocation(): Location? {
        if (!hasLocationPermissions()) {
            Log.w("LOCATION_PROCESS", "⚠️ Cannot get fresh GPS. Permissions missing.")
            return null
        }

        return try {
            val locationTask = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()

            if (locationTask == null) {
                Log.w("LOCATION_PROCESS", "⚠️ No fresh GPS location available.")
                return null
            }

            Log.d("LOCATION_PROCESS", "✅ Fresh GPS Location: ${locationTask.latitude}, ${locationTask.longitude}")
            locationTask

        } catch (e: Exception) {
            Log.e("LOCATION_PROCESS", "❌ Error getting fresh GPS: ${e.message}")
            null
        }
    }


    /** ✅ Gets the closest stop based on stored locations */
    suspend fun getClosestStop(latitude: Double, longitude: Double, tripId: String): String {
        return withContext(Dispatchers.IO) {
            val trip = tripDao.getTripById(tripId) ?: return@withContext "Unknown"
            val route = routeDao.getRouteByName(trip.routeName) ?: return@withContext "Unknown"

            val stopsWithCoordinates = route.stops.split(",").mapNotNull { stopName ->
                locationDao.getLocationByCity(stopName)?.let { it.cityName to Pair(it.latitude, it.longitude) }
            }

            stopsWithCoordinates.minByOrNull { (_, stopCoords) ->
                haversineDistance(latitude, longitude, stopCoords.first, stopCoords.second)
            }?.first ?: "Unknown"
        }
    }
}
