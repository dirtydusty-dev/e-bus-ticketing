package com.sinarowa.e_bus_ticket.utils

import kotlin.math.*

object Haversine {

    private const val EARTH_RADIUS = 6371000.0 // Earth's radius in meters

    /**
     * ✅ Calculates the distance between two GPS coordinates using Haversine formula.
     * @param lat1 Latitude of first location
     * @param lon1 Longitude of first location
     * @param lat2 Latitude of second location
     * @param lon2 Longitude of second location
     * @return Distance in meters
     */
    fun calculate(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS * c
    }
}
