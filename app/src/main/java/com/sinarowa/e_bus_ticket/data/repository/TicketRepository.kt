package com.sinarowa.e_bus_ticket.data.repository
import com.sinarowa.e_bus_ticket.data.local.dao.TicketDao
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.entities.TripDetails
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TicketRepository @Inject constructor(
    private val ticketDao: TicketDao
) {
    fun getTicketsByTrip(tripId: String): Flow<List<Ticket>> {
        return ticketDao.getTicketsByTrip(tripId)
    }

    suspend fun insertTicket(ticket: Ticket) {
        ticketDao.insertTicket(ticket)
    }

    suspend fun deleteAllTickets() {
        ticketDao.clearTickets()
    }
    suspend fun getTicketById(ticketId: String): Ticket?{
       return ticketDao.getTicketById(ticketId)
    }

    suspend fun getAllTickets(): List<Ticket> {
        return ticketDao.getAllTickets()
    }

    suspend fun cancelTicket(ticketId: String,isCancelled: Int,cancelReason: String){
        return ticketDao.updateTicketCancellation(ticketId,isCancelled,cancelReason)
    }
}
