package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinarowa.e_bus_ticket.data.local.entities.LocationEntity

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations")
    fun getAllLocations(): List<LocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)

    @Query("SELECT * FROM locations WHERE cityName = :cityName LIMIT 1")
    fun getLocationByCity(cityName: String): LocationEntity?

    @Query("SELECT * FROM locations WHERE latitude BETWEEN :latMin AND :latMax AND longitude BETWEEN :lngMin AND :lngMax LIMIT 1")
    fun getLocationByCoordinates(latMin: Double, latMax: Double, lngMin: Double, lngMax: Double): LocationEntity?
}
