package com.sinarowa.e_bus_ticket.ui.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.*

class BluetoothPrinterHelper(private val context: Context) { // ✅ Pass Context to check permissions

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    /**
     * ✅ Checks if the app has Bluetooth permissions
     */
    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else { // Below Android 12
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.BLUETOOTH_ADMIN
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * ✅ Returns a list of paired Bluetooth printers
     */
    fun getPairedPrinters(): List<BluetoothDevice> {
        return try {
            if (!hasBluetoothPermissions()) {
                Log.e("BluetoothPrinter", "❌ Missing Bluetooth permissions! Request permissions in MainActivity.")
                return emptyList()
            }

            val pairedDevices = bluetoothAdapter?.bondedDevices?.toList().orEmpty()

            if (pairedDevices.isEmpty()) {
                Log.w("BluetoothPrinter", "⚠️ No paired Bluetooth devices found.")
            } else {
                Log.d("BluetoothPrinter", "✅ Found ${pairedDevices.size} paired printers.")
            }

            pairedDevices
        } catch (e: SecurityException) {
            Log.e("BluetoothPrinter", "❌ SecurityException: ${e.message}")
            emptyList()
        }
    }

    /**
     * ✅ Connects to a Bluetooth printer in a coroutine (Non-Blocking)
     */
    suspend fun connectToPrinter(device: BluetoothDevice): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!hasBluetoothPermissions()) {
                    Log.e("BluetoothPrinter", "❌ Cannot connect: Missing Bluetooth permissions!")
                    return@withContext false
                }

                if (bluetoothAdapter?.isDiscovering == true) {
                    bluetoothAdapter.cancelDiscovery() // ✅ Cancel discovery before connection
                }

                val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // ✅ Standard UUID for Bluetooth printers

                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream

                Log.d("BluetoothPrinter", "✅ Connected to printer: ${device.name}")
                return@withContext true
            } catch (e: SecurityException) {
                Log.e("BluetoothPrinter", "❌ SecurityException: ${e.message}. Check permissions!")
                return@withContext false
            } catch (e: IOException) {
                Log.e("BluetoothPrinter", "❌ Connection failed: ${e.message}")
                return@withContext false
            }
        }
    }

    /**
     * ✅ Sends text to the printer
     */
    fun printText(text: String) {
        try {
            if (!hasBluetoothPermissions()) {
                Log.e("BluetoothPrinter", "❌ Cannot print: Missing Bluetooth permissions!")
                return
            }

            outputStream?.write("$text\n".toByteArray()) // ✅ Appends newline for some printers
            outputStream?.flush()
            Log.d("BluetoothPrinter", "✅ Printed: $text")
        } catch (e: IOException) {
            Log.e("BluetoothPrinter", "❌ Error printing: ${e.message}")
        }
    }

    /**
     * ✅ Closes the printer connection
     */
    fun closeConnection() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
            Log.d("BluetoothPrinter", "✅ Connection closed")
        } catch (e: IOException) {
            Log.e("BluetoothPrinter", "❌ Error closing connection: ${e.message}")
        }
    }
}
