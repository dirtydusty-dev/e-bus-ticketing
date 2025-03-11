package com.sinarowa.e_bus_ticket.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.sinarowa.e_bus_ticket.data.local.entities.Bus
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.data.repository.BusRepository
import com.sinarowa.e_bus_ticket.data.repository.RouteRepository
import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import com.sinarowa.e_bus_ticket.domain.usecase.CreateTripUseCase
import com.sinarowa.e_bus_ticket.domain.usecase.EndTripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private val _state = MutableStateFlow(TripState(isLoading = true))
    val state: StateFlow<TripState> get() = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Load all data concurrently
            try {
                val routes = routeRepository.getAllRoutes()
                val buses = busRepository.getAllBuses()
                tripRepository.loadActiveTrip()

                // Update state with fetched data
                _state.value = _state.value.copy(
                    routes = routes,
                    buses = buses,
                    activeTrip = tripRepository.activeTrip.value,
                    isLoading = false
                )

                // Reactively update activeTrip when it changes
                tripRepository.activeTrip.asFlow().collect { trip ->
                    _state.value = _state.value.copy(activeTrip = trip)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load initial data: ${e.message}"
                )
                Log.e("TripViewModel", "❌ Error in init: ${e.message}")
            }
        }
    }

    fun refreshTrip() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                tripRepository.loadActiveTrip()
                _state.value = _state.value.copy(
                    activeTrip = tripRepository.activeTrip.value,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to refresh trip: ${e.message}"
                )
            }
        }
    }

    fun loadActiveTrip() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                tripRepository.loadActiveTrip()
                _state.value = _state.value.copy(
                    activeTrip = tripRepository.activeTrip.value,
                    isLoading = false
                )
                Log.d("TripViewModel", "✅ Active trip loaded: ${tripRepository.activeTrip.value?.trip?.tripId ?: "None"}")
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load trip: ${e.message}"
                )
                Log.e("TripViewModel", "❌ Error loading active trip: ${e.message}")
            }
        }
    }

    fun endTrip(tripId: String) {
        viewModelScope.launch {
            try {
                val result = endTripUseCase.execute(tripId)
                if (result.isSuccess) {
                    _state.value = _state.value.copy(activeTrip = null)
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

    fun createTrip(route: RouteEntity, bus: Bus) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val result = createTripUseCase.execute(route.routeId, bus.busId)
                if (result.isSuccess) {
                    val tripWithRoute = result.getOrNull()
                    tripRepository.updateActiveTrip(tripWithRoute!!) // Update active trip
                    _state.value = _state.value.copy(
                        activeTrip = tripWithRoute,
                        isLoading = false
                    )
                    Log.d("TripViewModel", "🆕 New trip created: ${tripWithRoute?.trip?.tripId}")
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error creating trip: ${e.message}"
                )
            }
        }
    }
}