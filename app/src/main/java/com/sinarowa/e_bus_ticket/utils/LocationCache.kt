package com.sinarowa.e_bus_ticket.utils

import com.sinarowa.e_bus_ticket.domain.models.CachedLocation


object LocationCache {
    private var currentLocation: CachedLocation? = null

    fun updateLocation(location: CachedLocation) {
        currentLocation = location
    }

    fun getLastKnownLocation(): CachedLocation? = currentLocation
}
