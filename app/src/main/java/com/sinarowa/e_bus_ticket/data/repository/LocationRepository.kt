package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.LocationDao
import com.sinarowa.e_bus_ticket.data.local.entities.LocationEntity
import javax.inject.Inject

class LocationRepository @Inject constructor(private val locationDao: LocationDao) {
    fun getAllLocations(): List<LocationEntity> = locationDao.getAllLocations()

    fun getLocationByCity(cityName: String): LocationEntity? = locationDao.getLocationByCity(cityName)

    fun getLocationByCoordinates(latitude: Double, longitude: Double): LocationEntity? {
        val range = 0.05  // Adjust tolerance as needed
        return locationDao.getLocationByCoordinates(
            latitude - range, latitude + range,
            longitude - range, longitude + range
        )
    }

    // ✅ This is the missing function to get city name
    fun getCityName(latitude: Double, longitude: Double): String {
        return getLocationByCoordinates(latitude, longitude)?.cityName ?: "Unknown"
    }
}
