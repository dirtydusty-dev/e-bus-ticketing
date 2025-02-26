/*
package com.sinarowa.e_bus_ticket.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sinarowa.e_bus_ticket.utils.PdfUtils
import com.sinarowa.e_bus_ticket.utils.ReportUtils
import kotlinx.coroutines.launch

@Composable
fun TripSalesScreen(tripSales: List<TripViewModel.TripSale>, context: Context) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    // 🔹 Prepare Data for the Report
    val tripsCount = tripSales.size
    val totalTickets = tripSales.sumOf { it.totalTickets }
    val cancelledTickets = 0  // Placeholder, update if needed
    val firstTicketNumber = "000002" // Placeholder, fetch dynamically
    val lastTicketNumber = "000003"  // Placeholder, fetch dynamically
    val firstTicketTime = "12:23" // Placeholder, fetch dynamically
    val lastTicketTime = "12:23"  // Placeholder, fetch dynamically

    val ticketDetails = tripSales.flatMap { it.routeBreakdown }
        .flatMap { it.ticketBreakdown }
        .groupBy { it.type }
        .mapValues { (_, tickets) -> tickets.sumOf { it.count } to tickets.sumOf { it.amount } }

    val paymentDetails = mapOf("Cash" to (2 to 7.50)) // Placeholder, update dynamically

    val tripSalesList = tripSales.map { it.routeName to it.totalSales }

    val expenses = mapOf("CLEANING" to 12.25, "DRIVER WAGE" to 0.38) // Placeholder, update dynamically

    // 🔹 Generate Report Text
  */
/*  val reportText = ReportUtils.generateDailySalesReport(
        companyName = "Sinarowa Limited",
        date = "2/21/25",
        deviceId = "63d9c65eed502d2d",
        tripsCount = tripsCount,
        totalTickets = totalTickets,
        cancelledTickets = cancelledTickets,
        firstTicketNumber = firstTicketNumber,
        lastTicketNumber = lastTicketNumber,
        firstTicketTime = firstTicketTime,
        lastTicketTime = lastTicketTime,
        ticketDetails = ticketDetails,
        paymentDetails = paymentDetails,
        tripSales = tripSalesList,
        expenses = expenses
    )*//*


    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Daily Sales Report") },
                backgroundColor = Color(0xFF1565C0),
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            */
/*LazyColumn(
                modifier = Modifier.weight(1f) // Allows scrolling
            ) {
                item {
                    Text(
                        reportText,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }*//*


            // ✅ Fixed Button at the Bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            PdfUtils.generateTripSalesPdf(
                                context, tripSales, "Sinarowa Limited",
                                "+263 712 345 678 ", "John Doe",
                                "06:30 AM", "19 Feb 2025",
                                "ZIM 1234", "Blue Express"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Export PDF")
                }
            }
        }
    }
}


*/
