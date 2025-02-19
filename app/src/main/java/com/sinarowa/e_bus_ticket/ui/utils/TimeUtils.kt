package com.sinarowa.e_bus_ticket.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    /**
     * ✅ Returns the current timestamp formatted as "yyyy/MM/dd HH:mm:ss"
     */
    fun getFormattedTimestamp(): String {
        val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        format.timeZone = TimeZone.getDefault()
        return format.format(Date()) // Get current time
    }

    fun formatTimestamp(timestamp: Long): String {
        val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        format.timeZone = TimeZone.getDefault()
        return format.format(Date(timestamp))
    }
}
