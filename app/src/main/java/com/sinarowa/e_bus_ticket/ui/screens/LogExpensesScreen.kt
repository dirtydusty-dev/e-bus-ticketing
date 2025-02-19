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
    val expenseTypes = listOf("Fuel","Inspector Allowance","Driver Allowance","Conductor Allowance","Advantages", "Toll Gate", "Meals", "Police Ticket", "Rank Fees", "Vouchers", "Other")
    var selectedExpense by remember { mutableStateOf(expenseTypes.first()) }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var expenseTypeError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var formStateKey by remember { mutableStateOf(0) }

    key(formStateKey) {
        val fromCity = remember { mutableStateOf("Detecting...") }

        // Fetch city from GPS
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                fromCity.value = ticketViewModel.getCityFromCoordinates(tripId)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Log Expense", style = MaterialTheme.typography.h5, color = Color(0xFF1565C0))
            Spacer(modifier = Modifier.height(16.dp))

            // 🔹 Expense Type Dropdown
            DropdownMenuComponent(
                label = "Select Expense Type",
                items = expenseTypes,
                selectedItem = selectedExpense,
                onSelectionChanged = { selectedExpense = it }
            )
            if (expenseTypeError) {
                Text("Please select an expense type.", color = Color.Red, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))

            // 🔹 Description Field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF1565C0),
                    unfocusedBorderColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 🔹 Amount Field with Validation
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                    amountError = it.isBlank() || it.toDoubleOrNull() == null || it.toDouble() <= 0.0
                },
                label = { Text("Amount (USD)") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF1565C0),
                    unfocusedBorderColor = Color.Gray
                )
            )
            if (amountError) {
                Text("Please enter a valid amount.", color = Color.Red, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 🔹 Save Expense Button
            Button(
                onClick = {
                    // **Validation before logging**
                    expenseTypeError = selectedExpense.isBlank()
                    amountError = amount.isBlank() || amount.toDoubleOrNull() == null || amount.toDouble() <= 0.0

                    if (!amountError && !expenseTypeError) {
                        scope.launch {
                            val newExpense = Expense(
                                expenseId = UUID.randomUUID().toString(),
                                tripId = tripId,
                                type = selectedExpense,
                                amount = amount.toDouble(),
                                description = description,
                                location = fromCity.value
                            )
                            expensesViewModel.insertExpense(newExpense)
                            showConfirmation = true

                            // 🔄 Reset form
                            amount = ""
                            description = ""
                            selectedExpense = expenseTypes.first()

                            delay(1500) // ✅ Show confirmation for 1.5 seconds
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFEB3B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.AttachMoney, contentDescription = "Save", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Expense", color = Color.Black, fontSize = 18.sp)
            }
        }

        // 🔹 Confirmation Dialog
        if (showConfirmation) {
            AlertDialog(
                onDismissRequest = { showConfirmation = false },
                title = { Text("Success", color = Color(0xFF1565C0), fontSize = 20.sp) },
                text = { Text("Expense successfully logged.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmation = false
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1565C0))
                    ) {
                        Text("OK", color = Color.White)
                    }
                }
            )
        }
    }
}
