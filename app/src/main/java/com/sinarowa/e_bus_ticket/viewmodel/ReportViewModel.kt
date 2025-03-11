package com.sinarowa.e_bus_ticket.viewmodel

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ReportState(
    val selectedTrip: TripWithRoute? = null,
    val tickets: List<Ticket> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val totalSales: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netSales: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val reportType: String = "Check", // "Check", "Daily", "Trip"
    val ticketSummary: Map<String, Pair<Int, Double>> = emptyMap() // 🔥 NEW FIELD
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
                val trip = withContext(Dispatchers.IO) { tripRepository.getActiveTripWithRoute() }
                val tickets = withContext(Dispatchers.IO) { ticketRepository.getAllTickets() }

                if (trip != null) {
                    _state.value = _state.value.copy(
                        selectedTrip = trip,
                        tickets = tickets,
                        totalSales = calculateTotalSales(tickets),
                        ticketSummary = calculateTicketSummary(tickets), // ✅ Add ticket summary
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
                val trip = withContext(Dispatchers.IO) { tripRepository.getActiveTripWithRoute() }
                val tickets = withContext(Dispatchers.IO) { ticketRepository.getAllTickets() }
                val expenses = withContext(Dispatchers.IO) { expenseRepository.getAllExpenses() }

                _state.value = _state.value.copy(
                    selectedTrip = trip,
                    tickets = tickets,
                    expenses = expenses,
                    totalSales = calculateTotalSales(tickets),
                    totalExpenses = expenses.sumOf { it.amount },
                    netSales = calculateTotalSales(tickets) - expenses.sumOf { it.amount },
                    ticketSummary = calculateTicketSummary(tickets), // ✅ Add ticket summary
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
                val trip = withContext(Dispatchers.IO) { tripRepository.getActiveTripWithRoute() }
                val tickets = withContext(Dispatchers.IO) { ticketRepository.getAllTickets() }

                if (trip != null) {
                    _state.value = _state.value.copy(
                        selectedTrip = trip,
                        tickets = tickets,
                        totalSales = calculateTotalSales(tickets),
                        ticketSummary = calculateTicketSummary(tickets), // ✅ Add ticket summary
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

    // 🧮 Helper function to calculate total sales
    private fun calculateTotalSales(tickets: List<Ticket>): Double {
        return tickets.filter { it.status == TicketStatus.VALID }.sumOf { it.amount }
    }

    // 🔥 NEW FUNCTION: Calculate ticket summary dynamically
    private fun calculateTicketSummary(tickets: List<Ticket>): Map<String, Pair<Int, Double>> {
        return tickets
            .filter { it.status == TicketStatus.VALID }
            .groupBy { it.paymentCategory }  // Group by category (Adult, Child, $1 Short, etc.)
            .mapValues { (_, ticketList) ->
                Pair(ticketList.size, ticketList.sumOf { it.amount }) // Count and total amount per category
            }
    }
}

