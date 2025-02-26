package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Price
import kotlinx.coroutines.flow.Flow


@Dao
interface PriceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrice(price: Price)

    @Delete
    suspend fun deletePrice(price: Price)

    @Query("SELECT amount FROM prices WHERE startStation = :startStation AND stopStation = :stopStation")
    fun getPriceForRoute(startStation: String, stopStation: String): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrices(prices: List<Price>)
}
