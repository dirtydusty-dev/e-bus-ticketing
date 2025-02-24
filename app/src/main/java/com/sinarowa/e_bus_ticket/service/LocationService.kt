/*
package com.sinarowa.e_bus_ticket.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.sinarowa.e_bus_ticket.R
import com.sinarowa.e_bus_ticket.data.repository.LocationRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var locationRepository: LocationRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createNotificationChannel()
        startForegroundService()
        startTrackingLocation()
    }

    */
/**
     * ✅ Creates a foreground notification
     *//*

    private fun startForegroundService() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking Location")
            .setContentText("Your location is being tracked for active trips.")
            .setSmallIcon(R.drawable.logo) // ✅ Replace with your actual icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    */
/**
     * ✅ Creates a notification channel (for Android 8+)
     *//*

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    */
/**
     * ✅ Starts tracking location updates every 10 seconds
     *//*

    @SuppressLint("MissingPermission")
    private fun startTrackingLocation() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
            .setMinUpdateIntervalMillis(5000L)
            .build()

        // Ensure locationCallback is initialized
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { location ->
                    Log.d("LOCATION_SERVICE", "✅ Updated Location: ${location.latitude}, ${location.longitude}")
                    locationRepository.cacheLocation(location)
                }
            }
        }

        if (checkPermissions()) {
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
                Log.d("LOCATION_SERVICE", "✅ Started location tracking successfully.")
            } catch (e: SecurityException) {
                Log.e("LOCATION_SERVICE", "❌ SecurityException: ${e.message}")
            }
        } else {
            Log.e("LOCATION_SERVICE", "❌ Location permission not granted. Cannot request updates.")
        }
    }



    */
/**
     * ✅ Checks location permissions (including background for Android 10+)
     *//*

    private fun checkPermissions(): Boolean {
        val hasFineLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        // ✅ For Android 10+ (API 29+), check ACCESS_BACKGROUND_LOCATION
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val hasBackgroundLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            hasFineLocation && hasCoarseLocation && hasBackgroundLocation
        } else {
            hasFineLocation && hasCoarseLocation
        }
    }

    */
/**
     * ✅ Stops tracking location when the service is destroyed
     *//*

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("LOCATION_SERVICE", "❌ Location Service Stopped")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val CHANNEL_ID = "location_tracking_channel"
    }
}
*/
