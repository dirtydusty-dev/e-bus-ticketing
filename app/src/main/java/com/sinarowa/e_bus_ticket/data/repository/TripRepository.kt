package com.sinarowa.e_bus_ticket.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sinarowa.e_bus_ticket.data.local.dao.BusDao
import com.sinarowa.e_bus_ticket.data.local.dao.PriceDao
import com.sinarowa.e_bus_ticket.data.local.dao.RouteDao
import com.sinarowa.e_bus_ticket.data.local.dao.TripDao
import com.sinarowa.e_bus_ticket.data.local.entities.Bus
import com.sinarowa.e_bus_ticket.data.local.entities.Price
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.data.local.entities.Trip
import com.sinarowa.e_bus_ticket.data.local.enums.SyncStatus
import com.sinarowa.e_bus_ticket.data.local.enums.TripStatus
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import com.sinarowa.e_bus_ticket.domain.usecase.GetActiveTripsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TripRepository @Inject constructor(
    private val tripDao: TripDao,
    private val routeDao: RouteDao,   // Assuming you have this
    private val busDao: BusDao,
    private val priceDao: PriceDao,
    //private val getActiveTripsUseCase: GetActiveTripsUseCase
) {


    private val _activeTrip = MutableLiveData<TripWithRoute?>()
    val activeTrip: LiveData<TripWithRoute?> get() = _activeTrip

    init {
        // ✅ Load the active trip asynchronously in a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            loadActiveTrip()
        }
    }

    // Load active trip from DB or API
    suspend fun loadActiveTrip() {
        withContext(Dispatchers.IO) {
            try {
                val trip = tripDao.getActiveTripWithRoute(TripStatus.IN_PROGRESS)
                if (trip != null) {
                    Log.d("TripRepository", "✅ Active trip loaded: ${trip.trip.tripId}")
                } else {
                    Log.e("TripRepository", "❌ No active trip found in database.")
                }
                _activeTrip.postValue(trip) // ✅ Ensure value is posted even if null
            } catch (e: Exception) {
                Log.e("TripRepository", "❌ Error loading active trip: ${e.message}")
                _activeTrip.postValue(null)
            }
        }
    }


    // Set active trip manually (e.g. when creating a trip)
    fun setActiveTrip(trip: TripWithRoute?) {
        _activeTrip.value = trip
    }


    suspend fun createTrip(tripWithRoute: TripWithRoute) {
        // Insert the route if it doesn't exist
        val routeId = insertRouteIfNotExists(tripWithRoute.route.route)
        val busId = insertBusIfNotExists(tripWithRoute.bus)

        // Create the trip
        val trip = tripWithRoute.trip.copy(routeId = routeId, busId = busId)
        tripDao.insertTrip(trip)
    }

    // Insert route if not already exists
    private suspend fun insertRouteIfNotExists(route: RouteEntity): String {
        // Check if route already exists
        val existingRoute = routeDao.getRouteByName(route.routeName)
        return if (existingRoute.isEmpty()) {
            route.routeId
        } else {
            existingRoute.first().routeId // Return the existing routeId
        }
    }

    suspend fun getPricesForRoute(routeId: String): List<Price> {
        return priceDao.getPricesForRoute(routeId)
    }

    suspend fun getPriceForStations(startStationId: String, destinationStationId: String): Double {
        return priceDao.getPrice(startStationId, destinationStationId)?.amount ?: 0.0
    }

    // Insert bus if not already exists
    private suspend fun insertBusIfNotExists(bus: Bus): String {
        // Check if bus already exists
        val existingBus = busDao.getBusByRegistrationNumber(bus.busNumber)
        return if (existingBus == null) {
            bus.busId
        } else {
            existingBus.busId
        }
    }

    suspend fun hasActiveTrip(): Boolean {
        return tripDao.getActiveTrip() != null
    }

    suspend fun getActiveTrip(): Trip? {
        return tripDao.getActiveTrip()
    }

    // Get active trip with its route
    suspend fun getActiveTripWithRoute(): TripWithRoute? {
        return tripDao.getActiveTripWithRoute(TripStatus.IN_PROGRESS)
    }

    suspend fun updateTripSyncStatus(tripId: String){
        tripDao.updateSyncStatus(tripId,SyncStatus.SYNCED)
    }

    suspend fun getTripById(tripId: String): Trip? {
        return tripDao.getTripById(tripId)
    }

    suspend fun updateTripStatus(trip: Trip) {
        tripDao.updateTripStatus(trip)
    }

}
