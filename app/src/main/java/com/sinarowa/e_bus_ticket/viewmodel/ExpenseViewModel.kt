package com.sinarowa.e_bus_ticket.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinarowa.e_bus_ticket.domain.usecase.LogExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpenseState(
    val tripId: String = "",
    val expenseType: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val isProcessing: Boolean = false,
    val logResult: Result<Unit>? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val logExpenseUseCase: LogExpenseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseState())
    val state: StateFlow<ExpenseState> get() = _state.asStateFlow()

    fun initialize(tripId: String) {
        _state.value = _state.value.copy(tripId = tripId)
        Log.d("ExpenseViewModel", "Initialized with tripId: $tripId")
    }

    fun setExpenseType(type: String) {
        _state.value = _state.value.copy(expenseType = type, errorMessage = null)
        Log.d("ExpenseViewModel", "Expense type set: $type")
    }

    fun setDescription(description: String) {
        _state.value = _state.value.copy(description = description)
        Log.d("ExpenseViewModel", "Description set: $description")
    }

    fun setAmount(amount: Double) {
        _state.value = _state.value.copy(amount = amount, errorMessage = null)
        Log.d("ExpenseViewModel", "Amount set: $amount")
    }

    fun logExpense() {
        val currentState = _state.value
        if (currentState.expenseType.isBlank()) {
            _state.value = currentState.copy(errorMessage = "Please select an expense type")
            return
        }
        if (currentState.amount <= 0) {
            _state.value = currentState.copy(errorMessage = "Amount must be greater than zero")
            return
        }

        _state.value = currentState.copy(isProcessing = true, errorMessage = null, logResult = null)
        viewModelScope.launch {
            try {
                val result = logExpenseUseCase.execute(
                    tripId = currentState.tripId,
                    expenseType = currentState.expenseType,
                    description = currentState.description,
                    amount = currentState.amount
                )
                _state.value = _state.value.copy(isProcessing = false, logResult = result)
                if (result.isFailure) {
                    Log.e("ExpenseViewModel", "Failed to log expense: ${result.exceptionOrNull()?.message}")
                    _state.value = _state.value.copy(errorMessage = "Failed to log expense: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("ExpenseViewModel", "Exception logging expense: ${e.message}")
                _state.value = _state.value.copy(
                    isProcessing = false,
                    logResult = Result.failure(e),
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

    fun resetFields() {
        _state.value = _state.value.copy(
            expenseType = "",
            description = "",
            amount = 0.0,
            isProcessing = false,
            logResult = null,
            errorMessage = null
        )
        Log.d("ExpenseViewModel", "Fields reset")
    }
}