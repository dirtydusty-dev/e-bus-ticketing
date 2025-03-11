package com.sinarowa.e_bus_ticket.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sinarowa.e_bus_ticket.ui.components.DropdownMenuComponent
import com.sinarowa.e_bus_ticket.ui.theme.GrayDisabled
import com.sinarowa.e_bus_ticket.ui.theme.LightBlueBackground
import com.sinarowa.e_bus_ticket.ui.theme.SkyBluePrimary
import com.sinarowa.e_bus_ticket.ui.theme.YellowSecondary
import com.sinarowa.e_bus_ticket.viewmodel.ExpenseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("LongLogTag")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogExpenseScreen(
    navController: NavController,
    tripId: String,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.initialize(tripId)
    }

    LaunchedEffect(state.logResult) {
        if (state.logResult?.isSuccess == true) {
            Log.d("LogExpenseScreen", "✅ Expense logged successfully, resetting fields")
            amount = ""
            description = ""
            viewModel.resetFields()
            showSuccess = true
            // Show success message for 2 seconds, then hide
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Expense logged successfully!",
                    duration = SnackbarDuration.Short
                )
                showSuccess = false
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = LightBlueBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Log Expense",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyBluePrimary),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp)),
                    containerColor = SkyBluePrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = YellowSecondary
                        )
                        Text(
                            text = data.visuals.message,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Expense Type Dropdown
            item {
                DropdownMenuComponent(
                    label = "Select Expense Type",
                    items = listOf(
                        "Fuel Cost", "Toll Gates", "Vouchers", "Inspector Allowance", "Driver Allowance",
                        "Conductor Allowance", "Advantages", "RM", "Police Tickets", "Other"
                    ),
                    selectedItem = state.expenseType,
                    onSelectionChanged = { viewModel.setExpenseType(it) },
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowSecondary,
                        unfocusedBorderColor = SkyBluePrimary,
                        focusedLabelColor = YellowSecondary,
                        unfocusedLabelColor = SkyBluePrimary,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // Description Field (Mandatory)
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        viewModel.setDescription(it)
                    },
                    label = { Text("Description (Required)", color = SkyBluePrimary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowSecondary,
                        unfocusedBorderColor = SkyBluePrimary,
                        focusedLabelColor = YellowSecondary,
                        unfocusedLabelColor = SkyBluePrimary,
                        cursorColor = YellowSecondary,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    isError = description.isBlank()
                )
            }

            // Amount Field
            item {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { value ->
                        amount = value.filter { it.isDigit() || it == '.' }
                        viewModel.setAmount(amount.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("Amount (USD)", color = SkyBluePrimary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowSecondary,
                        unfocusedBorderColor = SkyBluePrimary,
                        focusedLabelColor = YellowSecondary,
                        unfocusedLabelColor = SkyBluePrimary,
                        cursorColor = YellowSecondary,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    isError = amount.isBlank() || amount.toDoubleOrNull() == null || amount.toDouble() <= 0
                )
            }

            // Error Message
            state.errorMessage?.let {
                item {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Log Expense Button
            item {
                val isButtonEnabled = state.expenseType.isNotBlank() &&
                        state.amount > 0 &&
                        description.isNotBlank() &&
                        !state.isProcessing

                Button(
                    onClick = { viewModel.logExpense() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isButtonEnabled) YellowSecondary else GrayDisabled,
                        disabledContainerColor = GrayDisabled
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isButtonEnabled,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (state.isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Log Expense",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Log Expense",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Bottom padding
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}