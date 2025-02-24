package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinarowa.e_bus_ticket.data.local.entities.BusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusDao {
    @Query("SELECT * FROM buses")
    fun getAllBuses(): List<BusEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuses(buses: List<BusEntity>)


    @Query("SELECT * FROM buses WHERE busName = :busName LIMIT 1")
    fun getBusByName(busName: String): BusEntity

}
