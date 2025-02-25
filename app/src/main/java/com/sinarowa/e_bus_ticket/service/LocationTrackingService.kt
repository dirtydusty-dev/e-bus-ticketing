package com.sinarowa.e_bus_ticket.services

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.sinarowa.e_bus_ticket.data.repository.LocationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject
    lateinit var locationRepository: LocationRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var tripId: String? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // ✅ Configure location request for high accuracy
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // Every 10 seconds
            fastestInterval = 5000
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { location ->
                    Log.d("FOREGROUND_SERVICE", "📌 Location Updated: ${location.latitude}, ${location.longitude}")

                    tripId?.let {
                        serviceScope.launch {
                            processLocationUpdate(it, location)
                        }
                    } ?: Log.w("LOCATION_TRACKING", "⚠️ No active trip found, skipping updates.")
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        tripId = intent?.getStringExtra("TRIP_ID")

        if (tripId == null) {
            Log.e("LOCATION_TRACKING", "❌ No tripId provided, stopping service.")
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundService()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * ✅ Processes location updates asynchronously.
     */
    private suspend fun processLocationUpdate(tripId: String, location: Location) {
        val closestStop = locationRepository.getClosestStop(tripId)
        Log.d("LOCATION_TRACKING", "🚏 Detected stop: $closestStop")

        if (closestStop != "Unknown") {
            locationRepository.updateDepartedTickets(tripId, closestStop)
        }
    }

    /**
     * ✅ Starts foreground location tracking with a notification.
     */
    @Suppress("MissingPermission")
    private fun startForegroundService() {
        if (!hasLocationPermissions()) {
            Log.e("LOCATION_TRACKING", "❌ Missing location permissions, stopping service.")
            stopSelf()
            return
        }

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }

    /**
     * ✅ Checks for location permissions before starting tracking.
     */
    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * ✅ Creates the foreground service notification.
     */
    private fun createNotification(): Notification {
        val channelId = "location_tracking"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Tracking Location")
            .setContentText("Your location is being tracked for trip updates.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
        Log.d("FOREGROUND_SERVICE", "❌ Location tracking stopped.")
    }

    companion object {
        private const val NOTIFICATION_ID = 1001

        /**
         * ✅ Starts the location tracking service with a trip ID.
         */
        fun startService(context: Context, tripId: String) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                putExtra("TRIP_ID", tripId)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        /**
         * ✅ Stops the tracking service.
         */
        fun stopService(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java)
            context.stopService(intent)
        }
    }
}
