package com.sinarowa.e_bus_ticket.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.sinarowa.e_bus_ticket.domain.models.CachedLocation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.*

object LocationUtils {
    private const val EARTH_RADIUS_KM = 6371.0 // Earth's radius in kilometers

    /**
     * Computes the Haversine distance between two geographical coordinates.
     * @param lat1 Latitude of first point.
     * @param lon1 Longitude of first point.
     * @param lat2 Latitude of second point.
     * @param lon2 Longitude of second point.
     * @param inMeters Whether to return distance in meters (default is kilometers).
     * @return Distance between the two points in the desired unit.
     */
    fun haversine(
        lat1: Double, lon1: Double, lat2: Double, lon2: Double, inMeters: Boolean = false
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = EARTH_RADIUS_KM * c

        return if (inMeters) distance * 1000 else distance // Convert to meters if needed
    }


    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // 1️⃣ Check cached location first
        LocationCache.getLastKnownLocation()?.let {
            return Location("").apply {
                latitude = it.latitude
                longitude = it.longitude
            }
        }

        // 2️⃣ Request last known location from FusedLocationClient
        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // Update cache and return location
                        LocationCache.updateLocation(
                            CachedLocation(location.latitude, location.longitude, System.currentTimeMillis())
                        )
                        continuation.resume(location)
                    } else {
                        Log.e("LocationUtils", "No last known location available")
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LocationUtils", "Error getting location: ${e.message}")
                    continuation.resume(null)
                }
        }
    }








    /**
     * Finds the nearest valid station within the trip's route.
     * @param currentLat Current latitude of the user/device.
     * @param currentLon Current longitude of the user/device.
     * @param routeStations List of station names in the correct route order.
     * @param stationCoordinates Map of station names to their coordinates (latitude, longitude).
     * @param lastKnownStation Last known valid station (used as a fallback if no better match is found).
     * @return The name of the closest valid station, or "Unknown" if no match is found.
     */
    fun findNearestStation(
        currentLat: Double?, currentLon: Double?,
        routeStations: List<String>,
        stationCoordinates: Map<String, Pair<Double, Double>>,
        lastKnownStation: String?
    ): String {
        // Edge case: No current location data
        if (currentLat == null || currentLon == null) return lastKnownStation ?: "Unknown"

        // Edge case: No stations to check
        if (routeStations.isEmpty() || stationCoordinates.isEmpty()) return lastKnownStation ?: "Unknown"

        // Find the closest station
        return routeStations
            .mapNotNull { station -> stationCoordinates[station]?.let { station to haversine(currentLat, currentLon, it.first, it.second) } }
            .minByOrNull { it.second } // Find station with the shortest distance
            ?.first ?: lastKnownStation ?: "Unknown" // Return closest station, else last known, else "Unknown"
    }

    /**
     * Checks if the current location is within a given radius of a target station.
     * @param currentLat Current latitude.
     * @param currentLon Current longitude.
     * @param targetLat Target station latitude.
     * @param targetLon Target station longitude.
     * @param radiusMeters Radius in meters within which the location should be considered "at the station."
     * @return True if within the specified radius, false otherwise.
     */
    fun isWithinRadius(
        currentLat: Double, currentLon: Double,
        targetLat: Double, targetLon: Double,
        radiusMeters: Double
    ): Boolean {
        return haversine(currentLat, currentLon, targetLat, targetLon, inMeters = true) <= radiusMeters
    }
}
