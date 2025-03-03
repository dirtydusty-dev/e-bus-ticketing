package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sinarowa.e_bus_ticket.data.local.entities.Bus
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel
import kotlinx.coroutines.launch

@Composable
fun CreateTripScreen(viewModel: TripViewModel, navController: NavController) {
    // Observe routes and buses
    val routes by viewModel.routes.observeAsState(emptyList())
    val buses by viewModel.buses.observeAsState(emptyList())

    // Hold selected route and bus
    var selectedRoute by remember { mutableStateOf<RouteEntity?>(null) }
    var selectedBus by remember { mutableStateOf<Bus?>(null) }

    val isCreatingTrip by viewModel.isLoading.observeAsState(false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create a New Trip", style = MaterialTheme.typography.h5, color = Color(0xFF1565C0))
        Spacer(modifier = Modifier.height(12.dp))

        // ✅ Route Dropdown
        DropdownSelector(
            label = "Select Route",
            items = routes,
            selectedItem = selectedRoute,
            onSelectionChanged = { selectedRoute = it },
            displayText = { it.routeName }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ Bus Dropdown
        DropdownSelector(
            label = "Select Bus",
            items = buses,
            selectedItem = selectedBus,
            onSelectionChanged = { selectedBus = it },
            displayText = { it.busName }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ Create Trip Button
        Button(
            onClick = {
                selectedRoute?.let { route ->
                    selectedBus?.let { bus ->
                        viewModel.createTrip(route, bus)
                        navController.popBackStack() // Navigate after creating trip
                    }
                }
            },
            enabled = selectedRoute != null && selectedBus != null && !isCreatingTrip,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFEB3B))
        ) {
            if (isCreatingTrip) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
            } else {
                Text("Create Trip", color = Color.Black)
            }
        }

    }
}


/**
 * ✅ Reusable Dropdown Selector Component (Fixed for Real-time Updates)
 */
@Composable
fun <T> DropdownSelector(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onSelectionChanged: (T) -> Unit,
    displayText: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText by rememberUpdatedState(selectedItem?.let { displayText(it) } ?: label)

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
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF1565C0),
                unfocusedBorderColor = Color.Gray
            )
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(onClick = {
                    onSelectionChanged(item)
                    expanded = false
                }) {
                    Text(displayText(item))
                }
            }
        }
    }
}
