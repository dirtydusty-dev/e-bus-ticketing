package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Price
import com.sinarowa.e_bus_ticket.data.local.entities.StationEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface StationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: StationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStations(stations: List<StationEntity>)

    // Get a station by its name (assuming name is unique)
    @Query("SELECT * FROM stops WHERE name = :name LIMIT 1")
    suspend fun getStationByName(name: String): List<StationEntity>

    // Get all stations
    @Query("SELECT * FROM stops")
    suspend fun getAllStations(): List<StationEntity>

    // Insert multiple stations at once
    @Insert
    suspend fun insertMultipleStations(stations: List<StationEntity>)

}
