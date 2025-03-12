package com.sinarowa.e_bus_ticket.domain.models

data class TicketWithRoute(
    val ticketId: String,
    val paymentCategory: String,
    val amount: Double,
    val startStationName: String,
    val destinationStationName: String
)
