package com.sinarowa.e_bus_ticket.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.repository.LocationRepository
import com.sinarowa.e_bus_ticket.data.repository.PriceRepository
import com.sinarowa.e_bus_ticket.data.repository.RouteRepository
import com.sinarowa.e_bus_ticket.data.repository.TicketRepository
import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val locationRepository: LocationRepository,
    private val priceRepository: PriceRepository,
    private val routeRepository: RouteRepository,
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _ticketCount = MutableStateFlow(0)
    val ticketCount = _ticketCount.asStateFlow()

    private val _ticketPrice = MutableStateFlow(0.0)  // ✅ Store the price
    val ticketPrice = _ticketPrice.asStateFlow()

    private val _routeStops = MutableStateFlow<List<String>>(emptyList()) // ✅ Route stops
    val routeStops = _routeStops.asStateFlow()


    fun updateTicketCount(tripId: String) {
        viewModelScope.launch {
            ticketRepository.getAllTickets()
                .collect { tickets ->
                    val soldTickets = tickets.count { it.tripId == tripId }
                    _ticketCount.value = soldTickets
                }
        }
    }

    fun getTicketsByCity(): Flow<Map<String, List<Ticket>>> {
        return ticketRepository.getAllTickets()
            .map { tickets -> tickets.groupBy { it.fromStop } } // Group tickets by 'fromStop'
    }



    fun insertTicket(ticket: Ticket) {
        viewModelScope.launch {
            ticketRepository.insertTicket(ticket)
            updateTicketCount(ticket.tripId)
        }
    }

    fun getAllTickets(tripId: String): Flow<List<Ticket>> {
        return ticketRepository.getTicketsByTrip(tripId)
    }


    /**
     * ✅ when sync api is ready use this
     */

    /*fun insertTicket(ticket: Ticket, isOnline: Boolean) {
        viewModelScope.launch {
            ticketRepository.insertTicket(ticket)
            updateTicketCount(ticket.tripId)
            if (isOnline) {
                val success = tryToSyncTicket(ticket)
                if (!success) syncQueueRepository.addToQueue("TICKET", convertTicketToJson(ticket))
            } else {
                syncQueueRepository.addToQueue("TICKET", convertTicketToJson(ticket))
            }
        }
    }

    private suspend fun tryToSyncTicket(ticket: Ticket): Boolean {
        return try {
            val response = apiService.uploadTicket(ticket)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
    */



    suspend fun cancelTicket(ticketId: String, cancelReason: String, activeTripId: String): Boolean {
        val ticket = ticketRepository.getTicketById(ticketId) // Fetch ticket by ID

        return if (ticket != null && ticket.tripId == activeTripId) {
            // ✅ Ensure ticket belongs to the active trip before canceling
            ticketRepository.cancelTicket(ticketId, 1, cancelReason)
            true
        } else {
            false // ❌ Ticket does not belong to the active trip or does not exist
        }
    }

    /**
     * ✅ Get city name based on GPS coordinates
     */
/*    suspend fun getCityFromCoordinates(latitude: Double, longitude: Double, tripId: String): String {
        return withContext(Dispatchers.IO) {
            val tripDetails = tripRepository.getTripById(tripId)
            val routeId = tripDetails?.routeId ?: return@withContext "Unknown"
            getClosestCityForRoute(latitude, longitude, routeId)
        }
    }*/

/*

    suspend fun getClosestCityForRoute1(latitude: Double, longitude: Double, routeId: String): String {
        val route = routeRepository.getRouteById(routeId) ?: return "Unknown"
        val routeStops = route.stops.split(",").map { it.trim() }  // 🔹 Extract stops list

        // 🔹 Get only locations that exist in this route's stops
        val validLocations = locationRepository.getAllLocations().filter { it.cityName in routeStops }

        return validLocations.minByOrNull { location ->
            calculateDistance(latitude, longitude, location.latitude, location.longitude)
        }?.cityName ?: "Unknown"
    }*/

   /* private suspend fun getClosestCityForRoute(latitude: Double, longitude: Double, routeId: String): String {
        val route = routeRepository.getRouteById(routeId) ?: return "Unknown"
        val routeStops = route.stops.split(",").map { it.trim() }

        val validLocations = locationRepository.getAllLocations().filter { it.cityName in routeStops }

        val distances = validLocations.associateWith { location ->
            calculateDistance(latitude, longitude, location.latitude, location.longitude)
        }

        // Log all distances
        distances.forEach { (location, distance) ->
            Log.d("GPS_DEBUG", "City: ${location.cityName}, Distance: $distance meters")
        }

        val nearestLocation = distances.minByOrNull { it.value }

        return nearestLocation?.key?.cityName ?: "Unknown"
    }
*/

    /**
     * ✅ Haversine Formula to calculate distance between two latitude/longitude points
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double, lat2: Double, lon2: Double
    ): Double {
        val R = 6371.0 // Earth's radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c // Distance in km
    }




   /* suspend fun getClosestCity(latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            val allLocations = locationRepository.getAllLocations()

            val closestLocation = allLocations.minByOrNull { location ->
                haversineDistance(latitude, longitude, location.latitude, location.longitude)
            }

            return@withContext closestLocation?.cityName ?: "Unknown"
        }
    }

    *//**
     * ✅ Haversine Formula to Calculate Distance Between Two GPS Coordinates
     *//*
    fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Radius of Earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }
*/


    /**
     * ✅ Get ticket price based on from and to city
     */
    fun fetchRouteStops(tripId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val tripDetails = tripRepository.getTripById(tripId) // ✅ Get trip
            val routeDetails = tripDetails?.let { routeRepository.getRouteById(it.routeId) } // ✅ Get route
            _routeStops.value = routeDetails?.stops?.split(",")?.map { it.trim() } ?: emptyList()
        }
    }



    /**
     * ✅ Get ticket price from price table
     */
    suspend fun getPrice(fromCity: String, toCity: String, ticketType: String): Double {
        return withContext(Dispatchers.IO) {
            val priceEntity = priceRepository.getPrice(fromCity, toCity)
            when (ticketType) {
                "Adult" -> priceEntity?.adultPrice ?: 0.0
                "Child" -> priceEntity?.childPrice ?: 0.0
                "$1 Short" -> priceEntity?.dollarShort ?: 0.0
                "$2 Short" -> priceEntity?.twoDollarShort ?: 0.0
                else -> 0.0
            }
        }
    }




    suspend fun getCityFromCoordinates(tripId: String): String {
        return withContext(Dispatchers.IO) {
            Log.d("LOCATION_PROCESS", "🔄 Fetching city from coordinates...")
            val cityName = locationRepository.getCityNameWithFallback(tripId)

            Log.d("LOCATION_PROCESS", "✅ Determined city: $cityName")

            return@withContext cityName
        }
    }

    suspend fun getStationSales(tripId: String): Map<String, Pair<Int, Double>> {
        return ticketRepository.calculateStationSales(tripId)
    }

    suspend fun getTicketBreakdown(tripId: String): Map<Pair<String, String>, Pair<Int, Double>> {
        return ticketRepository.calculateTicketBreakdown(tripId)
    }


}
