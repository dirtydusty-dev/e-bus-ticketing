package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sinarowa.e_bus_ticket.utils.TimeUtils.getFormattedTimestamp




@Entity(tableName = "trip_details")
data class TripDetails(
    @PrimaryKey val tripId: String,
    val routeId: String,      // ✅ Store only route ID
    val routeName: String,    // ✅ Store route name for display
    val creationTime: String = getFormattedTimestamp(),
    val busId: String,        // ✅ Store selected bus ID
    val busName: String,       // ✅ Store bus name for UI
    val isComplete: Int = 0,
    val endTripCompleteTime: String? = null
)
