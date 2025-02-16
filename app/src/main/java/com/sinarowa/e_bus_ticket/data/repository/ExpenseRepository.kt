package com.sinarowa.e_bus_ticket.data.repository
import com.sinarowa.e_bus_ticket.data.local.dao.ExpenseDao
import com.sinarowa.e_bus_ticket.data.local.entities.Expense
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    fun getExpensesByTrip(tripId: String): Flow<List<Expense>> {
        return expenseDao.getExpensesByTrip(tripId)
    }

    suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    suspend fun deleteAllExpenses() {
        expenseDao.clearExpenses()
    }

    suspend fun getAllExpenses(): List<Expense> {
        return expenseDao.getAllExpenses()
    }
}
