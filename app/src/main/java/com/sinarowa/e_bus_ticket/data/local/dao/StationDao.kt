package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Price
import com.sinarowa.e_bus_ticket.data.local.entities.StationCoordinates
import kotlinx.coroutines.flow.Flow


@Dao
interface StationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: StationCoordinates)

    @Query("SELECT * FROM station_coordinates WHERE station = :stationName")
    fun getStationCoordinates(stationName: String): Flow<StationCoordinates?>

    @Query("SELECT * FROM station_coordinates")
    fun getAllStations(): Flow<List<StationCoordinates>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoordinates(prices: List<StationCoordinates>)
}
