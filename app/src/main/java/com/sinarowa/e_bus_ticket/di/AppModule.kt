package com.sinarowa.e_bus_ticket.di

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.room.Room
import androidx.work.WorkManager
import com.sinarowa.e_bus_ticket.data.local.BusTicketingDatabase
import com.sinarowa.e_bus_ticket.data.local.dao.*
import com.sinarowa.e_bus_ticket.worker.SyncTripWorkerFactory
import com.sinarowa.e_bus_ticket.worker.WorkerScheduler
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
        ).build()
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

    @Provides
    fun provideRouteStopDao(database: BusTicketingDatabase): RouteStopDao = database.routeStopDao()

    @Provides
    fun provideTripSyncQueueDao(database: BusTicketingDatabase): TripSyncQueueDao = database.tripSyncQueueDao()


    @Provides
    @Singleton
    fun provideContext(app: Application): Context = app.applicationContext

    @Provides
    fun provideWorkManager(context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    fun provideWorkerScheduler(workManager: WorkManager): WorkerScheduler {
        return WorkerScheduler(workManager)
    }

    @Provides
    @Singleton
    fun provideSyncTripWorkerFactory(factory: HiltWorkerFactory): SyncTripWorkerFactory {
        return SyncTripWorkerFactory(factory)
    }


}
