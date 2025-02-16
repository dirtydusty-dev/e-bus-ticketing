package com.sinarowa.e_bus_ticket.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sinarowa.e_bus_ticket.data.local.dao.*
import com.sinarowa.e_bus_ticket.data.local.entities.*

@Database(
    entities = [
        TripDetails::class,
        Ticket::class,
        Expense::class,
        RouteEntity::class,   // ✅ Added
        LocationEntity::class, // ✅ Added
        PriceEntity::class,   // ✅ Added
        BusEntity::class      // ✅ Added
    ],
    version = 2, // ✅ Increment version if you changed schema
    exportSchema = false
)
abstract class BusTicketingDatabase : RoomDatabase() {

    abstract fun tripDetailsDao(): TripDetailsDao
    abstract fun ticketDao(): TicketDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun routeDao(): RouteDao
    abstract fun locationDao(): LocationDao
    abstract fun priceDao(): PriceDao
    abstract fun busDao(): BusDao

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
