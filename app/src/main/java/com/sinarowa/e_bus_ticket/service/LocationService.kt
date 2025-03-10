package com.sinarowa.e_bus_ticket.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.*
import com.sinarowa.e_bus_ticket.domain.models.CachedLocation
import com.sinarowa.e_bus_ticket.utils.LocationCache
import com.sinarowa.e_bus_ticket.worker.LocationServiceMonitorWorker
import java.util.concurrent.TimeUnit

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
        startLocationRetryLoop() // Persistent retry for initial location
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
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10s
            fastestInterval = 5000 // 5s
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 50f // Update if moved 50m
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        checkManualGPS() // Immediate fallback check
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val newLocation = locationResult.lastLocation
                if (newLocation != null) {
                    updateLocationCache(newLocation)
                } else {
                    Log.w("LocationService", "⚠️ Fused location failed, forcing GPS fallback...")
                    checkManualGPS()
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                if (!locationAvailability.isLocationAvailable) {
                    Log.w("LocationService", "⚠️ Fused location unavailable, forcing GPS fallback...")
                    checkManualGPS()
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

            if (bestLocation != null && isLocationFresh(bestLocation)) {
                updateLocationCache(bestLocation)
                Log.d("LocationService", "📌 Fallback to GPS success: $bestLocation")
            } else {
                Log.w("LocationService", "⚠️ No fresh location, requesting GPS update...")
                requestFreshGPSUpdate()
            }
        } catch (e: Exception) {
            Log.e("LocationService", "❌ Error in manual GPS check: ${e.message}")
        }
    }

    private fun isLocationFresh(location: Location): Boolean {
        val locationAge = System.currentTimeMillis() - location.time
        return locationAge < 10_000 // Less than 10 seconds old
    }

    @SuppressLint("MissingPermission")
    private fun requestFreshGPSUpdate() {
        val listener = object : LocationListener {
            override fun onLocationChanged(newLocation: Location) {
                if (isLocationFresh(newLocation) && newLocation.accuracy < 50f) {
                    updateLocationCache(newLocation)
                    Log.d("LocationService", "✅ Fresh GPS Update: $newLocation")
                    locationManager?.removeUpdates(this)
                } else {
                    Log.w("LocationService", "⚠️ GPS update not good enough: $newLocation")
                }
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                Log.w("LocationService", "❌ GPS provider disabled")
            }
        }
        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5000L, // 5s interval
            5f,    // 5m displacement
            listener,
            Looper.getMainLooper()
        )
        Handler(Looper.getMainLooper()).postDelayed({
            locationManager?.removeUpdates(listener)
            Log.w("LocationService", "⚠️ GPS update timed out, retrying...")
            checkManualGPS()
        }, 60000L) // 1-minute timeout
    }

    private fun updateLocationCache(location: Location) {
        val cachedLocation = CachedLocation(location.latitude, location.longitude, System.currentTimeMillis())
        LocationCache.updateLocation(cachedLocation)
        with(getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE).edit()) {
            putFloat("last_lat", location.latitude.toFloat())
            putFloat("last_lon", location.longitude.toFloat())
            putLong("last_time", System.currentTimeMillis())
            apply()
        }
        Log.d("LocationService", "✅ Updated cache: $cachedLocation")
    }

    private fun startLocationRetryLoop() {
        Thread {
            var retryDelay = 5000L // Start with 5 seconds
            val maxDelay = 60000L // Cap at 1 minute
            while (LocationCache.getLastKnownLocation() == null) {
                checkManualGPS()
                if (LocationCache.getLastKnownLocation() != null) break
                Log.w("LocationService", "⚠️ No location yet, retrying in ${retryDelay / 1000}s...")
                Thread.sleep(retryDelay)
                retryDelay = (retryDelay * 2).coerceAtMost(maxDelay)
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        locationManager?.removeUpdates { /* no-op */ }
        // Schedule a restart via WorkManager
        val restartRequest = OneTimeWorkRequestBuilder<LocationServiceMonitorWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(this).enqueue(restartRequest)
        Log.d("LocationService", "ℹ️ Service destroyed, scheduled restart")
    }

    override fun onBind(intent: Intent?) = null
}

