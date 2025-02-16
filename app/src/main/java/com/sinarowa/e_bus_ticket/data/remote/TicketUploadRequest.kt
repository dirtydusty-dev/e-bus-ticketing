package com.sinarowa.e_bus_ticket.data.remote



data class TicketUploadRequest(
    val tickets: List<TicketData>  // The API expects a JSON object with a list inside
)

data class TicketData(
    val ticketId: String,
    val tripId: String,
    //val seatNumber: Int,
    val fromStop: String,
    val toStop: String,
    val price: Double,
    val timestamp: Long
)
