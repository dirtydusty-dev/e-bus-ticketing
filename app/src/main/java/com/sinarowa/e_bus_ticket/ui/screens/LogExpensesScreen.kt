package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sinarowa.e_bus_ticket.data.local.entities.Expense
import com.sinarowa.e_bus_ticket.viewmodel.ExpensesViewModel
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun LogExpensesScreen(
    tripId: String,
    navController: NavController,
    expensesViewModel: ExpensesViewModel = viewModel(),
    ticketViewModel: TicketViewModel = viewModel()
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val expenseTypes = listOf(
        "Fuel", "Inspector Allowance", "Driver Allowance", "Conductor Allowance",
        "Advantages", "Toll Gate", "Meals", "Police Ticket", "Rank Fees", "Vouchers", "Other"
    )

    var selectedExpense by remember { mutableStateOf(expenseTypes.first()) }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) } // ✅ Track button loading

    val fromCity = remember { mutableStateOf("Detecting...") }

    // ✅ Fetch city from GPS
    LaunchedEffect(tripId) {
        if (fromCity.value == "Detecting...") { // ✅ Prevent multiple calls
            coroutineScope.launch {
                fromCity.value = ticketViewModel.getCityFromCoordinates(tripId)
            }
        }
    }


    Scaffold(
        scaffoldState = scaffoldState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Log Expense", style = MaterialTheme.typography.h5, color = Color(0xFF1565C0))
            Spacer(modifier = Modifier.height(16.dp))

            // 🔹 Expense Type Dropdown
            DropdownMenuComponent("Select Expense Type", expenseTypes, selectedExpense) { newSelection ->
                selectedExpense = newSelection
            }
            Spacer(modifier = Modifier.height(12.dp))

            // 🔹 Description Field (Optional)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 🔹 Amount Field with Validation
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                    amountError = it.toDoubleOrNull()?.let { it <= 0.0 } ?: true
                },
                label = { Text("Amount (USD)") },
                modifier = Modifier.fillMaxWidth(),
                isError = amountError
            )
            if (amountError) {
                Text("Invalid amount. Enter a number greater than 0.", color = Color.Red, style = MaterialTheme.typography.body2)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Save Expense Button
            val isButtonEnabled = !isProcessing && amount.isNotEmpty() && !amountError

            Button(
                onClick = {
                    isProcessing = true // ✅ Disable button immediately

                    coroutineScope.launch {
                        val newExpense = Expense(
                            expenseId = UUID.randomUUID().toString(),
                            tripId = tripId,
                            type = selectedExpense,
                            amount = amount.toDouble(),
                            description = description,
                            location = fromCity.value
                        )
                        expensesViewModel.insertExpense(newExpense)

                        // ✅ Show success message using Snackbar
                        scaffoldState.snackbarHostState.showSnackbar("Expense logged successfully!")

                        // 🔄 Reset fields properly
                        selectedExpense = expenseTypes.first()
                        description = ""
                        amount = ""
                        isProcessing = false // ✅ Re-enable button after success
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = if (isButtonEnabled) Color(0xFFFFEB3B) else Color.Gray),
                shape = RoundedCornerShape(8.dp),
                enabled = isButtonEnabled
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black
                    )
                } else {
                    Text("Save Expense", color = Color.Black, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
        }
    }
}

