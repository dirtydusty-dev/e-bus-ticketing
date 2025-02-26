package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.TicketDao
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TicketRepository @Inject constructor(private val ticketDao: TicketDao) {

    suspend fun insertTicket(ticket: Ticket) {
        ticketDao.insertTicket(ticket)
    }

    suspend fun deleteTicket(ticket: Ticket) {
        ticketDao.deleteTicket(ticket)
    }

    fun getTicketsForTrip(tripId: Long): Flow<List<Ticket>> {
        return ticketDao.getTicketsForTrip(tripId)
    }

    fun getTotalSalesForTrip(tripId: Long): Flow<Double?> {
        return ticketDao.getTotalSalesForTrip(tripId)
    }

    fun getValidTicketCountForTrip(tripId: Long): Flow<Int> {
        return ticketDao.getValidTicketCountForTrip(tripId)
    }


    fun getTicketCountForTrip(tripId: Long): Flow<Int> {
        return ticketDao.getPassengerTicketCount(tripId)
    }

    fun getLuggageCountForTrip(tripId: Long): Flow<Int> {
        return ticketDao.getLuggageTicketCount(tripId)
    }

    fun getDepartedCountForTrip(tripId: Long): Flow<Int> {
        return ticketDao.getDepartedCountForTrip(tripId) // ✅ Fetch ARRIVED tickets
    }

    suspend fun getRouteStops(tripId: Long): List<String> {
        return ticketDao.getStopsForTrip(tripId)
    }

    suspend fun calculatePrice(startStation: String, stopStation: String): Double {
        return withContext(Dispatchers.IO) { // ✅ Run database queries in a background thread
            ticketDao.getTicketPrice(startStation, stopStation) ?: 0.0
        }
    }
}
