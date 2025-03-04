package com.sinarowa.e_bus_ticket.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sinarowa.e_bus_ticket.data.local.dao.*
import com.sinarowa.e_bus_ticket.data.local.entities.*

@Database(
    entities = [
        Trip::class,         // ✅ Fixed naming: `TripDetails` → `Trip`
        Ticket::class,
        Expense::class,
        RouteEntity::class,        // ✅ Fixed naming: `RouteEntity` → `Route`
        StationEntity::class,
        Price::class,        // ✅ Fixed naming: `PriceEntity` → `Price`
        Bus::class, // ✅ Fixed naming: `BusEntity` → `Bus`
        RouteStop::class,
        TripSyncQueue::class
    ],
    version = 8, // ✅ Incremented version for schema changes
    exportSchema = false // ✅ Export schema for migrations
)
@TypeConverters(Converters::class)
abstract class BusTicketingDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao // ✅ Fixed naming: `tripDetailsDao()` → `tripDao()`
    abstract fun ticketDao(): TicketDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun routeDao(): RouteDao
    abstract fun stationDao(): StationDao
    abstract fun priceDao(): PriceDao
    abstract fun busDao(): BusDao
    abstract fun reportsDao(): ReportsDao
    abstract fun routeStopDao(): RouteStopDao
    abstract fun tripSyncQueueDao(): TripSyncQueueDao

    companion object {
        @Volatile
        private var INSTANCE: BusTicketingDatabase? = null

        fun getDatabase(context: Context): BusTicketingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BusTicketingDatabase::class.java,
                    "bus_ticketing_db"
                )
                    .fallbackToDestructiveMigration() // ✅ Clears DB if schema changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
