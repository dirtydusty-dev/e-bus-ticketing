/*
package com.sinarowa.e_bus_ticket.ui.screens

import android.content.Context
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
import com.sinarowa.e_bus_ticket.utils.LocationUtils
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun PassengerTicketingScreen(
    tripId: Long,
    ticketViewModel: TicketingViewModel,
) {
    val coroutineScope = rememberCoroutineScope()

    // **State Variables**
    var destination by remember { mutableStateOf("Select Destination") }
    var ticketType by remember { mutableStateOf("Select Ticket Type") }
    var shortAmount by remember { mutableStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }

    // **Live Data**
    val ticketCount by ticketViewModel.ticketCount.collectAsState()
    val luggageCount by ticketViewModel.luggageCount.collectAsState()
    val departedCount by ticketViewModel.departedCount.collectAsState()
    val currentCoordinates by ticketViewModel.currentCoordinates.collectAsState()
    val routeStations by ticketViewModel.routeStops.collectAsState()
    val busCapacity by ticketViewModel.busCapacity.collectAsState()
    val stationCoordinates by ticketViewModel.stationCoordinates.collectAsState()
    val price by ticketViewModel.ticketPrice.collectAsState()

    val location by ticketViewModel.locationState.collectAsState()

    // **Find Nearest Station**
    val fromCity by remember(currentCoordinates, routeStations) {
        mutableStateOf(
            if (currentCoordinates != null) {
                LocationUtils.findNearestStation(
                    currentCoordinates!!.latitude,
                    currentCoordinates!!.longitude,
                    routeStations,
                    stationCoordinates,
                    lastKnownStation = ticketViewModel.lastKnownStation
                )
            } else {
                "Unknown"
            }
        )
    }

    // **Get Valid Stops After `fromCity`**
    val validStops = remember(routeStations, fromCity) {
        LocationUtils.getValidStops(routeStations, fromCity)
    }

    // **Remaining Seats Calculation**
    val remainingSeats = derivedStateOf { max(0, busCapacity - ticketCount) }

    // **UI Layout**
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Passenger Ticketing", style = MaterialTheme.typography.h5, color = Color(0xFF1565C0))
        Spacer(modifier = Modifier.height(8.dp))

        // **From City Field**
        OutlinedTextField(
            value = fromCity,
            onValueChange = {},
            label = { Text("From City") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // **Destination Dropdown**
        DropdownMenuComponent("Select Destination", validStops, destination) { newSelection ->
            destination = newSelection
        }
        Spacer(modifier = Modifier.height(8.dp))

        // **Ticket Type Dropdown**
        DropdownMenuComponent("Select Ticket Type", listOf("Adult", "Child"), ticketType) { newType ->
            ticketType = newType
            shortAmount = 0
        }
        Spacer(modifier = Modifier.height(8.dp))

        // **Show Short Amount Interface Only for Adult Tickets**
        if (ticketType == "Adult") {
            PriceAdjustmentComponent(shortAmount, price.toInt()) { newAmount ->
                shortAmount = newAmount
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        TicketSummaryCard(ticketCount, luggageCount, departedCount, remainingSeats.value)

        // **Final Price**
        Spacer(modifier = Modifier.height(8.dp))
        Text("Price: $${price - shortAmount}", style = MaterialTheme.typography.h5, color = Color(0xFFFFEB3B))
        Spacer(modifier = Modifier.height(16.dp))

        val isButtonEnabled = destination != "Select Destination" &&
                ticketType != "Select Ticket Type" &&
                fromCity != destination &&
                (price - shortAmount) > 0.0 &&
                remainingSeats.value > 0

        // **Sell & Print Ticket Button**
        Button(
            onClick = {
                isProcessing = true
                coroutineScope.launch {
                    ticketViewModel.sellTicket(tripId, fromCity, destination, ticketType, price - shortAmount)
                    destination = "Select Destination"
                    ticketType = "Select Ticket Type"
                    shortAmount = 0
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
fun PriceAdjustmentComponent(
    shortAmount: Int,
    maxAmount: Int,
    onAmountChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { if (shortAmount > 0) onAmountChanged(shortAmount - 1) },
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
            onClick = { if (shortAmount < maxAmount - 1) onAmountChanged(shortAmount + 1) },
            modifier = Modifier.size(40.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
        ) {
            Text("+", color = Color.White)
        }
    }
}


@Composable
fun TicketSummaryCard(
    ticketCount: Int,
    luggageCount: Int,
    departedCount: Int,
    remainingSeats: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = 4.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Passenger Tickets Sold", style = MaterialTheme.typography.body2)
                Text("$ticketCount", style = MaterialTheme.typography.h6, color = Color(0xFF1565C0))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Luggage Tickets Sold", style = MaterialTheme.typography.body2)
                Text("$luggageCount", style = MaterialTheme.typography.h6, color = Color(0xFF1565C0))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Departed Customers", style = MaterialTheme.typography.body2)
                Text("$departedCount", style = MaterialTheme.typography.h6, color = Color(0xFFE65100))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Open Seats Remaining", style = MaterialTheme.typography.body2)
                Text("$remainingSeats", style = MaterialTheme.typography.h6, color = Color(0xFF43A047))
            }
        }
    }
}

*/
