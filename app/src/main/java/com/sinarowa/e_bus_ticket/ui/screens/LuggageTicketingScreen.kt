package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
import kotlinx.coroutines.launch

@Composable
fun LuggageTicketingScreen(
    tripId: String,
    ticketViewModel: TicketViewModel = viewModel(),
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val fromCity = remember { mutableStateOf("Detecting...") }
    val routeStops by ticketViewModel.routeStops.collectAsState()
    LaunchedEffect(tripId) { ticketViewModel.fetchRouteStops(tripId) }

    val allStops = remember(routeStops) { routeStops }
    var destination by remember { mutableStateOf("") }
    var luggageDescription by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var priceError by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) } // ✅ Track button loading

    // ✅ Detect user's city
    LaunchedEffect(tripId) {
        if (fromCity.value == "Detecting...") { // ✅ Prevent multiple calls
            coroutineScope.launch {
                fromCity.value = ticketViewModel.getCityFromCoordinates(tripId)
            }
        }
    }


    // ✅ Filter stops: Exclude `fromCity`
    val validStops = remember(allStops, fromCity.value) {
        allStops.filter { it != fromCity.value }
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
            Text("Luggage Ticketing", style = MaterialTheme.typography.h5, color = Color(0xFF1565C0))
            Spacer(modifier = Modifier.height(8.dp))

            // 🏙️ FROM City (Auto-detected)
            OutlinedTextField(
                value = fromCity.value,
                onValueChange = {},
                label = { Text("From City") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 📌 Destination Selection
            DropdownMenuComponent("Select Destination", validStops, destination) { newSelection ->
                destination = newSelection
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 🏋🏽 Luggage Weight/Description (Optional)
            OutlinedTextField(
                value = luggageDescription,
                onValueChange = { luggageDescription = it },
                label = { Text("Estimate Weight / Description (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 💰 Price Input (Manual Entry)
            OutlinedTextField(
                value = price,
                onValueChange = {
                    price = it
                    priceError = it.toDoubleOrNull()?.let { it <= 0.0 } ?: true
                },
                label = { Text("Ticket Price") },
                modifier = Modifier.fillMaxWidth(),
                isError = priceError
            )
            if (priceError) {
                Text("Invalid price. Enter a number greater than 0.", color = Color.Red, style = MaterialTheme.typography.body2)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Issue Luggage Ticket Button
            val isButtonEnabled = destination.isNotEmpty() && !priceError && price.isNotEmpty() && !isProcessing

            Button(
                onClick = {
                    isProcessing = true // ✅ Disable button immediately

                    coroutineScope.launch {
                        val newTicket = com.sinarowa.e_bus_ticket.data.local.entities.Ticket(
                            ticketId = System.currentTimeMillis().toString(),
                            tripId = tripId,
                            fromStop = fromCity.value,
                            toStop = destination,
                            luggage = luggageDescription,
                            price = price.toDouble(),
                            ticketType = "Luggage" + if (luggageDescription.isNotEmpty()) " ($luggageDescription)" else ""
                        )
                        ticketViewModel.insertTicket(newTicket)

                        // ✅ Show success message
                        scaffoldState.snackbarHostState.showSnackbar("Luggage ticket issued successfully!")

                        // 🔄 Reset fields properly
                        destination = ""
                        luggageDescription = ""
                        price = ""
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
                    Text("Issue Luggage Ticket", color = Color.Black, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
        }
    }
}
