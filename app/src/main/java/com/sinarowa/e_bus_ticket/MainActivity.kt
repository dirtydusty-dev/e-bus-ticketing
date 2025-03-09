package com.sinarowa.e_bus_ticket

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.sinarowa.e_bus_ticket.service.LocationService
import com.sinarowa.e_bus_ticket.ui.screens.CreateTripScreen
import com.sinarowa.e_bus_ticket.ui.screens.HomeScreen
import com.sinarowa.e_bus_ticket.ui.screens.PassengerTicketingScreen
import com.sinarowa.e_bus_ticket.ui.screens.RequestPermissionsScreen
import com.sinarowa.e_bus_ticket.ui.screens.TripDashboardScreen
import com.sinarowa.e_bus_ticket.viewmodel.PermissionViewModel
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel
import com.sinarowa.e_bus_ticket.worker.WorkerScheduler
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val tripViewModel: TripViewModel by viewModels()
    private val ticketViewModel: TicketViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()

    @Inject
    lateinit var workerScheduler: WorkerScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions()

        Log.d("TEST", "Log is working fine!")

        setContent {
            val navController = rememberNavController()

            NavHost(navController, startDestination = "home") {
                composable("home") { HomeScreen(tripViewModel, navController) }
                composable("createTrip") { CreateTripScreen(tripViewModel, navController) }

                // Add the TripDashboardScreen route without the 'tripId'
                composable("tripDashboard") {
                    TripDashboardScreen(navController = navController, tripViewModel = tripViewModel)
                }

                composable("passenger_ticketing") {
                    PassengerTicketingScreen(ticketViewModel = ticketViewModel, navController = navController)
                }
            }
        }

        // Step 4: Schedule the SyncTripWorker when the app starts
        Log.d("MainActivity", "Scheduling SyncTripWorker")
        workerScheduler.scheduleSyncTripWorker(applicationContext) // Use `this` to refer to the Activity context

    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Android 13+ needs extra permission for notifications (for foreground service)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (neededPermissions.isNotEmpty()) {
            permissionLauncher.launch(neededPermissions.toTypedArray())
        } else {
            startLocationTracking() // ✅ Start location service if already granted
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            startLocationTracking() // ✅ If granted, start tracking
        }
    }

    private fun startLocationTracking() {
        val serviceIntent = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("MainActivity", "Android 8+ detected. Using startForegroundService()...")
            startForegroundService(serviceIntent)
        } else {
            Log.d("MainActivity", "Android 7 or lower detected. Using startService()...")
            startService(serviceIntent)
        }
    }

}
