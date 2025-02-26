package com.sinarowa.e_bus_ticket.domain.usecase

import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveTripsUseCase @Inject constructor(private val tripRepository: TripRepository) {
    operator fun invoke(): Flow<List<TripWithRoute>> {
        return tripRepository.getActiveTripsWithRoute()
    }
}
