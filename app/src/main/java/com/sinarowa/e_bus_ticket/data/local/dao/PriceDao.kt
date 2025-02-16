package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinarowa.e_bus_ticket.data.local.entities.PriceEntity

@Dao
interface PriceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrices(prices: List<PriceEntity>)

    @Query("SELECT * FROM prices WHERE `from` = :fromCity AND `to` = :toCity LIMIT 1")
    fun getPrice(fromCity: String, toCity: String): PriceEntity?
}
