package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Price
import kotlinx.coroutines.flow.Flow


@Dao
interface PriceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrice(price: Price)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPrices(prices: List<Price>)

    @Insert
    suspend fun insertPrices(prices: List<Price>)

    @Query("SELECT * FROM prices WHERE startStationId = :fromStationId AND destinationStationId = :toStationId LIMIT 1")
    suspend fun getPrice(fromStationId: String, toStationId: String): Price?

    @Query("SELECT * FROM prices WHERE startStationId IN (SELECT route_stop_station_id FROM routestop WHERE route_stop_route_id = :routeId)")
    suspend fun getPricesForRoute(routeId: String): List<Price>


}
