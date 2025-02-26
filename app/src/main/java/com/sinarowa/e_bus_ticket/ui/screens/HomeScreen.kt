package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import com.sinarowa.e_bus_ticket.viewmodel.CreateTripViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(tripViewModel: CreateTripViewModel, navController: NavController) {
    // Collect state from the ViewModel
    val trips by tripViewModel.activeTrips.collectAsState()
    val isLoading by tripViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Trips", color = Color.White) },
                backgroundColor = Color(0xFF1565C0),
                navigationIcon = {
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(onClick = {
                                menuExpanded = false
                                navController.navigate("bluetoothScreen")
                            }) {
                                Text("Connect Printer")
                            }
                            DropdownMenuItem(onClick = {
                                menuExpanded = false
                                navController.navigate("settings")
                            }) {
                                Text("Settings")
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    // Show loading indicator while trips are being fetched
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
                trips.isEmpty() -> {
                    // If no trips are available, show empty state
                    EmptyState(navController)
                }
                else -> {
                    // Show trips in a LazyColumn when available
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
                    ) {
                        items(trips) { trip ->
                            TripItem(trip, onClick = { navController.navigate("tripDashboard/${trip.id}") })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "No trips available yet!",
            fontSize = MaterialTheme.typography.h5.fontSize,
            color = Color(0xFF1565C0)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Start by creating a new trip below.",
            fontSize = MaterialTheme.typography.body1.fontSize,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("createTrip") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFEB3B)),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.elevation(6.dp)
        ) {
            Text("Create New Trip", color = Color.Black)
        }
    }
}

@Composable
fun TripItem(trip: TripWithRoute, onClick: (String) -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick(trip.id.toString()) },
        backgroundColor = Color.White,
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Route", tint = Color(0xFF1565C0))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Trip: ${trip.routeName}",  // ✅ Using `routeName` from `TripWithRoute`
                        style = MaterialTheme.typography.h6,
                        color = Color(0xFF1565C0)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.DirectionsBus, contentDescription = "Bus", tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "Bus: ${trip.busName}",
                            style = MaterialTheme.typography.body2,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Start: ${trip.startTime}",
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = Color.Gray,
                            fontSize = MaterialTheme.typography.caption.fontSize
                        )
                    }
                }
            }

            // ✅ Blinking Green Dot and "Active" label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF00C853).copy(alpha = blinkAlpha), shape = RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Active",
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = Color(0xFF00C853),
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}
