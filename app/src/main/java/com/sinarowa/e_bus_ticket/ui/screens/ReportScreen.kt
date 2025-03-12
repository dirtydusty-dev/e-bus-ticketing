package com.sinarowa.e_bus_ticket.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinarowa.e_bus_ticket.ui.theme.SkyBluePrimary
import com.sinarowa.e_bus_ticket.ui.theme.YellowSecondary
import com.sinarowa.e_bus_ticket.viewmodel.ReportViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    reportType: String,
    viewModel: ReportViewModel = hiltViewModel(),
    context: Context = LocalContext.current
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(reportType) {
        when (reportType) {
            "Daily" -> viewModel.loadDailySalesReport(context)
            "Trip" -> viewModel.loadTripReport(context)
            "Detailed" -> viewModel.loadDetailedReport(context)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (reportType) {
                            "Daily Sales" -> "Daily Sales Report"
                            "Trip" -> "Trip Report"
                            "Detailed" -> "Detailed Statistics Report"
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = YellowSecondary, modifier = Modifier.size(48.dp))
                }
            } else if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                // 📝 Scrollable Report Content (keeps buttons visible)
                Box(
                    modifier = Modifier
                        .weight(1f) // Makes sure content scrolls and doesn't push buttons offscreen
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    SelectionContainer {
                        LazyColumn {
                            item {
                                Text(
                                    text = state.formattedReport,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Black,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🖨️ Fixed Buttons at Bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* Handle OK */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "OK", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("OK", color = Color.White, fontSize = 16.sp)
                    }

                    Button(
                        onClick = { /* Handle Print */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = YellowSecondary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = "Print", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PRINT", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
