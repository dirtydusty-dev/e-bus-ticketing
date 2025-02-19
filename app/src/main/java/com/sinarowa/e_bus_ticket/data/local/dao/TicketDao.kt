package com.sinarowa.e_bus_ticket.data.local.dao
import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.entities.TripDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {

    @Query("SELECT * FROM tickets")
    fun getAllTickets(): Flow<List<Ticket>>  // ✅ Fetch all unsynced tickets

    @Query("SELECT * FROM tickets WHERE tripId = :tripId")
    fun getTicketsByTrip(tripId: String): Flow<List<Ticket>>

    @Query("SELECT * FROM tickets WHERE tripId = :tripId")
    suspend fun getTicketsForTrip(tripId: String): List<Ticket>

    @Query("SELECT COUNT(*) FROM tickets WHERE tripId = :tripId")
    fun getTicketCountForTrip(tripId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: Ticket)

    @Query("DELETE FROM tickets")
    suspend fun clearTickets()

    @Query("SELECT * FROM tickets WHERE ticketId = :ticketId")
    suspend fun getTicketById(ticketId: String): Ticket?

    @Query("UPDATE tickets SET isCancelled = :isCancelled, cancelReason = :cancelReason WHERE ticketId = :ticketId")
    suspend fun updateTicketCancellation(ticketId: String, isCancelled: Int, cancelReason: String)
}
