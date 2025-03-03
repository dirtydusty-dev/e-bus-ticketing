/*
package com.sinarowa.e_bus_ticket.location

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class LocationTracker @Inject constructor(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location

    private var cachedLocation: Location? = null
    private var lastLocationUpdateTime: Long = 0
    private val CACHE_EXPIRY_TIME = 5 * 60 * 1000 // 5 minutes
    private val MAX_ACCEPTABLE_ACCURACY = 50.0f // 50 meters

    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = 10000 // 10 seconds update interval
        fastestInterval = 5000
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                if (isLocationReliable(location)) {
                    Log.d("LocationTracker", "Location updated: ${location.latitude}, ${location.longitude}")
                    _location.value = location
                    updateCachedLocation(location)
                }
            }
        }
    }

    fun startTracking() {
        Log.d("LocationTracker", "Starting location tracking")
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } catch (e: SecurityException) {
            Log.e("LocationTracker", "Location permission denied", e)
        }
    }

    fun stopTracking() {
        Log.d("LocationTracker", "Stopping location tracking")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun isLocationReliable(location: Location): Boolean {
        return location.accuracy <= MAX_ACCEPTABLE_ACCURACY
    }

    private fun updateCachedLocation(location: Location) {
        cachedLocation = location
        lastLocationUpdateTime = System.currentTimeMillis()
    }

    fun getCachedLocation(): Location? {
        val currentTime = System.currentTimeMillis()
        return if (currentTime - lastLocationUpdateTime <= CACHE_EXPIRY_TIME) {
            cachedLocation
        } else {
            null // Cache expired
        }
    }
}*/
