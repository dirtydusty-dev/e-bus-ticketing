package com.sinarowa.e_bus_ticket.domain.models

data class SegmentStat(
    val from: String,
    val to: String,
    val ticketType: String,  // Added ticket type
    val count: Int,
    val amount: Double
)
