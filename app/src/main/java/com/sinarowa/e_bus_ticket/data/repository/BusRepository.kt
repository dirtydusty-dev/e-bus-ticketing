package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.BusDao
import com.sinarowa.e_bus_ticket.data.local.entities.BusEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BusRepository @Inject constructor(private val busDao: BusDao) {

    /*fun getBusById(busId: String): Flow<BusEntity?> = busDao.getBusById(busId)*/

}