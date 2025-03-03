package com.sinarowa.e_bus_ticket.data.repository

import android.util.Log
import com.sinarowa.e_bus_ticket.data.local.dao.TripDao
import com.sinarowa.e_bus_ticket.data.local.entities.Trip
import com.sinarowa.e_bus_ticket.data.local.enums.TripStatus
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TripRepository @Inject constructor(private val tripDao: TripDao) {

    suspend fun createTrip(trip: Trip) {
        tripDao.insertTrip(trip)
    }

    suspend fun hasActiveTrip(): Boolean {
        return tripDao.getActiveTrip() != null
    }

    suspend fun getActiveTrip(): Trip? {
        return tripDao.getActiveTrip()
    }

    // Get active trip with its route
    suspend fun getActiveTripWithRoute(status: TripStatus): TripWithRoute? {
        return tripDao.getActiveTripWithRoute(status)
    }

}
