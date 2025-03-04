package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.entities.Trip
import com.sinarowa.e_bus_ticket.data.local.enums.TicketStatus
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TripDashboardScreen(
    navController: NavController,
    tripViewModel: TripViewModel
) {
    // Observe the active trip
    val tripWithRoute by tripViewModel.activeTrip.observeAsState()

    // Check if tripWithRoute is null and display loading or error state
    if (tripWithRoute == null) {
        // Show a loading spinner or some placeholder if the trip data is null
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        return
    }

    // Now you can safely access tripWithRoute as TripWithRoute
    val ticketCount = tripWithRoute!!.tickets.filter { it.ticket.status == TicketStatus.VALID }.size
    val luggageCount = tripWithRoute!!.tickets.filter { it.ticket.paymentCategory == "Luggage" }.size
    val availableSeats = tripWithRoute!!.bus.capacity - ticketCount
    val activePassengers = ticketCount

    // State to handle the end trip dialog
    var showEndTripDialog by remember { mutableStateOf(false) }
    var confirmationText by remember { mutableStateOf("") }
    var endTripCountdown by remember { mutableStateOf(3) }
    var isCountingDown by remember { mutableStateOf(false) }

    // Launch countdown when dialog is shown
    val scope = rememberCoroutineScope()

    LaunchedEffect(showEndTripDialog) {
        if (showEndTripDialog) {
            scope.launch {
                isCountingDown = true
                for (i in 3 downTo 1) {
                    endTripCountdown = i
                    delay(1000L)
                }
                endTripCountdown = 0
                isCountingDown = false
            }
        }
    }

    val isConfirmEnabled = confirmationText.uppercase() == "END" && endTripCountdown == 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Trip Info Card
        TripInfoCard(
            trip = tripWithRoute!!,
            ticketCount = ticketCount,
            luggageCount = luggageCount,
            availableSeats = availableSeats,
            activePassengers = activePassengers
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Dashboard Buttons
        DashboardButton("Passenger Tickets", Color(0xFFFFEB3B), Icons.Filled.Person) {
            navController.navigate("passengerTickets/${tripWithRoute!!.trip.tripId}")
        }
        DashboardButton("Luggage Tickets", Color(0xFFFFEB3B), Icons.Filled.Luggage) {
            navController.navigate("luggageTickets/${tripWithRoute!!.trip.tripId}")
        }
        DashboardButton("Log Expenses", Color(0xFFFFEB3B), Icons.Filled.AttachMoney) {
            navController.navigate("expenses/${tripWithRoute!!.trip.tripId}")
        }
        DashboardButton("View Reports", Color(0xFF1565C0), Icons.Filled.BarChart) {
            navController.navigate("reports/${tripWithRoute!!.trip.tripId}")
        }
        DashboardButton("End Trip", Color.Red, Icons.Filled.Warning) {
            showEndTripDialog = true
        }
    }

    // End Trip Confirmation Dialog
    if (showEndTripDialog) {
        EndTripDialog(
            showEndTripDialog = showEndTripDialog,
            confirmationText = confirmationText,
            onConfirmationTextChange = { confirmationText = it },
            endTripCountdown = endTripCountdown,
            isCountingDown = isCountingDown,
            isConfirmEnabled = isConfirmEnabled,
            onConfirm = {
                scope.launch {
                    tripViewModel.endTrip(tripWithRoute!!.trip.tripId)
                    navController.popBackStack() // Return after ending the trip
                }
            },
            onDismiss = { showEndTripDialog = false }
        )
    }
}



// Trip Info Card Composable
@Composable
fun TripInfoCard(
    trip: TripWithRoute,
    ticketCount: Int,
    luggageCount: Int,
    availableSeats: Int,
    activePassengers: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFF87CEEB)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.BusAlert, contentDescription = "Trip Icon", tint = Color.White, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Trip: ${trip.route.routeName}", style = MaterialTheme.typography.h6, color = Color.White)
            Text("Bus: ${trip.bus.busName}", style = MaterialTheme.typography.body1, color = Color.White)
            Text("Passenger Tickets Sold: $ticketCount", style = MaterialTheme.typography.body1, color = Color.White)
            Text("Luggage Tickets Sold: $luggageCount", style = MaterialTheme.typography.body1, color = Color.White)
            Text("Available Seats: $availableSeats", style = MaterialTheme.typography.body1, color = Color.White)
            Text("Active Passengers Onboard: $activePassengers", style = MaterialTheme.typography.body1, color = Color.White)
        }
    }
}

// Reusable Dashboard Button Composable
@Composable
fun DashboardButton(text: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = text, tint = Color.Black, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = Color.Black, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
    }
}

// End Trip Confirmation Dialog Composable
@Composable
fun EndTripDialog(
    showEndTripDialog: Boolean,
    confirmationText: String,
    onConfirmationTextChange: (String) -> Unit,
    endTripCountdown: Int,
    isCountingDown: Boolean,
    isConfirmEnabled: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Confirm End Trip", color = Color(0xFF1565C0), fontSize = 20.sp) },
        text = {
            Column {
                Text("⚠️ Are you sure you want to end this trip?")
                Text("🚨 Once ended, no more tickets or expenses can be logged.")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmationText,
                    onValueChange = onConfirmationTextChange,
                    label = { Text("Type 'END' to confirm") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (isCountingDown) {
                    Text("⌛ Confirming in $endTripCountdown seconds...", color = Color.Red)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm() },
                colors = ButtonDefaults.buttonColors(backgroundColor = if (isConfirmEnabled) Color.Red else Color.Gray),
                enabled = isConfirmEnabled
            ) {
                Text("Yes, End Trip", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

