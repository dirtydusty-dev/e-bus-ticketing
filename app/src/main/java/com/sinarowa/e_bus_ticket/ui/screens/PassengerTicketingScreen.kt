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
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
import kotlinx.coroutines.launch

@Composable
fun PassengerTicketingScreen(
    tripId: String,
    ticketViewModel: TicketViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val ticketCount by ticketViewModel.ticketCount.collectAsState()
    val remainingSeats = remember { mutableStateOf(50 - ticketCount) } // Assuming max seats = 50

    // ✅ Fetch GPS Location & Convert to City
    val fromCity = remember { mutableStateOf("Detecting...") }
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val location = getLastKnownLocation(context)
            fromCity.value = ticketViewModel.getCityFromCoordinates(location.latitude, location.longitude, tripId)
        }
    }

    val location = getLastKnownLocation(context)
    Log.d("GPS", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")



    // ✅ Fetch Route Stops
    val routeStops by ticketViewModel.routeStops.collectAsState()
    LaunchedEffect(tripId) {
        ticketViewModel.fetchRouteStops(tripId)
    }
    val allStops = remember(routeStops) { routeStops }

    // ✅ Ensure `fromCity` Exists in Stops
    val validStops = remember(allStops, fromCity.value) {
        val fromIndex = allStops.indexOf(fromCity.value)
        if (fromIndex != -1) allStops.subList(fromIndex + 1, allStops.size) else allStops
    }

    var destination by remember { mutableStateOf(validStops.firstOrNull() ?: "") }
    var ticketType by remember { mutableStateOf("Adult") }
    var price by remember { mutableStateOf(0.0) }

    // ✅ Fetch Price Dynamically
    LaunchedEffect(fromCity.value, destination, ticketType) {
        coroutineScope.launch {
            price = ticketViewModel.getPrice(fromCity.value, destination, ticketType)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Passenger Ticketing", style = MaterialTheme.typography.h5, color = Color(0xFF1565C0))

        Spacer(modifier = Modifier.height(8.dp))

        // 🏙️ GPS-based FROM City
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

        // 🎟️ Ticket Type Selection
        DropdownMenuComponent("Select Ticket Type", listOf("Adult", "Child", "Luggage"), ticketType) { newType ->
            ticketType = newType
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 💺 Seat Number & Remaining Seats
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp,
            backgroundColor = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Seat Number: ${ticketCount + 1}", style = MaterialTheme.typography.h6, color = Color(0xFF1565C0))
                Text("Remaining Seats: ${remainingSeats.value}", style = MaterialTheme.typography.body2, color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 💰 Display Price
        Text("Price: $$price", style = MaterialTheme.typography.h6, color = Color(0xFFFFEB3B))

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Sell Ticket Button
        Button(
            onClick = {
                coroutineScope.launch {
                    val newTicket = Ticket(
                        ticketId = System.currentTimeMillis().toString(),
                        tripId = tripId,
                        fromStop = fromCity.value,
                        toStop = destination,
                        price = price,
                        timestamp = System.currentTimeMillis()
                    )
                    ticketViewModel.insertTicket(newTicket)
                    remainingSeats.value -= 1
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFEB3B)), // Yellow Button
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Sell Ticket", color = Color.Black, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
    }
}

/**
 * ✅ Dropdown Menu for Selecting Items
 */
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

/**
 * ✅ Get GPS-Based Current City from Database
 */
@SuppressLint("MissingPermission")
fun getLastKnownLocation(context: Context): Location {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val gpsLocation: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    val networkLocation: Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

    return when {
        gpsLocation != null -> gpsLocation
        networkLocation != null -> networkLocation
        else -> Location("").apply {
            latitude = 0.0
            longitude = 0.0
        }
    }
}

