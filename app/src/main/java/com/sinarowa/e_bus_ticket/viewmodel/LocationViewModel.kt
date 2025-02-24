//package com.sinarowa.e_bus_ticket.ui.viewmodel
//
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import androidx.lifecycle.ViewModel
//import com.sinarowa.e_bus_ticket.service.LocationService
//import dagger.hilt.android.lifecycle.HiltViewModel
//import dagger.hilt.android.qualifiers.ApplicationContext
//import javax.inject.Inject
//
//@HiltViewModel
//class LocationViewModel @Inject constructor(
//    @ApplicationContext private val context: Context
//) : ViewModel() {
//
//    fun startLocationTracking() {
//        val intent = Intent(context, LocationService::class.java)
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            // ✅ For Android 8.0+ (API 26+), use startForegroundService()
//            context.startForegroundService(intent)
//        } else {
//            // ✅ For Android 7.1 (API 25) and below, use startService()
//            context.startService(intent)
//        }
//    }
//
//
//    fun stopLocationTracking() {
//        val intent = Intent(context, LocationService::class.java)
//        context.stopService(intent)
//    }
//}
