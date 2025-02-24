package com.sinarowa.e_bus_ticket.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Print
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.ui.bluetooth.BluetoothPrinterHelper
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun PassengerTicketingScreen(
    tripId: String,
    ticketViewModel: TicketViewModel = viewModel(),
    tripViewModel: TripViewModel = viewModel(),
    bluetoothHelper: BluetoothPrinterHelper
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Initialize ViewModels
    LaunchedEffect(tripId) {
        ticketViewModel.setTripId(tripId)
        tripViewModel.loadTripById(tripId)
        ticketViewModel.fetchRouteStops(tripId)
    }

    // Form state
    var destination by remember { mutableStateOf("Select Destination") }
    var ticketType by remember { mutableStateOf("Select Ticket Type") }
    var shortAmount by remember { mutableStateOf(0) }
    var price by remember { mutableStateOf(0.0) }
    var isProcessing by remember { mutableStateOf(false) }

    // Trip and ticket data
    val tripDetails by tripViewModel.selectedTrip.collectAsState()
    val busSeats = remember { mutableStateOf<Int?>(null) }
    val ticketCount by ticketViewModel.ticketCountSeat.collectAsState()
    val luggageCount by ticketViewModel.luggageCount.collectAsState()
    val departedCount by ticketViewModel.departedCount.collectAsState()
    val routeStops by ticketViewModel.routeStops.collectAsState()

    // Remaining seats calculation with non-negative enforcement
    val remainingSeats = remember(ticketCount, busSeats.value) {
        derivedStateOf {
            val totalSeats = busSeats.value ?: 0
            max(0, totalSeats - ticketCount) // Prevent negative seats
        }
    }

    // Fetch bus seats with logging
    LaunchedEffect(tripDetails) {
        tripDetails?.busName?.let { busName ->
            val seats = tripViewModel.getBusSeats(busName) ?: 50 // Default to 50 if null
            busSeats.value = seats
            println("Bus Seats Fetched for $busName: $seats")
        }
    }

    LaunchedEffect(tripId) {
        ticketViewModel.refreshDepartedCount(tripId)
    }

    // Location and pricing
    val fromCity = remember { mutableStateOf("Detecting...") }
    LaunchedEffect(tripId) {
        fromCity.value = ticketViewModel.getCityFromCoordinates(tripId)
    }

    val validStops = remember(routeStops, fromCity.value) {
        routeStops.filter { it != fromCity.value }
    }

    LaunchedEffect(fromCity.value, destination, ticketType) {
        if (fromCity.value.isNotEmpty() && destination != "Select Destination" && ticketType != "Select Ticket Type") {
            price = ticketViewModel.getPrice(fromCity.value, destination, ticketType)
        } else {
            price = 0.0
        }
    }

    // Debug logging
    LaunchedEffect(ticketCount, busSeats.value) {
        println("Ticket Count Updated: $ticketCount, Bus Seats: ${busSeats.value}, Remaining Seats: ${remainingSeats.value}")
    }

    // UI Layout
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Passenger Ticketing", style = MaterialTheme.typography.h5, color = Color(0xFF1565C0))
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = fromCity.value,
            onValueChange = {},
            label = { Text("From City") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        DropdownMenuComponent("Select Destination", validStops, destination) { newSelection ->
            destination = newSelection
        }
        Spacer(modifier = Modifier.height(8.dp))

        DropdownMenuComponent("Select Ticket Type", listOf("Adult", "Child"), ticketType) { newType ->
            ticketType = newType
            shortAmount = 0
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (ticketType == "Adult") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { if (shortAmount > 0) shortAmount -= 1 },
                    enabled = shortAmount > 0,
                    modifier = Modifier.size(40.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text("-", color = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Short: $$shortAmount", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { if (shortAmount < price.toInt() - 1) shortAmount += 1 },
                    modifier = Modifier.size(40.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                ) {
                    Text("+", color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            elevation = 4.dp,
            backgroundColor = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Passenger Tickets Sold", style = MaterialTheme.typography.body2)
                    Text("$ticketCount", style = MaterialTheme.typography.h6, color = Color(0xFF1565C0))
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Luggage Tickets Sold", style = MaterialTheme.typography.body2)
                    Text("$luggageCount", style = MaterialTheme.typography.h6, color = Color(0xFF1565C0))
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Departed Customers", style = MaterialTheme.typography.body2)
                    Text("$departedCount", style = MaterialTheme.typography.h6, color = Color(0xFFE65100))
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Open Seats Remaining", style = MaterialTheme.typography.body2)
                    Text("${remainingSeats.value}", style = MaterialTheme.typography.h6, color = Color(0xFF43A047))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Price: $${price - shortAmount}", style = MaterialTheme.typography.h5, color = Color(0xFFFFEB3B))
        Spacer(modifier = Modifier.height(16.dp))

        val isButtonEnabled = destination != "Select Destination" &&
                ticketType != "Select Ticket Type" &&
                fromCity.value != destination &&
                (price - shortAmount) > 0.0 &&
                remainingSeats.value > 0

        Button(
            onClick = {
                isProcessing = true
                coroutineScope.launch {
                    val ticketId = ticketViewModel.generateTicketId(tripId)
                    val formattedTicketType = when {
                        ticketType == "Child" -> "Child"
                        shortAmount > 0 -> "$$shortAmount Short"
                        else -> "Adult"
                    }
                    val newTicket = Ticket(
                        tripId = tripId,
                        ticketId = ticketId,
                        fromStop = fromCity.value,
                        toStop = destination,
                        price = price - shortAmount,
                        ticketType = formattedTicketType
                    )
                    ticketViewModel.insertTicket(newTicket)
                    tripDetails?.let {
                        bluetoothHelper.printTicketWithLogo(context, newTicket, it)
                    }
                    destination = "Select Destination"
                    ticketType = "Select Ticket Type"
                    shortAmount = 0
                    price = 0.0
                    isProcessing = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = if (isButtonEnabled) Color(0xFFFFEB3B) else Color.Gray),
            shape = RoundedCornerShape(8.dp),
            enabled = isButtonEnabled && !isProcessing
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
            } else {
                Icon(Icons.Default.Print, contentDescription = "Print")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sell & Print", color = Color.Black, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DropdownMenuComponent(
    label: String,
    items: List<String>,
    selectedItem: String,
    onSelectionChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(onClick = {
                    onSelectionChanged(item)
                    expanded = false
                }) {
                    Text(item)
                }
            }
        }
    }
}