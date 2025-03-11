package com.sinarowa.e_bus_ticket.domain.usecase

import com.sinarowa.e_bus_ticket.data.local.entities.Expense
import com.sinarowa.e_bus_ticket.data.local.enums.SyncStatus
import com.sinarowa.e_bus_ticket.data.repository.ExpenseRepository
import com.sinarowa.e_bus_ticket.utils.DateTimeUtils
import javax.inject.Inject

class LogExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    suspend fun execute(
        tripId: String,
        expenseType: String,
        description: String,
        amount: Double
    ): Result<Unit> {
        return try {
            // Validate inputs
            if (tripId.isBlank()) {
                return Result.failure(IllegalArgumentException("Trip ID cannot be blank"))
            }
            if (expenseType.isBlank()) {
                return Result.failure(IllegalArgumentException("Expense type cannot be blank"))
            }
            if (amount <= 0) {
                return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
            }

            // Create expense entity
            val expense = Expense(
                tripId = tripId,
                expenseType = expenseType,
                description = description,
                amount = amount,
                creationTime = DateTimeUtils.getCurrentDateTime(),
                syncStatus = SyncStatus.PENDING
            )

            // Insert into database
            expenseRepository.insertExpense(expense)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}