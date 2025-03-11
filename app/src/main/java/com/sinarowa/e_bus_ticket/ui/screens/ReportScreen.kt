package com.sinarowa.e_bus_ticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.sinarowa.e_bus_ticket.ui.theme.SkyBluePrimary
import com.sinarowa.e_bus_ticket.ui.theme.YellowSecondary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel(),
    reportType: String
) {
    val state by viewModel.state.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yy EEE HH:mm", Locale.getDefault())
    val currentDate = dateFormat.format(Date())

    LaunchedEffect(Unit) {
        when (reportType) {
            "Check" -> viewModel.loadCheckReport()
            "Daily" -> viewModel.loadDailySalesReport()
            "Trip" -> viewModel.loadTripReport()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("GOVASBURG SERVICES", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        when (reportType) {
                            "Check" -> "CHECK REPORT"
                            "Daily" -> "DAILY SALES REPORT"
                            "Trip" -> "TRIP REPORT"
                            else -> "REPORT"
                        },
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                    Text(currentDate, color = Color.Black, fontSize = 14.sp)
                    Text("Device-ID: 63d9c65eed502d2d", color = Color.Black, fontSize = 14.sp)
                }
            }

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
                // Trip Details
                if (state.selectedTrip != null) {
                    val trip = state.selectedTrip!!
                    item {
                        Text("ROUTE: ${trip.route.route.routeName}", color = Color.Black, fontSize = 16.sp)
                        Text("TOTAL-SALES: ${String.format("%.2f", state.totalSales)}", color = Color.Black, fontSize = 14.sp)
                        Text("TOTAL-TICKETS: ${state.tickets.size}", color = Color.Black, fontSize = 14.sp)
                    }
                }

                // Dynamic Ticket Breakdown
                if (state.ticketSummary.isNotEmpty()) {
                    item {
                        Text("TICKET DETAILS", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "CATEGORY    COUNT    AMOUNT",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Divider(color = Color.Gray, thickness = 1.dp)
                        state.ticketSummary.forEach { (category, details) ->
                            Text(
                                "$category  ${details.first}  ${String.format("%.2f", details.second)}",
                                color = Color.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Destination Breakdown
                if (state.selectedTrip != null) {
                    val trip = state.selectedTrip!!
                    item {
                        Text("DESTINATION DETAILS", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        val fromStation = trip.route.stationDetails.firstOrNull()?.name ?: "Unknown"
                        val toStation = trip.route.stationDetails.lastOrNull()?.name ?: "Unknown"
                        Text("$fromStation ➝ $toStation", color = Color.Black, fontSize = 14.sp)
                    }
                }

                // Expenses Breakdown
                if (state.expenses.isNotEmpty()) {
                    item {
                        Text("EXPENSES", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("CATEGORY   AMOUNT", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Divider(color = Color.Gray, thickness = 1.dp)
                        state.expenses.forEach { expense ->
                            Text("${expense.expenseType}  ${String.format("%.2f", expense.amount)}", color = Color.Black, fontSize = 14.sp)
                        }
                        Text("TOTAL-EXPENSES: ${String.format("%.2f", state.totalExpenses)}", color = Color.Black, fontSize = 14.sp)
                    }
                }

                // Net Sales
                item {
                    Text("NET SALES: ${String.format("%.2f", state.netSales)}", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { /* Handle OK */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("OK", color = Color.White, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { /* Handle PRINT */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = YellowSecondary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PRINT", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
