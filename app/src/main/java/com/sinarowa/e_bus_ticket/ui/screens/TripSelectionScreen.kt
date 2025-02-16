/*
package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sinarowa.e_bus_ticket.data.local.entities.TripDetails
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel
import android.util.Log

@Composable
fun TripSelectionScreen(tripViewModel: TripViewModel, navController: NavController) {
    val trips by tripViewModel.trips.collectAsState(initial = emptyList())

    Log.d("TripSelectionScreen", "🔥 Screen Composed!")


    LaunchedEffect(Unit) {
        Log.d("TripSelectionScreen", "🔥 LaunchedEffect triggered!")
        tripViewModel.loadTrips()
        Log.d("TripSelectionScreen", "🚀 UI Received Trips: ${trips.size} trips")
    }


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Select a Trip", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (trips.isEmpty()) {
            Button(
                onClick = { navController.navigate("createTrip") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create New Trip")
            }

        } else {
            LazyColumn {
                items(trips) { trip ->
                    TripItem(trip = trip, onClick = {
                        navController.navigate("ticketing/${trip.tripId}")
                    })
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
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Trip: ${trip.routeName}", style = MaterialTheme.typography.h6)
            Text("Bus: ${trip.busName}", style = MaterialTheme.typography.body2)
        }
    }
}
*/
