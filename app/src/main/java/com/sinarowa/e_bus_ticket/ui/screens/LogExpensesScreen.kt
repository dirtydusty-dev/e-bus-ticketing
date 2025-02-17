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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sinarowa.e_bus_ticket.data.local.entities.Expense
import com.sinarowa.e_bus_ticket.viewmodel.ExpensesViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun LogExpensesScreen(tripId: String, navController: NavController, expensesViewModel: ExpensesViewModel = viewModel()) {
    val expenseTypes = listOf("Fuel", "Tollgates", "Meals", "Allowances", "Police Ticket", "Rank Fees", "Other")
    var selectedExpense by remember { mutableStateOf(expenseTypes[0]) }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Log Expense", style = MaterialTheme.typography.h5, color = Color(0xFF1565C0))
        Spacer(modifier = Modifier.height(16.dp))

        DropdownMenuComponent(
            label = "Select Expense Type",
            items = expenseTypes,
            selectedItem = selectedExpense,
            onSelectionChanged = { selectedExpense = it }
        )
        Spacer(modifier = Modifier.height(12.dp))

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

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount (USD)") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF1565C0),
                unfocusedBorderColor = Color.Gray
            )
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (amount.isNotBlank() && amount.toDoubleOrNull() != null) {
                    scope.launch {
                        val newExpense = Expense(
                            expenseId = UUID.randomUUID().toString(),
                            tripId = tripId, // Replace with actual tripId
                            type = selectedExpense,
                            amount = amount.toDouble(),
                            description = description,
                            timestamp = System.currentTimeMillis()
                        )
                        expensesViewModel.insertExpense(newExpense)
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
}