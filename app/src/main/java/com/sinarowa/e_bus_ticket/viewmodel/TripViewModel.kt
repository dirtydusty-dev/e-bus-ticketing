package com.sinarowa.e_bus_ticket.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinarowa.e_bus_ticket.data.local.dao.RouteDao
import com.sinarowa.e_bus_ticket.data.local.dao.BusDao
import com.sinarowa.e_bus_ticket.data.local.dao.TripDetailsDao
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.data.local.entities.BusEntity
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.entities.TripDetails
import com.sinarowa.e_bus_ticket.data.repository.ExpenseRepository
import com.sinarowa.e_bus_ticket.data.repository.TicketRepository
import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import com.sinarowa.e_bus_ticket.utils.ReportUtils
import com.sinarowa.e_bus_ticket.utils.TimeUtils.getFormattedTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import android.provider.Settings
import android.content.Context
import android.os.Build

@HiltViewModel
class TripViewModel @Inject constructor(
    private val routeDao: RouteDao,
    private val busDao: BusDao,
    private val tripDao: TripDetailsDao,
    private val tripRepository: TripRepository,
    private val ticketRepository: TicketRepository,
    private val expensesRepository: ExpenseRepository
) : ViewModel() {

    private val _routes = MutableStateFlow<List<RouteEntity>>(emptyList())
    val routes = _routes.asStateFlow()

    private val _buses = MutableStateFlow<List<BusEntity>>(emptyList())
    val buses = _buses.asStateFlow()

    private val _activeTrip = MutableStateFlow<TripDetails?>(null)
    val activeTrip = _activeTrip.asStateFlow()

    val trips: StateFlow<List<TripDetails>> = tripRepository.getAllActiveTrips()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedTrip = MutableStateFlow<TripDetails?>(null)
    val selectedTrip: StateFlow<TripDetails?> = _selectedTrip

    private val _tripReport = MutableStateFlow<TripSale?>(null)
    val tripReport: StateFlow<TripSale?> = _tripReport




    init {
        Log.d("TripViewModel", "Initializing ViewModel, loading routes and buses")
        loadRoutes()
        loadBuses()
    }

    fun loadTripById(tripId: String) {
        viewModelScope.launch {
            _selectedTrip.value = tripDao.getTripById(tripId)
        }
    }

    /*fun generateTicketId(routeName: String): String {
        val prefix = routeName.uppercase() // First 3 letters of route name
        val timestamp = System.currentTimeMillis() // Unique time-based ID
        val randomPart = UUID.randomUUID().toString().takeLast(4) // Random for extra uniqueness
        return "$prefix-$timestamp-$randomPart"
    }

    fun generateTicketIdFallback(): String {
        return "UNKNOWN-${System.currentTimeMillis()}-${UUID.randomUUID().toString().takeLast(4)}"
    }

    fun generateRouteCode(routeName: String): String {
        return routeName.take(3).uppercase() // Example: Harare → HRE
    }

    fun generateBusCode(busName: String): String {
        return busName.take(3).uppercase() // Example: XYZ Bus → XYZ
    }*/



    fun generateRouteCode(routeName: String): String {
        return routeName.uppercase() // Example: Harare → HRE, Bulawayo → BUL
    }

    fun generateTripId(routeName: String): String {
        // Get current timestamp
        val timestamp = SimpleDateFormat("yyMMdd-HHmmss", Locale.getDefault()).format(Date())

        // Generate the route short code
        val routeCode = generateRouteCode(routeName)

        // Combine them to create trip ID
        return "$timestamp-$routeCode"
    }






    suspend fun endTrip(tripId: String) {
        tripRepository.endTripById(tripId, 1, getFormattedTimestamp())
    }

    fun loadRoutes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _routes.value = routeDao.getAllRoutes()
            } catch (e: Exception) {
                Log.e("TripViewModel", "Error loading routes: ${e.message}")
            }
        }
    }

    fun loadBuses() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _buses.value = busDao.getAllBuses()
            } catch (e: Exception) {
                Log.e("TripViewModel", "Error loading buses: ${e.message}")
            }
        }
    }

    private val _tripSales = MutableStateFlow<List<TripSale>>(emptyList())
    val tripSales: StateFlow<List<TripSale>> = _tripSales

    data class TripSale(
        val tripId: String,
        val routeName: String,
        val totalTickets: Int,
        val totalSales: Double,
        val totalExpenses: Double,
        val netSales: Double,
        val routeBreakdown: List<RouteBreakdown>,
        val expenseBreakdown: List<ExpenseBreakdown>,
        val firstTicketNumber: String = "0001",  // ✅ Add Default Value
        val lastTicketNumber: String = "0001",   // ✅ Add Default Value
        val firstTicketTime: String = "N/A",     // ✅ Add Default Value
        val lastTicketTime: String = "N/A"       // ✅ Add Default Value
    )

    data class RouteBreakdown(
        val fromCity: String,
        val toCity: String,
        val ticketBreakdown: List<TicketBreakdown>
    )

    data class TicketBreakdown(
        val type: String,
        val count: Int,
        val amount: Double
    )

    data class ExpenseBreakdown(
        val type: String,
        val count: Int,
        val totalAmount: Double
    )

    /**
     * ✅ **Fetch Sales & Expense Breakdown**
     */
    fun fetchTripSalesById(tripId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val trip = tripRepository.getTripById(tripId) ?: return@launch

            val ticketsFlow = ticketRepository.getTicketsByTrip(trip.tripId)
            val expensesFlow = expensesRepository.getExpensesByTrip(trip.tripId)

            val tickets = ticketsFlow.firstOrNull() ?: emptyList()
            val expenses = expensesFlow.firstOrNull() ?: emptyList()

            val routeBreakdown = generateRouteBreakdown(trip.tripId, tickets)

            val expenseBreakdown = expenses.groupBy { it.type }
                .map { (type, expenseList) ->
                    ExpenseBreakdown(type, expenseList.size, expenseList.sumOf { it.amount })
                }

            // ✅ Fetch First & Last Ticket Details
            val firstTicket = ticketRepository.getFirstTicket(tripId)
            val lastTicket = ticketRepository.getLastTicket(tripId)

            _tripReport.value = TripSale(
                tripId = trip.tripId,
                routeName = trip.routeName,
                totalTickets = tickets.size,
                totalSales = tickets.sumOf { it.price },
                totalExpenses = expenses.sumOf { it.amount },
                netSales = tickets.sumOf { it.price } - expenses.sumOf { it.amount },
                routeBreakdown = routeBreakdown,
                expenseBreakdown = expenseBreakdown,
                firstTicketNumber = firstTicket?.ticketId ?: "0001",
                lastTicketNumber = lastTicket?.ticketId ?: "0001",
                firstTicketTime = firstTicket?.creationTime ?: "N/A",
                lastTicketTime = lastTicket?.creationTime ?: "N/A"
            )
        }
    }


    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN"
    }



    fun getDeviceName(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }



    fun generateTripSalesReport(context: Context,tripId: String): String {
        val tripReport = _tripReport.value ?: return "No trip data available."

        val totalCash = tripReport.totalSales // Sum of all ticket prices
        val deviceId = getDeviceId(context)
        val device = getDeviceName();

        return ReportUtils.generateDailySalesReport(
            companyName = "GOVASBURG SERVICES",
            date = getFormattedTimestamp(),
            deviceId = deviceId,
            tripsCount = 1,
            deviceName = device,
            tripId = tripId,
            totalTickets = tripReport.totalTickets,
            cancelledTickets = 0, // TODO: Fetch actual canceled tickets count
            firstTicketNumber = tripReport.firstTicketNumber,
            lastTicketNumber = tripReport.lastTicketNumber,
            firstTicketTime = tripReport.firstTicketTime,
            lastTicketTime = tripReport.lastTicketTime,
            ticketDetails = tripReport.routeBreakdown.flatMap { it.ticketBreakdown }
                .groupBy { it.type }
                .mapValues { (_, tickets) ->
                    Pair(tickets.sumOf { it.count }, tickets.sumOf { it.amount })
                },
            paymentDetails = mapOf("Cash" to Pair(tripReport.totalTickets, totalCash)),
            tripSales = listOf(tripReport.routeName to tripReport.totalSales),
            expenses = tripReport.expenseBreakdown
                .groupBy { it.type }
                .mapValues { (_, expense) -> expense.sumOf { it.totalAmount } }
        )
    }




    fun generateRouteBreakdown(tripId: String, tickets: List<Ticket>): List<RouteBreakdown> {
        return tickets.groupBy { it.fromStop to it.toStop }
            .map { (route, tripTickets) ->
                RouteBreakdown(
                    fromCity = route.first,
                    toCity = route.second,
                    ticketBreakdown = tripTickets.groupBy { it.ticketType }
                        .map { (type, tickets) ->
                            TicketBreakdown(type, tickets.size, tickets.sumOf { it.price })
                        }
                )
            }
    }



    fun createTrip(route: RouteEntity, bus: BusEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val activeTrip = tripDao.getActiveTrip()

                if (activeTrip == null) {
                    val newTrip = TripDetails(
                        tripId = generateTripId(route.name),
                        routeId = route.routeId,
                        routeName = route.name,
                        busId = bus.busId,
                        busName = bus.busName
                    )
                    tripDao.insertTrip(newTrip)
                    _activeTrip.value = newTrip
                } else {
                    Log.e("TripViewModel", "Cannot create a new trip while one is active")
                }
            } catch (e: Exception) {
                Log.e("TripViewModel", "Error creating trip: ${e.message}")
            }
        }
    }
}
