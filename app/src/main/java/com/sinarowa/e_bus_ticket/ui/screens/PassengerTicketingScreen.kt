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
import androidx.compose.material.icons.filled.Print
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
    var isProcessing by remember { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState() // ✅ Define scaffoldState here
    var showSnackbar by remember { mutableStateOf(false) }


    val tripDetails by tripViewModel.selectedTrip.collectAsState()


    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar("Ticket Sold Successfully!")
            }
            showSnackbar = false // Reset Snackbar
        }
    }


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
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                elevation = 4.dp,
                backgroundColor = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Seat Number: ${ticketCount + 1}", style = MaterialTheme.typography.h6, color = Color(0xFF1565C0))
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Remaining Seats", style = MaterialTheme.typography.body2, color = Color.DarkGray)
                        Text("${remainingSeats.value}", style = MaterialTheme.typography.h6, color = Color.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 💰 Display Price
            Slider(
                value = price.toFloat(),
                onValueChange = {},
                valueRange = 0f..20f,
                enabled = false,// Set a reasonable ticket range
                steps = 5
            )
            Text("Price: $$price", style = MaterialTheme.typography.h6, color = Color(0xFFFFEB3B))

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Sell Ticket Button (DISABLED if conditions are not met)
            val isButtonEnabled = destination.isNotEmpty() &&
                    ticketType.isNotEmpty() &&
                    fromCity.value != destination &&
                    price > 0.0

            Button(
                onClick = {
                    isProcessing = true
                    coroutineScope.launch {  // ✅ Runs in coroutine
                        val ticketId = ticketViewModel.generateTicketId(tripId)  // ✅ Call suspend function inside coroutine

                        val newTicket = Ticket(
                            ticketId = ticketId, // ✅ Ensure ticketId is properly formatted
                            tripId = tripId,
                            fromStop = fromCity.value,
                            toStop = destination,
                            price = price,
                            ticketType = ticketType
                        )
                        ticketViewModel.insertTicket(newTicket)
                        ticketViewModel.refreshTicketCount(tripId, ticketId)  // ✅ Update ticket count

                        remainingSeats.value -= 1
                        formStateKey++

                        tripDetails?.let {
                            bluetoothHelper.printTicketWithLogo(context, newTicket, it)
                        }
                        isProcessing = false
                        showSnackbar = true
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
                    Icon(Icons.Default.Print, contentDescription = "Print")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sell & Print", color = Color.Black, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
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

