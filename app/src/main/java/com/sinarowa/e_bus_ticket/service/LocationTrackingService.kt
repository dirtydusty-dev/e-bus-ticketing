package com.sinarowa.e_bus_ticket.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.sinarowa.e_bus_ticket.data.repository.LocationRepository
import javax.inject.Inject

class LocationTrackingService : Service() {

    @Inject
    lateinit var locationRepository: LocationRepository

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Start tracking
        val routeId = intent.getStringExtra("ROUTE_ID") ?: return START_NOT_STICKY
        locationRepository.startTrackingLocation(routeId)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        locationRepository.stopTrackingLocation()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
