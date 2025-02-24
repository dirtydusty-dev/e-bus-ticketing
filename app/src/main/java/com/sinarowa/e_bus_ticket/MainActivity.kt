package com.sinarowa.e_bus_ticket

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sinarowa.e_bus_ticket.data.repository.LocationRepository
import com.sinarowa.e_bus_ticket.ui.bluetooth.BluetoothPrinterHelper
import dagger.hilt.android.AndroidEntryPoint
import com.sinarowa.e_bus_ticket.ui.screens.*
//import com.sinarowa.e_bus_ticket.ui.viewmodel.LocationViewModel
import com.sinarowa.e_bus_ticket.viewmodel.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    @Inject
    lateinit var locationRepository: LocationRepository

    private val REQUEST_CODE = 1001

    private val ALL_PERMISSIONS by lazy {
        mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                addAll(
                    listOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Request all necessary permissions at startup
        requestAllPermissions()

        setContent {
            val navController = rememberNavController()
            val tripViewModel: TripViewModel = hiltViewModel()
            val ticketViewModel: TicketViewModel = hiltViewModel()
            val expensesViewModel: ExpensesViewModel = hiltViewModel()
            val bluetoothHelper by lazy { BluetoothPrinterHelper(this) }

            NavHost(navController, startDestination = "home") {
                composable("home") { HomeScreen(tripViewModel, navController) }
                //composable("salesreports") { TripSalesScreen(tripViewModel.tripSales.collectAsState().value, this@MainActivity) }
                composable("createTrip") { CreateTripScreen(tripViewModel, navController, locationRepository) }

                composable("bluetoothScreen") {
                    BluetoothDevicesScreen(bluetoothHelper, this@MainActivity) // ✅ Pass Bluetooth Helper & Activity
                }

                composable("reports/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                    ReportsScreen(tripId, tripViewModel,ticketViewModel,bluetoothHelper)
                }

                composable("passengerTickets/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                    PassengerTicketingScreen(tripId, ticketViewModel,tripViewModel, bluetoothHelper)
                }
                composable("luggageTickets/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                    LuggageTicketingScreen(tripId, ticketViewModel,tripViewModel, bluetoothHelper)
                }
                composable("tripDashboard/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                    LaunchedEffect(tripId) {
                        tripViewModel.loadTripById(tripId)
                    }
                    val trip by tripViewModel.selectedTrip.collectAsState()
                    trip?.let { TripDashboardScreen(it, navController, ticketViewModel, tripViewModel,locationRepository) }
                }
                composable("expenses/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                    LogExpensesScreen(tripId, navController, expensesViewModel, ticketViewModel)
                }
                composable("cancelTicket/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                    CancelTicketScreen(navController, ticketViewModel, tripId)
                }
            }
        }
    }

    /** ✅ Request all required permissions */
    private fun requestAllPermissions() {
        if (!hasAllPermissions()) {
            ActivityCompat.requestPermissions(this, ALL_PERMISSIONS, REQUEST_CODE)
        }
    }

    /** ✅ Check if all required permissions are granted */
    private fun hasAllPermissions(): Boolean {
        return ALL_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /** ✅ Handle permission results */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            val deniedPermissions = permissions.zip(grantResults.toList())
                .filter { it.second != PackageManager.PERMISSION_GRANTED }
                .map { it.first }

            if (deniedPermissions.isNotEmpty()) {
                Log.w("PERMISSIONS", "⚠️ The following permissions were denied: $deniedPermissions")
                showSettingsRedirectDialog("All permissions are required for full app functionality.")
            } else {
                Log.d("PERMISSIONS", "✅ All permissions granted!")
            }
        }
    }

    /** ✅ Show dialog to guide user to settings if permissions are denied */
    private fun showSettingsRedirectDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("$message\n\nPlease enable it in App Settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}