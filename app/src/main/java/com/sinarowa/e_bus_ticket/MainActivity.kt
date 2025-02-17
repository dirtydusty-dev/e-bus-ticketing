package com.sinarowa.e_bus_ticket

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.sinarowa.e_bus_ticket.ui.screens.*
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.sinarowa.e_bus_ticket.ui.bluetooth.BluetoothPrinterHelper
import com.sinarowa.e_bus_ticket.utils.BluetoothHelper
import com.sinarowa.e_bus_ticket.viewmodel.ExpensesViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val REQUEST_BLUETOOTH_PERMISSION = 1001 // Unique request code

    // ✅ Required permissions based on Android version
    private val BLUETOOTH_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT, // ✅ Needed for Android 12+
            Manifest.permission.ACCESS_FINE_LOCATION // ✅ Needed for discovering Bluetooth devices
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION // ✅ Needed for Bluetooth device discovery
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Request Bluetooth permissions only if necessary
        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions()
        } else {
            Log.d("BluetoothPermission", "✅ Bluetooth permissions already granted!")
        }

        setContent {
            val navController = rememberNavController()
            val tripViewModel: TripViewModel = hiltViewModel()
            val ticketViewModel: TicketViewModel = hiltViewModel()
            val expensesViewModel: ExpensesViewModel = hiltViewModel()

            // ✅ Initialize BluetoothHelper with context
            val bluetoothHelper = remember { BluetoothPrinterHelper(this) }

            NavHost(navController, startDestination = "home") {
                composable("home") { HomeScreen(tripViewModel, navController) }
                composable("createTrip") { CreateTripScreen(tripViewModel, navController) }
                composable("passengerTickets/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                    PassengerTicketingScreen(tripId, ticketViewModel, navController)  // ✅ Added
                }
                composable("bluetoothScreen") { BluetoothDevicesScreen(bluetoothHelper) }

                composable("tripDashboard/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""

                    // 🔥 Trigger fetching the trip when we navigate
                    LaunchedEffect(tripId) {
                        tripViewModel.loadTripById(tripId)
                    }

                    val trip by tripViewModel.selectedTrip.collectAsState()

                    trip?.let { TripDashboardScreen(it, navController, ticketViewModel) }
                }
                composable("expenses/{tripId}"){ backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                    LogExpensesScreen(tripId,navController,expensesViewModel)  // ✅ Added
                }

                composable("cancelTicket/{tripId}"){ backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                    CancelTicketScreen(navController,ticketViewModel,tripId)  // ✅ Added
                }

            }
        }

    }

    /**
     * ✅ Checks if Bluetooth permissions are granted
     */
    private fun hasBluetoothPermissions(): Boolean {
        return BLUETOOTH_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * ✅ Requests Bluetooth permissions only if they are not already granted
     */
    private fun requestBluetoothPermissions() {
        if (shouldShowRequestPermissionRationale(BLUETOOTH_PERMISSIONS[0])) {
            // 🔹 User previously denied the permission, show why it's needed
            Toast.makeText(
                this, "Bluetooth permission is required for printing tickets.", Toast.LENGTH_LONG
            ).show()
        }

        ActivityCompat.requestPermissions(
            this, BLUETOOTH_PERMISSIONS, REQUEST_BLUETOOTH_PERMISSION
        )
    }

    /**
     * ✅ Handles Bluetooth permission request results
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            val granted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (granted) {
                Log.d("BluetoothPermission", "✅ Bluetooth permissions granted!")
            } else {
                Log.e("BluetoothPermission", "❌ Bluetooth permissions denied!")

                // 🔹 Check if the user selected "Don't ask again"
                val shouldShowRationale = permissions.any {
                    ActivityCompat.shouldShowRequestPermissionRationale(this, it)
                }

                if (!shouldShowRationale) {
                    // 🔹 The user selected "Don't ask again," so guide them to settings
                    Toast.makeText(
                        this,
                        "Bluetooth permission is permanently denied. Please enable it in settings.",
                        Toast.LENGTH_LONG
                    ).show()

                    openAppSettings()
                }
            }
        }
    }

    /**
     * ✅ Opens App Settings if the user permanently denies permissions
     */
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}
