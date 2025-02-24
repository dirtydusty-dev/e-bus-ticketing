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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.ui.bluetooth.BluetoothPrinterHelper
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel
import kotlinx.coroutines.launch

@Composable
fun LuggageTicketingScreen(
    tripId: String,
    ticketViewModel: TicketViewModel = viewModel(),
    tripViewModel: TripViewModel = viewModel(),
    bluetoothHelper: BluetoothPrinterHelper
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(tripId) {
        ticketViewModel.setTripId(tripId)
        tripViewModel.loadTripById(tripId)
        ticketViewModel.fetchRouteStops(tripId)
    }

    val fromCity = remember { mutableStateOf("Detecting...") }
    val routeStops by ticketViewModel.routeStops.collectAsState()
    val tripDetails by tripViewModel.selectedTrip.collectAsState()
    val luggageCount by ticketViewModel.luggageCount.collectAsState()

    LaunchedEffect(tripId) {
        fromCity.value = ticketViewModel.getCityFromCoordinates(tripId)
    }

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

            OutlinedTextField(
                value = fromCity.value,
                onValueChange = {},
                label = { Text("From City") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            DropdownMenuComponent("Select Destination", routeStops.filter { it != fromCity.value }, destination) { newSelection ->
                destination = newSelection
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = luggageDescription,
                onValueChange = { luggageDescription = it },
                label = { Text("Estimate Weight / Description (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

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

            Button(
                onClick = {
                    isProcessing = true
                    coroutineScope.launch {
                        val ticketId = ticketViewModel.generateTicketId(tripId)
                        val newTicket = Ticket(
                            ticketId = ticketId,
                            tripId = tripId,
                            fromStop = fromCity.value,
                            toStop = destination,
                            luggage = luggageDescription,
                            price = price.toDoubleOrNull() ?: 0.0,
                            ticketType = "Luggage"
                        )
                        ticketViewModel.insertTicket(newTicket)
                        tripDetails?.let {
                            bluetoothHelper.printTicketWithLogo(context, newTicket, it)
                        }
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

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            scaffoldState.snackbarHostState.showSnackbar("Luggage Ticket Issued Successfully!")
            showSnackbar = false
        }
    }
}
