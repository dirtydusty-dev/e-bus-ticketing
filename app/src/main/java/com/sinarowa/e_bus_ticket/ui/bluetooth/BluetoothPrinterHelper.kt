package com.sinarowa.e_bus_ticket.ui.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.sinarowa.e_bus_ticket.R
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.experimental.or

class BluetoothPrinterHelper(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    /**
     * ✅ Checks if the app has Bluetooth permissions
     */
    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.BLUETOOTH_ADMIN
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestBluetoothPermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )

            activity.requestPermissions(permissions, 1001)
        }
    }

    /**
     * ✅ Check if a printer is currently connected
     */
    fun isPrinterConnected(): Boolean {
        return try {
            bluetoothSocket?.isConnected == true
        } catch (e: Exception) {
            Log.e("BluetoothPrinter", "❌ Error checking connection: ${e.message}")
            false
        }
    }

    /**
     * ✅ Returns a list of paired Bluetooth printers
     */
    fun getPairedPrinters(): List<BluetoothDevice> {
        return try {
            if (!hasBluetoothPermissions()) {
                Log.e("BluetoothPrinter", "❌ Missing Bluetooth permissions!")
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
                    bluetoothAdapter.cancelDiscovery()
                }

                val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

                bluetoothSocket?.close() // Ensure no existing connection
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream

                Log.d("BluetoothPrinter", "✅ Connected to printer: ${device.name}")
                return@withContext true
            } catch (e: SecurityException) {
                Log.e("BluetoothPrinter", "❌ SecurityException: ${e.message}")
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

            if (!isPrinterConnected()) {
                Log.e("BluetoothPrinter", "❌ Printer is not connected!")
                return
            }

            outputStream?.write("$text\n".toByteArray())
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
            bluetoothSocket = null
            Log.d("BluetoothPrinter", "✅ Connection closed")
        } catch (e: IOException) {
            Log.e("BluetoothPrinter", "❌ Error closing connection: ${e.message}")
        }
    }

    fun printTicketWithLogo(context: Context, ticket: Ticket) {
        try {
            if (!hasBluetoothPermissions()) {
                Log.e("BluetoothPrinter", "❌ Cannot print: Missing Bluetooth permissions!")
                return
            }

            // 1️⃣ Print Logo
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.converted_logo)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 384, bitmap.height * 384 / bitmap.width, false)
            val escPosData = convertBitmapToEscPos(resizedBitmap)
            outputStream?.write(escPosData)
            outputStream?.flush()

            // 2️⃣ Print Ticket Details with Formatting
            val ticketText = buildString {
                append("\n")
                append("\u001B\u0041\u0001")  // Center align
                append("\u001B\u0045\u0001")  // Bold text
                append("=====================\n")
                append("   GOVASBURG TICKET  \n")
                append("=====================\n")
                append("\u001B\u0045\u0000")  // Cancel bold
                append("\u001B\u0044\u0005\u0014\u0000") // Set tab stops
                append("From: ${ticket.fromStop}\n")
                append("To:   ${ticket.toStop}\n")
                append("Price: $${ticket.price}\n")
                append("Seat:  ${ticket.ticketId.takeLast(2)}\n")
                append("=====================\n")
                append("   Thank You!  \n\n")
                append("\u001D\u0056\u0001") // Cut paper
            }

            outputStream?.write(ticketText.toByteArray())
            outputStream?.flush()

            Log.d("BluetoothPrinter", "✅ Printed ticket with logo and formatted text")

        } catch (e: IOException) {
            Log.e("BluetoothPrinter", "❌ Error printing: ${e.message}")
        }
    }


    /**
     * ✅ Converts Bitmap to ESC/POS (Thermal Printer Format)
     */
    fun convertBitmapToEscPos(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val bytesPerRow = (width + 7) / 8  // Ensure width is divisible by 8
        val imageBytes = ByteArray(height * (bytesPerRow + 4))  // Extra bytes for ESC/POS format
        var byteIndex = 0

        for (y in 0 until height) {
            // ESC/POS commands for printing bit images
            imageBytes[byteIndex++] = 0x1D.toByte()
            imageBytes[byteIndex++] = 0x76.toByte()
            imageBytes[byteIndex++] = 0x30.toByte()
            imageBytes[byteIndex++] = 0x00.toByte() // Normal mode
            imageBytes[byteIndex++] = (width / 8).toByte()
            imageBytes[byteIndex++] = 0x00.toByte()  // Width in pixels (little-endian)
            imageBytes[byteIndex++] = height.toByte()
            imageBytes[byteIndex++] = 0x00.toByte()  // Height in pixels (little-endian)

            var slice = 0
            var bit = 7
            for (x in 0 until width) {
                val color = pixels[y * width + x]
                val grayscale = (color shr 16 and 0xFF) * 0.3 + (color shr 8 and 0xFF) * 0.59 + (color and 0xFF) * 0.11
                val isBlack = grayscale < 128  // Threshold for black/white

                if (isBlack) slice = slice or (1 shl bit)

                if (bit == 0) {
                    imageBytes[byteIndex++] = slice.toByte()
                    slice = 0
                    bit = 7
                } else {
                    bit--
                }
            }

            // Fill remaining bits if not a multiple of 8
            if (bit != 7) {
                imageBytes[byteIndex++] = slice.toByte()
            }
        }

        return imageBytes.copyOf(byteIndex) // Trim excess bytes
    }



}
