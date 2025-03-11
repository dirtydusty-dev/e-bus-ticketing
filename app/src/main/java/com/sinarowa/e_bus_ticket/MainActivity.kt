package com.sinarowa.e_bus_ticket

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.sinarowa.e_bus_ticket.service.LocationService
import com.sinarowa.e_bus_ticket.ui.screens.CreateTripScreen
import com.sinarowa.e_bus_ticket.ui.screens.HomeScreen
import com.sinarowa.e_bus_ticket.ui.screens.LogExpenseScreen
import com.sinarowa.e_bus_ticket.ui.screens.PassengerTicketingScreen
import com.sinarowa.e_bus_ticket.ui.screens.ReportScreen
import com.sinarowa.e_bus_ticket.ui.screens.RequestPermissionsScreen
import com.sinarowa.e_bus_ticket.ui.screens.TripDashboardScreen
import com.sinarowa.e_bus_ticket.viewmodel.PermissionViewModel
import com.sinarowa.e_bus_ticket.viewmodel.TicketViewModel
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel
import com.sinarowa.e_bus_ticket.worker.WorkerScheduler
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
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
        Timber.plant(Timber.DebugTree())
        checkAndRequestPermissions()

        permissionViewModel.permissionsGranted.observe(this) { granted ->
            if (granted) {
                startLocationTracking()
                scheduleServiceMonitor()
            } else {
                Timber.w("⚠️ Permissions or location services not fully granted")
            }
        }

        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = if (permissionViewModel.arePermissionsGranted()) "home" else "requestPermissions") {
                composable("requestPermissions") { RequestPermissionsScreen(permissionViewModel, navController) }
                composable("home") { HomeScreen(tripViewModel, navController) }
                composable("createTrip") { CreateTripScreen(tripViewModel, navController) }
                composable("tripDashboard") { TripDashboardScreen(navController, tripViewModel) }
                composable("passenger_ticketing/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId")
                    val state by tripViewModel.state.collectAsState()
                    val activeTrip = state.activeTrip ?: throw IllegalStateException("No active trip found for ID: $tripId")
                    PassengerTicketingScreen(activeTrip = activeTrip)
                }
                composable("expenses/{tripId}") { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                    LogExpenseScreen(navController = navController, tripId = tripId)
                }
                composable("reports/{reportType}") { backStackEntry ->
                    val reportType = backStackEntry.arguments?.getString("reportType") ?: "Daily"
                    ReportScreen(reportType = reportType) // ✅ Pass tripId properly
                }




            }
        }

        workerScheduler.scheduleSyncTripWorker(applicationContext)
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (neededPermissions.isEmpty() && isLocationEnabled()) {
            permissionViewModel.updatePermissionsGranted(true)
        } else {
            permissionLauncher.launch(neededPermissions.toTypedArray())
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it } && isLocationEnabled()
        permissionViewModel.updatePermissionsGranted(allGranted)
        if (!allGranted) {
            Timber.w("⚠️ Some permissions denied or location services off: $permissions")
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!enabled) Timber.w("❌ Location services disabled")
        return enabled
    }

    private fun startLocationTracking() {
        if (!isLocationEnabled()) {
            Timber.w("❌ Location services disabled, aborting start")
            permissionViewModel.updatePermissionsGranted(false)
            return
        }
        val serviceIntent = Intent(this, LocationService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
                Timber.d("✅ LocationService started as foreground service")
            } else {
                startService(serviceIntent)
                Timber.d("✅ LocationService started")
            }
        } catch (e: Exception) {
            Timber.e("❌ Failed to start LocationService: ${e.message}")
        }
    }

    private fun scheduleServiceMonitor() {
        val workRequest = PeriodicWorkRequestBuilder<LocationServiceMonitorWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "LocationServiceMonitor",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        Timber.d("✅ Scheduled LocationServiceMonitorWorker")
    }
}

class LocationServiceMonitorWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result {
        val serviceIntent = Intent(applicationContext, LocationService::class.java)
        if (!isServiceRunning(LocationService::class.java)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(serviceIntent)
            } else {
                applicationContext.startService(serviceIntent)
            }
            Timber.d("✅ Restarted LocationService")
        } else {
            Timber.d("ℹ️ LocationService already running")
        }
        return Result.success()
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }
}