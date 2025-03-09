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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val ticketRepository: TicketRepository,
    private val sellTicketUseCase: SellTicketUseCase
) : ViewModel() {

    val activeTrip: LiveData<TripWithRoute?> get() = tripRepository.activeTrip

    private val _fromStation = MutableLiveData<String>()
    val fromStation: LiveData<String> get() = _fromStation

    private val _validDestinations = MutableLiveData<List<String>>()
    val validDestinations: LiveData<List<String>> get() = _validDestinations

    private val _ticketPrice = MutableLiveData<Double>()
    val ticketPrice: LiveData<Double> get() = _ticketPrice

    private val _shortAmount = MutableLiveData<Int>()
    val shortAmount: LiveData<Int> get() = _shortAmount

    private val _isProcessing = MutableLiveData<Boolean>()
    val isProcessing: LiveData<Boolean> get() = _isProcessing

    private val _sellTicketResult = MutableLiveData<Result<Unit>>()
    val sellTicketResult: LiveData<Result<Unit>> get() = _sellTicketResult

    // ✅ Add destination property
    private val _destination = MutableLiveData<String>()
    val destination: LiveData<String> get() = _destination

    // ✅ Add ticketType property
    private val _ticketType = MutableLiveData<String>()
    val ticketType: LiveData<String> get() = _ticketType

    private val _remainingSeats = MutableLiveData(0)
    val remainingSeats: LiveData<Int> get() = _remainingSeats

    private val _activePassengers = MutableLiveData(0)
    val activePassengers: LiveData<Int> get() = _activePassengers

    init {
        _shortAmount.value = 0
        _destination.value = ""
        _ticketType.value = ""
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("TicketViewModel", "Initializing TicketViewModel...")
            tripRepository.loadActiveTrip() // ✅ Ensure the trip is loaded
        }
    }

    init {
        activeTrip.observeForever { trip ->
            if (trip != null) {
                Log.d("TicketViewModel", "✅ Active trip updated: ${trip.trip.tripId}")

                // 🚀 Update values when activeTrip changes
                updateDestinations(trip)
                //updateTicketPrice(trip)
                calculateRemainingSeats(trip)
            } else {
                Log.e("TicketViewModel", "❌ No active trip found!")
            }
        }
    }


    private fun updateDestinations(trip: TripWithRoute) {
        val currentStation = _fromStation.value
        if (currentStation == null) {
            Log.e("TicketViewModel", "❌ updateDestinations: No current station available.")
            return
        }

        // ✅ Create a map of stationId -> stationName for easy lookup
        val stationIdToName = trip.route.stationDetails.associateBy { it.stationId }

        // ✅ Sort RouteStops by stopOrder, then get station names
        val sortedStations = trip.route.stops
            .sortedBy { it.stopOrder } // Sort by stop order
            .mapNotNull { stationIdToName[it.stationId]?.name } // Convert to station names

        Log.d("TicketViewModel", "🚏 Sorted route stations: $sortedStations")
        Log.d("TicketViewModel", "📍 Current station: $currentStation")

        // Find index of current station in sorted list
        val currentIndex = sortedStations.indexOf(currentStation)

        if (currentIndex == -1) {
            Log.e("TicketViewModel", "❌ updateDestinations: Current station '$currentStation' not found in sorted route!")
            return
        }

        // ✅ Get destinations after the current station
        val destinations = sortedStations.subList(currentIndex + 1, sortedStations.size)

        if (destinations.isEmpty()) {
            Log.w("TicketViewModel", "⚠️ No valid destinations found after $currentStation.")
        } else {
            Log.d("TicketViewModel", "✅ Updated valid destinations: $destinations")
        }

        _validDestinations.value = destinations
    }

    fun updateTicketPrice() {
        val fromStationName = _fromStation.value ?: return
        val toStationName = _destination.value ?: return
        val trip = activeTrip.value ?: return

        Log.d("TicketViewModel", "🔍 Resolving Station Names → IDs: $fromStationName -> ?, $toStationName -> ?")

        val fromStationId = trip.route.stops.find { stop ->
            stop.stationId == trip.route.stationDetails.find { it.name == fromStationName }?.stationId
        }?.stationId ?: return

        val toStationId = trip.route.stops.find { stop ->
            stop.stationId == trip.route.stationDetails.find { it.name == toStationName }?.stationId
        }?.stationId ?: return

        Log.d("TicketViewModel", "✅ Resolved Station Names → IDs: $fromStationName -> $fromStationId, $toStationName -> $toStationId")

        // ✅ Fetch price from repository
        viewModelScope.launch {
            val price = tripRepository.getPriceForStations(fromStationId, toStationId)
            _ticketPrice.postValue(price)

            Log.d("TicketViewModel", "💰 Ticket price set: $price USD (From: $fromStationId, To: $toStationId)")
        }
    }


    fun getTicketPriceValue(): Double {
        return _ticketPrice.value ?: 0.0
    }






    private fun calculateTripDetails(tripWithRoute: TripWithRoute) {
        val ticketCount = tripWithRoute.tickets.count { it.status == TicketStatus.VALID }
        val luggageCount = tripWithRoute.tickets.count { it.paymentCategory == "Luggage" }
        val availableSeats = tripWithRoute.bus.capacity - ticketCount

        _remainingSeats.value = availableSeats
        _activePassengers.value = ticketCount
    }



    // ✅ Add setter for destination
    fun setDestination(destination: String) {
        _destination.value = destination
        updateTicketPrice()
    }

    // ✅ Add setter for ticketType
    fun setTicketType(type: String) {
        _ticketType.value = type
    }

    // ✅ Add setter for shortAmount
    fun setShortAmount(amount: Int) {
        val maxShortAmount = _ticketPrice.value ?: 0.0

        if (amount in 0..maxShortAmount.toInt()) {
            _shortAmount.value = amount

            // ✅ Adjust Ticket Price Dynamically
            val adjustedPrice = maxShortAmount - amount
            _ticketPrice.value = adjustedPrice

            // ✅ Set Ticket Type
            _ticketType.value = if (amount > 0) "$$amount Short" else "Adult"

            Log.d("TicketViewModel", "💰 Adjusted Ticket Price: $adjustedPrice USD (Short: $amount)")
            Log.d("TicketViewModel", "🎫 Updated Ticket Type: ${_ticketType.value}")
        }
    }




    // ✅ Find nearest station and update `fromStation`
    fun updateFromStation(context: Context) {
        Log.d("TicketViewModel", "updateFromStation() called")

        viewModelScope.launch {
            Log.d("TicketViewModel", "Fetching current location...")

            val location = LocationUtils.getCurrentLocation(context)

            if (location == null) {
                Log.e("TicketViewModel", "❌ Failed to get current location. Skipping station update.")
                return@launch
            }

            if (activeTrip.value == null) {
                tripRepository.loadActiveTrip()
                delay(500) // Small delay to allow loading (adjust if needed)
            }

            Log.d(
                "TicketViewModel",
                "✅ Current location obtained: Latitude=${location.latitude}, Longitude=${location.longitude}"
            )

            val trip = activeTrip.value
            if (trip == null) {
                Log.e("TicketViewModel", "❌ No active trip found. Cannot update station.")
                return@launch
            }

            Log.d("TicketViewModel", "✅ Active trip found: Trip ID = ${trip.trip.tripId}")

            val stations = trip.route.stationDetails.map { it.name }
            val stationCoordinates = trip.route.stationDetails.associate { station ->
                station.name to (station.latitude to station.longitude)
            }

            val nearestStation = LocationUtils.findNearestStation(
                location.latitude, location.longitude, stations, stationCoordinates, _fromStation.value
            )

            Log.d("TicketViewModel", "🔍 Nearest station found: $nearestStation")

            _fromStation.value = nearestStation
            updateDestinations(trip) // 🚀 Update destinations **after** `_fromStation` is set
            //updateTicketPrice(trip) // 🚀 Update price too
        }
    }


    fun updateChildTicketPrice() {
        val originalPrice = _ticketPrice.value
        if (originalPrice != null) {
            _ticketPrice.value = originalPrice / 2
        }
        Log.d("TicketViewModel", "👶 Child Ticket Price Set: ${_ticketPrice.value} USD")
    }




    fun resetFields() {
        _destination.value = ""
        _ticketType.value = "Adult" // Default to Adult
        _ticketPrice.value = 0.0
        _shortAmount.value = 0
        Log.d("TicketViewModel", "🔄 Fields Reset: Destination, Ticket Type, Price, Short Amount")
    }





    fun sellTicket() {
        Log.d("TicketViewModel", "sellTicket() called")

        _isProcessing.value = true

        val trip = activeTrip.value
        if (trip == null) {
            Log.e("TicketViewModel", "❌ No active trip found. Cannot process ticket sale.")
            _isProcessing.value = false
            return
        }

        Log.d("TicketViewModel", "✅ Active trip found: ${trip.trip.tripId}")

        val fromStationId = _fromStation.value
        if (fromStationId.isNullOrBlank()) {
            Log.e("TicketViewModel", "❌ 'From' station is missing. Cannot process ticket sale.")
            _isProcessing.value = false
            return
        }

        val toStationId = _destination.value
        if (toStationId.isNullOrBlank()) {
            Log.e("TicketViewModel", "❌ 'To' station is missing. Cannot process ticket sale.")
            _isProcessing.value = false
            return
        }

        val ticketType = _ticketType.value
        if (ticketType.isNullOrBlank()) {
            Log.e("TicketViewModel", "❌ Ticket type is missing. Cannot process ticket sale.")
            _isProcessing.value = false
            return
        }

        Log.d("TicketViewModel", "🎟️ Selling ticket: From=$fromStationId, To=$toStationId, Type=$ticketType")

        val remainingSeats = _remainingSeats.value ?: 0
        if (remainingSeats <= 0) {
            Log.e("TicketViewModel", "❌ No available seats. Cannot sell ticket.")
            _sellTicketResult.value = Result.failure(Exception("No available seats"))
            _isProcessing.value = false
            return
        }


        Log.d("TicketViewModel", "🪑 Available seats: $remainingSeats")

        viewModelScope.launch {
            try {
                Log.d("TicketViewModel", "📡 Sending ticket sale request...")

                val result = sellTicketUseCase.execute(trip.trip.tripId, fromStationId, toStationId, ticketType)
                _sellTicketResult.value = result

                if (result.isSuccess) {
                    Log.d("TicketViewModel", "✅ Ticket sale successful! Updating seats...")
                    calculateRemainingSeats(trip)
                } else {
                    Log.e("TicketViewModel", "❌ Ticket sale failed: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("TicketViewModel", "❌ Exception during ticket sale: ${e.message}")
                _sellTicketResult.value = Result.failure(e)
            } finally {
                _isProcessing.value = false
                Log.d("TicketViewModel", "✅ Ticket sale process completed.")
            }
        }
    }


    private fun calculateRemainingSeats(trip: TripWithRoute) {
        val ticketCount = trip.tickets.count { it.status == TicketStatus.VALID }
        val availableSeats = trip.bus.capacity - ticketCount
        _remainingSeats.value = availableSeats
    }
}
