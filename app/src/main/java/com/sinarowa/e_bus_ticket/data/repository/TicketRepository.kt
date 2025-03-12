package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.TicketDao
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.enums.SyncStatus
import com.sinarowa.e_bus_ticket.data.local.enums.TicketStatus
import com.sinarowa.e_bus_ticket.domain.models.TicketWithRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TicketRepository @Inject constructor(private val ticketDao: TicketDao) {

    suspend fun getUnsyncedTickets(tripId: String, status: SyncStatus): List<Ticket> {
        return ticketDao.getUnsyncedTickets(tripId, status)
    }

    suspend fun updateTicketSyncStatus(ticket: Ticket) {
        ticketDao.updateTicketSyncStatus(ticket)
    }

    suspend fun insertTicket(ticket: Ticket) {
        ticketDao.insert(ticket)
    }

    suspend fun getLastTicketNumberForTrip(tripId: String): Int? {
        val lastTicket = ticketDao.getLastTicketForTrip(tripId) ?: return null
        return lastTicket.ticketId.toIntOrNull() // Convert from "0001" to 1
    }


    suspend fun getAllTickets(): List<Ticket>{
        return ticketDao.getAllTickets()
    }

    suspend fun getTicketsWithRoute(tripId: String): List<TicketWithRoute>{
        return ticketDao.getTicketsWithRoute(tripId)
    }

}
