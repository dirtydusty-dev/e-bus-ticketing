package com.sinarowa.e_bus_ticket.viewmodel

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.entities.Expense
import com.sinarowa.e_bus_ticket.data.repository.TicketRepository
import com.sinarowa.e_bus_ticket.data.repository.ExpenseRepository
import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import com.sinarowa.e_bus_ticket.domain.models.ReportState
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import com.sinarowa.e_bus_ticket.utils.ReportUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val ticketRepository: TicketRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> get() = _state.asStateFlow()

    /**
     * 🚀 Load Daily Sales Report
     */
    fun loadDailySalesReport(context: Context) {
        viewModelScope.launch {
            Log.d("ReportViewModel", "🟢 Loading Daily Sales Report...")
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                val trip = withContext(Dispatchers.IO) { tripRepository.getActiveTripWithRoute() }
                val tickets = withContext(Dispatchers.IO) { ticketRepository.getAllTickets() }
                val expenses = withContext(Dispatchers.IO) { expenseRepository.getAllExpenses() }

                val totalSales = tickets.sumOf { it.amount }
                val totalExpenses = expenses.sumOf { it.amount }
                val netSales = totalSales - totalExpenses

                Log.d("ReportViewModel", "✅ Trip: ${trip?.trip?.tripId ?: "No Active Trip"}")
                Log.d("ReportViewModel", "✅ Tickets: ${tickets.size}, Total Sales: $totalSales")
                Log.d("ReportViewModel", "✅ Expenses: ${expenses.size}, Total Expenses: $totalExpenses")

                val formattedReport = ReportUtils.generateDailySalesReport(
                    companyName = "Speedlada Travels Limited",
                    date = getFormattedDate(),
                    deviceId = getDeviceId(context),
                    tripId = trip?.trip?.tripId ?: "N/A",
                    tripsCount = 1,
                    deviceName = getDeviceName(),
                    totalTickets = tickets.size,
                    cancelledTickets = 0,
                    firstTicketNumber = tickets.firstOrNull()?.ticketId?.toString() ?: "0001",
                    lastTicketNumber = tickets.lastOrNull()?.ticketId?.toString() ?: "0001",
                    firstTicketTime = tickets.firstOrNull()?.creationTime ?: "N/A",
                    lastTicketTime = tickets.lastOrNull()?.creationTime ?: "N/A",
                    ticketDetails = tickets.groupBy { it.paymentCategory ?: "Unknown" }
                        .mapValues { (_, list) -> Pair(list.size, list.sumOf { it.amount }) },
                    paymentDetails = mapOf("Cash" to Pair(tickets.size, totalSales)),
                    tripSales = listOf((trip?.route?.route?.routeName ?: "Unknown") to totalSales),
                    expenses = expenses.groupBy { it.expenseType }
                        .mapValues { (_, list) -> list.sumOf { it.amount } }
                )

                _state.value = _state.value.copy(
                    selectedTrip = trip,
                    tickets = tickets,
                    expenses = expenses,
                    totalSales = totalSales,
                    totalExpenses = totalExpenses,
                    netSales = netSales,
                    formattedReport = formattedReport,
                    isLoading = false,
                    reportType = "Daily"
                )

                Log.d("ReportViewModel", "🟢 Daily Sales Report Generated Successfully!")

            } catch (e: Exception) {
                Log.e("ReportViewModel", "❌ Error loading Daily Sales Report: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading daily sales report: ${e.message}"
                )
            }
        }
    }

    /**
     * 🚀 Load Trip Report
     */
    fun loadTripReport(context: Context) {
        viewModelScope.launch {
            Log.d("ReportViewModel", "🟢 Loading Trip Report...")
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                val trip = withContext(Dispatchers.IO) { tripRepository.getActiveTripWithRoute() }
                val tickets = withContext(Dispatchers.IO) { ticketRepository.getAllTickets() }

                val totalSales = tickets.sumOf { it.amount }

                Log.d("ReportViewModel", "✅ Trip: ${trip?.trip?.tripId ?: "No Active Trip"}")
                Log.d("ReportViewModel", "✅ Tickets: ${tickets.size}, Total Sales: $totalSales")

                val formattedReport = ReportUtils.generateTripSalesReport(
                    companyName = "Speedlada Travels Limited",
                    date = getFormattedDate(),
                    deviceId = getDeviceId(context),
                    tripId = trip?.trip?.tripId ?: "N/A",
                    routeName = trip?.route?.route?.routeName ?: "Unknown",
                    totalTickets = tickets.size,
                    cancelledTickets = 0,
                    firstTicketNumber = tickets.firstOrNull()?.ticketId?.toString() ?: "0001",
                    lastTicketNumber = tickets.lastOrNull()?.ticketId?.toString() ?: "0001",
                    firstTicketTime = tickets.firstOrNull()?.creationTime ?: "N/A",
                    lastTicketTime = tickets.lastOrNull()?.creationTime ?: "N/A",
                    ticketDetails = tickets.groupBy { it.paymentCategory ?: "Unknown" }
                        .mapValues { (_, list) -> Pair(list.size, list.sumOf { it.amount }) },
                    paymentDetails = mapOf("Cash" to Pair(tickets.size, totalSales)),
                    tripSales = listOf((trip?.route?.route?.routeName ?: "Unknown") to totalSales)
                )

                _state.value = _state.value.copy(
                    selectedTrip = trip,
                    tickets = tickets,
                    totalSales = totalSales,
                    formattedReport = formattedReport,
                    isLoading = false,
                    reportType = "Trip"
                )

                Log.d("ReportViewModel", "🟢 Trip Report Generated Successfully!")

            } catch (e: Exception) {
                Log.e("ReportViewModel", "❌ Error loading Trip Report: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading trip report: ${e.message}"
                )
            }
        }
    }

    /**
     * 🕵️ Get Device ID
     */
    private fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN"
    }

    /**
     * 📱 Get Device Name
     */
    private fun getDeviceName(): String {
        return "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
    }

    /**
     * 📅 Get Formatted Date
     */
    private fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yy EEE HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
