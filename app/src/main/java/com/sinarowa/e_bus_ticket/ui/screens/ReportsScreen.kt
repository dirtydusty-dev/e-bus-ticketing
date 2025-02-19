package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel

@Composable
fun ReportsScreen(tripId: String,tripViewModel: TripViewModel,ticketViewModel: TicketViewModel) {
    var selectedReport by remember { mutableStateOf("Trip Sales") }

    LaunchedEffect(Unit){
        tripViewModel.fetchTripSales()
    }

    val context = LocalContext.current

    // ✅ Use `tripSales` directly from StateFlow
    val tripSales by tripViewModel.tripSales.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        DropdownMenuComponent(
            label = "Select Report",
            items = listOf("Tickets", "Trip Sales"),
            selectedItem = selectedReport
        ) { newSelection ->
            selectedReport = newSelection
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedReport) {
            "Trip Sales" -> TripSalesScreen(tripSales, context)
            "Tickets" -> TicketSalesScreen(ticketViewModel, context, tripId)
        }
    }
}
