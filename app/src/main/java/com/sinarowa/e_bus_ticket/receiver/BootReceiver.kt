package com.sinarowa.e_bus_ticket.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.sinarowa.e_bus_ticket.service.LocationService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device restarted, starting LocationService...")
            context?.let {
                val serviceIntent = Intent(it, LocationService::class.java)
                ContextCompat.startForegroundService(it, serviceIntent)
            }
        }
    }
}
