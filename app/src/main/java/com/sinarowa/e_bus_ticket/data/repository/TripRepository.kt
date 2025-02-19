package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.TripDetailsDao
import com.sinarowa.e_bus_ticket.data.local.entities.TripDetails
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TripRepository @Inject constructor(
    private val tripDao: TripDetailsDao
) {
    /**
     * Fetch trips stored locally in the database (NO API CALLS)
     */
    fun getAllTrips(): Flow<List<TripDetails>> = tripDao.getAllTrips()


    fun getAllActiveTrips(): Flow<List<TripDetails>> = tripDao.getAllActiveTrips()

    /**
     * Fetch a single trip by ID from the database
     */
    suspend fun getTripById(tripId: String): TripDetails? {
        return tripDao.getTripById(tripId)
    }


    suspend fun endTripById(tripId: String,isComplete: Int,endTripCompleteTime: String){
        return tripDao.endTrip(tripId,isComplete,endTripCompleteTime)
    }



    /**
     * Inserts a trip into the database
     */
    suspend fun insertTrip(trip: TripDetails) {
        tripDao.insertTrip(trip)
    }

    /**
     * Clears all trips (for resetting the database)
     */
    suspend fun clearTrips() {
        tripDao.clearTrips()
    }
}
