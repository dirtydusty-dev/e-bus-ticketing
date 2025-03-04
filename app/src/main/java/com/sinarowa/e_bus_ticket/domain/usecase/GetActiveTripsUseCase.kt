package com.sinarowa.e_bus_ticket.domain.usecase

import com.sinarowa.e_bus_ticket.data.local.enums.TripStatus
import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveTripsUseCase @Inject constructor(private val tripRepository: TripRepository) {

    // This method now returns a Flow of TripWithRoute, which combines Trip and Route data
    suspend fun execute(): TripWithRoute? {
        return tripRepository.getActiveTripWithRoute() // Fetches the active trip along with its route
    }
}
