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
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel
import com.sinarowa.e_bus_ticket.utils.PdfUtils
import kotlinx.coroutines.launch

@Composable
fun TripSalesScreen(tripSales: List<TripViewModel.TripSale>, context: Context) {
    var isExporting by remember { mutableStateOf(false) } // ✅ Track export state
    var showSnackbar by remember { mutableStateOf(false) } // ✅ Track snackbar visibility
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp), // ✅ Moves Snackbar down slightly from the absolute top
                contentAlignment = Alignment.TopCenter
            ) {
                SnackbarHost(scaffoldState.snackbarHostState)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Trip Sales Report", style = MaterialTheme.typography.h5)

            LazyColumn {
                items(tripSales) { sale ->
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Route: ${sale.routeName}")
                            Text("Total Tickets Sold: ${sale.totalTickets}")
                            Text("Total Sales: \$${sale.totalSales}")
                            Text("Total Expenses: \$${sale.totalExpenses}")
                            Text("Net Sales: \$${sale.netSales}")

                            Spacer(modifier = Modifier.height(8.dp))
                            sale.routeBreakdown.forEach { route ->
                                Text("From: ${route.fromCity} To: ${route.toCity}")
                                route.ticketBreakdown.forEach { ticket ->
                                    Text("  - ${ticket.type}: ${ticket.count} tickets (\$${ticket.amount})")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Expense Breakdown:")
                            sale.expenseBreakdown.forEach { expense ->
                                Text("  - ${expense.type}: ${expense.count} entries (\$${expense.totalAmount})")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ✅ Export PDF Button with Loading Indicator & Snackbar
            Button(
                onClick = {
                    isExporting = true // ✅ Instantly update the UI
                    coroutineScope.launch {
                        PdfUtils.generateTripSalesPdf(
                            context,
                            tripSales,
                            companyName = "Govasburg Bus Company",
                            companyContact = "+263 712 345 678 ",
                            conductorName = "John Doe",
                            tripStartTime = "06:30 AM",
                            tripDate = "19 Feb 2025",
                            busReg = "ZIM 1234",
                            busName = "Blue Express"
                        )
                        isExporting = false // ✅ Hide loading when done
                        showSnackbar = true  // ✅ Show success Snackbar
                    }
                },
                enabled = !isExporting, // ✅ Disable while exporting
                modifier = Modifier.fillMaxWidth().height(50.dp) // ✅ Ensure consistent height
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp) // ✅ Ensure proper size
                            .padding(4.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Export PDF")
                }
            }
        }
    }

    // ✅ Show Snackbar properly
    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar("PDF Exported Successfully!") // ✅ Display success message
            }
            showSnackbar = false // ✅ Reset snackbar trigger
        }
    }
}
