package com.sinarowa.e_bus_ticket.domain.usecase

import com.sinarowa.e_bus_ticket.data.local.entities.Trip
import com.sinarowa.e_bus_ticket.data.local.enums.TripStatus
import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import com.sinarowa.e_bus_ticket.utils.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*

class CreateTripUseCase @Inject constructor(private val tripRepository: TripRepository) {

    suspend fun execute(routeId: String, busId: String): Result<Trip> {
        // Check if there is any active trip
        if (tripRepository.hasActiveTrip()) {
            return Result.failure(Exception("There is already an active trip"))
        }

        // Generate a unique tripId using the routeId, busId, and startTime
        val tripId = generateTripId(routeId, busId)

        // Proceed to create the trip with status IN_PROGRESS (assuming this is the desired initial status)
        val trip = Trip(
            tripId = tripId,  // Generated unique tripId
            routeId = routeId,
            busId = busId,
            startTime = DateTimeUtils.getCurrentDateTime(),
            endTime = null,
            status = TripStatus.IN_PROGRESS // Set the initial status to IN_PROGRESS
        )

        return try {
            tripRepository.createTrip(trip)
            Result.success(trip)
        } catch (e: Exception) {
            // Return the exception if something goes wrong
            Result.failure(e)
        }
    }

    fun generateTripId(routeId: String, busId: String): String {
        // Get the current time as a timestamp
        val timestamp = System.currentTimeMillis()

        // Format the timestamp into a readable format (e.g., "yyyyMMdd_HHmmss")
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val formattedTimestamp = sdf.format(Date(timestamp))

        // Combine routeId, busId, and the formatted timestamp to create a unique tripId
        return "TRIP_${routeId}_${busId}_$formattedTimestamp"
    }
}






