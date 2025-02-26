package com.sinarowa.e_bus_ticket.data.local.preferences

import android.content.Context
import android.content.SharedPreferences

class TripPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("trip_prefs", Context.MODE_PRIVATE)

    // ✅ Save Active Trip ID
    fun saveActiveTripId(tripId: Long) {
        sharedPreferences.edit().putLong("active_trip_id", tripId).apply()
    }

    // ✅ Get Active Trip ID
    fun getActiveTripId(): Long? {
        val tripId = sharedPreferences.getLong("active_trip_id", -1L)
        return if (tripId != -1L) tripId else null
    }

    // ✅ Clear Active Trip (When Trip Ends)
    fun clearActiveTrip() {
        sharedPreferences.edit().remove("active_trip_id").apply()
    }
}
