package com.sinarowa.e_bus_ticket.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinarowa.e_bus_ticket.domain.models.TripWithRoute
import com.sinarowa.e_bus_ticket.ui.components.DropdownMenuComponent
import com.sinarowa.e_bus_ticket.ui.theme.GrayDisabled
import com.sinarowa.e_bus_ticket.ui.theme.LightBlueBackground
import com.sinarowa.e_bus_ticket.ui.theme.SkyBluePrimary
import com.sinarowa.e_bus_ticket.ui.theme.YellowSecondary
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel

@SuppressLint("LongLogTag")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerTicketingScreen(
    activeTrip: TripWithRoute,
    viewModel: TicketViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        Log.d("PassengerTicketingScreen", "State: $state")
    }

    LaunchedEffect(Unit) {
        viewModel.initializeWithTrip(activeTrip, context)
    }

    LaunchedEffect(state.sellResult) {
        if (state.sellResult?.isSuccess == true) {
            Log.d("PassengerTicketingScreen", "✅ Sale successful, resetting fields")
            viewModel.resetFields(context)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = LightBlueBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Passenger Ticketing",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SkyBluePrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // From Station
            OutlinedTextField(
                value = state.fromStation,
                onValueChange = {},
                label = { Text("From City", color = SkyBluePrimary) },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = YellowSecondary,
                    unfocusedBorderColor = SkyBluePrimary,
                    focusedLabelColor = YellowSecondary,
                    unfocusedLabelColor = SkyBluePrimary,
                    cursorColor = YellowSecondary,
                    disabledTextColor = GrayDisabled
                ),
                isError = state.fromStation.isBlank(),
                placeholder = { Text("Fetching location...", color = GrayDisabled) }
            )

            // Destination Dropdown
            if (state.fromStation.isNotBlank()) {
                if (state.validDestinations.isEmpty()) {
                    Text(
                        "No valid destinations available.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    DropdownMenuComponent(
                        label = "Select Destination",
                        items = state.validDestinations,
                        selectedItem = state.destination,
                        onSelectionChanged = { viewModel.setDestination(it) },
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
            }

            // Ticket Type Dropdown
            if (state.destination.isNotBlank()) {
                DropdownMenuComponent(
                    label = "Select Ticket Type",
                    items = listOf("Adult", "Child"),
                    selectedItem = state.ticketType,
                    onSelectionChanged = { viewModel.setTicketType(it) },
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

            // Short Amount Selector (for Adult tickets)
            if (state.ticketType == "Adult" && state.originalPrice > 0) {
                ShortAmountSelector(
                    shortAmount = state.shortAmount,
                    maxAmount = state.originalPrice.toInt(),
                    onAmountChanged = { viewModel.setShortAmount(it) }
                )
            }

            // Final Ticket Price
            if (state.displayPrice > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SkyBluePrimary.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Final Ticket Price",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SkyBluePrimary
                        )
                        Text(
                            "${state.displayPrice} USD",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = YellowSecondary
                        )
                        if (state.shortAmount > 0) {
                            Text(
                                "Short Applied: -$${state.shortAmount}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (state.ticketType == "Child") {
                            Text(
                                "Child Fare Applied",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else if (state.destination.isNotBlank() && state.originalPrice == 0.0) {
                Text(
                    "Error fetching ticket price. Try again.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Seat and Passenger Info
            if (state.remainingSeats > 0 || state.activePassengers > 0) {
                TicketSummaryCard(
                    remainingSeats = state.remainingSeats,
                    activePassengers = state.activePassengers
                )
            } else {
                Text(
                    "Loading trip data...",
                    color = SkyBluePrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (state.remainingSeats == 0 && state.activePassengers > 0) {
                Text(
                    "No seats available.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Sell & Print Button
            val isButtonEnabled = state.fromStation.isNotBlank() &&
                    state.destination.isNotBlank() &&
                    state.fromStation != state.destination &&
                    state.displayPrice > 0 &&
                    state.remainingSeats > 0

            Button(
                onClick = { viewModel.sellTicket(state.displayPrice) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isButtonEnabled) YellowSecondary else GrayDisabled,
                    disabledContainerColor = GrayDisabled
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = isButtonEnabled && !state.isProcessing,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (state.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Icon(
                        Icons.Default.Print,
                        contentDescription = "Print",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Sell & Print",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ShortAmountSelector(shortAmount: Int, maxAmount: Int, onAmountChanged: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (shortAmount > 0) onAmountChanged(shortAmount - 1) },
                enabled = shortAmount > 0,
                modifier = Modifier
                    .size(40.dp)
                    .background(YellowSecondary.copy(alpha = if (shortAmount > 0) 1f else 0.5f), CircleShape)
            ) {
                Text(
                    "-",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "Short: $$shortAmount",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SkyBluePrimary
            )
            IconButton(
                onClick = { if (shortAmount < maxAmount - 1) onAmountChanged(shortAmount + 1) },
                modifier = Modifier
                    .size(40.dp)
                    .background(YellowSecondary, CircleShape)
            ) {
                Text(
                    "+",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TicketSummaryCard(remainingSeats: Int, activePassengers: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Seats Remaining",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SkyBluePrimary
                )
                Text(
                    "$remainingSeats",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = YellowSecondary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Active Passengers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SkyBluePrimary
                )
                Text(
                    "$activePassengers",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = YellowSecondary
                )
            }
        }
    }
}