package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.PriceDao
import com.sinarowa.e_bus_ticket.data.local.entities.Price
import javax.inject.Inject

class PriceRepository @Inject constructor(
    private val priceDao: PriceDao
) {
    suspend fun getPrice(fromStationId: String, toStationId: String): Price? {
        return priceDao.getPrice(fromStationId, toStationId)
    }
}
