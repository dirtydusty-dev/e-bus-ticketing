package com.sinarowa.e_bus_ticket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinarowa.e_bus_ticket.data.local.entities.Expense
import com.sinarowa.e_bus_ticket.data.repository.ExpenseRepository
import com.sinarowa.e_bus_ticket.utils.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(private val expenseRepository: ExpenseRepository) : ViewModel() {

    fun logExpense(tripId: Long, type: String, description: String, amount: Double) {
        viewModelScope.launch {
            val expense = Expense(
                tripId = tripId,
                type = type,
                description = description,
                amount = amount,
                creationTime = DateTimeUtils.getCurrentDateTime()
            )
            expenseRepository.insertExpense(expense)
        }
    }

    fun getExpensesForTrip(tripId: Long) = expenseRepository.getExpensesForTrip(tripId)
}
