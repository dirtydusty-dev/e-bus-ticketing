package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.BusDao
import com.sinarowa.e_bus_ticket.data.local.entities.Bus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BusRepository @Inject constructor(private val busDao: BusDao) {

    suspend fun getAllBuses(): List<Bus> {
        return busDao.getAllBuses()
    }
}
