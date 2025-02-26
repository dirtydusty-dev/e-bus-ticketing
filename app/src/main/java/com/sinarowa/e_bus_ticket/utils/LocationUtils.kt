package com.sinarowa.e_bus_ticket.utils

import kotlin.math.*

object LocationUtils {
    private const val EARTH_RADIUS_KM = 6371.0

    // Haversine function to calculate distance between two coordinates
    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    // Find the nearest valid station within the trip's route
    fun findNearestStation(
        currentLat: Double?,
        currentLon: Double?,
        routeStations: List<String>,
        stationCoordinates: Map<String, Pair<Double, Double>>,
        lastKnownStation: String?
    ): String {
        if (currentLat == null || currentLon == null) {
            return lastKnownStation ?: "Unknown"
        }

        var closestStation: String? = null
        var minDistance = Double.MAX_VALUE

        for (station in routeStations) {
            val coordinates = stationCoordinates[station] ?: continue
            val distance = haversine(currentLat, currentLon, coordinates.first, coordinates.second)

            if (distance < minDistance) {
                minDistance = distance
                closestStation = station
            }
        }

        return closestStation ?: lastKnownStation ?: "Unknown"
    }

    // Get valid stops after the selected station in route order
    fun getValidStops(routeStations: List<String>, startStation: String): List<String> {
        val startIndex = routeStations.indexOf(startStation)
        return if (startIndex != -1) routeStations.subList(startIndex + 1, routeStations.size) else emptyList()
    }
}
