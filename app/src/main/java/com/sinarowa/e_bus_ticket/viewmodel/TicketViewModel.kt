package com.sinarowa.e_bus_ticket.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
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

    val activeTrip: LiveData<TripWithRoute?> get() = tripRepository.activeTrip

    private val _state = MutableStateFlow(TicketState())
    val state: StateFlow<TicketState> get() = _state.asStateFlow()

    init {
        viewModelScope.launch {
            tripRepository.loadActiveTrip() // Ensure active trip is loaded initially
            activeTrip.asFlow().collect { trip ->
                if (trip != null) {
                    Log.d("TicketViewModel", "✅ Active trip updated: ${trip.trip.tripId}")
                    updateTripData(trip)
                } else {
                    Log.e("TicketViewModel", "❌ No active trip found!")
                    _state.value = _state.value.copy(validDestinations = emptyList(), remainingSeats = 0, activePassengers = 0)
                }
            }
        }
    }

    private fun updateTripData(trip: TripWithRoute) {
        val currentState = _state.value
        val ticketCount = trip.tickets.count { it.status == TicketStatus.VALID }
        _state.value = currentState.copy(
            remainingSeats = trip.bus.capacity - ticketCount,
            activePassengers = ticketCount,
            validDestinations = getValidDestinations(trip, currentState.fromStation)
        )
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

    fun updateFromStation(context: Context) {
        viewModelScope.launch {
            val location = LocationUtils.getCurrentLocation(context)

            // ✅ Ensure trip is properly loaded and assigned
            val trip = activeTrip.value ?: run {
                tripRepository.loadActiveTrip()
                activeTrip.value.also { if (it == null) Log.e("TicketViewModel", "❌ No active trip found.") }
            } ?: return@launch  // 🚀 Ensures the trip is available

            if (location == null) {
                Log.w("TicketViewModel", "⚠️ Location unavailable, using last known station or retrying...")
                val lastStation = _state.value.fromStation.takeIf { it.isNotBlank() }
                if (lastStation != null) {
                    _state.value = _state.value.copy(fromStation = lastStation, validDestinations = getValidDestinations(trip, lastStation))
                    updatePrice()
                }
                return@launch
            }

            val stations = trip.route.stationDetails.map { it.name }
            val stationCoordinates = trip.route.stationDetails.associate { it.name to (it.latitude to it.longitude) }
            val nearestStation = LocationUtils.findNearestStation(
                location.latitude, location.longitude, stations, stationCoordinates, _state.value?.fromStation
            ) ?: run {
                Log.e("TicketViewModel", "❌ Could not determine nearest station.")
                return@launch
            }

            Log.d("TicketViewModel", "📍 Nearest Station Found: $nearestStation")
            _state.value = _state.value.copy(fromStation = nearestStation, validDestinations = getValidDestinations(trip, nearestStation))
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
        val trip = activeTrip.value ?: return
        if (currentState.fromStation.isBlank() || currentState.destination.isBlank()) {
            _state.value = currentState.copy(originalPrice = 0.0, displayPrice = 0.0)
            return
        }

        val fromStationId = trip.route.stationDetails.find { it.name == currentState.fromStation }?.stationId
        val toStationId = trip.route.stationDetails.find { it.name == currentState.destination }?.stationId

        if (fromStationId == null || toStationId == null) {
            Log.e("TicketViewModel", "❌ Could not resolve station IDs!")
            _state.value = currentState.copy(originalPrice = 0.0, displayPrice = 0.0)
            return
        }

        viewModelScope.launch {
            val price = tripRepository.getPriceForStations(fromStationId, toStationId)
            val newOriginalPrice = price ?: 0.0
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
        val trip = activeTrip.value ?: return
        if (currentState.fromStation.isBlank() || currentState.destination.isBlank() || currentState.displayPrice <= 0) return

        val fromStationId = trip.route.stationDetails.find { it.name == currentState.fromStation }?.stationId
        val toStationId = trip.route.stationDetails.find { it.name == currentState.destination }?.stationId

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

        Log.d("TicketViewModel", "Selling ticket: tripId=${trip.trip.tripId}, from=$fromStationId, to=$toStationId, paymentCategory=$paymentCategory")

        _state.value = currentState.copy(
            sellResult = null,  // ✅ Reset previous result
            isProcessing = true
        )
        viewModelScope.launch {
            try {
                val result = sellTicketUseCase.execute(
                    tripId = trip.trip.tripId,
                    fromStationId = fromStationId,
                    toStationId = toStationId,
                    paymentCategory = paymentCategory,
                    amount = amount
                )

                _state.value = _state.value.copy(sellResult = result, isProcessing = false)

                if (result.isSuccess) {
                    tripRepository.loadActiveTrip() // Force refresh of trip data
                    _state.value = _state.value.copy(sellResult = null)
                }

            } catch (e: Exception) {
                _state.value = _state.value.copy(sellResult = Result.failure(e), isProcessing = false)
            }
        }
    }

    fun resetFields(context: Context? = null) {
        val currentState = _state.value
        _state.value = TicketState(fromStation = currentState.fromStation)
        context?.let { updateFromStation(it) } // Re-fetch location and trip data
    }

}