package com.sinarowa.e_bus_ticket.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.*
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest
import com.sinarowa.e_bus_ticket.domain.models.CachedLocation
import com.sinarowa.e_bus_ticket.utils.LocationCache

class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var locationManager: LocationManager? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        setupLocationCallback()
        startForegroundService()
        startLocationUpdates()
    }

    private fun startForegroundService() {
        val channelId = "location_service"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Location Tracker", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Tracking Location")
            .setContentText("Waiting for movement...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW) // ✅ Prevents Android killing the service
            .build()

        startForeground(1, notification)
    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationRequest = LocationRequest.create().apply {
            interval = 30000 // 30s fallback if needed
            fastestInterval = 10000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 100f // Only update if moved 100 meters
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        // Fallback: Schedule a manual check if FusedLocation fails
        checkManualGPS()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.lastLocation != null) {
                    updateLocationCache(locationResult.lastLocation!!)
                } else {
                    Log.w("LocationService", "Fused location failed, trying fallback")
                    checkManualGPS() // Fallback if fused location fails
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                if (!locationAvailability.isLocationAvailable) {
                    Log.w("LocationService", "Fused location unavailable, trying fallback")
                    checkManualGPS() // Fallback if location is unavailable
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkManualGPS() {
        try {
            val gpsLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val networkLocation = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            val bestLocation = when {
                gpsLocation != null && networkLocation != null ->
                    if (gpsLocation.accuracy < networkLocation.accuracy) gpsLocation else networkLocation
                gpsLocation != null -> gpsLocation
                networkLocation != null -> networkLocation
                else -> null
            }

            bestLocation?.let {
                updateLocationCache(it)
                Log.d("LocationService", "Fallback to GPS success: $it")
            } ?: Log.e("LocationService", "Fallback GPS also failed")
        } catch (e: Exception) {
            Log.e("LocationService", "Error getting fallback GPS: ${e.message}")
        }
    }

    private fun updateLocationCache(location: Location) {
        val cachedLocation = CachedLocation(location.latitude, location.longitude, System.currentTimeMillis())
        LocationCache.updateLocation(cachedLocation)
        Log.d("LocationService", "Updated cache: $cachedLocation")
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?) = null
}
