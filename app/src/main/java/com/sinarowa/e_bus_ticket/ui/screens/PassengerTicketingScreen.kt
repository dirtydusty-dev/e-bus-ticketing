package com.sinarowa.e_bus_ticket.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.entities.TripDetails
import com.sinarowa.e_bus_ticket.ui.bluetooth.BluetoothPrinterHelper
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun PassengerTicketingScreen(
    tripId: String,
    ticketViewModel: TicketViewModel = viewModel(),
    tripViewModel: TripViewModel = viewModel(),
    bluetoothHelper: BluetoothPrinterHelper
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val ticketCount by ticketViewModel.ticketCount.collectAsState()
    val remainingSeats = remember { mutableStateOf(50 - ticketCount) }

    var destination by remember { mutableStateOf("") }
    var ticketType by remember { mutableStateOf("") }

    // ✅ Track state to reset UI
    var formStateKey by remember { mutableStateOf(0) }


    val tripDetails by tripViewModel.selectedTrip.collectAsState()



    LaunchedEffect(tripId) {
        tripViewModel.loadTripById(tripId)
    }


    key(formStateKey) {
        val fromCity = remember { mutableStateOf("Detecting...") }
        val routeStops by ticketViewModel.routeStops.collectAsState()
        LaunchedEffect(tripId) { ticketViewModel.fetchRouteStops(tripId) }
        val allStops = remember(routeStops) { routeStops }

        // ✅ Ensure no default values on refresh
        var destination by remember { mutableStateOf("") }
        var ticketType by remember { mutableStateOf("") }
        var price by remember { mutableStateOf(0.0) }

        // ✅ Fetch GPS-based city
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                fromCity.value = ticketViewModel.getCityFromCoordinates(tripId)
            }
        }

        // ✅ Filter stops: Exclude `fromCity`
        val validStops = remember(allStops, fromCity.value) {
            allStops.filter { it != fromCity.value }
        }

        // ✅ Fetch Price
        LaunchedEffect(fromCity.value, destination, ticketType) {
            if (fromCity.value.isNotEmpty() && destination.isNotEmpty() && ticketType.isNotEmpty()) {
                coroutineScope.launch {
                    price = ticketViewModel.getPrice(fromCity.value, destination, ticketType)
                }
            } else {
                price = 0.0
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Passenger Ticketing", style = MaterialTheme.typography.h5, color = Color(0xFF1565C0))
            Spacer(modifier = Modifier.height(8.dp))

            // 🏙️ FROM City (GPS-based)
            OutlinedTextField(value = fromCity.value, onValueChange = {}, label = { Text("From City") }, readOnly = true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            // 📌 Destination Selection
            DropdownMenuComponent("Select Destination", validStops, destination) { newSelection ->
                destination = newSelection // ✅ Corrected Assignment
            }

            Spacer(modifier = Modifier.height(8.dp))

            DropdownMenuComponent("Select Ticket Type", listOf("Adult", "Child", "$1 Short", "$2 Short"), ticketType) { newType ->
                ticketType = newType // ✅ Corrected Assignment
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 💺 Seat Number & Remaining Seats
            Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp, backgroundColor = Color.White) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Seat Number: ${ticketCount + 1}", style = MaterialTheme.typography.h6, color = Color(0xFF1565C0))
                    Text("Remaining Seats: ${remainingSeats.value}", style = MaterialTheme.typography.body2, color = Color.DarkGray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 💰 Display Price
            Text("Price: $$price", style = MaterialTheme.typography.h6, color = Color(0xFFFFEB3B))
            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Sell Ticket Button (DISABLED if conditions are not met)
            val isButtonEnabled = destination.isNotEmpty() &&
                    ticketType.isNotEmpty() &&
                    fromCity.value != destination &&
                    price > 0.0

            val ticketId = tripDetails?.let { tripViewModel.generateTicketId(it.routeName) } ?: tripViewModel.generateTicketIdFallback()

            Button(
                onClick = {
                    coroutineScope.launch {
                        val newTicket = Ticket(
                            ticketId = ticketId,
                            tripId = tripId,
                            fromStop = fromCity.value,
                            toStop = destination,
                            price = price,
                            ticketType = ticketType
                        )
                        ticketViewModel.insertTicket(newTicket)
                        remainingSeats.value -= 1

                        // 🔄 Reset page
                        formStateKey++

                        // 🖨️ Print ticket
                        tripDetails?.let {
                            bluetoothHelper.printTicketWithLogo(context, newTicket, it)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = if (isButtonEnabled) Color(0xFFFFEB3B) else Color.Gray),
                shape = RoundedCornerShape(8.dp),
                enabled = isButtonEnabled
            ) {
                Text("Sell Ticket", color = Color.Black, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DropdownMenuComponent(
    label: String,
    items: List<String>,
    selectedItem: String?,
    onSelectionChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = remember { mutableStateOf(selectedItem ?: "") }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected.value,
            onValueChange = {}, // Read-only field
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        selected.value = item
                        onSelectionChanged(item)
                        expanded = false
                    }
                ) {
                    Text(item)
                }
            }
        }
    }
}

