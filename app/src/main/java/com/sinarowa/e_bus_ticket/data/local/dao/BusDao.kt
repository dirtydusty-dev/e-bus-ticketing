package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Bus
import com.sinarowa.e_bus_ticket.data.local.entities.Price
import kotlinx.coroutines.flow.Flow


@Dao
interface BusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBus(bus: Bus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuses(prices: List<Bus>)

    @Query("SELECT * FROM buses")
    suspend fun getAllBuses(): List<Bus>

    // Get a bus by its ID (to check if the bus already exists)
    @Query("SELECT * FROM buses WHERE busId = :busId LIMIT 1")
    suspend fun getBusById(busId: String): Bus?

}
