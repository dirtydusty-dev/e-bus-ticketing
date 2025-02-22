package com.sinarowa.e_bus_ticket.data.repository
import com.sinarowa.e_bus_ticket.data.local.dao.TicketCounterDao
import com.sinarowa.e_bus_ticket.data.local.dao.TicketDao
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.entities.TicketCounter
import com.sinarowa.e_bus_ticket.data.local.entities.TicketSummary
import com.sinarowa.e_bus_ticket.data.local.entities.TripDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TicketRepository @Inject constructor(
    private val ticketDao: TicketDao,
    private val ticketCounterDao: TicketCounterDao
) {


    fun getFirstTicket(tripId: String): TicketSummary? {
        return ticketDao.getFirstTicket(tripId)
    }

    fun getLastTicket(tripId: String): TicketSummary? {
        return ticketDao.getLastTicket(tripId)
    }


    suspend fun getLastTicketNumber(tripId: String): Int {
        return withContext(Dispatchers.IO) {  // ✅ Runs in the background
            ticketCounterDao.getLastTicketNumber(tripId) ?: 0
        }
    }

    suspend fun updateLastTicketNumber(tripId: String, newNumber: Int) {
        withContext(Dispatchers.IO) { // ✅ Runs in the background
            ticketCounterDao.insertOrUpdate(TicketCounter(tripId, newNumber))
        }
    }

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

    fun getAllTickets(): Flow<List<Ticket>> {
        return ticketDao.getAllTickets()
    }

    suspend fun cancelTicket(ticketId: String,isCancelled: Int,cancelReason: String){
        return ticketDao.updateTicketCancellation(ticketId,isCancelled,cancelReason)
    }

    suspend fun calculateStationSales(tripId: String): Map<String, Pair<Int, Double>> {
        val tickets = ticketDao.getTicketsForTrip(tripId)

        return tickets.groupBy { it.fromStop }
            .mapValues { (_, ticketList) ->
                val count = ticketList.size
                val amount = ticketList.sumOf { it.price }
                count to amount
            }
    }

    suspend fun calculateTicketBreakdown(tripId: String): Map<Pair<String, String>, Pair<Int, Double>> {
        val tickets = ticketDao.getTicketsForTrip(tripId)

        return tickets.groupBy { it.fromStop to it.toStop }
            .mapValues { (_, ticketList) ->
                val count = ticketList.size
                val amount = ticketList.sumOf { it.price }
                count to amount
            }
    }
}
