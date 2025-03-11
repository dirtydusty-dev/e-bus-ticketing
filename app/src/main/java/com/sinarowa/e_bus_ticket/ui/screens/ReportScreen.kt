package com.sinarowa.e_bus_ticket.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinarowa.e_bus_ticket.viewmodel.ReportViewModel
import com.sinarowa.e_bus_ticket.ui.theme.LightBlueBackground
import com.sinarowa.e_bus_ticket.ui.theme.SkyBluePrimary
import com.sinarowa.e_bus_ticket.ui.theme.YellowSecondary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel(),
    tripId: String? = null,
    reportType: String
) {
    val state by viewModel.state.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yy EEE HH:mm", Locale.getDefault())
    val currentDate = dateFormat.format(Date())

    LaunchedEffect(Unit) {
        when (reportType) {
            "Check" -> tripId?.let { viewModel.loadCheckReport() }
            "Daily" -> viewModel.loadDailySalesReport()
            "Trip" -> tripId?.let { viewModel.loadTripReport() }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = LightBlueBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (reportType) {
                            "Check" -> "Check Report"
                            "Daily" -> "Daily Sales Report"
                            "Trip" -> "Trip Report"
                            else -> "Report"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyBluePrimary)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            item {
                Text("Speedlada Travels Limited", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    when (reportType) {
                        "Check" -> "CHECK REPORT"
                        "Daily" -> "DAILY SALES REPORT"
                        "Trip" -> "TRIP REPORT"
                        else -> "REPORT"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                Text("$currentDate", color = Color.White, fontSize = 14.sp)
                Text("Device-ID: 63d9c65eed502d2d", color = Color.White, fontSize = 14.sp) // Replace with actual device ID
            }

            // Loading or Error State
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = YellowSecondary, modifier = Modifier.size(48.dp))
                    }
                }
            } else if (state.errorMessage != null) {
                item {
                    Text(
                        text = state.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                // Extract unique categories from tickets
                val allCategories = state.tickets.map { it.paymentCategory }.distinct()

                // Ticket Details with Dynamic Categories
                if (state.tickets.isNotEmpty()) {
                    item {
                        Text("TICKET DETAILS", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        state.tickets.takeLast(20).forEach { ticket ->
                            val fromStation = state.selectedTrip?.route?.stationDetails?.firstOrNull()?.name ?: "Unknown"
                            val toStation = state.selectedTrip?.route?.stationDetails?.lastOrNull()?.name ?: "Unknown"

                            // Count occurrences for each category
                            val categoryCounts = allCategories.associateWith { category ->
                                if (ticket.paymentCategory == category) 1 else 0
                            }

                            // Format the category string dynamically
                            val categoryString = allCategories.joinToString(" ") { category ->
                                val shortForm = if (category.contains("Short")) "$" else category.first().toString()
                                "$shortForm ${categoryCounts[category]}"
                            }

                            Text(
                                String.format(
                                    "%-8s %-18s %-5s %s FARE %-6.2f",
                                    ticket.ticketId,
                                    "$fromStation-$toStation",
                                    ticket.creationTime,
                                    categoryString,
                                    ticket.amount
                                ),
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Payment Details
                if (state.tickets.isNotEmpty()) {
                    item {
                        Text("PAYMENT DETAILS", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            String.format("PAYMENT  COUNT  AMOUNT\nCash  %-6d  %-6.2f", state.tickets.size, state.totalSales),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }

                // Expenses
                if (reportType == "Daily" && state.expenses.isNotEmpty()) {
                    item {
                        Text("EXPENSES", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        state.expenses.forEach { expense ->
                            Text(
                                String.format("%-15s %-6.2f", expense.expenseType, expense.amount),
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            "TOTAL-EXPENSES ${String.format("%.2f", state.totalExpenses)}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }

                // Net Sales
                if (reportType == "Daily") {
                    item {
                        Text("NET-SALES ${String.format("%.2f", state.netSales)}", color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            // Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { /* Handle OK */ }, colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)) {
                        Text("OK", color = Color.White)
                    }
                    Button(onClick = { /* Handle PRINT */ }, colors = ButtonDefaults.buttonColors(containerColor = YellowSecondary)) {
                        Icon(Icons.Default.Print, contentDescription = "Print", tint = Color.White)
                        Text("PRINT", color = Color.White)
                    }
                }
            }
        }
    }
}
