package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sinarowa.e_bus_ticket.data.local.enums.TicketStatus
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel

@Composable
fun TripDashboardScreen(
    navController: NavController,
    tripViewModel: TripViewModel
) {
    val state by tripViewModel.state.collectAsState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    state.activeTrip?.let { tripWithRoute ->
        val ticketCount = tripWithRoute.tickets.count { it.status == TicketStatus.VALID }
        val luggageCount = tripWithRoute.tickets.count { it.paymentCategory == "Luggage" }
        val availableSeats = tripWithRoute.bus.capacity - ticketCount
        val activePassengers = ticketCount

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            TripInfoCard(
                trip = tripWithRoute,
                ticketCount = ticketCount,
                luggageCount = luggageCount,
                availableSeats = availableSeats,
                activePassengers = activePassengers
            )

            Spacer(modifier = Modifier.height(24.dp))

            TileGrid(navController, tripWithRoute.trip.tripId)
        }
    } ?: run {
        EmptyState(navController)
    }
}

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
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.DirectionsBus,
                contentDescription = "Trip Icon",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("Trip: ${trip.route.route.routeName}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
            Text("Bus: ${trip.bus.busName}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
            Text("Passenger Tickets Sold: $ticketCount", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)
            Text("Luggage Tickets Sold: $luggageCount", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)
            Text("Available Seats: $availableSeats", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)
            Text("Active Passengers: $activePassengers", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun TileGrid(navController: NavController, tripId: String) {
    val tiles = listOf(
        TileItem("Passenger Tickets", Icons.Filled.Person, MaterialTheme.colorScheme.primary) {
            navController.navigate("passenger_ticketing")
        },
        TileItem("Luggage Tickets", Icons.Filled.Luggage, MaterialTheme.colorScheme.secondary) {
            navController.navigate("luggageTickets/$tripId")
        },
        TileItem("Log Expenses", Icons.Filled.AttachMoney, MaterialTheme.colorScheme.tertiary) {
            navController.navigate("expenses/$tripId")
        },
        TileItem("View Reports", Icons.Filled.BarChart, MaterialTheme.colorScheme.surfaceVariant) {
            navController.navigate("reports/$tripId")
        },
        TileItem("End Trip", Icons.Filled.Warning, Color.Red) {
            // Handle End Trip
        }
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(tiles) { tile ->
            DashboardTile(tile)
        }
    }
}

data class TileItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun DashboardTile(tile: TileItem) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { tile.onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = tile.color),
        elevation = CardDefaults.elevatedCardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tile.icon,
                contentDescription = tile.label,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tile.label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}

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
    if (showEndTripDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text("Confirm End Trip", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp) },
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
                    colors = ButtonDefaults.buttonColors(containerColor = if (isConfirmEnabled) Color.Red else Color.Gray),
                    enabled = isConfirmEnabled
                ) {
                    Text("Yes, End Trip", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = { onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}
