package com.sinarowa.e_bus_ticket.data.repository

import android.location.Location
import com.sinarowa.e_bus_ticket.data.local.dao.TicketCounterDao
import com.sinarowa.e_bus_ticket.data.local.dao.TicketDao
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.entities.TicketCounter
import com.sinarowa.e_bus_ticket.data.local.entities.TicketSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TicketRepository @Inject constructor(
    private val ticketDao: TicketDao,
    private val ticketCounterDao: TicketCounterDao
) {


    suspend fun getDepartedCustomerCount(tripId: String): Int {
        return ticketDao.getDepartedCustomerCount(tripId)
    }


    fun getFirstTicket(tripId: String): TicketSummary? = ticketDao.getFirstTicket(tripId)

    fun getLastTicket(tripId: String): TicketSummary? = ticketDao.getLastTicket(tripId)

    suspend fun getLastTicketNumber(tripId: String): Int {
        return withContext(Dispatchers.IO) {
            ticketCounterDao.getLastTicketNumber(tripId) ?: 0
        }
    }

    suspend fun updateLastTicketNumber(tripId: String, newNumber: Int) {
        withContext(Dispatchers.IO) {
            ticketCounterDao.insertOrUpdate(TicketCounter(tripId, newNumber))
        }
    }

    fun getTicketsByTrip(tripId: String): Flow<List<Ticket>> = ticketDao.getTicketsByTrip(tripId)

    suspend fun insertTicket(ticket: Ticket) {
        ticketDao.insertTicket(ticket)
        updateLastTicketNumber(ticket.tripId, ticket.ticketId)
    }

    suspend fun deleteAllTickets() {
        ticketDao.clearTickets()
    }

    suspend fun getTicketById(ticketId: String): Ticket? = ticketDao.getTicketById(ticketId)

    fun getAllTickets(): Flow<List<Ticket>> = ticketDao.getAllTickets()

    suspend fun cancelTicket(ticketId: String, isCancelled: Int, cancelReason: String) {
        ticketDao.updateTicketCancellation(ticketId, isCancelled, cancelReason)
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

    fun getNonLuggageTicketCount(tripId: String): Flow<Int> = ticketDao.getNonLuggageTicketCount(tripId)

    fun getLuggageTicketCount(tripId: String): Flow<Int> = ticketDao.getLuggageTicketCount(tripId)
}