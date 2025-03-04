package com.sinarowa.e_bus_ticket.domain.usecase

import com.google.gson.Gson
import com.sinarowa.e_bus_ticket.data.dto.CreateTripRequest
import com.sinarowa.e_bus_ticket.data.local.entities.Trip
import com.sinarowa.e_bus_ticket.data.local.entities.TripSyncQueue
import com.sinarowa.e_bus_ticket.data.local.enums.TripStatus
import com.sinarowa.e_bus_ticket.data.repository.BusRepository
import com.sinarowa.e_bus_ticket.data.repository.RouteRepository
import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import com.sinarowa.e_bus_ticket.data.repository.TripSyncQueueRepository
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import com.sinarowa.e_bus_ticket.service.ApiService
import com.sinarowa.e_bus_ticket.utils.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel

class CreateTripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    private val tripSyncQueueRepository: TripSyncQueueRepository,
    private val apiService: ApiService,
    private val routeRepository: RouteRepository,
    private val busRepository: BusRepository
) {

    suspend fun execute(routeId: String, busId: String): Result<TripWithRoute> {
        // Debugging log
        Log.d("CreateTripUseCase", "Starting createTrip execution...")

        // Check if there is any active trip
        val activeTrip = tripRepository.getActiveTrip()
        Log.d("CreateTripUseCase", "Checking for active trip: $activeTrip")

        if (activeTrip != null) {
            Log.d("CreateTripUseCase", "Active trip found, cannot create a new trip.")
            return Result.failure(Exception("There is already an active trip"))
        }

        // Generate a unique tripId using the routeId, busId, and startTime
        val tripId = generateTripId(routeId, busId)
        Log.d("CreateTripUseCase", "Generated unique tripId: $tripId")

        // Proceed to create the trip with status IN_PROGRESS
        val trip = Trip(
            tripId = tripId,  // Generated unique tripId
            routeId = routeId,
            busId = busId,
            startTime = DateTimeUtils.getCurrentDateTime(),
            endTime = null,
            status = TripStatus.IN_PROGRESS
        )
        Log.d("CreateTripUseCase", "Created new trip: $trip")

        // Get Route and Bus data (you would need to fetch these from your repository or API)
        val route = routeRepository.getRouteById(routeId)
        val bus = busRepository.getBusById(busId)

        if (route == null) {
            Log.e("CreateTripUseCase", "Route with ID $routeId not found.")
            return Result.failure(Exception("Route not found"))
        }
        if (bus == null) {
            Log.e("CreateTripUseCase", "Bus with ID $busId not found.")
            return Result.failure(Exception("Bus not found"))
        }

        Log.d("CreateTripUseCase", "Fetched route and bus data. Creating TripWithRoute.")

        // Create a TripWithRoute object
        val tripWithRoute = TripWithRoute(trip = trip, route = route, bus = bus)

        // Save the TripWithRoute to the database
        tripRepository.createTrip(tripWithRoute)
        Log.d("CreateTripUseCase", "Saved trip to database.")


        // Log the trip request before sending it to the API
        val tripRequest = createTripRequestFromTrip(tripWithRoute)
        Log.d("CreateTripUseCase", "Sending request to API: $tripRequest")

        // Try to sync the trip with the server
        val syncResult = syncTripWithServer(tripWithRoute)

        // Debugging log
        Log.d("CreateTripUseCase", "Sync result: $syncResult")

        return if (syncResult.isSuccess) {
            Log.d("CreateTripUseCase", "Trip synced successfully with server.")
            Result.success(tripWithRoute)
        } else {
            // Queue the trip for later sync if failed to sync with the server
            queueTripForSync(tripWithRoute)
            Log.d("CreateTripUseCase", "Sync failed, queued trip for later sync.")
            Result.failure(Exception("Trip created locally, but sync failed"))
        }
    }

    private suspend fun syncTripWithServer(trip: TripWithRoute): Result<TripWithRoute> {
        // Debugging log
        Log.d("CreateTripUseCase", "Attempting to sync trip with server...")

        return try {
            val tripRequest = createTripRequestFromTrip(trip)
            Log.d("CreateTripUseCase", "API request body: ${Gson().toJson(tripRequest)}")

            val response = apiService.createTrip(tripRequest)

            // Debugging log
            Log.d("CreateTripUseCase", "API response: ${response.isSuccessful}")

            if (response.isSuccessful) {
                // Mark as synced in DB
                tripRepository.updateTripSyncStatus(trip.trip.tripId)
                Log.d("CreateTripUseCase", "Trip synced with server, updated sync status.")
                Result.success(trip)
            } else {
                Log.e("CreateTripUseCase", "Failed to sync trip with server.")
                Result.failure(Exception("Failed to sync trip"))
            }
        } catch (e: Exception) {
            Log.e("CreateTripUseCase", "Exception during sync: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun queueTripForSync(trip: TripWithRoute) {
        Log.d("CreateTripUseCase", "Queuing trip for later sync...")

        val tripRequestJson = Gson().toJson(createTripRequestFromTrip(trip))
        val tripSyncQueue = TripSyncQueue(tripRequestJson = tripRequestJson, status = "PENDING")

        // Insert the trip into the queue
        tripSyncQueueRepository.insertTripSyncQueue(tripSyncQueue)
        Log.d("CreateTripUseCase", "Trip added to sync queue.")
    }

    private fun createTripRequestFromTrip(trip: TripWithRoute): CreateTripRequest {
        return CreateTripRequest(
            creationTime = trip.trip.startTime,
            endTime = trip.trip.endTime ?: "",
            registrationNumber = trip.bus.busNumber,
            routeName = trip.route.routeName,
            tripIdentifier = trip.trip.tripId,
            tripStatus = trip.trip.status.toString()
        )
    }

    fun generateTripId(routeId: String, busId: String): String {
        val timestamp = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "TRIP_${routeId}_${busId}_${sdf.format(Date(timestamp))}"
    }
}
