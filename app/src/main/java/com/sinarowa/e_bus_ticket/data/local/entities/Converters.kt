package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sinarowa.e_bus_ticket.data.local.enums.TicketStatus
import com.sinarowa.e_bus_ticket.data.local.enums.TripStatus

class Converters {

    private val gson = Gson()

    // ✅ Convert TicketStatus Enum
    @TypeConverter
    fun fromTicketStatus(value: TicketStatus): String = value.name

    @TypeConverter
    fun toTicketStatus(value: String): TicketStatus = enumValueOf(value)

    // ✅ Convert TripStatus Enum
    @TypeConverter
    fun fromTripStatus(value: TripStatus): String = value.name

    @TypeConverter
    fun toTripStatus(value: String): TripStatus = enumValueOf(value)

}
