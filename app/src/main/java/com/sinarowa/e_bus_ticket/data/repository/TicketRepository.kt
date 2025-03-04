package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.TicketDao
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.enums.SyncStatus
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


}
