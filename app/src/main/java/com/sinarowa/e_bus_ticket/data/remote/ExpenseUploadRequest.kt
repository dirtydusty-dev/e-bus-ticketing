package com.sinarowa.e_bus_ticket.data.remote



data class ExpenseUploadRequest(
    val expenses: List<ExpenseData>  // Same structure for expenses
)

data class ExpenseData(
    val expenseId: String,
    val tripId: String,
    val type: String,
    val amount: Double,
    val description: String,
    val timestamp: Long
)
