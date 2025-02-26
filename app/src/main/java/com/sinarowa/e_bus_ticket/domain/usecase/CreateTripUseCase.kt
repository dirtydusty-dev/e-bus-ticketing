package com.sinarowa.e_bus_ticket.domain.usecase

import com.sinarowa.e_bus_ticket.data.local.entities.Trip
import com.sinarowa.e_bus_ticket.data.local.enums.TripStatus
import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import com.sinarowa.e_bus_ticket.utils.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateTripUseCase @Inject constructor(private val tripRepository: TripRepository) {
    suspend fun execute(routeId: Long, busId: Long, scope: CoroutineScope): Long {
        return withContext(Dispatchers.IO) {
            val newTrip = Trip(
                routeId = routeId,
                busId = busId,
                startTime = DateTimeUtils.getCurrentDateTime(),
                stopTime = null,
                status = TripStatus.IN_PROGRESS
            )
            tripRepository.insertTrip(newTrip)
        }

    }
}