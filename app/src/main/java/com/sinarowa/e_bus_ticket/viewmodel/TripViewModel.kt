package com.sinarowa.e_bus_ticket.viewmodel

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinarowa.e_bus_ticket.data.local.dao.BusDao
import com.sinarowa.e_bus_ticket.data.local.dao.RouteDao
import com.sinarowa.e_bus_ticket.data.local.dao.TripDetailsDao
import com.sinarowa.e_bus_ticket.data.local.entities.BusEntity
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.entities.TripDetails
import com.sinarowa.e_bus_ticket.data.repository.ExpenseRepository
import com.sinarowa.e_bus_ticket.data.repository.LocationRepository
import com.sinarowa.e_bus_ticket.data.repository.TicketRepository
import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import com.sinarowa.e_bus_ticket.utils.ReportUtils
import com.sinarowa.e_bus_ticket.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    private val routeDao: RouteDao,
    private val busDao: BusDao,
    private val tripDao: TripDetailsDao,
    private val tripRepository: TripRepository,
    private val ticketRepository: TicketRepository,
    private val expensesRepository: ExpenseRepository,
    private val locationRepository: LocationRepository
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
    val selectedTrip: StateFlow<TripDetails?> = _selectedTrip.asStateFlow()

    private val _tripReport = MutableStateFlow<TripSale?>(null)
    val tripReport: StateFlow<TripSale?> = _tripReport.asStateFlow()

    init {
        Log.d("TripViewModel", "Initializing ViewModel, loading routes and buses")
        loadRoutes()
        loadBuses()
    }

    fun loadTripById(tripId: String) {
        viewModelScope.launch {
            val trip = tripDao.getTripById(tripId)
            _selectedTrip.value = trip
        }
    }

    suspend fun getBusSeats(busName: String): Int? {
        return withContext(Dispatchers.IO) {
            busDao.getBusByName(busName)?.totalSeats
        }
    }

    suspend fun endTrip(tripId: String) {
        tripRepository.endTripById(tripId, 1, TimeUtils.getFormattedTimestamp())
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

    fun generateTripId(routeName: String): String {
        val timestamp = SimpleDateFormat("yyMMdd-HHmmss", Locale.getDefault()).format(Date())
        val routeCode = routeName.uppercase()
        return "$timestamp-$routeCode"
    }

    data class TripSale(
        val tripId: String,
        val routeName: String,
        val totalTickets: Int,
        val totalSales: Double,
        val totalExpenses: Double,
        val netSales: Double,
        val routeBreakdown: List<RouteBreakdown>,
        val expenseBreakdown: List<ExpenseBreakdown>,
        val firstTicketNumber: String = "0001",
        val lastTicketNumber: String = "0001",
        val firstTicketTime: String = "N/A",
        val lastTicketTime: String = "N/A"
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
                firstTicketNumber = firstTicket?.ticketId?.toString() ?: "0001",
                lastTicketNumber = lastTicket?.ticketId?.toString() ?: "0001",
                firstTicketTime = firstTicket?.creationTime ?: "N/A",
                lastTicketTime = lastTicket?.creationTime ?: "N/A"
            )
        }
    }

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN"
    }

    fun getDeviceName(): String = "${Build.MANUFACTURER} ${Build.MODEL}"

    fun generateTripSalesReport(context: Context, tripId: String): String {
        val tripReport = _tripReport.value ?: return "No trip data available."
        val totalCash = tripReport.totalSales
        val deviceId = getDeviceId(context)
        val device = getDeviceName()

        return ReportUtils.generateDailySalesReport(
            companyName = "GOVASBURG SERVICES",
            date = TimeUtils.getFormattedTimestamp(),
            deviceId = deviceId,
            tripsCount = 1,
            deviceName = device,
            tripId = tripId,
            totalTickets = tripReport.totalTickets,
            cancelledTickets = 0,
            firstTicketNumber = tripReport.firstTicketNumber,
            lastTicketNumber = tripReport.lastTicketNumber,
            firstTicketTime = tripReport.firstTicketTime,
            lastTicketTime = tripReport.lastTicketTime,
            ticketDetails = tripReport.routeBreakdown.flatMap { it.ticketBreakdown }
                .groupBy { it.type }
                .mapValues { (_, tickets) -> Pair(tickets.sumOf { it.count }, tickets.sumOf { it.amount }) },
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
                val tripId = generateTripId(route.name)

                if (activeTrip == null) {
                    val newTrip = TripDetails(
                        tripId = tripId,
                        routeId = route.routeId,
                        routeName = route.name,
                        busId = bus.busId,
                        busName = bus.busName
                    )
                    tripDao.insertTrip(newTrip)
                    _activeTrip.value = newTrip
                    locationRepository.startTrackingLocation(tripId)
                } else {
                    Log.e("TripViewModel", "Cannot create a new trip while one is active")
                }
            } catch (e: Exception) {
                Log.e("TripViewModel", "Error creating trip: ${e.message}")
            }
        }
    }
}