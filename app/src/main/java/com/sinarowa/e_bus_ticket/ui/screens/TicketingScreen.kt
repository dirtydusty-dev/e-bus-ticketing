package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun TicketingScreen(
    tripId: String,
    ticketViewModel: TicketViewModel
) {
    val ticketCount by ticketViewModel.ticketCount.collectAsState()

    LaunchedEffect(tripId) {
        ticketViewModel.updateTicketCount(tripId)  // ✅ Fetch ticket count
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Trip ID: $tripId", style = MaterialTheme.typography.h5)
        Text("Tickets Sold: $ticketCount", style = MaterialTheme.typography.body1)  // ✅ Display ticket count

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val newTicket = Ticket(
                ticketId = System.currentTimeMillis().toString(),
                tripId = tripId,
                //seatNumber = ticketCount + 1,
                fromStop = "Harare", // Replace with actual stop
                toStop = "Bulawayo", // Replace with actual stop
                price = 10.0,
                timestamp = System.currentTimeMillis()
            )
            ticketViewModel.insertTicket(newTicket)  // ✅ Add new ticket
        }) {
            Text("Sell Ticket")
        }
    }
}










