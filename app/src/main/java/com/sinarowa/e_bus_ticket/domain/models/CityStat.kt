package com.sinarowa.e_bus_ticket.domain.models

data class CityStat(
    val cityName: String,
    val ticketType: String,  // Added ticket type
    val count: Int,
    val amount: Double
)
