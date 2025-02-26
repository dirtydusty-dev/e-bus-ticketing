package com.sinarowa.e_bus_ticket

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sinarowa.e_bus_ticket.data.repository.StationRepository
import com.sinarowa.e_bus_ticket.ui.screens.*
import com.sinarowa.e_bus_ticket.viewmodel.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var locationRepository: StationRepository

    private val REQUEST_CODE = 1001

    private val REQUIRED_PERMISSIONS by lazy {
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
        }.toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestAllPermissions()

        setContent {
            val navController = rememberNavController()
            val tripViewModel: CreateTripViewModel = hiltViewModel()
            val ticketViewModel: TicketingViewModel = hiltViewModel()
            //val bluetoothHelper by lazy { BluetoothPrinterHelper(this) }

            NavHost(navController, startDestination = "home") {
                composable("home") { HomeScreen(tripViewModel, navController) }
                composable("createTrip") { CreateTripScreen(tripViewModel, navController) }
                composable("passengerTickets/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                    PassengerTicketingScreen(tripId.toLong(), ticketViewModel)
                }
                composable("luggageTickets/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                    LuggageTicketingScreen(tripId.toLong(), ticketViewModel)
                }
                composable("tripDashboard/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId")?.toLongOrNull() ?: return@composable
                    LaunchedEffect(tripId) {
                        tripViewModel.loadTrip(tripId) // Ensure trip data is loaded
                    }

                    val tripWithRoute by tripViewModel.selectedTrip.collectAsState()

                    tripWithRoute?.let { trip ->
                        TripDashboardScreen(trip, navController, ticketViewModel, tripViewModel)
                    }
                }

            }
        }
    }

    private fun requestAllPermissions() {
        if (!hasAllPermissions()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE)
        }
    }

    private fun hasAllPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
