package com.sinarowa.e_bus_ticket.viewmodel

import com.sinarowa.e_bus_ticket.data.repository.BusRepository
import com.sinarowa.e_bus_ticket.data.repository.RouteRepository
import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinarowa.e_bus_ticket.data.local.entities.Bus
import com.sinarowa.e_bus_ticket.data.local.entities.Route
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import com.sinarowa.e_bus_ticket.domain.usecase.CreateTripUseCase
import com.sinarowa.e_bus_ticket.domain.usecase.GetActiveTripsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTripViewModel @Inject constructor(
    private val getActiveTripsUseCase: GetActiveTripsUseCase,
    private val createTripUseCase: CreateTripUseCase,
    private val routeRepository: RouteRepository,
    private val busRepository: BusRepository,
    private val tripRepository: TripRepository
) : ViewModel() {

    // ✅ Holds route list
    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes = _routes.asStateFlow()

    // ✅ Holds active trips
    private val _activeTrips = MutableStateFlow<List<TripWithRoute>>(emptyList())
    val activeTrips: StateFlow<List<TripWithRoute>> = _activeTrips.asStateFlow()

    // ✅ Holds bus list
    private val _buses = MutableStateFlow<List<Bus>>(emptyList())
    val buses = _buses.asStateFlow()

    // ✅ Holds selected trip
    private val _selectedTrip = MutableStateFlow<TripWithRoute?>(null)
    val selectedTrip: StateFlow<TripWithRoute?> = _selectedTrip.asStateFlow()

    // ✅ Holds loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadRoutes()
        loadBuses()
        loadActiveTrips()
    }

    // ✅ Fetch routes from repository
    private fun loadRoutes() {
        viewModelScope.launch {
            routeRepository.getAllRoutes().collectLatest { fetchedRoutes ->
                _routes.value = fetchedRoutes
            }
        }
    }

    // ✅ Fetch buses from repository
    private fun loadBuses() {
        viewModelScope.launch {
            busRepository.getAllBuses().collectLatest { fetchedBuses ->
                _buses.value = fetchedBuses
            }
        }
    }

    // ✅ Fetch active trips using `GetActiveTripsUseCase`
    private fun loadActiveTrips() {
        viewModelScope.launch {
            _isLoading.value = true // Start loading
            getActiveTripsUseCase().collectLatest { trips ->
                _activeTrips.value = trips
                _isLoading.value = false // Stop loading
            }
        }
    }

    // ✅ Load a specific trip by ID
    fun loadTrip(tripId: Long) {
        viewModelScope.launch {
            _selectedTrip.value = tripRepository.getTripById(tripId).firstOrNull()
        }
    }

    // ✅ Create a new trip
    suspend fun createTrip(routeId: Long, busId: Long) {
        createTripUseCase.execute(routeId, busId, viewModelScope)
    }

    // ✅ End a trip by updating its status
    fun endTrip(tripId: Long) {
        viewModelScope.launch {
            tripRepository.updateTripStatus(tripId, "COMPLETED") // ✅ Set trip status to COMPLETED
        }
    }
}