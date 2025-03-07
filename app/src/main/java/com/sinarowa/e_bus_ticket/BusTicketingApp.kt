package com.sinarowa.e_bus_ticket

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.sinarowa.e_bus_ticket.data.local.BusTicketingDatabase
import com.sinarowa.e_bus_ticket.data.repository.DatabaseSeeder
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class BusTicketingApp : Application(), Configuration.Provider
{

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree()) // Debug tree for logging in debug builds
        } else {
            Timber.plant(Timber.DebugTree()) // For simplicity, using DebugTree in production as well
        }

        // Database seeding if required
        CoroutineScope(Dispatchers.IO).launch {
            val db = BusTicketingDatabase.getDatabase(applicationContext)
            val sharedPrefs = applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val isDataSeeded = sharedPrefs.getBoolean("is_data_seeded", false)

            if (!isDataSeeded) {
                DatabaseSeeder.seedData(db.routeDao(), db.stationDao(), db.routeStopDao(), db.priceDao(), db.busDao())
                sharedPrefs.edit().putBoolean("is_data_seeded", true).apply()
            }
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.VERBOSE) // Enable verbose logging
            .build()
    }

}
