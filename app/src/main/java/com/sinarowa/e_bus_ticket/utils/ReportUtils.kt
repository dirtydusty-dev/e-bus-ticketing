package com.sinarowa.e_bus_ticket.utils

import android.util.Log
import com.sinarowa.e_bus_ticket.domain.models.CityStat
import com.sinarowa.e_bus_ticket.domain.models.SegmentStat
import com.sinarowa.e_bus_ticket.domain.models.TicketWithRoute

object ReportUtils {

    val printerWidth = 32  // Set width based on printer support

    /**
     * 📄 Generate Daily Sales Report
     */
    fun generateDailySalesReport(
        companyName: String,
        date: String,
        deviceId: String,
        tripId: String,
        tripsCount: Int,
        deviceName: String,
        totalTickets: Int,
        cancelledTickets: Int,
        firstTicketNumber: String,
        lastTicketNumber: String,
        firstTicketTime: String,
        lastTicketTime: String,
        ticketDetails: Map<String, Pair<Int, Double>>,
        paymentDetails: Map<String, Pair<Int, Double>>,
        tripSales: List<Pair<String, Double>>,
        expenses: Map<String, Double>
    ): String {
        val totalSales = tripSales.sumOf { it.second }
        val totalExpenses = expenses.values.sum()
        val netSales = totalSales - totalExpenses

        Log.d("ReportUtils", "🔹 Generating Daily Sales Report for Trip ID: $tripId")
        Log.d("ReportUtils", "📅 Date: $date | Device ID: $deviceId | Device Name: $deviceName")
        Log.d("ReportUtils", "🎟 Total Tickets: $totalTickets | Cancelled: $cancelledTickets")
        Log.d("ReportUtils", "💰 Total Sales: $totalSales | Total Expenses: $totalExpenses | Net Sales: $netSales")

        return buildString {
            appendLine(companyName)
            appendLine("=".repeat(printerWidth))
            appendLine("DAILY SALES REPORT")
            appendLine("-".repeat(printerWidth))
            appendLine(date)
            appendLine("Device ID  : $deviceId")
            appendLine("Device Name: $deviceName")
            appendLine("=".repeat(printerWidth))

            appendLine("TRIPS : $tripsCount")
            appendLine("TICKETS : $totalTickets  CANCELLED : $cancelledTickets")
            appendLine("NUMBERS : $firstTicketNumber - $lastTicketNumber")
            appendLine("FIRST   : $firstTicketTime")
            appendLine("LAST    : $lastTicketTime")
            appendLine()

            // 🎟 Ticket Breakdown
            appendLine(formatHeaderRow("CATEGORY", "COUNT", "AMOUNT"))
            ticketDetails.forEach { (type, data) ->
                appendLine(formatRow(type, data.first, data.second))
            }
            appendLine()

            // 💳 Payment Methods
            appendLine(formatHeaderRow("METHOD", "COUNT", "AMOUNT"))
            paymentDetails.forEach { (method, data) ->
                appendLine(formatRow(method, data.first, data.second))
            }
            appendLine()

            // 🚍 Trip Sales
            appendLine(formatHeaderRow("ROUTE", "", "AMOUNT"))
            tripSales.forEach { (tripName, sales) ->
                appendLine(formatRow(tripName, "", sales))
            }
            appendLine(formatRow("TOTAL-SALES", "", totalSales))
            appendLine()

            // 📑 Expenses
            appendLine(formatHeaderRow("CATEGORY", "", "AMOUNT"))
            expenses.forEach { (type, amount) ->
                appendLine(formatRow(type, "", amount))
            }
            appendLine(formatRow("TOTAL-EXPENSES", "", totalExpenses))
            appendLine()

            // 💰 Net Sales
            appendLine("-".repeat(printerWidth))
            appendLine(formatRow("NET SALES", "", netSales))
            appendLine("-".repeat(printerWidth))
        }
    }

    /**
     * 📄 Generate Trip Sales Report
     */
    fun generateTripSalesReport(
        companyName: String,
        date: String,
        deviceId: String,
        tripId: String,
        routeName: String,
        totalTickets: Int,
        cancelledTickets: Int,
        firstTicketNumber: String,
        lastTicketNumber: String,
        firstTicketTime: String,
        lastTicketTime: String,
        ticketDetails: Map<String, Pair<Int, Double>>,
        paymentDetails: Map<String, Pair<Int, Double>>,
        tripSales: List<Pair<String, Double>>
    ): String {
        val totalSales = tripSales.sumOf { it.second }

        Log.d("ReportUtils", "🔹 Generating Trip Sales Report for Trip ID: $tripId")
        Log.d("ReportUtils", "📅 Date: $date | Route: $routeName | Device ID: $deviceId")
        Log.d("ReportUtils", "🎟 Total Tickets: $totalTickets | Cancelled: $cancelledTickets")
        Log.d("ReportUtils", "💰 Total Trip Sales: $totalSales")

        return buildString {
            appendLine(companyName)
            appendLine("=".repeat(printerWidth))
            appendLine("TRIP $tripId REPORT")
            appendLine("-".repeat(printerWidth))
            appendLine(date)
            appendLine("Device ID  : $deviceId")
            appendLine("=".repeat(printerWidth))

            appendLine("ROUTE : $routeName")
            appendLine("TICKETS : $totalTickets  CANCELLED : $cancelledTickets")
            appendLine("NUMBERS : $firstTicketNumber - $lastTicketNumber")
            appendLine("FIRST   : $firstTicketTime")
            appendLine("LAST    : $lastTicketTime")
            appendLine()

            // 🎟 Ticket Breakdown
            appendLine(formatHeaderRow("CATEGORY", "COUNT", "AMOUNT"))
            ticketDetails.forEach { (type, data) ->
                appendLine(formatRow(type, data.first, data.second))
            }
            appendLine()

            // 💳 Payment Methods
            appendLine(formatHeaderRow("METHOD", "COUNT", "AMOUNT"))
            paymentDetails.forEach { (method, data) ->
                appendLine(formatRow(method, data.first, data.second))
            }
            appendLine()

            // 🚍 Trip Sales Breakdown
            appendLine(formatHeaderRow("ROUTE", "", "AMOUNT"))
            tripSales.forEach { (tripName, sales) ->
                appendLine(formatRow(tripName, "", sales))
            }
            appendLine(formatRow("TRIP-SALES", "", totalSales))
            appendLine("-".repeat(printerWidth))
        }
    }


    /**
     * ✅ Format Row for Alignment
     */
    fun formatHeaderRow(left: String, middle: String, right: String, width: Int = printerWidth): String {
        val col1Width = (width * 0.45).toInt()  // 45% for first column
        val col2Width = (width * 0.25).toInt()  // 25% for second column
        val col3Width = (width * 0.30).toInt()  // 30% for third column

        return String.format(
            "%-${col1Width}s %${col2Width}s %${col3Width}s",
            left.take(col1Width),
            middle.take(col2Width),
            right.take(col3Width)
        )
    }

    fun formatRow(left: String, middle: Any?, right: Any?, width: Int = printerWidth): String {
        val col1Width = (width * 0.45).toInt()
        val col2Width = (width * 0.25).toInt()
        val col3Width = (width * 0.30).toInt()

        return String.format(
            "%-${col1Width}s %${col2Width}d %${col3Width}.2f",
            left.take(col1Width),
            middle?.toString()?.toIntOrNull() ?: 0,
            right?.toString()?.toDoubleOrNull() ?: 0.00
        )
    }


    /**
     * 📄 Generate Detailed Stats Report (Segment + City Breakdown)
     */
    fun generateDetailedStatsReport(
        companyName: String,
        date: String,
        deviceId: String,
        tripId: String,
        tickets: List<TicketWithRoute>,
        cityStats: List<CityStat>,
        segmentStats: List<SegmentStat>
    ): String {

        val totalRevenue = tickets.sumOf { it.amount }
        val ticketTypes = segmentStats.map { shortenTicketType(it.ticketType) }
            .distinct()
            .sorted()

        return buildString {
            appendLine(companyName.take(printerWidth))
            appendLine("=".repeat(printerWidth))
            appendLine("DETAILED TRIP STATS".padStart((printerWidth / 2) + 5))
            appendLine("-".repeat(printerWidth))
            appendLine(date.take(printerWidth))
            appendLine("Device ID: $deviceId".take(printerWidth))
            appendLine("=".repeat(printerWidth))

            // 🚍 **Segment Breakdown**
            appendLine("SEGMENT BREAKDOWN")
            appendLine("-".repeat(printerWidth))
            segmentStats.groupBy { it.from to it.to }.forEach { (segment, segmentData) ->
                appendLine(formatSegmentRow(segment.first, segment.second, segmentData, ticketTypes))
            }
            appendLine()

            // 🏙 **City Breakdown**
            appendLine("CITY BREAKDOWN")
            appendLine("-".repeat(printerWidth))
            cityStats.groupBy { it.cityName }.forEach { (city, cityData) ->
                appendLine(formatCityRow(shortenCityName(city), cityData, ticketTypes))
            }
            appendLine()

            // 💰 **Total Revenue**
            appendLine("-".repeat(printerWidth))
            appendLine("TOTAL REVENUE: $${"%.2f".format(totalRevenue)}".padEnd(printerWidth))
            appendLine("-".repeat(printerWidth))

            // 📌 **Legend (Key)**
            appendLine("KEY:")
            appendLine("A - Adult    C - Child")
            appendLine("L - Luggage  T - Total Count")
            appendLine("T$ - Total Sales")

            // 🔥 Dynamically generate short fare types
            val shortFareTypes = ticketTypes.filter { it.startsWith("$") }
            if (shortFareTypes.isNotEmpty()) {
                shortFareTypes.forEach { fare ->
                    appendLine("$fare - $fare Short")
                }
            }

            appendLine("-".repeat(printerWidth))
        }
    }

    /**
     * ✅ Formatting Helper for Segment Breakdown Row (Includes Total Count + Total Sales)
     */
    fun formatSegmentRow(from: String, to: String, tickets: List<SegmentStat>, ticketTypes: List<String>, width: Int = printerWidth): String {
        val groupedByType = tickets.groupBy { shortenTicketType(it.ticketType) }
        val totalTickets = tickets.sumOf { it.count }
        val totalAmount = tickets.sumOf { it.amount }

        val segmentHeader = "${shortenCityName(from)} → ${shortenCityName(to)}   | $totalTickets  | $$totalAmount"
        val ticketCounts = ticketTypes.map { "${it}:${groupedByType[it]?.sumOf { it.count } ?: 0}" }

        return buildString {
            appendLine(segmentHeader.take(width))
            appendLine("  " + ticketCounts.joinToString("  ").take(width))  // Ticket type breakdown
        }
    }

    /**
     * ✅ Formatting Helper for City Breakdown Row (Includes Total Count + Total Sales)
     */
    fun formatCityRow(city: String, tickets: List<CityStat>, ticketTypes: List<String>, width: Int = printerWidth): String {
        val groupedByType = tickets.groupBy { shortenTicketType(it.ticketType) }
        val totalTickets = tickets.sumOf { it.count }
        val totalAmount = tickets.sumOf { it.amount }

        val cityHeader = "${shortenCityName(city)}  | $totalTickets  | $$totalAmount"
        val ticketCounts = ticketTypes.map { "${it}:${groupedByType[it]?.sumOf { it.count } ?: 0}" }

        return buildString {
            appendLine(cityHeader.take(width))
            appendLine("  " + ticketCounts.joinToString("  ").take(width))  // Ticket type breakdown
        }
    }

    /**
     * ✅ Shorten Ticket Type (Initials)
     */
    fun shortenTicketType(ticketType: String): String {
        return when (ticketType.lowercase()) {
            "adult" -> "A"
            "luggage" -> "L"
            "child" -> "C"
            "short $1" -> "$1"
            "short $2" -> "$2"
            "short $5" -> "$5"
            "short $8" -> "$8"
            else -> ticketType.take(2).uppercase()
        }
    }

    /**
     * ✅ Shorten City Name (Max 4 Letters)
     */
    fun shortenCityName(cityName: String): String {
        return cityName.take(4) // Ensures cities are max 4 letters
    }
}
