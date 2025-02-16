package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinarowa.e_bus_ticket.data.local.entities.BusEntity

@Dao
interface BusDao {
    @Query("SELECT * FROM buses")
    fun getAllBuses(): List<BusEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuses(buses: List<BusEntity>)
}
