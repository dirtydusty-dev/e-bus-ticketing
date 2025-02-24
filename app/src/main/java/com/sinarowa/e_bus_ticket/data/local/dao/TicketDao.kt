package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.entities.TicketSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {

    @Query("SELECT ticketId, creationTime FROM tickets WHERE tripId = :tripId ORDER BY ticketId ASC LIMIT 1")
    fun getFirstTicket(tripId: String): TicketSummary?

    @Query("SELECT ticketId, creationTime FROM tickets WHERE tripId = :tripId ORDER BY ticketId DESC LIMIT 1")
    fun getLastTicket(tripId: String): TicketSummary?

    @Query("SELECT COUNT(*) FROM tickets WHERE tripId = :tripId AND status = 1")
    suspend fun getDepartedCustomerCount(tripId: String): Int

    @Query("SELECT * FROM tickets")
    fun getAllTickets(): Flow<List<Ticket>>

    @Query("SELECT * FROM tickets WHERE tripId = :tripId")
    fun getTicketsByTrip(tripId: String): Flow<List<Ticket>>

    @Query("SELECT * FROM tickets WHERE tripId = :tripId")
    suspend fun getTicketsForTrip(tripId: String): List<Ticket>

    @Query("SELECT COUNT(*) FROM tickets WHERE tripId = :tripId")
    fun getTicketCountForTrip(tripId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM tickets WHERE tripId = :tripId AND ticketType != 'Luggage'")
    fun getNonLuggageTicketCount(tripId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM tickets WHERE tripId = :tripId AND ticketType = 'Luggage'")
    fun getLuggageTicketCount(tripId: String): Flow<Int>

    @Query("UPDATE tickets SET status = 1 WHERE tripId = :tripId AND toStop = :destination AND status = 0")
    suspend fun updateDepartedTickets(tripId: String, destination: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: Ticket)

    @Query("DELETE FROM tickets")
    suspend fun clearTickets()

    @Query("SELECT * FROM tickets WHERE ticketId = :ticketId")
    suspend fun getTicketById(ticketId: String): Ticket?

    @Query("UPDATE tickets SET isCancelled = :isCancelled, cancelReason = :cancelReason WHERE ticketId = :ticketId")
    suspend fun updateTicketCancellation(ticketId: String, isCancelled: Int, cancelReason: String)
}