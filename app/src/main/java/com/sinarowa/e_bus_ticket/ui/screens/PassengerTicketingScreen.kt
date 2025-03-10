package com.sinarowa.e_bus_ticket.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinarowa.e_bus_ticket.ui.components.DropdownMenuComponent
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color

@SuppressLint("LongLogTag")
@Composable
fun PassengerTicketingScreen(viewModel: TicketViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // Reset fields and fetch location on screen load
    LaunchedEffect(Unit) {
        viewModel.resetFields(context)
    }

    LaunchedEffect(state.isProcessing) {
        if (!state.isProcessing) {
            Log.d("PassengerTicketingScreen", "✅ Sell process completed, refreshing UI.")
        }
    }

    // React to sell result and refresh
    LaunchedEffect(state.sellResult) {
        if (state.sellResult?.isSuccess == true) {
            viewModel.resetFields(context) // Reset and re-fetch after sale
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Passenger Ticketing",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Step 1: From City
        OutlinedTextField(
            value = state.fromStation,
            onValueChange = {},
            label = { Text("From City") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            isError = state.fromStation.isBlank(),
            placeholder = { if (state.fromStation.isBlank()) Text("Fetching location...") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Step 2: Select Destination
        if (state.fromStation.isNotBlank()) {
            if (state.validDestinations.isEmpty()) {
                Text("No valid destinations available.", color = MaterialTheme.colorScheme.error)
            } else {
                DropdownMenuComponent(
                    label = "Select Destination",
                    items = state.validDestinations,
                    selectedItem = state.destination,
                    onSelectionChanged = { viewModel.setDestination(it) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Step 3: Select Ticket Type
        if (state.destination.isNotBlank()) {
            DropdownMenuComponent(
                label = "Select Ticket Type",
                items = listOf("Adult", "Child"),
                selectedItem = state.ticketType,
                onSelectionChanged = { viewModel.setTicketType(it) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Step 4: Short Amount (Adult only)
        if (state.ticketType == "Adult" && state.originalPrice > 0) {
            ShortAmountSelector(
                shortAmount = state.shortAmount,
                maxAmount = state.originalPrice.toInt(),
                onAmountChanged = { viewModel.setShortAmount(it) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Step 5: Final Ticket Price
        if (state.displayPrice > 0) {
            OutlinedTextField(
                value = "${state.displayPrice} USD",
                onValueChange = {},
                label = { Text("Final Ticket Price") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (state.shortAmount > 0) {
                Text("Short Applied: -$${state.shortAmount}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (state.ticketType == "Child") {
                Text("Child Fare Applied", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
        } else if (state.destination.isNotBlank() && state.originalPrice == 0.0) {
            Text("Error fetching ticket price. Try again.", color = MaterialTheme.colorScheme.error)
        }

        // Step 6: Seat & Passenger Info
        if (state.remainingSeats > 0 || state.activePassengers > 0) {
            TicketSummaryCard(
                remainingSeats = state.remainingSeats,
                activePassengers = state.activePassengers
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (state.remainingSeats == 0 && state.activePassengers > 0) {
            Text("No seats available.", color = MaterialTheme.colorScheme.error)
        }

        // Step 7: Sell & Print Button
        val isButtonEnabled = state.fromStation.isNotBlank() &&
                state.destination.isNotBlank() &&
                state.fromStation != state.destination &&
                state.displayPrice > 0 &&
                state.remainingSeats > 0

        Button(
            onClick = { viewModel.sellTicket(state.displayPrice) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = if (isButtonEnabled) MaterialTheme.colorScheme.tertiary else Color.Gray),
            shape = RoundedCornerShape(8.dp),
            enabled = isButtonEnabled && !state.isProcessing
        ) {
            if (state.isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onTertiary)
            } else {
                Icon(Icons.Default.Print, contentDescription = "Print")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sell & Print", color = MaterialTheme.colorScheme.onTertiary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Short Amount Selector
@Composable
fun ShortAmountSelector(shortAmount: Int, maxAmount: Int, onAmountChanged: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { if (shortAmount > 0) onAmountChanged(shortAmount - 1) },
            enabled = shortAmount > 0,
            modifier = Modifier.size(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("-", color = MaterialTheme.colorScheme.onError)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text("Short: $$shortAmount", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = { if (shortAmount < maxAmount - 1) onAmountChanged(shortAmount + 1) },
            modifier = Modifier.size(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("+", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

// Ticket Summary Card
@Composable
fun TicketSummaryCard(remainingSeats: Int, activePassengers: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Seats Remaining", style = MaterialTheme.typography.bodyMedium)
                Text("$remainingSeats", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Active Passengers", style = MaterialTheme.typography.bodyMedium)
                Text("$activePassengers", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
