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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.sinarowa.e_bus_ticket.R
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.entities.TripDetails
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


    // ✅ Converts PNG/JPG to a **black-and-white** ESC/POS compatible Bitmap
    fun convertImageToEscPosBitmap(context: Context, imageResId: Int): Bitmap {
        val originalBitmap = BitmapFactory.decodeResource(context.resources, imageResId)

        // Convert to pure black & white using dithering
        val bwBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bwBitmap)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) }) // Grayscale
        canvas.drawBitmap(originalBitmap, 0f, 0f, paint)

        // Apply threshold to make it purely black & white (no grayscale)
        for (y in 0 until bwBitmap.height) {
            for (x in 0 until bwBitmap.width) {
                val pixel = bwBitmap.getPixel(x, y)
                val grayscale = Color.red(pixel) // Since grayscale, R = G = B
                val bwColor = if (grayscale < 128) Color.BLACK else Color.WHITE
                bwBitmap.setPixel(x, y, bwColor)
            }
        }

        return bwBitmap
    }

    // ✅ Converts a **black-and-white bitmap** to ESC/POS format
    fun convertBitmapToEscPos(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val bytesPerRow = (width + 7) / 8  // Each byte stores 8 pixels
        val escPosData = ByteArrayOutputStream()

        // ✅ ESC/POS Command: Start Image Printing
        escPosData.write(byteArrayOf(0x1D, 0x76, 0x30, 0x00)) // Image mode
        escPosData.write(byteArrayOf((width / 8).toByte(), 0x00, height.toByte(), 0x00)) // Image dimensions

        for (y in 0 until height) {
            var rowByte = 0
            var bit = 7
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val isBlack = (pixel and 0xFF) < 128 // Convert to black & white threshold

                if (isBlack) rowByte = rowByte or (1 shl bit)

                if (bit == 0) {
                    escPosData.write(rowByte) // ✅ Write a single processed row byte
                    rowByte = 0
                    bit = 7
                } else {
                    bit--
                }
            }
            // ✅ Ensure width is a multiple of 8 (fill remaining bits)
            if (bit != 7) {
                escPosData.write(rowByte)
            }
        }

        return escPosData.toByteArray() // ✅ Convert final data to a valid ByteArray
    }


    fun printReport(context: Context, report: String){

        try {
            if (!hasBluetoothPermissions()) {
                Log.e("BluetoothPrinter", "❌ Cannot print: Missing Bluetooth permissions!")
                return
            }

            if (!isPrinterConnected()) {
                Log.e("BluetoothPrinter", "❌ Printer is not connected!")
                return
            }

            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.converted_logo)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 384, bitmap.height * 384 / bitmap.width, false)
            val escPosData = convertBitmapToEscPos(resizedBitmap)

            // ✅ Send Image Data to Printer
            outputStream?.write(escPosData) // ✅ Properly sends ByteArray
            outputStream?.flush()

            val formattedReport = buildString {
                append("\n")
                append("\n")
                append(report) // ✅ Print the full formatted report text
                append("\n")
                append("\n")
                append("\u001D\u0056\u0001") // Cut paper
            }

            outputStream?.write(formattedReport.toByteArray())
            outputStream?.flush()

        } catch (e: IOException) {
            Log.e("BluetoothPrinter", "❌ Error printing: ${e.message}")
        }

    }



    // ✅ Prints a Ticket with a **Logo Image** + Text
    fun printTicketWithLogo(context: Context, ticket: Ticket, tripDetails: TripDetails) {
        try {
            if (!hasBluetoothPermissions()) {
                Log.e("BluetoothPrinter", "❌ Cannot print: Missing Bluetooth permissions!")
                return
            }

            if (!isPrinterConnected()) {
                Log.e("BluetoothPrinter", "❌ Printer is not connected!")
                return
            }

            // ✅ Convert Logo Image to ESC/POS Format
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.converted_logo)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 384, bitmap.height * 384 / bitmap.width, false)
            val escPosData = convertBitmapToEscPos(resizedBitmap)

            // ✅ Send Image Data to Printer
            outputStream?.write(escPosData) // ✅ Properly sends ByteArray
            outputStream?.flush()

            // ✅ Print Ticket Details
            val ticketText = buildString {
                append("\n")
                append("\u001B\u0041\u0001")  // Center align
                append("\u001B\u0045\u0001")  // Bold text
                append("=====================\n")
                append("   GOVASBURG TICKET  \n")
                append("Manager +263772701350\n")
                append("${tripDetails.creationTime} \n")
                append("=====================\n")
                append("\u001B\u0045\u0000")  // Cancel bold
                append("From: ${ticket.fromStop}\n")
                append("To:   ${ticket.toStop}\n")
                append("Type:   ${ticket.ticketType}\n")
                append("Price: $${ticket.price}\n")
                append("\n")
                append("\n")
                append("${ticket.ticketId}\n")
                append("=====================\n")
                append("   Thank You!  \n\n")
                append("\n")
                append("=====================\n")
                append("\u001D\u0056\u0001") // Cut paper
            }

            outputStream?.write(ticketText.toByteArray())
            outputStream?.flush()

            Log.d("BluetoothPrinter", "✅ Printed ticket with logo and formatted text")

        } catch (e: IOException) {
            Log.e("BluetoothPrinter", "❌ Error printing: ${e.message}")
        }
    }

}
