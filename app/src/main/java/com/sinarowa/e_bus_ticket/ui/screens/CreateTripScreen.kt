package com.sinarowa.e_bus_ticket.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sinarowa.e_bus_ticket.data.local.entities.Bus
import com.sinarowa.e_bus_ticket.data.local.entities.RouteEntity
import com.sinarowa.e_bus_ticket.ui.components.DropdownMenuComponent
import com.sinarowa.e_bus_ticket.ui.theme.GrayDisabled
import com.sinarowa.e_bus_ticket.ui.theme.LightBlueBackground
import com.sinarowa.e_bus_ticket.ui.theme.SkyBluePrimary
import com.sinarowa.e_bus_ticket.ui.theme.YellowSecondary
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(viewModel: TripViewModel, navController: NavController) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var selectedRoute by remember { mutableStateOf<RouteEntity?>(null) }
    var selectedBus by remember { mutableStateOf<Bus?>(null) }

    LaunchedEffect(state) {
        Log.d("CreateTripScreen", "State: routes=${state.routes.size}, buses=${state.buses.size}, isLoading=${state.isLoading}")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = LightBlueBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create a New Trip",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyBluePrimary),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = YellowSecondary,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                // Route Dropdown
                if (state.routes.isEmpty()) {
                    Text(
                        "No routes available.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    DropdownMenuComponent(
                        label = "Select Route",
                        items = state.routes.map { it.routeName },
                        selectedItem = selectedRoute?.routeName ?: "",
                        onSelectionChanged = { selectedRoute = state.routes.firstOrNull { route -> route.routeName == it } },
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YellowSecondary,
                            unfocusedBorderColor = SkyBluePrimary,
                            focusedLabelColor = YellowSecondary,
                            unfocusedLabelColor = SkyBluePrimary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Bus Dropdown
                if (state.buses.isEmpty()) {
                    Text(
                        "No buses available.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    DropdownMenuComponent(
                        label = "Select Bus",
                        items = state.buses.map { it.busName },
                        selectedItem = selectedBus?.busName ?: "",
                        onSelectionChanged = { selectedBus = state.buses.firstOrNull { bus -> bus.busName == it } },
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YellowSecondary,
                            unfocusedBorderColor = SkyBluePrimary,
                            focusedLabelColor = YellowSecondary,
                            unfocusedLabelColor = SkyBluePrimary,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Create Trip Button
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedRoute != null && selectedBus != null && !state.isLoading) YellowSecondary else GrayDisabled,
                        disabledContainerColor = GrayDisabled
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            "Create Trip",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                // Error Message
                state.errorMessage?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}