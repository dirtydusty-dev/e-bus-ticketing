package com.sinarowa.e_bus_ticket.viewmodel

import androidx.lifecycle.*
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.data.local.entities.Bus
import com.sinarowa.e_bus_ticket.data.local.entities.Trip
import com.sinarowa.e_bus_ticket.data.local.enums.TripStatus
import com.sinarowa.e_bus_ticket.data.repository.BusRepository
import com.sinarowa.e_bus_ticket.data.repository.RouteRepository
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import com.sinarowa.e_bus_ticket.domain.usecase.CreateTripUseCase
import com.sinarowa.e_bus_ticket.domain.usecase.GetActiveTripsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    private val createTripUseCase: CreateTripUseCase,
    private val getActiveTripUseCase: GetActiveTripsUseCase,
    private val routeRepository: RouteRepository,
    private val busRepository: BusRepository
) : ViewModel() {

    private val _activeTrip = MutableLiveData<TripWithRoute?>()
    val activeTrip: LiveData<TripWithRoute?> get() = _activeTrip

    // LiveData to expose the result of the trip creation
    private val _createTripResult = MutableLiveData<Result<Trip>>()
    val createTripResult: LiveData<Result<Trip>> get() = _createTripResult

    // LiveData for handling UI loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData for displaying error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // LiveData for loading buses and routes
    private val _routes = MutableLiveData<List<RouteEntity>>()
    val routes: LiveData<List<RouteEntity>> get() = _routes

    private val _buses = MutableLiveData<List<Bus>>()
    val buses: LiveData<List<Bus>> get() = _buses

    // Load routes and buses
    init {
        loadRoutes()
        loadBuses()
    }


    // Function to fetch the active trip
    fun loadActiveTrip() {
        viewModelScope.launch {
            try {
                val trip = getActiveTripUseCase.execute()
                _activeTrip.value = trip
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching active trip: ${e.message}"
            }
        }
    }


    private fun loadRoutes() {
        viewModelScope.launch {
            try {
                val routeList = routeRepository.getAllRoutes()
                _routes.value = routeList
            } catch (e: Exception) {
                _errorMessage.value = "Error loading routes: ${e.message}"
            }
        }
    }

    private fun loadBuses() {
        viewModelScope.launch {
            try {
                val busList = busRepository.getAllBuses()
                _buses.value = busList
            } catch (e: Exception) {
                _errorMessage.value = "Error loading buses: ${e.message}"
            }
        }
    }

    // Function to create a new trip
    fun createTrip(route: RouteEntity, bus: Bus) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Call the CreateTripUseCase and get the result
                val result = createTripUseCase.execute(route.routeId, bus.busId)

                // Post the result to LiveData
                _createTripResult.value = result

                if (result.isFailure) {
                    // Handle failure case
                    _errorMessage.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                // Handle any exceptions during the trip creation process
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                // Set loading state to false once the request is completed
                _isLoading.value = false
            }
        }
    }


}
