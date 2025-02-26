package com.sinarowa.e_bus_ticket.data.local.entities

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sinarowa.e_bus_ticket.data.local.enums.TicketStatus
import com.sinarowa.e_bus_ticket.data.local.enums.TripStatus

class Converters {

    private val gson = Gson()


    @TypeConverter
    fun fromTrip(trip: Trip?): String? {
        return gson.toJson(trip)
    }

    @TypeConverter
    fun toTrip(tripJson: String?): Trip? {
        return gson.fromJson(tripJson, object : TypeToken<Trip>() {}.type)
    }

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

    // ✅ Convert List<String> using Gson
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        return if (value.isNullOrEmpty()) emptyList()
        else gson.fromJson<List<String>>(value, object : TypeToken<List<String>>() {}.type)
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String {
        return if (list.isNullOrEmpty()) "[]" else gson.toJson(list)
    }
}
