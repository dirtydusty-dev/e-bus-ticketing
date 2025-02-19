package com.sinarowa.e_bus_ticket.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
import com.sinarowa.e_bus_ticket.utils.PdfUtils

@Composable
fun TicketSalesScreen(ticketViewModel: TicketViewModel, context: Context, tripId: String) {
    val stationSales = remember { mutableStateOf(mutableMapOf<String, Pair<Int, Double>>()) }
    val breakdown = remember { mutableStateOf(mutableMapOf<Pair<String, String>, Pair<Int, Double>>()) }

    // ✅ Fetch data when screen is loaded
    LaunchedEffect(tripId) {
        val salesData = ticketViewModel.getStationSales(tripId)
        val breakdownData = ticketViewModel.getTicketBreakdown(tripId)

        // ✅ Update maps correctly
        stationSales.value = salesData.toMutableMap()
        breakdown.value = breakdownData.toMutableMap()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Ticket Sales Report", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        // **Station Sales Summary**
        Text(text = "Station Sales", style = MaterialTheme.typography.h6)
        TableHeader(listOf("Station", "Count", "Amount"))

        LazyColumn {
            items(stationSales.value.entries.toList()) { (station, data) ->
                TableRow(listOf(station, data.first.toString(), "$${data.second}"))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // **Breakdown Section**
        Text(text = "Breakdown", style = MaterialTheme.typography.h6)
        TableHeader(listOf("Start", "Destination", "Count", "Amount"))

        LazyColumn {
            items(breakdown.value.entries.toList()) { (route, data) ->
                TableRow(listOf(route.first, route.second, data.first.toString(), "$${data.second}"))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Print Report Button
        Button(onClick = { PdfUtils.generateTicketSalesPdf(context, stationSales.value, breakdown.value) }) {
            Text(text = "Print Report")
        }
    }
}

@Composable
fun TableHeader(columns: List<String>) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        columns.forEach { columnName ->
            Text(
                text = columnName,
                modifier = Modifier
                    .weight(1f) // Ensures equal column spacing
                    .padding(4.dp),
                style = MaterialTheme.typography.body1.copy(color = Color.Black)
            )
        }
    }
}

@Composable
fun TableRow(values: List<String>) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        values.forEach { value ->
            Text(
                text = value,
                modifier = Modifier
                    .weight(1f) // Ensures equal column spacing
                    .padding(4.dp),
                style = MaterialTheme.typography.body2
            )
        }
    }
}


