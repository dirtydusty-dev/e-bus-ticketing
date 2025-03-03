/*
package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity)

    @Query("SELECT * FROM locations WHERE cityName = :cityName LIMIT 1")
    suspend fun getLocationByCity(cityName: String): LocationEntity?

    @Query("SELECT * FROM locations")
    suspend fun getAllCities(): List<LocationEntity>
}

*/
