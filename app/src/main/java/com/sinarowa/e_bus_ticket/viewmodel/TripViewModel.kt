package com.sinarowa.e_bus_ticket.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinarowa.e_bus_ticket.data.local.dao.RouteDao
import com.sinarowa.e_bus_ticket.data.local.dao.BusDao
import com.sinarowa.e_bus_ticket.data.local.dao.TripDetailsDao
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.data.local.entities.BusEntity
import com.sinarowa.e_bus_ticket.data.local.entities.Expense
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.entities.TripDetails
import com.sinarowa.e_bus_ticket.data.repository.ExpenseRepository
import com.sinarowa.e_bus_ticket.data.repository.TicketRepository
import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import com.sinarowa.e_bus_ticket.utils.TimeUtils.getFormattedTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

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

    fun generateTicketId(routeName: String): String {
        val prefix = routeName.uppercase() // First 3 letters of route name
        val timestamp = System.currentTimeMillis() // Unique time-based ID
        val randomPart = UUID.randomUUID().toString().takeLast(4) // Random for extra uniqueness
        return "$prefix-$timestamp-$randomPart"
    }

    fun generateTicketIdFallback(): String {
        return "UNKNOWN-${System.currentTimeMillis()}-${UUID.randomUUID().toString().takeLast(4)}"
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
        val expenseBreakdown: List<ExpenseBreakdown>
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
    fun fetchTripSales() {
        viewModelScope.launch {
            tripRepository.getAllTrips().collect { trips ->
                val sales = trips.map { trip ->
                    val tickets = ticketRepository.getTicketsByTrip(trip.tripId).first()
                    val expenses = expensesRepository.getExpensesByTrip(trip.tripId).first()

                    // Group tickets by (from -> to)
                    val routeBreakdown = tickets.groupBy { it.fromStop to it.toStop }
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

                    // ✅ Group expenses by type
                    val expenseBreakdown = expenses.groupBy { it.type }
                        .map { (type, expenseList) ->
                            ExpenseBreakdown(type, expenseList.size, expenseList.sumOf { it.amount })
                        }

                    TripSale(
                        tripId = trip.tripId,
                        routeName = trip.routeName,
                        totalTickets = tickets.size,
                        totalSales = tickets.sumOf { it.price },
                        totalExpenses = expenses.sumOf { it.amount },
                        netSales = tickets.sumOf { it.price } - expenses.sumOf { it.amount },
                        routeBreakdown = routeBreakdown,
                        expenseBreakdown = expenseBreakdown
                    )
                }
                _tripSales.value = sales
            }
        }
    }


    fun createTrip(route: RouteEntity, bus: BusEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val activeTrip = tripDao.getActiveTrip()

                if (activeTrip == null) {
                    val newTrip = TripDetails(
                        tripId = UUID.randomUUID().toString(),
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
