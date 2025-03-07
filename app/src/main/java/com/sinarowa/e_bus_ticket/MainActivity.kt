package com.sinarowa.e_bus_ticket

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import com.sinarowa.e_bus_ticket.ui.screens.CreateTripScreen
import com.sinarowa.e_bus_ticket.ui.screens.HomeScreen
import com.sinarowa.e_bus_ticket.ui.screens.TripDashboardScreen
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

    @Inject
    lateinit var workerScheduler: WorkerScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            }
        }

        // Step 4: Schedule the SyncTripWorker when the app starts
        Log.d("MainActivity", "Scheduling SyncTripWorker")
        workerScheduler.scheduleSyncTripWorker(applicationContext) // Use `this` to refer to the Activity context

    }

}
