package com.sinarowa.e_bus_ticket.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.enums.TicketStatus
import com.sinarowa.e_bus_ticket.data.repository.*
import com.sinarowa.e_bus_ticket.domain.usecase.GetLocationUseCase
import com.sinarowa.e_bus_ticket.utils.DateTimeUtils
import com.sinarowa.e_bus_ticket.utils.LocationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TicketingViewModel @Inject constructor(
    application: Application,
    private val getLocationUseCase: GetLocationUseCase,
    private val ticketRepository: TicketRepository,
    private val tripRepository: TripRepository,
    private val routeRepository: RouteRepository,
    private val stationRepository: StationRepository,
    private val busRepository: BusRepository // ✅ Fix: Added BusRepository
) : AndroidViewModel(application) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _currentTripId = MutableStateFlow<Long?>(null)

    private val _routeStops = MutableStateFlow<List<String>>(emptyList())
    val routeStops: StateFlow<List<String>> = _routeStops.asStateFlow()

    private val _currentLocation = MutableStateFlow("Detecting...")
    val currentLocation: StateFlow<String> = _currentLocation.asStateFlow()

    private val _currentCoordinates = MutableStateFlow<Location?>(null)
    val currentCoordinates: StateFlow<Location?> = _currentCoordinates.asStateFlow()

    private val _stationCoordinates = MutableStateFlow<Map<String, Pair<Double, Double>>>(emptyMap())
    val stationCoordinates: StateFlow<Map<String, Pair<Double, Double>>> = _stationCoordinates.asStateFlow()

    private val _busCapacity = MutableStateFlow(50) // ✅ Fix: Default 50, updates dynamically
    val busCapacity: StateFlow<Int> = _busCapacity.asStateFlow()

    var lastKnownStation: String? = null

    private val _ticketPrice = MutableStateFlow(0.0)
    val ticketPrice: StateFlow<Double> = _ticketPrice.asStateFlow()

    fun getPrice(startStation: String, stopStation: String) {
        viewModelScope.launch(Dispatchers.IO) { // Move to IO thread
            val price = ticketRepository.calculatePrice(startStation, stopStation)
            _ticketPrice.value = price // Update on IO thread, will propagate to UI
        }
    }

    private val _locationState = MutableStateFlow<String>("")
    val locationState: StateFlow<String> = _locationState

    // Fetch the best location for ticketing
    fun fetchLocation(routeId: Long) {
        viewModelScope.launch {
            val location = getLocationUseCase(routeId)
            _locationState.value = location
        }
    }



    // ✅ Fetch real-time ticket counts for the current trip
    val ticketCount = _currentTripId
        .filterNotNull()
        .flatMapLatest { tripId -> ticketRepository.getTicketCountForTrip(tripId) }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val luggageCount = _currentTripId
        .filterNotNull()
        .flatMapLatest { tripId -> ticketRepository.getLuggageCountForTrip(tripId) }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val departedCount = _currentTripId
        .filterNotNull()
        .flatMapLatest { tripId -> ticketRepository.getDepartedCountForTrip(tripId) }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // ✅ Load trip data when set
    fun setTrip(tripId: Long) {
        _currentTripId.value = tripId
        fetchRouteStops(tripId)
        fetchStationCoordinates()
        loadBusCapacity(tripId) // ✅ Fix: Ensure we fetch the correct capacity
    }

    private fun loadBusCapacity(tripId: Long) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripId).firstOrNull() ?: return@launch
            val bus = busRepository.getBusById(trip.busId).firstOrNull() ?: return@launch
            _busCapacity.value = bus.capacity // ✅ Fix: Fetch and set bus capacity
        }
    }

    private fun fetchStationCoordinates() {
        viewModelScope.launch {
            _stationCoordinates.value = stationRepository.getStationCoordinatesMap()
        }
    }

    fun fetchRouteStops(tripId: Long) {
        viewModelScope.launch {
            _routeStops.value = ticketRepository.getRouteStops(tripId)
        }
    }


    fun sellTicket(
        tripId: Long, from: String, to: String, ticketType: String, price: Double
    ) {
        viewModelScope.launch {
            val ticket = Ticket(
                tripId = tripId,
                startStation = from,
                stopStation = to,
                type = ticketType,
                creationTime = DateTimeUtils.getCurrentDateTime(),
                amount = price,
                status = TicketStatus.VALID
            )
            ticketRepository.insertTicket(ticket)
        }
    }

    // ✅ Location Tracking for Nearest Station
    @SuppressLint("MissingPermission")
    fun startLocationTracking(tripId: Long) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        _currentCoordinates.value = location
                        findNearestStation(tripId, location.latitude, location.longitude)
                    } else {
                        fallbackToLastKnownLocation(tripId)
                    }
                }
            },
            Looper.getMainLooper()
        )
    }

    @SuppressLint("MissingPermission")
    fun fallbackToLastKnownLocation(tripId: Long) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                _currentCoordinates.value = location
                findNearestStation(tripId, location.latitude, location.longitude)
            } else {
                // No GPS, no last location -> fallback to last known station
                _currentLocation.value = lastKnownStation ?: "Unknown"
            }
        }
    }

    private fun findNearestStation(tripId: Long, latitude: Double?, longitude: Double?) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripId).firstOrNull() ?: return@launch
            val route = routeRepository.getRouteById(trip.routeId).firstOrNull() ?: return@launch

            val routeStations = route.stations
            val stationCoordinates = stationRepository.getStationCoordinatesMap()

            val closestStation = LocationUtils.findNearestStation(latitude, longitude, routeStations, stationCoordinates, lastKnownStation)
            _currentLocation.value = closestStation

            if (closestStation != "Unknown") {
                lastKnownStation = closestStation // Update last known station
            }
        }
    }

    fun getTicketsForTrip(tripId: Long): Flow<List<Ticket>> {
        return ticketRepository.getTicketsForTrip(tripId)
    }
}
