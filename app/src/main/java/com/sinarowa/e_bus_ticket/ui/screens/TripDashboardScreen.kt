package com.sinarowa.e_bus_ticket.ui.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sinarowa.e_bus_ticket.R
import com.sinarowa.e_bus_ticket.data.local.entities.TripDetails
import androidx.compose.runtime.*
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
import kotlinx.coroutines.launch

@Composable
fun TripDashboardScreen(
    trip: TripDetails,
    navController: NavController,
    ticketViewModel: TicketViewModel
) {
    val ticketCount by ticketViewModel.ticketCount.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(trip.tripId) {
        ticketViewModel.updateTicketCount(trip.tripId)  // ✅ Fetch ticket count
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFE3F2FD)), // Light Blue Background
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Trip: ${trip.routeName}", style = MaterialTheme.typography.h5, color = Color(0xFF1565C0))
        Text("Bus: ${trip.busName}", style = MaterialTheme.typography.body1, color = Color.DarkGray)
        Text("Tickets Sold: $ticketCount", style = MaterialTheme.typography.body1, color = Color(0xFF1565C0))

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("passengerTickets/${trip.tripId}") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFEB3B)), // Yellow
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Sell Tickets", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { navController.navigate("luggageTickets/${trip.tripId}") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFEB3B)), // Yellow
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Log Expenses", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { navController.navigate("tripReports/${trip.tripId}") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1565C0)), // Blue
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("View Reports", color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                scope.launch {
                    //ticketViewModel.endTrip(trip.tripId)  // ✅ End trip logic
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red), // Red for End Trip
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("End Trip", color = Color.White)
        }
    }
}


@Composable
fun DashboardButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}
