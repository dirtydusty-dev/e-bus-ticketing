package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BusAlert
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sinarowa.e_bus_ticket.data.local.entities.TripDetails
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TripDashboardScreen(
    trip: TripDetails,
    navController: NavController,
    ticketViewModel: TicketViewModel,
    tripViewModel: TripViewModel
) {
    val ticketCount by ticketViewModel.ticketCount.collectAsState()
    var showEndTripDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var endTripCountdown by remember { mutableStateOf(3) }
    var confirmationText by remember { mutableStateOf("") }
    var isCountingDown by remember { mutableStateOf(false) } // ✅ Track countdown state

    LaunchedEffect(showEndTripDialog) {
        if (showEndTripDialog) {
            isCountingDown = true
            endTripCountdown = 3
            for (i in 3 downTo 1) {
                delay(1000L)
                endTripCountdown = i - 1
            }
            isCountingDown = false // ✅ Stop countdown
        }
    }

    val isConfirmEnabled = confirmationText.uppercase() == "END" && endTripCountdown == 0

    OutlinedTextField(
        value = confirmationText,
        onValueChange = { confirmationText = it },
        label = { Text("Type 'END' to confirm") },
        modifier = Modifier.fillMaxWidth()
    )





    LaunchedEffect(trip.tripId) {
        ticketViewModel.updateTicketCount(trip.tripId)  // ✅ Fetch ticket count dynamically
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Modern White Background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp),
            backgroundColor = Color(0xFF87CEEB) // Sky Blue
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.BusAlert, contentDescription = "Trip Icon", tint = Color.White, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Trip: ${trip.routeName}", style = MaterialTheme.typography.h6, color = Color.White)
                Text("Bus: ${trip.busName}", style = MaterialTheme.typography.body1, color = Color.White)
                Text("Tickets Sold: $ticketCount", style = MaterialTheme.typography.body1, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        DashboardButton(
            text = "Passenger Tickets",
            color = Color(0xFFFFEB3B), // Yellow
            icon = Icons.Filled.Person,
            onClick = { navController.navigate("passengerTickets/${trip.tripId}") }
        )

        // 🎒 Luggage Tickets Button
        DashboardButton(
            text = "Luggage Tickets",
            color = Color(0xFFFFEB3B), // Yellow
            icon = Icons.Filled.Luggage,
            onClick = { navController.navigate("luggageTickets/${trip.tripId}") }
        )

        // 📝 Log Expenses Button
        DashboardButton(
            text = "Log Expenses",
            color = Color(0xFFFFEB3B), // Yellow
            icon = Icons.Filled.AttachMoney,
            onClick = { navController.navigate("expenses/${trip.tripId}") }
        )

        // 📊 View Reports Button
        DashboardButton(
            text = "View Reports",
            color = Color(0xFF1565C0), // Blue
            icon = Icons.Filled.BarChart,
            onClick = { navController.navigate("reports/${trip.tripId}") }
        )

        // ❌ End Trip Button
        DashboardButton(
            text = "End Trip",
            color = Color.Red, // Red for danger action
            icon = Icons.Filled.Warning,
            onClick = { showEndTripDialog = true }
        )
    }
    if (showEndTripDialog) {
        AlertDialog(
            onDismissRequest = { showEndTripDialog = false },
            title = { Text("Confirm End Trip", color = Color(0xFF1565C0), fontSize = 20.sp) },
            text = {
                Column {
                    Text("⚠️ Are you sure you want to end this trip?")
                    Text("🚨 Once ended, no more tickets or expenses can be logged.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmationText,
                        onValueChange = { confirmationText = it },
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
                    onClick = {
                        if (isConfirmEnabled) {
                            scope.launch {
                                tripViewModel.endTrip(trip.tripId)
                                showEndTripDialog = false
                                navController.popBackStack()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = if (isConfirmEnabled) Color.Red else Color.Gray),
                    enabled = isConfirmEnabled // ✅ This now updates properly
                ) {
                    Text("Yes, End Trip", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showEndTripDialog = false },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )

    }
}

@Composable
fun DashboardButton(
    text: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector, // ✅ Added icon parameter
    onClick: () -> Unit
) {
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

