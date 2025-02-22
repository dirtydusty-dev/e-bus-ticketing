package com.sinarowa.e_bus_ticket.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.sinarowa.e_bus_ticket.ui.bluetooth.BluetoothPrinterHelper
import com.sinarowa.e_bus_ticket.utils.PdfUtils
import com.sinarowa.e_bus_ticket.utils.ReportUtils
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun ReportsScreen(tripId: String, tripViewModel: TripViewModel, ticketViewModel: TicketViewModel, bluetoothPrinterHelper: BluetoothPrinterHelper) {
    var selectedReport by remember { mutableStateOf("Trip Sales") }
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    // ✅ Fetch Trip Report by ID
    LaunchedEffect(tripId) {
        tripViewModel.fetchTripSalesById(tripId)
    }

    val tripSales by tripViewModel.tripSales.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp) // ✅ Smaller padding for a compact look
    ) {
        // ✅ Dropdown to select report type
        DropdownMenuComponent(
            label = "Select Report",
            items = listOf("Tickets", "Trip Sales"),
            selectedItem = selectedReport
        ) { newSelection ->
            selectedReport = newSelection
        }

        Spacer(modifier = Modifier.height(8.dp))

        val tripReport by tripViewModel.tripReport.collectAsState()
        val generatedReport = when (selectedReport) {
            "Trip Sales" -> tripReport?.let { tripViewModel.generateTripSalesReport(context,tripId) } ?: "Generating Report..."
            "Tickets" -> ReportUtils.generateTicketSalesReport(
                companyName = "ABC Transport",
                date = "2025-02-21",
                time = "15:30",
                deviceId = "123456",
                totalTickets = 55,
                stationSummary = mapOf(
                    "Harare" to Pair(30, 500.0),
                    "Bulawayo" to Pair(15, 350.0),
                    "Gweru" to Pair(10, 200.0)
                ),
                routeBreakdown = listOf(
                    Triple("Harare", "Bulawayo", Pair(25, 400.0)),
                    Triple("Harare", "Gweru", Pair(15, 250.0)),
                    Triple("Gweru", "Bulawayo", Pair(15, 300.0))
                ),
                totalSales = 950.0,
                expenses = mapOf("Fuel" to 220.0, "Staff" to 180.0)
            )
            else -> "No Report Available"
        }

        // ✅ Scrollable Report View
        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color.LightGray)
                .padding(8.dp)
        ) {
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(4.dp) // ✅ Smaller padding
                ) {
                    Text(
                        text = generatedReport,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ✅ Buttons for Print and Export
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                coroutineScope.launch {
                    generatedReport.let { reportText ->
                        bluetoothPrinterHelper.printReport(context, reportText) // ✅ Print the correct report
                    }
                }
            }) {
                Text("Print")
            }

            Button(onClick = {
                coroutineScope.launch {
                    generatedReport.let { reportText ->
                        PdfUtils.saveReportToFile(context, reportText) // ✅ Print the correct report
                    }
                }
            }) {
                Text("Export")
            }
        }
    }
}
