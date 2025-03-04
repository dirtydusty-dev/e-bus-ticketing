package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.BusDao
import com.sinarowa.e_bus_ticket.data.local.entities.Bus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BusRepository @Inject constructor(private val busDao: BusDao) {

    // Insert a new bus or update if exists
    suspend fun insertBus(bus: Bus) {
        return busDao.insertBuses(listOf(bus))
    }

    suspend fun getAllBuses(): List<Bus> {
        return busDao.getAllBuses()
    }

    suspend fun getBusByRegistrationNumber(busNumber: String): Bus? {
        return busDao.getBusByRegistrationNumber(busNumber)
    }

    suspend fun getBusById(busId: String): Bus{
        return busDao.getBusById(busId)
    }


}
