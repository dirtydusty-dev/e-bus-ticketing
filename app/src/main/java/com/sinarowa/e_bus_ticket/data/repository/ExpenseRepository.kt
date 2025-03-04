package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.ExpenseDao
import com.sinarowa.e_bus_ticket.data.local.entities.Expense
import com.sinarowa.e_bus_ticket.data.local.enums.SyncStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExpenseRepository @Inject constructor(private val expenseDao: ExpenseDao) {

    suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    fun getExpensesForTrip(tripId: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesForTrip(tripId)
    }

    fun getTotalExpensesForTrip(tripId: Long): Flow<Double?> {
        return expenseDao.getTotalExpensesForTrip(tripId)
    }

    suspend fun getUnsyncedExpenses(tripId: String, status: SyncStatus): List<Expense> {
        return expenseDao.getUnsyncedExpenses(tripId, status)
    }

    suspend fun updateExpenseSyncStatus(expense: Expense) {
        expenseDao.updateExpenseSyncStatus(expense)
    }
}
