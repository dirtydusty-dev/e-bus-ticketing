package com.sinarowa.e_bus_ticket.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinarowa.e_bus_ticket.data.local.enums.TicketStatus
import com.sinarowa.e_bus_ticket.data.repository.TicketRepository
import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import com.sinarowa.e_bus_ticket.domain.usecase.SellTicketUseCase
import com.sinarowa.e_bus_ticket.utils.LocationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TicketState(
    val fromStation: String = "",
    val destination: String = "",
    val validDestinations: List<String> = emptyList(),
    val ticketType: String = "Adult",
    val originalPrice: Double = 0.0,
    val displayPrice: Double = 0.0,
    val shortAmount: Int = 0,
    val remainingSeats: Int = 0,
    val activePassengers: Int = 0,
    val isProcessing: Boolean = false,
    val sellResult: Result<Unit>? = null
)

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val ticketRepository: TicketRepository,
    private val sellTicketUseCase: SellTicketUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TicketState())
    val state: StateFlow<TicketState> get() = _state.asStateFlow()
    private lateinit var currentTrip: TripWithRoute

    fun initializeWithTrip(trip: TripWithRoute, context: Context) {
        currentTrip = trip
        Log.d("TicketViewModel", "✅ Initialized with trip: ${trip.trip.tripId}")
        updateTripData(trip)
        updateFromStation(context)
    }

    private fun updateTripData(trip: TripWithRoute) {
        Log.d("TicketViewModel", "Trip: ${trip.trip.tripId}, Bus Capacity: ${trip.bus.capacity}, Tickets: ${trip.tickets.size}")
        val ticketCount = trip.tickets.count { it.status == TicketStatus.VALID }
        Log.d("TicketViewModel", "Ticket Count: $ticketCount")
        val currentState = _state.value
        _state.value = currentState.copy(
            remainingSeats = trip.bus.capacity - ticketCount,
            activePassengers = ticketCount,
            validDestinations = getValidDestinations(trip, currentState.fromStation)
        )
        Log.d("TicketViewModel", "Updated State: ${_state.value}")
        updatePrice()
    }

    private fun getValidDestinations(trip: TripWithRoute, fromStation: String): List<String> {
        if (fromStation.isBlank()) return emptyList()
        val stationIdToName = trip.route.stationDetails.associateBy { it.stationId }
        val sortedStations = trip.route.stops
            .sortedBy { it.stopOrder }
            .mapNotNull { stationIdToName[it.stationId]?.name }
        val currentIndex = sortedStations.indexOf(fromStation)
        return if (currentIndex != -1) sortedStations.subList(currentIndex + 1, sortedStations.size) else emptyList()
    }

    private fun updateFromStation(context: Context) {
        viewModelScope.launch {
            val location = LocationUtils.getCurrentLocation(context)
            Log.d("TicketViewModel", "Location: $location")

            if (location == null) {
                Log.w("TicketViewModel", "⚠️ Location unavailable, using default station")
                val defaultStation = currentTrip.route.stationDetails.firstOrNull()?.name ?: "Default Station"
                _state.value = _state.value.copy(
                    fromStation = defaultStation,
                    validDestinations = getValidDestinations(currentTrip, defaultStation)
                )
                updatePrice()
                return@launch
            }

            val stations = currentTrip.route.stationDetails.map { it.name }
            val stationCoordinates = currentTrip.route.stationDetails.associate { it.name to (it.latitude to it.longitude) }
            val nearestStation = LocationUtils.findNearestStation(
                location.latitude, location.longitude, stations, stationCoordinates, _state.value.fromStation
            ) ?: run {
                Log.e("TicketViewModel", "❌ Could not determine nearest station.")
                return@launch
            }

            Log.d("TicketViewModel", "📍 Nearest Station Found: $nearestStation")
            _state.value = _state.value.copy(
                fromStation = nearestStation,
                validDestinations = getValidDestinations(currentTrip, nearestStation)
            )
            updatePrice()
        }
    }

    fun setDestination(destination: String) {
        val currentState = _state.value
        if (destination.isBlank() || destination == currentState.fromStation) return
        _state.value = currentState.copy(destination = destination, shortAmount = 0)
        Log.d("TicketViewModel", "📍 Destination updated: $destination")
        updatePrice()
    }

    fun setTicketType(type: String) {
        val currentState = _state.value
        _state.value = currentState.copy(ticketType = type, shortAmount = 0)
        updatePrice()
    }

    fun setShortAmount(amount: Int) {
        val currentState = _state.value
        if (currentState.ticketType != "Adult" || amount < 0 || amount >= currentState.originalPrice) return
        _state.value = currentState.copy(shortAmount = amount)
        updatePrice()
    }

    private fun updatePrice() {
        val currentState = _state.value
        if (currentState.fromStation.isBlank() || currentState.destination.isBlank()) {
            _state.value = currentState.copy(originalPrice = 0.0, displayPrice = 0.0)
            return
        }

        val fromStationId = currentTrip.route.stationDetails.find { it.name == currentState.fromStation }?.stationId
        val toStationId = currentTrip.route.stationDetails.find { it.name == currentState.destination }?.stationId

        if (fromStationId == null || toStationId == null) {
            Log.e("TicketViewModel", "❌ Could not resolve station IDs!")
            _state.value = currentState.copy(originalPrice = 0.0, displayPrice = 0.0)
            return
        }

        viewModelScope.launch {
            val price = tripRepository.getPriceForStations(fromStationId, toStationId) ?: run {
                Log.e("TicketViewModel", "Price not found for stations $fromStationId to $toStationId")
                0.0
            }
            val newOriginalPrice = price
            val newDisplayPrice = when (currentState.ticketType) {
                "Child" -> newOriginalPrice / 2
                else -> newOriginalPrice - currentState.shortAmount
            }
            _state.value = currentState.copy(originalPrice = newOriginalPrice, displayPrice = newDisplayPrice)
            Log.d("TicketViewModel", "💰 Updated Prices: Original=$newOriginalPrice, Display=$newDisplayPrice")
        }
    }

    fun sellTicket(amount: Double) {
        val currentState = _state.value
        if (currentState.fromStation.isBlank() || currentState.destination.isBlank() || currentState.displayPrice <= 0) return

        val fromStationId = currentTrip.route.stationDetails.find { it.name == currentState.fromStation }?.stationId
        val toStationId = currentTrip.route.stationDetails.find { it.name == currentState.destination }?.stationId

        if (fromStationId == null || toStationId == null) {
            Log.e("TicketViewModel", "❌ Could not resolve station IDs for selling ticket!")
            _state.value = currentState.copy(sellResult = Result.failure(IllegalArgumentException("Invalid station IDs")), isProcessing = false)
            return
        }

        val paymentCategory = when {
            currentState.ticketType == "Child" -> "Child"
            currentState.shortAmount > 0 -> "$${currentState.shortAmount} Short"
            else -> "Adult"
        }

        Log.d("TicketViewModel", "Selling ticket: tripId=${currentTrip.trip.tripId}, from=$fromStationId, to=$toStationId, paymentCategory=$paymentCategory")

        _state.value = currentState.copy(sellResult = null, isProcessing = true)
        viewModelScope.launch {
            try {
                val result = sellTicketUseCase.execute(
                    tripId = currentTrip.trip.tripId,
                    fromStationId = fromStationId,
                    toStationId = toStationId,
                    paymentCategory = paymentCategory,
                    amount = amount
                )

                _state.value = _state.value.copy(sellResult = result, isProcessing = false)

                if (result.isSuccess) {
                    // Refresh trip data and update both view models
                    tripRepository.loadActiveTrip() // Async refresh
                    val updatedTrip = tripRepository.activeTrip.value // Get latest value
                    if (updatedTrip != null) {
                        currentTrip = updatedTrip
                        updateTripData(updatedTrip)
                        tripRepository.updateActiveTrip(updatedTrip) // Update TripViewModel's LiveData
                    } else {
                        Log.e("TicketViewModel", "❌ Failed to refresh trip data after sale")
                        tripRepository.loadActiveTrip() // Retry loading
                    }
                }

            } catch (e: Exception) {
                Log.e("TicketViewModel", "Sell ticket failed: ${e.message}")
                _state.value = _state.value.copy(sellResult = Result.failure(e), isProcessing = false)
            }
        }
    }

    fun resetFields(context: Context) {
        Log.d("TicketViewModel", "Resetting fields")
        _state.value = TicketState(
            fromStation = _state.value.fromStation,
            remainingSeats = _state.value.remainingSeats,
            activePassengers = _state.value.activePassengers,
            validDestinations = _state.value.validDestinations
        )
        updateFromStation(context)
    }
}