package com.sinarowa.e_bus_ticket.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel

@Composable
fun HomeScreen(tripViewModel: TripViewModel, navController: NavController) {
    val trips by tripViewModel.trips.collectAsState()
    var isLoading by remember { mutableStateOf(true) }

    // ✅ Ensure loading stops ONLY after first fetch is complete
    LaunchedEffect(trips) {
        if (isLoading && trips.isNotEmpty()) {
            isLoading = false
        } else if (isLoading && trips.isEmpty()) {
            // Simulate a short delay for a smoother transition
            kotlinx.coroutines.delay(500)
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFE3F2FD)), // Light Blue Background
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(color = Color(0xFF1565C0))
            }

            trips.isEmpty() -> {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "No Trips",
                    modifier = Modifier.size(150.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No trips available yet!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Start by creating a new trip below.",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("createTrip") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFEB3B)),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.elevation(defaultElevation = 4.dp)
                ) {
                    Text("Create New Trip", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("bluetoothScreen") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFEB3B)),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.elevation(defaultElevation = 4.dp)
                ) {
                    Text("Printer Test", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            else -> {
                Text(
                    "Select a Trip",
                    style = MaterialTheme.typography.h5,
                    color = Color(0xFF1565C0)
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    items(trips) { trip ->
                        TripItem(trip = trip, onClick = {
                            navController.navigate("tripDashboard/${trip.tripId}")
                        })
                    }
                }
            }
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
        elevation = 6.dp,
        backgroundColor = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Trip: ${trip.routeName}",
                style = MaterialTheme.typography.h6,
                color = Color(0xFF1565C0)
            )
            Text(
                "Bus: ${trip.busName}",
                style = MaterialTheme.typography.body2,
                color = Color.DarkGray
            )
        }
    }
}
