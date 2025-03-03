package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel
import androidx.compose.runtime.livedata.observeAsState


@Composable
fun HomeScreen(
    viewModel: TripViewModel,
    navController: NavController,
) {
    // Observe active trip and error message
    val activeTrip by viewModel.activeTrip.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false) // Loading state

    // Load active trip when the screen is launched
    LaunchedEffect(Unit) {
        viewModel.loadActiveTrip() // Fetch the active trip
    }

    // Animation for blinking "No Active Trip"
    val infiniteTransition = rememberInfiniteTransition()
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Trip", color = Color.White) },
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
                    // Show loading indicator while fetching active trip
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
                activeTrip != null -> {
                    // Move the trip item to the top when there is an active trip
                    activeTrip?.let { tripWithRoute: TripWithRoute ->
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Active trip at the top
                            TripItem(
                                tripWithRoute = tripWithRoute,
                                onClick = { navController.navigate("tripDashboard/${tripWithRoute.trip.tripId}") }
                            )

                            // Rest of the content can be displayed here (error message etc.)
                        }
                    }
                }
                else -> {
                    // Show "No Active Trip" message and button to create a trip
                    EmptyState(navController)
                }
            }

            // Show error message if any
            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Error: $it", color = Color.Red)
            }
        }
    }
}


@Composable
fun TripItem(tripWithRoute: TripWithRoute, onClick: (String) -> Unit) {
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
            .clickable { onClick(tripWithRoute.trip.tripId) },
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
                        "Trip: ${tripWithRoute.route.routeName}",  // ✅ Using `routeName` from `TripWithRoute`
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
                            "Bus: ${tripWithRoute.bus.busName}",
                            style = MaterialTheme.typography.body2,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Start: ${tripWithRoute.trip.startTime}",
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
