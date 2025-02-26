package com.sinarowa.e_bus_ticket
import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.room.Room
import com.sinarowa.e_bus_ticket.data.local.BusTicketingDatabase
import com.sinarowa.e_bus_ticket.data.repository.DatabaseSeeder



@HiltAndroidApp
class BusTicketingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            // Use the singleton getDatabase function to access the database
            val db = BusTicketingDatabase.getDatabase(applicationContext)

            val sharedPrefs = applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val isDataSeeded = sharedPrefs.getBoolean("is_data_seeded", false)

            if (!isDataSeeded) {
                CoroutineScope(Dispatchers.IO).launch {
                    val db = BusTicketingDatabase.getDatabase(applicationContext)
                    DatabaseSeeder.seedData(db.routeDao(), db.priceDao(), db.stationDao(), db.busDao())
                    sharedPrefs.edit().putBoolean("is_data_seeded", true).apply()
                }
            }
        }
    }
}

