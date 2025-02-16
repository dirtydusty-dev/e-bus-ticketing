package com.sinarowa.e_bus_ticket.data.repository


import com.sinarowa.e_bus_ticket.data.local.dao.ExpenseDao
import com.sinarowa.e_bus_ticket.data.local.dao.TicketDao
import com.sinarowa.e_bus_ticket.data.remote.ExpenseData
import com.sinarowa.e_bus_ticket.data.remote.ExpenseUploadRequest
import com.sinarowa.e_bus_ticket.data.remote.RetrofitClient
import com.sinarowa.e_bus_ticket.data.remote.TicketData
import com.sinarowa.e_bus_ticket.data.remote.TicketUploadRequest
import javax.inject.Inject

class SyncRepository @Inject constructor(
    private val ticketDao: TicketDao,
    private val expenseDao: ExpenseDao
) {
    suspend fun syncTickets() {
        val offlineTickets = ticketDao.getAllTickets()
        if (offlineTickets.isNotEmpty()) {
            val ticketRequest = TicketUploadRequest(
                tickets = offlineTickets.map {
                    TicketData(
                        it.ticketId, it.tripId, it.fromStop, it.toStop, it.price, it.timestamp
                    )
                }
            )

            val response = RetrofitClient.api.uploadTickets(ticketRequest)
            if (response.isSuccessful) {
                ticketDao.clearTickets()  // Clear local tickets after successful sync
            }
        }
    }

    suspend fun syncExpenses() {
        val offlineExpenses = expenseDao.getAllExpenses()
        if (offlineExpenses.isNotEmpty()) {
            val expenseRequest = ExpenseUploadRequest(
                expenses = offlineExpenses.map {
                    ExpenseData(
                        it.expenseId, it.tripId, it.type, it.amount, it.description, it.timestamp
                    )
                }
            )

            val response = RetrofitClient.api.uploadExpenses(expenseRequest)
            if (response.isSuccessful) {
                expenseDao.clearExpenses()  // Clear local expenses after successful sync
            }
        }
    }
}
