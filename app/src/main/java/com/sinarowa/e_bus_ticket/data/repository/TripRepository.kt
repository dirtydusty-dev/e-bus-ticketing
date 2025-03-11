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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TripRepository @Inject constructor(
    private val tripDao: TripDao,
    private val routeDao: RouteDao,
    private val busDao: BusDao,
    private val priceDao: PriceDao
) {

    private val _activeTrip = MutableLiveData<TripWithRoute?>()
    val activeTrip: LiveData<TripWithRoute?> get() = _activeTrip

    init {
        CoroutineScope(Dispatchers.IO).launch {
            loadActiveTrip()
        }
    }

    fun updateActiveTrip(trip: TripWithRoute?) {
        _activeTrip.postValue(trip)
        Log.d("TripRepository", "Updated activeTrip: ${trip?.trip?.tripId ?: "null"}")
    }

    suspend fun loadActiveTrip() {
        withContext(Dispatchers.IO) {
            try {
                val trip = tripDao.getActiveTripWithRoute(TripStatus.IN_PROGRESS)
                _activeTrip.postValue(trip)
                Log.d("TripRepository", "✅ Active trip loaded: ${trip?.trip?.tripId ?: "None"}")
            } catch (e: Exception) {
                Log.e("TripRepository", "❌ Error loading active trip: ${e.message}")
                _activeTrip.postValue(null)
            }
        }
    }

    suspend fun createTrip(tripWithRoute: TripWithRoute) {
        withContext(Dispatchers.IO) {
            val routeId = tripWithRoute.route.route.routeId
            val busId = tripWithRoute.bus.busId
            val trip = tripWithRoute.trip.copy(routeId = routeId, busId = busId)
            tripDao.insertTrip(trip)
            updateActiveTrip(tripWithRoute) // Ensure active trip is updated
        }
    }

    private suspend fun insertRouteIfNotExists(route: RouteEntity): String {
        val existingRoute = routeDao.getRouteByName(route.routeName)
        return if (existingRoute.isEmpty()) {
            routeDao.insert(route)
            route.routeId
        } else {
            existingRoute.first().routeId
        }
    }


    suspend fun getPricesForRoute(routeId: String): List<Price> {
        return priceDao.getPricesForRoute(routeId)
    }

    suspend fun getPriceForStations(startStationId: String, destinationStationId: String): Double {
        return priceDao.getPrice(startStationId, destinationStationId)?.amount ?: 0.0
    }

    suspend fun hasActiveTrip(): Boolean = tripDao.getActiveTrip() != null

    suspend fun getActiveTrip(): Trip? = tripDao.getActiveTrip()

    suspend fun getActiveTripWithRoute(): TripWithRoute? = tripDao.getActiveTripWithRoute(TripStatus.IN_PROGRESS)

    suspend fun updateTripSyncStatus(tripId: String) {
        tripDao.updateSyncStatus(tripId, SyncStatus.SYNCED)
    }

    suspend fun getTripById(tripId: String): Trip? = tripDao.getTripById(tripId)

    suspend fun updateTripStatus(trip: Trip) {
        tripDao.updateTripStatus(trip)
    }
}