package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: Ticket)

    @Delete
    suspend fun deleteTicket(ticket: Ticket)

    @Query("SELECT * FROM tickets WHERE tripId = :tripId")
    fun getTicketsForTrip(tripId: Long): Flow<List<Ticket>>

    @Query("SELECT COUNT(*) FROM tickets WHERE tripId = :tripId")
    fun getTicketCountForTrip(tripId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM tickets WHERE tripId = :tripId AND status = 'VALID'")
    fun getValidTicketCountForTrip(tripId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM tickets WHERE tripId = :tripId AND type != 'LUGGAGE'")
    fun getPassengerTicketCount(tripId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM tickets WHERE tripId = :tripId AND type == 'LUGGAGE'")
    fun getLuggageTicketCount(tripId: Long): Flow<Int>


    @Query("SELECT COUNT(*) FROM tickets WHERE tripId = :tripId AND status == 'ARRIVED'")
    fun getDepartedCountForTrip(tripId: Long): Flow<Int>


    @Query("SELECT SUM(amount) FROM tickets WHERE tripId = :tripId")
    fun getTotalSalesForTrip(tripId: Long): Flow<Double?>

    @Query("SELECT stations FROM routes INNER JOIN trips ON routes.id = trips.routeId WHERE trips.id = :tripId")
    suspend fun getStopsForTrip(tripId: Long): List<String>

    @Query("SELECT amount FROM prices WHERE startStation = :from AND stopStation = :to")
    fun getTicketPrice(from: String, to: String): Double?
}
