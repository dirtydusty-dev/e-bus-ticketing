/*
package com.sinarowa.e_bus_ticket.data.repository

import android.location.Location
import android.util.Log
import com.sinarowa.e_bus_ticket.data.local.dao.LocationDao
import com.sinarowa.e_bus_ticket.utils.Haversine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GeofenceRepository @Inject constructor(
    private val locationDao: LocationDao
) {

    */
/**
     * ✅ Determines the current geofenced city based on location.
     * - Finds the **closest city within a threshold**.
     *//*

    suspend fun getGeofencedCity(latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            val allCities = locationDao.getAllCities()
            val closestCity = allCities.minByOrNull { city ->
                Haversine.calculate(latitude, longitude, city.latitude, city.longitude)
            }

            val distance = closestCity?.let { Haversine.calculate(latitude, longitude, it.latitude, it.longitude) }

            return@withContext if (closestCity != null && distance != null && distance <= GEOFENCE_RADIUS_METERS) {
                Log.d("GEOFENCE", "✅ Inside city: ${closestCity.station}, Distance: $distance meters")
                closestCity.station
            } else {
                Log.w("GEOFENCE", "❌ Outside any city geofence.")
                "Unknown"
            }
        }
    }

    */
/**
     * ✅ Allows **manual override** in case of GPS failure.
     *//*

    fun manuallySetCity(cityName: String) {
        Log.d("GEOFENCE_OVERRIDE", "✅ Manual override set to: $cityName")
        _manualOverrideCity = cityName
    }

    */
/**
     * ✅ Gets the currently overridden city (if set).
     *//*

    fun getOverriddenCity(): String? {
        return _manualOverrideCity
    }

    companion object {
        private const val GEOFENCE_RADIUS_METERS = 1000.0 // ✅ 1km threshold for valid ticketing zone
        private var _manualOverrideCity: String? = null
    }
}
*/
