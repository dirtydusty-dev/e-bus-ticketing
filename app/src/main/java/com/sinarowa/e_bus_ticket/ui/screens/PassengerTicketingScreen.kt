package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController


@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun PassengerTicketingScreen(
    ticketViewModel: TicketViewModel = viewModel(),
    navController: NavController,
) {
    val fromStation by ticketViewModel.fromStation.asFlow().collectAsStateWithLifecycle("")
    val validDestinations by ticketViewModel.validDestinations.asFlow().collectAsStateWithLifecycle(emptyList())
    var ticketPrice by remember { mutableStateOf(0.0) }
    var shortAmount by remember { mutableStateOf(0) }
    val remainingSeats by ticketViewModel.remainingSeats.asFlow().collectAsStateWithLifecycle(0)
    val activePassengers by ticketViewModel.activePassengers.asFlow().collectAsStateWithLifecycle(0)
    var destination by remember { mutableStateOf("") }
    var ticketType by remember { mutableStateOf("") }

    val context = LocalContext.current
    var isProcessing by remember { mutableStateOf(false) }

    // ✅ Reset fields when screen loads
    LaunchedEffect(Unit) {
        ticketViewModel.updateFromStation(context)
        ticketViewModel.resetFields()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Passenger Ticketing", style = MaterialTheme.typography.h5, color = Color.Blue)
        Spacer(modifier = Modifier.height(8.dp))

        // ✅ From Station (Always Visible)
        OutlinedTextField(
            value = fromStation,
            onValueChange = {},
            label = { Text("From City") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // ✅ Select Destination (Appears After From is Set)
        if (fromStation.isNotEmpty()) {
            DropdownSelector(
                label = "Select Destination",
                items = validDestinations,
                selectedItem = destination,
                onSelectionChanged = { selectedDestination ->
                    destination = selectedDestination
                    ticketViewModel.setDestination(selectedDestination)

                    // ✅ Update ticket price when destination changes
                    ticketViewModel.updateTicketPrice()
                    ticketPrice = ticketViewModel.getTicketPriceValue()

                    // ✅ Reset Short Amount when destination changes
                    shortAmount = 0
                },
                displayText = { it }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ✅ Select Ticket Type (Appears After Destination is Selected)
        if (destination.isNotEmpty()) {
            DropdownSelector(
                label = "Select Ticket Type",
                items = listOf("Adult", "Child"),
                selectedItem = ticketType,
                onSelectionChanged = { newType ->
                    ticketType = newType
                    ticketViewModel.setTicketType(newType)

                    if (newType == "Child") {
                        ticketPrice /= 2 // ✅ Halve the ticket price for child
                        shortAmount = 0  // ✅ Reset Short Amount when child is selected
                    } else {
                        ticketPrice = ticketViewModel.getTicketPriceValue()
                    }
                },
                displayText = { it }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ✅ Display Ticket Price (Appears After Ticket Type is Selected)
        if (ticketType.isNotEmpty()) {
            Text(
                text = "💰 Ticket Price: $ticketPrice USD",
                style = MaterialTheme.typography.h6,
                color = Color.Green
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ✅ Short Amount Selector (Only for Adults, Stays Visible Until Reset)
        if (ticketType == "Adult" && ticketPrice > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Short Amount", style = MaterialTheme.typography.body1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (shortAmount > 0) {
                                shortAmount -= 1
                                ticketPrice = ticketViewModel.getTicketPriceValue() - shortAmount
                            }
                        }
                    ) {
                        Text("➖", style = MaterialTheme.typography.h6)
                    }
                    Text(
                        text = "$shortAmount USD",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(
                        onClick = {
                            if (shortAmount + 1 < ticketPrice) {
                                shortAmount += 1
                                ticketPrice = ticketViewModel.getTicketPriceValue() - shortAmount
                            }
                        }
                    ) {
                        Text("➕", style = MaterialTheme.typography.h6)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ✅ Display Final Ticket Price
        if (ticketPrice > 0) {
            OutlinedTextField(
                value = "${ticketPrice - shortAmount} USD",
                onValueChange = {},
                label = { Text("Final Ticket Price") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ✅ Seat & Passenger Info
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Seats Remaining", style = MaterialTheme.typography.body2)
                    Text("$remainingSeats", style = MaterialTheme.typography.h6, color = Color.Green)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Active Passengers", style = MaterialTheme.typography.body2)
                    Text("$activePassengers", style = MaterialTheme.typography.h6, color = Color.Blue)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ✅ Sell & Print Button
        Button(
            onClick = {
                isProcessing = true
                ticketViewModel.sellTicket()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isProcessing && fromStation.isNotEmpty() && destination.isNotEmpty() && ticketType.isNotEmpty() && ticketPrice > 0
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Sell & Print")
            }
        }
    }
}
