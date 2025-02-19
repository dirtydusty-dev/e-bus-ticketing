package com.sinarowa.e_bus_ticket.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinarowa.e_bus_ticket.data.local.entities.Expense
import com.sinarowa.e_bus_ticket.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    fun getExpensesByTrip(tripId: String) {
        viewModelScope.launch {
            expenseRepository.getExpensesByTrip(tripId).collect { expenseList ->
                _expenses.value = expenseList
            }
        }
    }

    fun insertExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {  // ✅ Ensure it's done on IO thread
            expenseRepository.insertExpense(expense)
        }
    }

    suspend fun getTotalExpenses(): Double {
        return expenseRepository.getAllExpenses().sumOf { it.amount }
    }


    fun clearExpenses() {
        viewModelScope.launch {
            expenseRepository.deleteAllExpenses()
        }
    }
}
