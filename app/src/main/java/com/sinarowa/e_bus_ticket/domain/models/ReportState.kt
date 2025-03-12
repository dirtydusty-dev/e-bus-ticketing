package com.sinarowa.e_bus_ticket.domain.models

import com.sinarowa.e_bus_ticket.data.local.entities.Expense
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket

data class ReportState(
    val selectedTrip: TripWithRoute? = null, // 🚍 Holds the trip being reported
    val tickets: List<Ticket> = emptyList(), // 🎟 List of tickets
    val ticketsWithRoute: List<TicketWithRoute> = emptyList(),
    val expenses: List<Expense> = emptyList(), // 💰 List of expenses
    val totalSales: Double = 0.0, // 💵 Total ticket sales
    val totalExpenses: Double = 0.0, // 💵 Total expenses
    val netSales: Double = 0.0, // 📈 Net sales (Total Sales - Expenses)
    val isLoading: Boolean = false, // ⏳ Loading state
    val errorMessage: String? = null, // ❌ Error messages
    val reportType: String = "Daily", // 📊 "Daily" or "Trip"
    val formattedReport: String = "", // 📝 The generated report text
    val ticketDetails: Map<String, Pair<Int, Double>> = emptyMap(), // 🎟 Ticket breakdown (Type → Count & Amount)
    val paymentDetails: Map<String, Pair<Int, Double>> = emptyMap(), // 💳 Payment breakdown (Method → Count & Amount)
    val tripSales: List<Pair<String, Double>> = emptyList(), // 🚍 Trip sales breakdown
    val expensesBreakdown: Map<String, Double> = emptyMap() // 💰 Expense breakdown
)
