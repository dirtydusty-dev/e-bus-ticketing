package com.sinarowa.e_bus_ticket.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.data.local.entities.Bus
import com.sinarowa.e_bus_ticket.data.repository.BusRepository
import com.sinarowa.e_bus_ticket.data.repository.RouteRepository
import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import com.sinarowa.e_bus_ticket.domain.usecase.CreateTripUseCase
import com.sinarowa.e_bus_ticket.domain.usecase.EndTripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Trip State Representation */
data class TripState(
    val activeTrip: TripWithRoute? = null,
    val routes: List<RouteEntity> = emptyList(),
    val buses: List<Bus> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TripViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val createTripUseCase: CreateTripUseCase,
    private val routeRepository: RouteRepository,
    private val busRepository: BusRepository,
    private val endTripUseCase: EndTripUseCase
) : ViewModel() {

    // ✅ Use StateFlow for reactivity
    private val _state = MutableStateFlow(TripState())
    val state: StateFlow<TripState> get() = _state.asStateFlow()

    init {
        loadRoutes()
        loadBuses()
        loadActiveTrip()
    }

    /** Load the active trip and update state */
    fun loadActiveTrip() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                tripRepository.loadActiveTrip() // ✅ Load trip (returns Unit)

                // ✅ Wait for activeTrip to be updated
                val trip = tripRepository.activeTrip.value // Explicitly fetch latest trip
                _state.value = _state.value.copy(activeTrip = trip, isLoading = false)

                Log.d("TripViewModel", "✅ Active trip loaded: ${trip?.trip?.tripId ?: "None"}")
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Failed to load trip")
                Log.e("TripViewModel", "❌ Error loading active trip: ${e.message}")
            }
        }
    }


    /** End the current trip */
    fun endTrip(tripId: String) {
        viewModelScope.launch {
            try {
                val result = endTripUseCase.execute(tripId)
                if (result.isSuccess) {
                    _state.value = _state.value.copy(activeTrip = null) // ✅ Clear active trip
                    Log.d("TripViewModel", "🚍 Trip ended successfully")
                } else {
                    _state.value = _state.value.copy(errorMessage = "Trip could not be closed")
                    Log.e("TripViewModel", "❌ Error ending trip")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = "Error ending trip: ${e.message}")
            }
        }
    }

    /** Load available routes */
    private fun loadRoutes() {
        viewModelScope.launch {
            try {
                val routeList = routeRepository.getAllRoutes()
                _state.value = _state.value.copy(routes = routeList)
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = "Error loading routes: ${e.message}")
            }
        }
    }

    /** Load available buses */
    private fun loadBuses() {
        viewModelScope.launch {
            try {
                val busList = busRepository.getAllBuses()
                _state.value = _state.value.copy(buses = busList)
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = "Error loading buses: ${e.message}")
            }
        }
    }

    /** Create a new trip */
    fun createTrip(route: RouteEntity, bus: Bus) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val result = createTripUseCase.execute(route.routeId, bus.busId)
                if (result.isSuccess) {
                    val tripWithRoute = result.getOrNull()
                    _state.value = _state.value.copy(activeTrip = tripWithRoute, isLoading = false)
                    Log.d("TripViewModel", "🆕 New trip created: ${tripWithRoute?.trip?.tripId}")
                } else {
                    _state.value = _state.value.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Error: ${e.message}")
            }
        }
    }
}
