package com.sinarowa.e_bus_ticket.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.entities.Expense
import com.sinarowa.e_bus_ticket.data.local.enums.TicketStatus
import com.sinarowa.e_bus_ticket.data.repository.TicketRepository
import com.sinarowa.e_bus_ticket.data.repository.ExpenseRepository
import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ReportState(
    val trips: TripWithRoute? = null,
    val selectedTrip: TripWithRoute? = null,
    val tickets: List<Ticket> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val totalSales: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netSales: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val reportType: String = "Check" // "Check", "Daily", "Trip"
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val ticketRepository: TicketRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> get() = _state.asStateFlow()

    fun loadCheckReport() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val trip = tripRepository.getActiveTripWithRoute()
                if (trip != null) {
                    val tickets = ticketRepository.getAllTickets()
                    _state.value = _state.value.copy(
                        selectedTrip = trip,
                        tickets = tickets,
                        totalSales = calculateTotalSales(tickets),
                        isLoading = false,
                        reportType = "Check"
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Trip not found"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading check report: ${e.message}"
                )
            }
        }
    }

    fun loadDailySalesReport() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val trips = tripRepository.getActiveTripWithRoute()
                val allTickets =  ticketRepository.getAllTickets()
                val allExpenses = expenseRepository.getAllExpenses()
                _state.value = _state.value.copy(
                    trips = trips,
                    tickets = allTickets,
                    expenses = allExpenses,
                    totalSales = calculateTotalSales(allTickets),
                    totalExpenses = allExpenses.sumOf { it.amount },
                    netSales = calculateTotalSales(allTickets) - allExpenses.sumOf { it.amount },
                    isLoading = false,
                    reportType = "Daily"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading daily sales report: ${e.message}"
                )
            }
        }
    }

    fun loadTripReport() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val trip = tripRepository.getActiveTripWithRoute()
                if (trip != null) {
                    val tickets = ticketRepository.getAllTickets()
                    _state.value = _state.value.copy(
                        selectedTrip = trip,
                        tickets = tickets,
                        totalSales = calculateTotalSales(tickets),
                        isLoading = false,
                        reportType = "Trip"
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Trip not found"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading trip report: ${e.message}"
                )
            }
        }
    }

    private fun calculateTotalSales(tickets: List<Ticket>): Double {
        return tickets.filter { it.status == TicketStatus.VALID }.sumOf { it.amount }
    }

    // Placeholder methods; replace with actual repository calls
    private suspend fun fetchTrips(): List<TripWithRoute> = emptyList()
}