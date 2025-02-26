/*
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
fun BluetoothDevicesScreen(bluetoothHelper: BluetoothPrinterHelper, activity: Activity) {
    val devices by bluetoothHelper.pairedDevices.collectAsState()
    val isConnected by bluetoothHelper.isConnected.collectAsState()

    var selectedPrinter by remember { mutableStateOf<BluetoothDevice?>(null) }
    var isConnecting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!bluetoothHelper.hasBluetoothPermissions()) {
            bluetoothHelper.requestBluetoothPermissions(activity)
        } else {
            bluetoothHelper.updatePairedDevices()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Paired Bluetooth Printers", style = MaterialTheme.typography.h5.copy(color = Color(0xFF1565C0)))
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isConnected) "Connected" else "Not Connected",
            color = if (isConnected) Color.Green else Color.Red,
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(devices) { device ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { if (!isConnecting) selectedPrinter = device },
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = if (selectedPrinter == device) Color(0xFFFFEB3B) else Color(0xFF87CEEB)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = device.name ?: "Unknown Device", color = Color.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                selectedPrinter?.let { device ->
                    isConnecting = true
                    coroutineScope.launch {
                        val success = bluetoothHelper.connectToPrinter(device)
                        isConnecting = false
                        if (!success) selectedPrinter = null
                    }
                }
            },
            enabled = selectedPrinter != null && !isConnecting,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = if (selectedPrinter != null) Color(0xFFFFEB3B) else Color.Gray),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isConnecting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
            } else {
                Text("Connect to Printer", color = Color.Black, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { bluetoothHelper.disconnectPrinter() },
            enabled = isConnected,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = if (isConnected) Color.Red else Color.Gray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Disconnect", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
    }
}
*/
