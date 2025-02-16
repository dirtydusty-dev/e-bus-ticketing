package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.data.local.entities.BusEntity
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel

@Composable
fun CreateTripScreen(viewModel: TripViewModel, navController: NavController) {
    val routes by viewModel.routes.collectAsState(initial = emptyList())
    val buses by viewModel.buses.collectAsState(initial = emptyList())

    var selectedRoute by remember { mutableStateOf<RouteEntity?>(null) }
    var selectedBus by remember { mutableStateOf<BusEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadRoutes()
        viewModel.loadBuses()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Select Route", style = MaterialTheme.typography.h6)

        // ✅ Route Dropdown (Only shows route name)
        DropdownMenuComponent(
            label = "Select Route",
            items = routes,
            selectedItem = selectedRoute,
            onSelectionChanged = { selectedRoute = it },
            displayText = { it.name }  // ✅ Extracts only the route name
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Select Bus", style = MaterialTheme.typography.h6)

        // ✅ Bus Dropdown (Only shows bus name)
        DropdownMenuComponent(
            label = "Select Bus",
            items = buses,
            selectedItem = selectedBus,
            onSelectionChanged = { selectedBus = it },
            displayText = { it.busName }  // ✅ Extracts only the bus name
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (selectedRoute != null && selectedBus != null) {
                    viewModel.createTrip(selectedRoute!!, selectedBus!!)
                    navController.popBackStack()
                }
            },
            enabled = selectedRoute != null && selectedBus != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Trip")
        }
    }
}

/**
 * ✅ Generic Dropdown Menu for Selecting Items
 */
@Composable
fun <T> DropdownMenuComponent(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onSelectionChanged: (T) -> Unit,
    displayText: (T) -> String  // ✅ Extracts only the name for display
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(label) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
            }
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(onClick = {
                    onSelectionChanged(item)
                    selectedText = displayText(item)  // ✅ Show only relevant name
                    expanded = false
                }) {
                    Text(displayText(item))
                }
            }
        }
    }
}
