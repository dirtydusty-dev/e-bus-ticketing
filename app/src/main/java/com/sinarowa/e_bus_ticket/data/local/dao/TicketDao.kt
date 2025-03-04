package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.enums.SyncStatus
import com.sinarowa.e_bus_ticket.domain.models.PriceWithTicket
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {

    @Transaction
    @Query("SELECT * FROM tickets WHERE ticket_tripId = :tripId")
    suspend fun getTicketsWithPrices(tripId: String): List<PriceWithTicket>

    @Query("SELECT * FROM tickets WHERE ticket_tripId = :tripId AND syncStatus = :status")
    suspend fun getUnsyncedTickets(tripId: String, status: SyncStatus): List<Ticket>

    @Update
    suspend fun updateTicketSyncStatus(ticket: Ticket)

}
