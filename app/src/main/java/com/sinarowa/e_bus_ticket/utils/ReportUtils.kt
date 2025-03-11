package com.sinarowa.e_bus_ticket.utils

import android.util.Log

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

            appendLine("TICKET DETAILS")
            appendLine("CATEGORY   COUNT   AMOUNT")
            ticketDetails.forEach { (type, data) ->
                appendLine(formatRow(type, data.first, data.second))
            }
            appendLine()

            appendLine("PAYMENT DETAILS")
            appendLine("METHOD   COUNT   AMOUNT")
            paymentDetails.forEach { (method, data) ->
                appendLine(formatRow(method, data.first, data.second))
            }
            appendLine()

            appendLine("TRIP SALES")
            appendLine("ROUTE   AMOUNT")
            tripSales.forEach { (tripName, sales) ->
                appendLine(formatRow(tripName, "", sales))
            }
            appendLine(formatRow("TOTAL-SALES", "", totalSales))
            appendLine()

            appendLine("EXPENSES")
            appendLine("CATEGORY   AMOUNT")
            expenses.forEach { (type, amount) ->
                appendLine(formatRow(type, "", amount))
            }
            appendLine(formatRow("TOTAL-EXPENSES", "", totalExpenses))
            appendLine()

            appendLine(formatRow("NET SALES", "", netSales))
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

            appendLine("TICKET DETAILS")
            appendLine("CATEGORY   COUNT   AMOUNT")
            ticketDetails.forEach { (type, data) ->
                appendLine(formatRow(type, data.first, data.second))
            }
            appendLine()

            appendLine("PAYMENT DETAILS")
            appendLine("METHOD   COUNT   AMOUNT")
            paymentDetails.forEach { (method, data) ->
                appendLine(formatRow(method, data.first, data.second))
            }
            appendLine()

            appendLine("TRIP SALES")
            appendLine("ROUTE   AMOUNT")
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
    fun formatRow(left: String, middle: Any?, right: Any?, width: Int = printerWidth): String {
        val formattedMiddle = when (middle) {
            is Int -> middle
            is String -> middle.toIntOrNull() ?: 0
            is Number -> middle.toInt()
            null -> 0
            else -> 0
        }

        val formattedRight = when (right) {
            is Float -> right.toDouble()
            is Double -> right
            is String -> right.toDoubleOrNull() ?: 0.00
            is Number -> right.toDouble()
            null -> 0.00
            else -> 0.00
        }

        return String.format("%-12s %6d %8.2f", left, formattedMiddle, formattedRight)
    }
}
