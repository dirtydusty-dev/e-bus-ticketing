package com.sinarowa.e_bus_ticket.domain.models

import com.sinarowa.e_bus_ticket.data.local.enums.TripStatus

data class TripWithRoute(
    val id: Long,
    val routeId: Long,
    val busId: Long,
    val stopTime: String?,
    val status: TripStatus,
    val routeName: String,
    val busName: String,
    val busCapacity: Int, // ✅ Add Bus Capacity
    val startTime: String,
    val ticketCount: Int = 0, // ✅ Total tickets sold
    val luggageCount: Int = 0, // ✅ Total luggage tickets
    val departedCount: Int = 0 // ✅ Tickets where status = ARRIVED
)
