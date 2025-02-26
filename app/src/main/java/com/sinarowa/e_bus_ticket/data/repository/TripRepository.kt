package com.sinarowa.e_bus_ticket.data.repository

import android.util.Log
import com.sinarowa.e_bus_ticket.data.local.dao.TripDao
import com.sinarowa.e_bus_ticket.data.local.entities.Trip
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TripRepository @Inject constructor(private val tripDao: TripDao) {

    suspend fun insertTrip(trip: Trip): Long {
        val result = tripDao.insertTrip(trip)
        Log.d("Database", "Inserted trip with ID: $result")
        return result
    }


    suspend fun deleteTrip(trip: Trip) {
        tripDao.deleteTrip(trip)
    }

    fun getTripById(tripId: Long): Flow<TripWithRoute?> {
        return tripDao.getTripById(tripId)
    }

    fun getActiveTrips(): Flow<List<Trip>> {
        return tripDao.getActiveTrips()
    }

    fun getActiveTripsWithRoute(): Flow<List<TripWithRoute>> {
        return tripDao.getActiveTripsWithRoute()
    }

    suspend fun updateTripStatus(tripId: Long, status: String) {
        tripDao.updateTripStatus(tripId, status)
    }

}
