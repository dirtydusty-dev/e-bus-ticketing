package com.sinarowa.e_bus_ticket.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinarowa.e_bus_ticket.data.local.dao.RouteDao
import com.sinarowa.e_bus_ticket.data.local.dao.BusDao
import com.sinarowa.e_bus_ticket.data.local.dao.TripDetailsDao
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.data.local.entities.BusEntity
import com.sinarowa.e_bus_ticket.data.local.entities.TripDetails
import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    private val routeDao: RouteDao,
    private val busDao: BusDao,
    private val tripDao: TripDetailsDao,
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _routes = MutableStateFlow<List<RouteEntity>>(emptyList())
    val routes = _routes.asStateFlow()

    private val _buses = MutableStateFlow<List<BusEntity>>(emptyList())
    val buses = _buses.asStateFlow()

    private val _activeTrip = MutableStateFlow<TripDetails?>(null)
    val activeTrip = _activeTrip.asStateFlow()

    // ✅ Trips now come from Room and update in real time
    val trips: StateFlow<List<TripDetails>> = tripRepository.getAllTrips()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedTrip = MutableStateFlow<TripDetails?>(null)
    val selectedTrip: StateFlow<TripDetails?> = _selectedTrip


    init {
        Log.d("TripViewModel", "Initializing ViewModel, loading routes and buses")
        loadRoutes()
        loadBuses()
    }

    fun loadTripById(tripId: String) {
        viewModelScope.launch {
            _selectedTrip.value = tripDao.getTripById(tripId)
        }
    }

    fun loadRoutes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val routesList = routeDao.getAllRoutes()
                _routes.value = routesList
            } catch (e: Exception) {
                Log.e("TripViewModel", "Error loading routes: ${e.message}")
            }
        }
    }

    fun loadBuses() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val busList = busDao.getAllBuses()
                _buses.value = busList
            } catch (e: Exception) {
                Log.e("TripViewModel", "Error loading buses: ${e.message}")
            }
        }
    }

    fun createTrip(route: RouteEntity, bus: BusEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val activeTrip = tripDao.getActiveTrip()

                if (activeTrip == null) {
                    val newTrip = TripDetails(
                        tripId = UUID.randomUUID().toString(),
                        routeId = route.routeId,
                        routeName = route.name,
                        date = System.currentTimeMillis(),
                        busId = bus.busId,
                        busName = bus.busName
                    )
                    tripDao.insertTrip(newTrip)
                    _activeTrip.value = newTrip
                } else {
                    Log.e("TripViewModel", "Cannot create a new trip while one is active")
                }
            } catch (e: Exception) {
                Log.e("TripViewModel", "Error creating trip: ${e.message}")
            }
        }
    }
}
