package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sinarowa.e_bus_ticket.data.local.entities.Bus
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.ui.components.DropdownMenuComponent
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(viewModel: TripViewModel, navController: NavController) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var selectedRoute by remember { mutableStateOf<RouteEntity?>(null) }
    var selectedBus by remember { mutableStateOf<Bus?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create a New Trip",
            style = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // ✅ Use DropdownMenuComponent for Routes
        DropdownMenuComponent(
            label = "Select Route",
            items = state.routes.map { it.routeName },
            selectedItem = selectedRoute?.routeName ?: "",
            onSelectionChanged = { selectedRoute = state.routes.firstOrNull { route -> route.routeName == it } }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ Use DropdownMenuComponent for Buses
        DropdownMenuComponent(
            label = "Select Bus",
            items = state.buses.map { it.busName },
            selectedItem = selectedBus?.busName ?: "",
            onSelectionChanged = { selectedBus = state.buses.firstOrNull { bus -> bus.busName == it } }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                selectedRoute?.let { route ->
                    selectedBus?.let { bus ->
                        viewModel.createTrip(route, bus)
                        navController.popBackStack()
                    }
                }
            },
            enabled = selectedRoute != null && selectedBus != null && !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Create Trip", color = Color.White)
            }
        }
    }
}
