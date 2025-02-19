package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusAlert
import androidx.compose.material.icons.filled.EventSeat
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
            onClick = { navController.navigate("passengerTickets/${trip.tripId}") }
        )

        DashboardButton(
            text = "Luggage Tickets",
            color = Color(0xFFFFEB3B), // Yellow
            onClick = { navController.navigate("luggageTickets/${trip.tripId}") }
        )

        DashboardButton(
            text = "Log Expenses",
            color = Color(0xFFFFEB3B), // Yellow
            onClick = { navController.navigate("expenses/${trip.tripId}") }
        )

        DashboardButton(
            text = "View Reports",
            color = Color(0xFF1565C0), // Blue
            onClick = { navController.navigate("reports/${trip.tripId}") }
        )

        /*DashboardButton(
            text = "Cancel Ticket",
            color = Color(0xFFFFEB3B), // Yellow
            onClick = { navController.navigate("cancelTicket/${trip.tripId}") }
        )*/

        DashboardButton(
            text = "End Trip",
            color = Color.Red, // Red for danger action
            onClick = { showEndTripDialog = true }
        )
    }
    if (showEndTripDialog) {
        AlertDialog(
            onDismissRequest = { showEndTripDialog = false },
            title = { Text("Confirm End Trip", color = Color(0xFF1565C0), fontSize = 20.sp) },
            text = { Text("Are you sure you want to end this trip? Once ended, you will not be able to add tickets, log expenses, or make further changes.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            tripViewModel.endTrip(trip.tripId)
                            showEndTripDialog = false
                            navController.popBackStack()
                        }
                        showEndTripDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
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
fun DashboardButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, color = Color.Black, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    }
}
