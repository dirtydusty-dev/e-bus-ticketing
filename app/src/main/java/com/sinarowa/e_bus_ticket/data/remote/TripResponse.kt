package com.sinarowa.e_bus_ticket.data.remote


data class TripResponse(
    val tripId: String,
    val from: String,
    val to: String,
    val date: String,
    val totalSeats: Int,
    val prices: List<PriceDetail>
)

data class PriceDetail(
    val from: String,
    val to: String,
    val price: Double
)
