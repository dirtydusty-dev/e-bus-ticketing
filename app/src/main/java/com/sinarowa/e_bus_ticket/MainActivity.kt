package com.sinarowa.e_bus_ticket

import android.Manifest
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
import dagger.hilt.android.AndroidEntryPoint
import com.sinarowa.e_bus_ticket.ui.screens.*
import com.sinarowa.e_bus_ticket.ui.viewmodel.LocationViewModel
import com.sinarowa.e_bus_ticket.viewmodel.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val locationViewModel: LocationViewModel by viewModels()
    private val REQUEST_LOCATION_PERMISSION = 2001
    private val REQUEST_BACKGROUND_LOCATION_PERMISSION = 2002
    private val REQUEST_BLUETOOTH_PERMISSION = 1001

    private val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val BACKGROUND_LOCATION_PERMISSION = arrayOf(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    private val BLUETOOTH_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Request foreground location permissions
        if (!hasLocationPermissions()) {
            requestLocationPermissions()
        } else {
            locationViewModel.startLocationTracking()
        }

        // ✅ Request background location permission (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasBackgroundLocationPermission()) {
            requestBackgroundLocationPermission()
        }

        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions()
        }

        setContent {
            val navController = rememberNavController()
            val tripViewModel: TripViewModel = hiltViewModel()
            val ticketViewModel: TicketViewModel = hiltViewModel()
            val expensesViewModel: ExpensesViewModel = hiltViewModel()

            NavHost(navController, startDestination = "home") {
                composable("home") { HomeScreen(tripViewModel, navController) }
                composable("salesreports") { TripSalesScreen(tripViewModel.tripSales.collectAsState().value, this@MainActivity) }
                composable("createTrip") { CreateTripScreen(tripViewModel, navController) }

                composable("reports/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                    ReportsScreen(tripId, tripViewModel,ticketViewModel)
                }

                composable("passengerTickets/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                    PassengerTicketingScreen(tripId, ticketViewModel)
                }
                composable("luggageTickets/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                    LuggageTicketingScreen(tripId, ticketViewModel)
                }
                composable("tripDashboard/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                    LaunchedEffect(tripId) {
                        tripViewModel.loadTripById(tripId)
                    }
                    val trip by tripViewModel.selectedTrip.collectAsState()
                    trip?.let { TripDashboardScreen(it, navController, ticketViewModel, tripViewModel) }
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

    override fun onDestroy() {
        super.onDestroy()
        locationViewModel.stopLocationTracking()
    }

    /** ✅ Check if location permissions are granted */
    private fun hasLocationPermissions(): Boolean {
        return LOCATION_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /** ✅ Check if background location permission is granted (Android 10+) */
    private fun hasBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /** ✅ Request foreground location permissions */
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSION)
    }

    /** ✅ Request background location permission */
    private fun requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(this, BACKGROUND_LOCATION_PERMISSION, REQUEST_BACKGROUND_LOCATION_PERMISSION)
    }

    /** ✅ Check if Bluetooth permissions are granted */
    private fun hasBluetoothPermissions(): Boolean {
        return BLUETOOTH_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /** ✅ Request Bluetooth permissions */
    private fun requestBluetoothPermissions() {
        if (shouldShowRequestPermissionRationale(BLUETOOTH_PERMISSIONS[0])) {
            Toast.makeText(this, "Bluetooth permission is required for printing tickets.", Toast.LENGTH_LONG).show()
        }
        ActivityCompat.requestPermissions(this, BLUETOOTH_PERMISSIONS, REQUEST_BLUETOOTH_PERMISSION)
    }

    /** ✅ Handle permission request results */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    locationViewModel.startLocationTracking()
                } else {
                    Toast.makeText(this, "Location permission denied. Enable it in settings.", Toast.LENGTH_LONG).show()
                    openAppSettings()
                }
            }
            REQUEST_BACKGROUND_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d("MainActivity", "✅ Background location permission granted")
                } else {
                    Toast.makeText(this, "Background location permission required for tracking.", Toast.LENGTH_LONG).show()
                    openAppSettings()
                }
            }
            REQUEST_BLUETOOTH_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d("MainActivity", "✅ Bluetooth permissions granted")
                } else {
                    Toast.makeText(this, "Bluetooth permission denied. Enable it in settings.", Toast.LENGTH_LONG).show()
                    openAppSettings()
                }
            }
        }
    }

    /** ✅ Open App Settings if permissions are denied */
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}
