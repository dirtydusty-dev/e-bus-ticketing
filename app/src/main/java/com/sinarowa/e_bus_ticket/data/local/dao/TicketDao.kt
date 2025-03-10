package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.enums.SyncStatus
import com.sinarowa.e_bus_ticket.domain.models.PriceWithTicket
import com.sinarowa.e_bus_ticket.data.local.enums.TicketStatus

@Dao
interface TicketDao {

    @Transaction
    @Query("SELECT * FROM tickets WHERE ticket_tripId = :tripId")
    suspend fun getTicketsWithPrices(tripId: String): List<PriceWithTicket>

    @Query("SELECT * FROM tickets WHERE ticket_tripId = :tripId AND syncStatus = :status")
    suspend fun getUnsyncedTickets(tripId: String, status: SyncStatus): List<Ticket>

    @Update
    suspend fun updateTicketSyncStatus(ticket: Ticket)

    @Insert
    suspend fun insert(ticket: Ticket)

    @Query("SELECT COUNT(*) FROM tickets WHERE ticket_tripId = :tripId and status = :status")
    suspend fun getTicketCount(tripId: String, status: TicketStatus): Int

    @Query("SELECT * FROM tickets WHERE ticket_tripId = :tripId ORDER BY ticketId DESC LIMIT 1")
    suspend fun getLastTicketForTrip(tripId: String): Ticket?


}
