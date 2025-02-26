package com.sinarowa.e_bus_ticket.reports

// **Breakdown of ticket sales per station**
data class TripWithTicketStationBreakdown(
    val startStation: String,
    val stopStation: String,
    val ticketBreakdown: List<TripWithTicketBreakdown> // Contains ticket sales per type
)

// **Summary of ticket sales per station for a trip**
data class TripWithStationTotals(
    val tripId: Long,
    val station: String,
    val ticketBreakdown: List<TripWithTicketBreakdown>,
    val totalAmount: Double
)

// **Raw version of station totals (used for fetching from Room)**
data class TripWithStationTotalsRaw(
    val tripId: Long,
    val station: String,
    val totalAmount: Double
)

// **Raw version of trip sales (used for fetching from Room)**
data class TripSalesRaw(
    val tripId: Long,
    val routeId: Long,
    val totalTickets: Int,
    val totalSales: Double?,
    val totalExpenses: Double?,
    val netSales: Double?
)

// **Overall trip sales and expense summary**
data class TripSales(
    val tripId: Long,
    val routeId: Long,
    val totalTickets: Int,
    val totalSales: Double,
    val totalExpenses: Double,
    val netSales: Double,
    val stationBreakdown: List<TripWithStationTotals> = emptyList(),
    val expenseBreakdown: List<TripWithExpenseBreakdown> = emptyList(),
    val ticketBreakdown: List<TripWithTicketBreakdown> = emptyList(),
    val ticketStationBreakdown: List<TripWithTicketStationBreakdown> = emptyList()
)

// **Breakdown of expenses for a trip (e.g., fuel, repairs, food)**
data class TripWithExpenseBreakdown(
    val type: String,
    val count: Int,
    val amount: Double
)

// **Breakdown of ticket sales per type (Adult, Child, etc.)**
data class TripWithTicketBreakdown(
    val startStation: String,  // ✅ Ensuring this field exists
    val stopStation: String?,  // Optional: Needed for station-based breakdowns
    val type: String,
    val count: Int,
    val amount: Double
)
