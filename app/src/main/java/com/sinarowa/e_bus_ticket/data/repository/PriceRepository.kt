/*
package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.PriceDao
import com.sinarowa.e_bus_ticket.data.local.entities.Price
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PriceRepository @Inject constructor(private val priceDao: PriceDao) {

    suspend fun insertPrice(price: Price) {
        priceDao.insertPrice(price)
    }

    suspend fun deletePrice(price: Price) {
        priceDao.deletePrice(price)
    }

    fun getPriceForRoute(startStation: String, stopStation: String): Flow<Double?> {
        return priceDao.getPriceForRoute(startStation, stopStation)
    }
}
*/
