package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.TripDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDetailsDao {

    @Query("SELECT * FROM trip_details")
    fun getAllTrips(): Flow<List<TripDetails>>

    @Query("SELECT * FROM trip_details WHERE IsComplete = 0")
    fun getAllActiveTrips(): Flow<List<TripDetails>>

    @Query("SELECT * FROM trip_details WHERE tripId = :tripId")
    suspend fun getTripById(tripId: String): TripDetails?

    @Query("SELECT * FROM trip_details WHERE IsComplete = 0")  // ✅ Get active trip (if exists)
    suspend fun getActiveTrip(): TripDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripDetails)  // ✅ Insert a single trip

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<TripDetails>)  // ✅ Insert multiple trips

    @Query("DELETE FROM trip_details")
    suspend fun clearTrips()

    @Query("UPDATE trip_details SET isComplete = :isComplete, endTripCompleteTime = :endTripCompleteTime WHERE tripId = :tripId")
    suspend fun endTrip(tripId: String, isComplete: Int, endTripCompleteTime: String)

}
