package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sinarowa.e_bus_ticket.data.local.enums.TicketStatus
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import com.sinarowa.e_bus_ticket.ui.theme.SkyBluePrimary
import com.sinarowa.e_bus_ticket.ui.theme.YellowSecondary
import com.sinarowa.e_bus_ticket.ui.theme.LightBlueBackground
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDashboardScreen(
    navController: NavController,
    tripViewModel: TripViewModel
) {
    val state by tripViewModel.state.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = LightBlueBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Trip Dashboard",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyBluePrimary),
                actions = {
                    IconButton(onClick = { tripViewModel.refreshTrip() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = YellowSecondary,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            state.activeTrip?.let { tripWithRoute ->
                val ticketCount = tripWithRoute.tickets.count { it.status == TicketStatus.VALID }
                val luggageCount = tripWithRoute.tickets.count { it.paymentCategory == "Luggage" }
                val availableSeats = tripWithRoute.bus.capacity - ticketCount
                val activePassengers = ticketCount

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LightBlueBackground)
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    TripInfoCard(
                        trip = tripWithRoute,
                        ticketCount = ticketCount,
                        luggageCount = luggageCount,
                        availableSeats = availableSeats,
                        activePassengers = activePassengers
                    )
                    TileGrid(navController, tripWithRoute)
                }
            } ?: run {
                EmptyState(navController, Modifier.padding(paddingValues))
            }
        }
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = SkyBluePrimary)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.DirectionsBus,
                contentDescription = "Trip Icon",
                tint = Color.White,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Trip: ${trip.route.route.routeName}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                "Bus: ${trip.bus.busName}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Tickets", ticketCount, Color.White)
                StatItem("Luggage", luggageCount, Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Seats", availableSeats, Color.White)
                StatItem("Passengers", activePassengers, Color.White)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = color.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TileGrid(navController: NavController, tripWithRoute: TripWithRoute) {
    val tiles = listOf(
        TileItem("Passenger Tickets", Icons.Filled.Person, YellowSecondary) {
            navController.navigate("passenger_ticketing/${tripWithRoute.trip.tripId}")
        },
        TileItem("Luggage Tickets", Icons.Filled.Luggage, SkyBluePrimary) {
            navController.navigate("luggageTickets/${tripWithRoute.trip.tripId}")
        },
        TileItem("Log Expenses", Icons.Filled.AttachMoney, YellowSecondary) {
            navController.navigate("expenses/${tripWithRoute.trip.tripId}")
        },
        TileItem("View Reports", Icons.Filled.BarChart, SkyBluePrimary) {
            navController.navigate("reports/${tripWithRoute.trip.tripId}")
        },
        TileItem("End Trip", Icons.Filled.Warning, Color.Red) {
            // Handle End Trip (to be implemented)
        }
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { tile.onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = tile.color),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = tile.label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EmptyState(navController: NavController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "No Active Trip",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SkyBluePrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Start a new trip to begin.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("createTrip") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = YellowSecondary),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Text(
                "Create New Trip",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}