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
import com.sinarowa.e_bus_ticket.domain.usecase.EndTripUseCase
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
    private val busRepository: BusRepository,
    private val endTripUseCase: EndTripUseCase
) : ViewModel() {

    private val _activeTrip = MutableLiveData<TripWithRoute?>()
    val activeTrip: LiveData<TripWithRoute?> get() = _activeTrip

    // LiveData to expose the result of the trip creation
    private val _createTripResult = MutableLiveData<Result<TripWithRoute>>()
    val createTripResult: LiveData<Result<TripWithRoute>> get() = _createTripResult

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

    fun endTrip(tripId: String) {
        viewModelScope.launch {
            val result = endTripUseCase.execute(tripId)
            result.onSuccess {
                // Handle success (e.g. show a success message)
            }.onFailure {
                _errorMessage.value = "Trip could not be closed"
                // Handle failure (e.g. show an error message)
            }
        }
    }


    // Function to fetch the active trip
    fun loadActiveTrip() {
        _isLoading.value = true // Start loading
        viewModelScope.launch {
            try {
                val trip = getActiveTripUseCase.execute()  // Fetch the active trip
                _activeTrip.value = trip  // Set the active trip
            } catch (e: Exception) {
                _errorMessage.value = "Error fetching active trip: ${e.message}"  // Handle errors
            } finally {
                _isLoading.value = false  // End loading once the process is complete
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

                if (result.isSuccess) {
                    // Set the active trip immediately after creation
                    val tripWithRoute = result.getOrNull()
                    if (tripWithRoute != null) {
                        _activeTrip.value = tripWithRoute
                        loadActiveTrip()
                    }
                } else {
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
