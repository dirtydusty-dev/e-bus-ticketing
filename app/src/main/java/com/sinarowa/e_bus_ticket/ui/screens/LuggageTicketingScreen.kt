/*
package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sinarowa.e_bus_ticket.ui.components.DropdownMenuComponent
import kotlinx.coroutines.launch

@Composable
fun LuggageTicketingScreen(
    tripId: Long, // ✅ Fixed: Trip ID should be Long
    ticketViewModel: TicketingViewModel,
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(tripId) {
        ticketViewModel.setTrip(tripId)
        ticketViewModel.fetchRouteStops(tripId)
    }

    val fromCity by ticketViewModel.currentLocation.collectAsState() // ✅ Now gets real-time location
    val routeStops by ticketViewModel.routeStops.collectAsState()
    val luggageCount by ticketViewModel.luggageCount.collectAsState()

    LaunchedEffect(luggageCount) {
        println("Luggage Count Updated: $luggageCount")
    }

    var destination by remember { mutableStateOf("") }
    var luggageDescription by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var priceError by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
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
            Text("Luggage Ticketing", style = MaterialTheme.typography.h5, color = Color(0xFF1565C0))
            Spacer(modifier = Modifier.height(8.dp))

            // ✅ From City (Auto-detected)
            OutlinedTextField(
                value = fromCity,
                onValueChange = {},
                label = { Text("From City") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Destination Dropdown
            DropdownMenuComponent("Select Destination", routeStops.filter { it != fromCity }, destination) { newSelection ->
                destination = newSelection
            }
            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Luggage Description Field
            OutlinedTextField(
                value = luggageDescription,
                onValueChange = { luggageDescription = it },
                label = { Text("Estimate Weight / Description (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Price Input
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

            val isButtonEnabled = destination.isNotEmpty() && !priceError && price.isNotEmpty() && !isProcessing

            // ✅ Sell & Print Button
            Button(
                onClick = {
                    isProcessing = true
                    coroutineScope.launch {
                        ticketViewModel.sellTicket(tripId,fromCity,destination,"Luggage",price.toDoubleOrNull() ?: 0.0)
                        destination = ""
                        luggageDescription = ""
                        price = ""
                        isProcessing = false
                        showSnackbar = true
                    }
                },
                enabled = isButtonEnabled,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = if (isButtonEnabled) Color(0xFFFFEB3B) else Color.Gray),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).padding(4.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Print, contentDescription = "Print", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Issue Luggage Ticket", color = Color.Black, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
        }
    }

    // ✅ Snackbar Confirmation
    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            scaffoldState.snackbarHostState.showSnackbar("Luggage Ticket Issued Successfully!")
            showSnackbar = false // ✅ Move inside LaunchedEffect
        }
    }
}
*/
