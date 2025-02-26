package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Trip
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import kotlinx.coroutines.flow.Flow


@Dao
interface TripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip): Long

    @Delete
    suspend fun deleteTrip(trip: Trip)

    @Query("SELECT * FROM trips WHERE status = 'IN_PROGRESS'")
    fun getActiveTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE routeId = :routeId")
    fun getTripsForRoute(routeId: Long): Flow<List<Trip>>

    @Query("UPDATE trips SET status = :status WHERE id = :tripId")
    suspend fun updateTripStatus(tripId: Long, status: String)

    @RewriteQueriesToDropUnusedColumns // ✅ Add this to optimize the query
    @Query("""
        SELECT trips.id, trips.routeId, trips.busId, trips.startTime, trips.stopTime, trips.status, 
               routes.routeName, routes.stations, 
               buses.busName, buses.capacity AS busCapacity,
               (SELECT COUNT(*) FROM tickets WHERE tickets.tripId = trips.id) AS ticketCount,
               (SELECT COUNT(*) FROM tickets WHERE tickets.tripId = trips.id AND tickets.status = 'ARRIVED') AS departedCount,
               (SELECT COUNT(*) FROM tickets WHERE tickets.tripId = trips.id AND tickets.type = 'LUGGAGE') AS luggageCount
        FROM trips
        INNER JOIN routes ON trips.routeId = routes.id
        INNER JOIN buses ON trips.busId = buses.id
        WHERE trips.id = :tripId
    """)
    fun getTripById(tripId: Long): Flow<TripWithRoute>

    @RewriteQueriesToDropUnusedColumns // ✅ Add this to optimize the query
    @Query("""
        SELECT trips.id, trips.routeId, trips.busId, trips.startTime, trips.stopTime, trips.status, 
               routes.routeName, routes.stations, 
               buses.busName, buses.capacity AS busCapacity,
               (SELECT COUNT(*) FROM tickets WHERE tickets.tripId = trips.id) AS ticketCount,
               (SELECT COUNT(*) FROM tickets WHERE tickets.tripId = trips.id AND tickets.status = 'ARRIVED') AS departedCount,
               (SELECT COUNT(*) FROM tickets WHERE tickets.tripId = trips.id AND tickets.type = 'LUGGAGE') AS luggageCount
        FROM trips
        INNER JOIN routes ON trips.routeId = routes.id
        INNER JOIN buses ON trips.busId = buses.id
        WHERE trips.status = 'IN_PROGRESS'
    """)
    fun getActiveTripsWithRoute(): Flow<List<TripWithRoute>>


}
