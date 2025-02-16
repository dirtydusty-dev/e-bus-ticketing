package com.sinarowa.e_bus_ticket
import android.app.Application
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
            val db = Room.databaseBuilder(applicationContext, BusTicketingDatabase::class.java, "bus_ticketing_db")
                .fallbackToDestructiveMigration()
                .build()

            DatabaseSeeder.seedData(db.routeDao(), db.priceDao(), db.locationDao(), db.busDao())  // ✅ Populate test data
        }
    }
}
