package com.sinarowa.e_bus_ticket.ui.screens

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sinarowa.e_bus_ticket.ui.bluetooth.BluetoothPrinterHelper
import kotlinx.coroutines.launch

@Composable
fun BluetoothDevicesScreen(bluetoothHelper: BluetoothPrinterHelper) {
    val devices = remember { mutableStateListOf<BluetoothDevice>() }
    val selectedPrinter = remember { mutableStateOf<BluetoothDevice?>(null) }
    var isConnected by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        devices.addAll(bluetoothHelper.getPairedPrinters())
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Paired Bluetooth Printers", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))

        if (devices.isEmpty()) {
            Text("No paired printers found.")
        } else {
            LazyColumn {
                items(devices) { device ->
                    Text(
                        text = "${device.name} - ${device.address}",
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                selectedPrinter.value = device
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val coroutineScope = rememberCoroutineScope() // ✅ Create coroutine scope

        Button(
            onClick = {
                selectedPrinter.value?.let { device ->
                    coroutineScope.launch {
                        isConnected = bluetoothHelper.connectToPrinter(device) // ✅ Now runs in a coroutine
                    }
                }
            },
            enabled = selectedPrinter.value != null
        ) {
            Text("Connect to Printer")
        }

        if (isConnected) {
            Button(
                onClick = {
                    bluetoothHelper.printText("Hello, Bluetooth Printer!")
                }
            ) {
                Text("Print Test Ticket")
            }
        }
    }
}
