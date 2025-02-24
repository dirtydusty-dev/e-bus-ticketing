package com.sinarowa.e_bus_ticket.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.repository.LocationRepository
import com.sinarowa.e_bus_ticket.data.repository.PriceRepository
import com.sinarowa.e_bus_ticket.data.repository.RouteRepository
import com.sinarowa.e_bus_ticket.data.repository.TicketRepository
import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val locationRepository: LocationRepository,
    private val priceRepository: PriceRepository,
    private val routeRepository: RouteRepository,
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _selectedTripId = MutableStateFlow<String?>(null)
    val selectedTripId: StateFlow<String?> = _selectedTripId.asStateFlow()

    fun setTripId(tripId: String) {
        _selectedTripId.value = tripId
    }

    fun getTripId(): String? = _selectedTripId.value

    val ticketCountSeat: StateFlow<Int> = _selectedTripId
        .filterNotNull()
        .flatMapLatest { tripId ->
            ticketRepository.getNonLuggageTicketCount(tripId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val luggageCount: StateFlow<Int> = _selectedTripId
        .filterNotNull()
        .flatMapLatest { tripId ->
            ticketRepository.getLuggageTicketCount(tripId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _departedCount = MutableStateFlow(0)
    val departedCount: StateFlow<Int> = _departedCount.asStateFlow()

    fun refreshDepartedCount(tripId: String) {
        viewModelScope.launch {
            val count = ticketRepository.getDepartedCustomerCount(tripId)
            _departedCount.value = count
        }
    }

    private val _ticketPrice = MutableStateFlow(0.0)
    val ticketPrice = _ticketPrice.asStateFlow()

    private val _routeStops = MutableStateFlow<List<String>>(emptyList())
    val routeStops = _routeStops.asStateFlow()

    private val _generatedTicketId = MutableLiveData<String>()
    val generatedTicketId: LiveData<String> = _generatedTicketId

    fun insertTicket(ticket: Ticket) {
        viewModelScope.launch {
            println("Inserting ticket: $ticket")
            ticketRepository.insertTicket(ticket)
            println("Post-insert ticketCountSeat: ${ticketCountSeat.value}, luggageCount: ${luggageCount.value}")
        }
    }

    suspend fun generateTicketId(tripId: String): Int {
        val lastTicketNumber = ticketRepository.getLastTicketNumber(tripId)
        return lastTicketNumber + 1
    }

    fun fetchRouteStops(tripId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val tripDetails = tripRepository.getTripById(tripId)
            val routeDetails = tripDetails?.let { routeRepository.getRouteById(it.routeId) }
            _routeStops.value = routeDetails?.stops?.split(",")?.map { it.trim() } ?: emptyList()
        }
    }

    suspend fun getPrice(fromCity: String, toCity: String, ticketType: String): Double {
        return withContext(Dispatchers.IO) {
            val priceEntity = priceRepository.getPrice(fromCity, toCity)
            when (ticketType) {
                "Adult" -> priceEntity?.adultPrice ?: 0.0
                "Child" -> priceEntity?.childPrice ?: 0.0
                else -> {
                    val shortMatch = Regex("\\$(\\d+) Short").matchEntire(ticketType)
                    if (shortMatch != null) {
                        when (shortMatch.groupValues[1]) {
                            "1" -> priceEntity?.dollarShort ?: 0.0
                            "2" -> priceEntity?.twoDollarShort ?: 0.0
                            else -> 0.0
                        }
                    } else 0.0
                }
            }
        }
    }

    suspend fun getCityFromCoordinates(tripId: String): String {
        return withContext(Dispatchers.IO) {
            Log.d("LOCATION_PROCESS", "🔄 Fetching city from coordinates...")
            val cityName = locationRepository.getBestTicketLocation(tripId)
            Log.d("LOCATION_PROCESS", "✅ Determined city: $cityName")
            cityName
        }
    }

    fun getAllTickets(tripId: String): Flow<List<Ticket>> = ticketRepository.getTicketsByTrip(tripId)

    fun getTicketsByCity(): Flow<Map<String, List<Ticket>>> {
        return ticketRepository.getAllTickets().map { tickets -> tickets.groupBy { it.fromStop } }
    }

    suspend fun cancelTicket(ticketId: String, cancelReason: String, activeTripId: String): Boolean {
        val ticket = ticketRepository.getTicketById(ticketId)
        return if (ticket != null && ticket.tripId == activeTripId) {
            ticketRepository.cancelTicket(ticketId, 1, cancelReason)
            true
        } else false
    }

    fun getDepartedCustomerCount(tripId: String): Int {
        return ticketRepository.getTicketsByTrip(tripId)
            .map { tickets -> tickets.count { it.status == 1 } }
            .stateIn(viewModelScope, SharingStarted.Lazily, 0).value
    }

    suspend fun getStationSales(tripId: String): Map<String, Pair<Int, Double>> {
        return ticketRepository.calculateStationSales(tripId)
    }

    suspend fun getTicketBreakdown(tripId: String): Map<Pair<String, String>, Pair<Int, Double>> {
        return ticketRepository.calculateTicketBreakdown(tripId)
    }
}