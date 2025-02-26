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
import com.sinarowa.e_bus_ticket.ui.components.DropdownMenuComponent
import com.sinarowa.e_bus_ticket.viewmodel.ExpenseViewModel
import com.sinarowa.e_bus_ticket.viewmodel.TicketingViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun LogExpensesScreen(
    tripId: Long, // ✅ Fixed: Trip ID should be Long
    expensesViewModel: ExpenseViewModel,
    ticketViewModel: TicketingViewModel
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
    var isProcessing by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    val fromCity by ticketViewModel.currentLocation.collectAsState()



    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp), // ✅ Moves Snackbar down slightly from the absolute top
                contentAlignment = Alignment.TopCenter
            ) {
                SnackbarHost(scaffoldState.snackbarHostState)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    isProcessing = true // ✅ Show loading indicator
                    coroutineScope.launch {
                        expensesViewModel.logExpense(tripId,selectedExpense,description,amount.toDouble(),)

                        // 🔄 Reset fields properly
                        selectedExpense = expenseTypes.first()
                        description = ""
                        amount = ""
                        isProcessing = false // ✅ Re-enable button after success
                        showSnackbar = true  // ✅ Show Snackbar
                    }
                },
                enabled = isButtonEnabled, // ✅ Disable while processing
                modifier = Modifier.fillMaxWidth().height(50.dp), // ✅ Set button height
                colors = ButtonDefaults.buttonColors(backgroundColor = if (isButtonEnabled) Color(0xFFFFEB3B) else Color.Gray),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp) // ✅ Ensure proper size
                            .padding(4.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.AttachMoney, contentDescription = "Money Icon", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Expense", color = Color.Black, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
        }
    }

    // ✅ Show Snackbar properly
    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar("Expense logged successfully!") // ✅ Display success message
            }
            showSnackbar = false // ✅ Reset snackbar trigger
        }
    }
}
