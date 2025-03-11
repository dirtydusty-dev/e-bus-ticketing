package com.sinarowa.e_bus_ticket.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinarowa.e_bus_ticket.viewmodel.ReportViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(reportType: String, viewModel: ReportViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        when (reportType) {
            "Daily" -> viewModel.loadDailySalesReport(context)
            "Trip" -> viewModel.loadTripReport(context)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("$reportType Report") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Blue)
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
                CircularProgressIndicator()
            } else if (state.errorMessage != null) {
                Text("Error: ${state.errorMessage}", color = Color.Red)
            } else {
                SelectionContainer {
                    Text(state.formattedReport, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
