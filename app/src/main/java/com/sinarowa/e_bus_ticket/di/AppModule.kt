package com.sinarowa.e_bus_ticket.di

import android.content.Context
import androidx.room.Room
import com.sinarowa.e_bus_ticket.data.local.BusTicketingDatabase
import com.sinarowa.e_bus_ticket.data.local.dao.*
import com.sinarowa.e_bus_ticket.data.repository.ReportsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ✅ Provide Database Instance
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BusTicketingDatabase {
        return Room.databaseBuilder(
            context,
            BusTicketingDatabase::class.java,
            "bus_ticketing_db"
        )//.fallbackToDestructiveMigration()
            .build()
    }

    // ✅ Provide All DAOs
    @Provides
    fun provideTripDao(database: BusTicketingDatabase): TripDao = database.tripDao()

    @Provides
    fun provideRouteDao(database: BusTicketingDatabase): RouteDao = database.routeDao()

    @Provides
    fun provideBusDao(database: BusTicketingDatabase): BusDao = database.busDao()

    @Provides
    fun provideExpenseDao(database: BusTicketingDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun providePriceDao(database: BusTicketingDatabase): PriceDao = database.priceDao()

    @Provides
    fun provideStationDao(database: BusTicketingDatabase): StationDao = database.stationDao()

    @Provides
    fun provideTicketDao(database: BusTicketingDatabase): TicketDao = database.ticketDao()

    @Provides
    fun provideReportsDao(database: BusTicketingDatabase): ReportsDao = database.reportsDao()

    // ✅ Provide ReportsRepository (FIXES YOUR ERROR)
    @Provides
    fun provideReportsRepository(reportsDao: ReportsDao): ReportsRepository {
        return ReportsRepository(reportsDao)
    }
}
