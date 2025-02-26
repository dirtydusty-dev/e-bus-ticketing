package com.sinarowa.e_bus_ticket.domain.usecase

import com.sinarowa.e_bus_ticket.data.repository.LocationRepository
import javax.inject.Inject

class GetLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {

    suspend operator fun invoke(routeId: Long): String {
        // Ensure currentLocation.value is not null and extract latitude and longitude
        val location = locationRepository.currentLocation.value
        return if (location != null) {
            // Pass latitude and longitude to getClosestStop
            locationRepository.getClosestStop(location.latitude, location.longitude, routeId)
        } else {
            "Unknown" // Or any default value in case location is null
        }
    }
}
