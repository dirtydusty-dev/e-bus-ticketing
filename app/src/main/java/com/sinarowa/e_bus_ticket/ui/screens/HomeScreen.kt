package com.sinarowa.e_bus_ticket.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sinarowa.e_bus_ticket.data.local.entities.TripDetails
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel

@Composable
fun HomeScreen(tripViewModel: TripViewModel, navController: NavController) {
    val trips by tripViewModel.trips.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(trips) {
        if (isLoading && trips.isNotEmpty()) {
            isLoading = false
        } else if (isLoading && trips.isEmpty()) {
            kotlinx.coroutines.delay(500)
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Background changed to white
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { Text("Available Trips", color = Color.White) },
            backgroundColor = Color(0xFF1565C0),
            navigationIcon = {
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
                            navController.navigate("tripHistory")
                        }) {
                            Text("Trip History")
                        }
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

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        color = Color(0xFF1565C0),
                        modifier = Modifier.size(50.dp).align(Alignment.Center)
                    )
                }
                trips.isEmpty() -> {
                    EmptyState(navController)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
                    ) {
                        items(trips) { trip ->
                            TripItem(trip, onClick = { navController.navigate("tripDashboard/${trip.tripId}") })
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
            fontWeight = FontWeight.Bold,
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
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFEB3B)),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.elevation(6.dp)
        ) {
            Text("Create New Trip", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TripItem(trip: TripDetails, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick(trip.tripId) },
        backgroundColor = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = "Route", tint = Color(0xFF1565C0))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Trip: ${trip.routeName}",
                    style = MaterialTheme.typography.h6,
                    color = Color(0xFF1565C0)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.DirectionsBus, contentDescription = "Bus", tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Bus: ${trip.busName}",
                    style = MaterialTheme.typography.body2,
                    color = Color.DarkGray
                )
            }
        }
    }
}
