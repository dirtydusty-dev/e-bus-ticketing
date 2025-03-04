package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Trip
import com.sinarowa.e_bus_ticket.data.local.enums.SyncStatus
import com.sinarowa.e_bus_ticket.data.local.enums.TripStatus
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import kotlinx.coroutines.flow.Flow


@Dao
interface TripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip): Long

    @Query("SELECT * FROM trips WHERE status = :status LIMIT 1")
    suspend fun getActiveTrip(status: TripStatus = TripStatus.IN_PROGRESS): Trip?

    // Query to get an active trip with its associated route
    @Transaction
    @Query("SELECT * FROM trips WHERE status = :status LIMIT 1")
    suspend fun getActiveTripWithRoute(status: TripStatus): TripWithRoute?

    @Query("UPDATE trips SET syncStatus = :status WHERE tripId = :tripId")
    suspend fun updateSyncStatus(tripId: String, status: SyncStatus)

    @Query("SELECT * FROM trips WHERE tripId = :tripId")
    suspend fun getTripById(tripId: String): Trip?

    @Update
    suspend fun updateTripStatus(trip: Trip)
}
