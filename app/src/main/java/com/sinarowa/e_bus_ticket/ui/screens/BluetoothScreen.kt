package com.sinarowa.e_bus_ticket.ui.screens

import android.app.Activity
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sinarowa.e_bus_ticket.ui.bluetooth.BluetoothPrinterHelper
import kotlinx.coroutines.launch

@Composable
fun BluetoothDevicesScreen(
    bluetoothHelper: BluetoothPrinterHelper,
    activity: Activity
) {
    val devices = remember { mutableStateListOf<BluetoothDevice>() }
    var selectedPrinter by remember { mutableStateOf<BluetoothDevice?>(null) }
    var isConnected by remember { mutableStateOf(bluetoothHelper.isPrinterConnected()) } // ✅ Use mutableStateOf
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!bluetoothHelper.hasBluetoothPermissions()) {
            bluetoothHelper.requestBluetoothPermissions(activity)
        } else {
            devices.clear()
            devices.addAll(bluetoothHelper.getPairedPrinters())
            isConnected = bluetoothHelper.isPrinterConnected()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Paired Bluetooth Printers",
            style = MaterialTheme.typography.h5.copy(color = Color(0xFF1565C0))
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isConnected) "Connected" else "Not Connected",
            color = if (isConnected) Color.Green else Color.Red,
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (devices.isEmpty()) {
            Text("No paired printers found.", color = Color.Gray)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(devices) { device ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                selectedPrinter = device
                            },
                        elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = if (selectedPrinter == device) Color(0xFFFFEB3B) else Color(0xFF87CEEB)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = device.name ?: "Unknown Device",
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                selectedPrinter?.let { device ->
                    coroutineScope.launch {
                        val success = bluetoothHelper.connectToPrinter(device)
                        if (success) {
                            isConnected = true // ✅ Properly update state
                        }
                    }
                }
            },
            enabled = selectedPrinter != null,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFEB3B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Connect to Printer", color = Color.Black)
        }
    }
}
