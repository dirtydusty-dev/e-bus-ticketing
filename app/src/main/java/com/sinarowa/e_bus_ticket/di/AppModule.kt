package com.sinarowa.e_bus_ticket.di

import android.content.Context
import androidx.room.Room
import com.sinarowa.e_bus_ticket.data.local.BusTicketingDatabase
import com.sinarowa.e_bus_ticket.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BusTicketingDatabase {
        return Room.databaseBuilder(
            context,
            BusTicketingDatabase::class.java,
            "bus_ticketing_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideTripDao(database: BusTicketingDatabase): TripDetailsDao = database.tripDetailsDao()

    @Provides
    fun provideRouteDao(database: BusTicketingDatabase): RouteDao = database.routeDao()

    @Provides
    fun provideBusDao(database: BusTicketingDatabase): BusDao = database.busDao()

    @Provides
    fun provideExpenseDao(database: BusTicketingDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun providePriceDao(database: BusTicketingDatabase): PriceDao = database.priceDao()

    @Provides
    fun provideLocationDao(database: BusTicketingDatabase): LocationDao = database.locationDao()

    @Provides
    fun provideTicketDao(database: BusTicketingDatabase): TicketDao = database.ticketDao()

    @Provides
    fun provideTicketCounterDao(database: BusTicketingDatabase): TicketCounterDao = database.ticketCounterDao()
}
