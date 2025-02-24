//package com.sinarowa.e_bus_ticket.ui.screens
//
//import android.content.Context
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
//import com.sinarowa.e_bus_ticket.utils.PdfUtils
//import kotlinx.coroutines.launch
//
//@Composable
//fun TicketSalesScreen(ticketViewModel: TicketViewModel, context: Context, tripId: String) {
//    val stationSales = remember { mutableStateOf(mutableMapOf<String, Pair<Int, Double>>()) }
//    val breakdown = remember { mutableStateOf(mutableMapOf<Pair<String, String>, Pair<Int, Double>>()) }
//    var isExporting by remember { mutableStateOf(false) } // ✅ Track export state
//    var showSnackbar by remember { mutableStateOf(false) } // ✅ Track snackbar visibility
//    val scaffoldState = rememberScaffoldState()
//    val coroutineScope = rememberCoroutineScope()
//
//    // ✅ Fetch data when screen is loaded
//    LaunchedEffect(tripId) {
//        val salesData = ticketViewModel.getStationSales(tripId)
//        val breakdownData = ticketViewModel.getTicketBreakdown(tripId)
//
//        // ✅ Update maps correctly
//        stationSales.value = salesData.toMutableMap()
//        breakdown.value = breakdownData.toMutableMap()
//    }
//
//    Scaffold(
//        scaffoldState = scaffoldState,
//        topBar = {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 16.dp), // ✅ Moves Snackbar down slightly from the absolute top
//                contentAlignment = Alignment.TopCenter
//            ) {
//                SnackbarHost(scaffoldState.snackbarHostState)
//            }
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .padding(16.dp)
//        ) {
//            Text(text = "Ticket Sales Report", style = MaterialTheme.typography.h5)
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // **Station Sales Summary**
//            Text(text = "Station Sales", style = MaterialTheme.typography.h6)
//            TableHeader(listOf("Station", "Count", "Amount"))
//
//            LazyColumn {
//                items(stationSales.value.entries.toList()) { (station, data) ->
//                    TableRow(listOf(station, data.first.toString(), "$${data.second}"))
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // **Breakdown Section**
//            Text(text = "Breakdown", style = MaterialTheme.typography.h6)
//            TableHeader(listOf("Start", "Destination", "Count", "Amount"))
//
//            LazyColumn {
//                items(breakdown.value.entries.toList()) { (route, data) ->
//                    TableRow(listOf(route.first, route.second, data.first.toString(), "$${data.second}"))
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // ✅ Print Report Button with Loading Indicator & Snackbar
//            Button(
//                onClick = {
//                    isExporting = true // ✅ Show loading
//                    coroutineScope.launch {
//                        PdfUtils.generateTicketSalesPdf(context, stationSales.value, breakdown.value)
//                        isExporting = false // ✅ Hide loading when done
//                        showSnackbar = true  // ✅ Show success Snackbar
//                    }
//                },
//                enabled = !isExporting, // ✅ Disable while exporting
//                modifier = Modifier.fillMaxWidth().height(50.dp) // ✅ Set button height
//            ) {
//                if (isExporting) {
//                    CircularProgressIndicator(
//                        modifier = Modifier
//                            .size(24.dp) // ✅ Ensure proper size
//                            .padding(4.dp),
//                        color = Color.White,
//                        strokeWidth = 2.dp
//                    )
//                } else {
//                    Text("Print Report")
//                }
//            }
//        }
//    }
//
//    // ✅ Show Snackbar properly
//    LaunchedEffect(showSnackbar) {
//        if (showSnackbar) {
//            coroutineScope.launch {
//                scaffoldState.snackbarHostState.showSnackbar("Report Exported Successfully!") // ✅ Display success message
//            }
//            showSnackbar = false // ✅ Reset snackbar trigger
//        }
//    }
//}
//
//@Composable
//fun TableHeader(columns: List<String>) {
//    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
//        columns.forEach { columnName ->
//            Text(
//                text = columnName,
//                modifier = Modifier
//                    .weight(1f) // Ensures equal column spacing
//                    .padding(4.dp),
//                style = MaterialTheme.typography.body1.copy(color = Color.Black)
//            )
//        }
//    }
//}
//
//@Composable
//fun TableRow(values: List<String>) {
//    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
//        values.forEach { value ->
//            Text(
//                text = value,
//                modifier = Modifier
//                    .weight(1f) // Ensures equal column spacing
//                    .padding(4.dp),
//                style = MaterialTheme.typography.body2
//            )
//        }
//    }
//}
