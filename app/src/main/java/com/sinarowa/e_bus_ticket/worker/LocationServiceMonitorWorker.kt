package com.sinarowa.e_bus_ticket.worker

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sinarowa.e_bus_ticket.service.LocationService
import timber.log.Timber

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
            Timber.d("ℹ️ LocationService is already running")
        }
        return Result.success()
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }
}