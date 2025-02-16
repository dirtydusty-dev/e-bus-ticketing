package com.sinarowa.e_bus_ticket.data.local.dao
import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {

    @Query("SELECT * FROM tickets")
    suspend fun getAllTickets(): List<Ticket>  // ✅ Fetch all unsynced tickets

    @Query("SELECT * FROM tickets WHERE tripId = :tripId")
    fun getTicketsByTrip(tripId: String): Flow<List<Ticket>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: Ticket)

    @Query("DELETE FROM tickets")
    suspend fun clearTickets()
}
