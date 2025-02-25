package com.sinarowa.e_bus_ticket.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinarowa.e_bus_ticket.data.local.dao.TicketDao
import com.sinarowa.e_bus_ticket.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val locationRepository: LocationRepository,
    private val priceRepository: PriceRepository,
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _selectedTripId = MutableStateFlow<String?>(null)
    val selectedTripId: StateFlow<String?> = _selectedTripId.asStateFlow()

    private val _fromCity = MutableStateFlow("Detecting...")
    val fromCity: StateFlow<String> = _fromCity.asStateFlow()

    private val _routeStops = MutableStateFlow<List<String>>(emptyList())
    val routeStops = _routeStops.asStateFlow()

    /**
     * ✅ Sets trip ID and fetches route stops.
     */
    fun setTripId(tripId: String) {
        _selectedTripId.value = tripId
        fetchRouteStops(tripId)
        updateFromCity(tripId)
    }

    /**
     * ✅ Gets closest stop from location as "From".
     */
    fun updateFromCity(tripId: String) {
        viewModelScope.launch {
            _fromCity.value = locationRepository.getClosestStop(tripId)
            Log.d("LOCATION_PROCESS", "✅ Auto-detected From: ${_fromCity.value}")
        }
    }

    /**
     * ✅ Gets valid stops from database.
     */
    fun fetchRouteStops(tripId: String) {
        viewModelScope.launch {
            val trip = routeRepository.getRouteByTrip(tripId)
            _routeStops.value = trip?.stops?.split(",")?.map { it.trim() } ?: emptyList()
        }
    }

    /**
     * ✅ Calculates fare dynamically.
     */
    suspend fun getPrice(fromCity: String, toCity: String, ticketType: String): Double {
        return priceRepository.getPrice(fromCity, toCity, ticketType) ?: 0.0
    }
}
