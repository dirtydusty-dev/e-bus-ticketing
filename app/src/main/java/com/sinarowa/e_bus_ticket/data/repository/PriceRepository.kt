package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.PriceDao
import com.sinarowa.e_bus_ticket.data.local.entities.PriceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PriceRepository @Inject constructor(private val priceDao: PriceDao) {
    suspend fun getPrice(fromCity: String, toCity: String): PriceEntity? {
        return priceDao.getPrice(fromCity, toCity)
    }
}
