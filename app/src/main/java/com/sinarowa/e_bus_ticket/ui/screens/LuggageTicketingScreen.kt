package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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

    val fromCity = remember { mutableStateOf("Detecting...") }
    val routeStops by ticketViewModel.routeStops.collectAsState()
    LaunchedEffect(tripId) { ticketViewModel.fetchRouteStops(tripId) }

    val allStops = remember(routeStops) { routeStops }
    var destination by remember { mutableStateOf("") }
    var luggageDescription by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var priceError by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) } // ✅ Track button loading
    var showSnackbar by remember { mutableStateOf(false) }


    val tripDetails by tripViewModel.selectedTrip.collectAsState()



    LaunchedEffect(tripId) {
        tripViewModel.loadTripById(tripId)
    }

    // ✅ Detect user's city
    LaunchedEffect(tripId) {
        if (fromCity.value == "Detecting...") { // ✅ Prevent multiple calls
            coroutineScope.launch {
                fromCity.value = ticketViewModel.getCityFromCoordinates(tripId)
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp), // ✅ Moves Snackbar down slightly from the absolute top
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
            DropdownMenuComponent("Select Destination", allStops.filter { it != fromCity.value }, destination) { newSelection ->
                destination = newSelection
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 🏋🏽 Luggage Weight/Description (Optional)
            /*OutlinedTextField(
                value = luggageDescription,
                onValueChange = { luggageDescription = it },
                label = { Text("Estimate Weight / Description (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))*/

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
                    isProcessing = true // ✅ Show loading indicator
                    coroutineScope.launch {
                        val ticketId = ticketViewModel.generateTicketId(tripId)  // ✅ Ensure this runs before creating ticket

                        val newTicket = Ticket(
                            ticketId = ticketId, // ✅ Ensure it's formatted correctly
                            tripId = tripId,
                            fromStop = fromCity.value,
                            toStop = destination,
                            luggage = luggageDescription,
                            price = price.toDoubleOrNull() ?: 0.0, // ✅ Ensure it's a valid Double
                            ticketType = "Luggage" + if (luggageDescription.isNotEmpty()) " ($luggageDescription)" else ""
                        )
                        ticketViewModel.insertTicket(newTicket)
                        ticketViewModel.refreshTicketCount(tripId, ticketId)

                        // 🔄 Reset fields properly
                        destination = ""
                        luggageDescription = ""
                        price = ""
                        isProcessing = false // ✅ Re-enable button after success
                        showSnackbar = true

                        tripDetails?.let {
                            bluetoothHelper.printTicketWithLogo(context, newTicket, it)
                        }
                    }
                },
                enabled = isButtonEnabled, // ✅ Disable while processing
                modifier = Modifier.fillMaxWidth().height(50.dp), // ✅ Set button height
                colors = ButtonDefaults.buttonColors(backgroundColor = if (isButtonEnabled) Color(0xFFFFEB3B) else Color.Gray),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp) // ✅ Ensure proper size
                            .padding(4.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Issue Luggage Ticket", color = Color.Black, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }

        }
    }

    // ✅ Show Snackbar properly
    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar("Luggage Ticket Issued Successfully!") // ✅ Display success message
            }
            showSnackbar = false // ✅ Reset snackbar trigger
        }
    }
}
