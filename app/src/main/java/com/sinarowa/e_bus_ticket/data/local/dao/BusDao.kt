package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Bus
import com.sinarowa.e_bus_ticket.data.local.entities.Price
import kotlinx.coroutines.flow.Flow


@Dao
interface BusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBus(bus: Bus)

    @Delete
    suspend fun deleteBus(bus: Bus)

    @Query("SELECT * FROM buses WHERE id = :busId")
    fun getBusById(busId: Long): Flow<Bus?>

    @Query("SELECT * FROM buses")
    fun getAllBuses(): Flow<List<Bus>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuses(prices: List<Bus>)
}
