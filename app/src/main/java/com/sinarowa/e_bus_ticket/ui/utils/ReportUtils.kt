/*
package com.sinarowa.e_bus_ticket.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ReportUtils {


    fun generateA4TicketSalesReport(
        context: Context,
        companyName: String,
        date: String,
        tripId: String,
        tripDuration: String,
        ticketsSold: Int,
        firstTicket: String,
        lastTicket: String,
        citySales: List<List<String>>, // City-Wise Breakdown
        ticketTypes: List<List<String>>, // Ticket Type Breakdown
        paymentSummary: List<List<String>>, // Payment Methods
        tripSales: List<List<String>>, // Trip Revenue Breakdown
        expenses: List<List<String>>, // Expense Breakdown
        routeSales: List<List<String>>, // Route-Based Ticket Breakdown
        totalSales: Double,
        totalExpenses: Double
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        var yPos = 50f

        fun drawTitle(title: String) {
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText(title, 50f, yPos, paint)
            yPos += 20f
            paint.textSize = 12f
            paint.isFakeBoldText = false
        }

        fun drawTable(headers: List<String>, rows: List<List<String>>) {
            canvas.drawText(headers.joinToString("   "), 50f, yPos, paint)
            yPos += 20f
            for (row in rows) {
                canvas.drawText(row.joinToString("   "), 50f, yPos, paint)
                yPos += 20f
            }
            yPos += 20f
        }

        // 🏁 Report Header
        drawTitle(companyName)
        paint.textSize = 14f
        canvas.drawText("Daily Ticket Sales Report", 50f, yPos, paint)
        yPos += 20f
        canvas.drawText("Date: $date", 50f, yPos, paint)
        canvas.drawText("Trip ID: $tripId", 400f, yPos, paint)
        yPos += 30f

        // 🚍 Trip Summary
        drawTitle("🚍 TRIP SUMMARY")
        drawTable(
            listOf("Tickets Sold", "First Ticket", "Last Ticket", "Trip Duration"),
            listOf(listOf("$ticketsSold", firstTicket, lastTicket, tripDuration))
        )

        // 📍 City-Wise Sales
        drawTitle("📍 CITY-WISE TICKET SALES BREAKDOWN")
        drawTable(
            listOf("City", "Adult", "Child", "$1 Short", "$2 Short", "Total Tickets", "Revenue"),
            citySales
        )

        // 🎟️ Ticket Type Breakdown
        drawTitle("🎟️ TICKET TYPE BREAKDOWN")
        drawTable(
            listOf("Ticket Type", "Total Tickets", "Total Revenue"),
            ticketTypes
        )

        // 💳 Payment Summary
        drawTitle("💳 PAYMENT SUMMARY")
        drawTable(
            listOf("Method", "Qty", "Total ($)"),
            paymentSummary
        )

        // 💰 Trip Sales Breakdown
        drawTitle("💰 TRIP SALES BREAKDOWN")
        drawTable(
            listOf("Trip", "Revenue ($)"),
            tripSales
        )

        // 📑 Expenses Breakdown
        drawTitle("📑 EXPENSES BREAKDOWN")
        drawTable(
            listOf("Expense Type", "Amount ($)"),
            expenses
        )

        // 🚍 Route-Based Ticket Sales Breakdown
        drawTitle("🚍 ROUTE-BASED TICKET SALES BREAKDOWN")
        drawTable(
            listOf("From", "To", "Adult", "Child", "$1 Short", "$2 Short", "Total Tickets", "Revenue"),
            routeSales
        )

        // 💵 Final Summary
        drawTitle("💵 FINAL SUMMARY")
        canvas.drawText("💰 Total Sales: $${"%.2f".format(totalSales)}", 50f, yPos, paint)
        yPos += 20f
        canvas.drawText("🛠 Total Expenses: $${"%.2f".format(totalExpenses)}", 50f, yPos, paint)
        yPos += 20f
        canvas.drawText("📈 Net Profit: $${"%.2f".format(totalSales - totalExpenses)}", 50f, yPos, paint)

        document.finishPage(page)

        // 📂 Save PDF
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "TicketReports"
        )
        if (!directory.exists()) directory.mkdirs()

        val file = File(directory, "Ticket_Report_${System.currentTimeMillis()}.pdf")
        try {
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            document.close()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }





    val printerWidth = 32 // Adjust if your printer supports 42 chars


    fun formatRow(left: String, middle: Any?, right: Any?, width: Int = printerWidth): String {
        val col1Width = (width * 0.4).toInt()  // 40% for left column
        val col2Width = (width * 0.2).toInt()  // 20% for middle column
        val col3Width = (width * 0.4).toInt()  // 40% for right column

        // 🔥 Ensure middle is always a valid Int
        val middleValue = when (middle) {
            is Int -> middle
            is String -> middle.toIntOrNull() ?: 0
            is Number -> middle.toInt()
            null -> 0
            else -> 0
        }

        // 🔥 Ensure right is always a valid Double
        val rightValue = when (right) {
            is Float -> right.toDouble()
            is Double -> right
            is String -> right.toDoubleOrNull() ?: 0.00
            is Number -> right.toDouble()
            null -> 0.00
            else -> 0.00
        }

        return String.format(
            "%-${col1Width}s %${col2Width}d %${col3Width}.2f",
            left.take(col1Width),
            middleValue,
            rightValue
        )
    }


    fun formatHeaderRow(left: String, middle: String, right: String, width: Int = printerWidth): String {
        val col1Width = (width * 0.4).toInt()  // 40% for left column
        val col2Width = (width * 0.2).toInt()  // 20% for middle column
        val col3Width = (width * 0.4).toInt()  // 40% for right column

        return String.format(
            "%-${col1Width}s %${col2Width}s %${col3Width}s",
            left.take(col1Width),
            middle.take(col2Width),
            right.take(col3Width)
        )
    }







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

        return buildString {
            appendLine(companyName)
            appendLine("=".repeat(printerWidth))
            appendLine("DAILY SALES REPORT  ")
            appendLine("-".repeat(printerWidth))
            appendLine(date)
            appendLine("Device ID  : $deviceId")
            appendLine("Device Name: $deviceName")
            appendLine("=".repeat(printerWidth))

            // 🟢 **Trip Summary**
            appendLine("Trip ID : $tripId")
            appendLine("Tickets : $totalTickets")
            appendLine("First   : $firstTicketNumber")
            appendLine("Last    : $lastTicketNumber")
            appendLine("Time    : $firstTicketTime - $lastTicketTime")
            appendLine()

            // 🟢 **Ticket Details**
            appendLine("---- Tickets ----")
            appendLine(formatHeaderRow("Type", "Qty", "Total", printerWidth)) // ✅ Use header row
            appendLine("-".repeat(printerWidth))

            ticketDetails.forEach { (type, data) ->
                appendLine(formatRow(type, data.first ?: 0, data.second ?: 0.00, printerWidth))
            }
            appendLine()

            // 🟢 **Payment Details**
            appendLine("---- Payments ----")
            appendLine(formatHeaderRow("Method", "Qty", "Total", printerWidth)) // ✅ Use header row
            appendLine("-".repeat(printerWidth))

            paymentDetails.forEach { (method, data) ->
                appendLine(formatRow(method, data.first ?: 0, data.second ?: 0.00, printerWidth))
            }
            appendLine()

            // 🟢 **Trip Sales Breakdown**
            appendLine("---- Trip Sales ----")
            appendLine(formatHeaderRow("Trip", "Qty", "Total", printerWidth)) // ✅ Use header row
            appendLine("-".repeat(printerWidth))

            tripSales.forEach { (tripName, sales) ->
                appendLine(formatRow(tripName, 1, sales ?: 0.00, printerWidth))
            }
            appendLine("-".repeat(printerWidth))
            appendLine(formatRow("Total:", "", totalSales ?: 0.00, printerWidth))
            appendLine()

            // 🟢 **Expenses**
            appendLine("---- Expenses ----")
            appendLine(formatHeaderRow("Type", "Qty", "Cost", printerWidth)) // ✅ Use header row
            appendLine("-".repeat(printerWidth))

            expenses.forEach { (type, amount) ->
                appendLine(formatRow(type, 1, amount ?: 0.00, printerWidth))
            }
            appendLine("-".repeat(printerWidth))
            appendLine(formatRow("Total:", "", totalExpenses ?: 0.00, printerWidth))
            appendLine()

            // 🟢 **Net Sales**
            appendLine("-".repeat(printerWidth))
            appendLine(formatRow("Net Sales:", "", netSales ?: 0.00, printerWidth))
            appendLine("-".repeat(printerWidth))
        }

    }


    fun generateDestinationDetails(routeBreakdown: List<TripViewModel.RouteBreakdown>): String {
        return buildString {
            append(" DESTINATION DETAILS\n")
            append(" FROM-TO        A  C  L  $1  $2  AMOUNT\n")

            routeBreakdown.forEach { route ->
                val totalAdults = route.ticketBreakdown.find { it.type == "Adult" }?.count ?: 0
                val totalChildren = route.ticketBreakdown.find { it.type == "Child" }?.count ?: 0
                val totalLuggage = route.ticketBreakdown.find { it.type == "Luggage" }?.count ?: 0
                val dollarShort = route.ticketBreakdown.find { it.type == "$1 Short" }?.count ?: 0
                val twoDollarShort = route.ticketBreakdown.find { it.type == "$2 Short" }?.count ?: 0
                val totalAmount = route.ticketBreakdown.sumOf { it.amount }

                // Format each route breakdown entry
                append("${route.fromCity.take(5)}-${route.toCity.take(5)}   ")
                append("$totalAdults  $totalChildren  $totalLuggage  $dollarShort  $twoDollarShort  ")
                append("${"%.2f".format(totalAmount)}\n")
            }

            append("\n A-Adult  C-Child  L-Luggage  $1-\$1 Short $2-\$2 Short \n")
        }
    }


















    fun generateTicketSalesReport(
        companyName: String,
        date: String,
        time: String,
        deviceId: String,
        totalTickets: Int,
        stationSummary: Map<String, Pair<Int, Double>>, // "Station" -> (Count, Amount)
        routeBreakdown: List<Triple<String, String, Pair<Int, Double>>>, // ("Start", "Destination", (Count, Amount))
        totalSales: Double,
        expenses: Map<String, Double>
    ): String {
        val totalExpenses = expenses.values.sum()
        val netSales = totalSales - totalExpenses

        return buildString {
            appendLine(companyName)
            appendLine("=".repeat(28))
            appendLine("   TICKET SALES REPORT   ")
            appendLine("-".repeat(28))
            appendLine("$date $time")
            appendLine("Device: $deviceId")
            appendLine("=".repeat(28))

            // 🟢 **Ticket Summary**
            appendLine("Total Tickets: $totalTickets")
            appendLine()

            // 🟢 **Station Breakdown**
            appendLine("---- Stations ----")
            appendLine("Station    Qty   Total")
            appendLine("-".repeat(22))

            stationSummary.forEach { (station, data) ->
                val count = data.first
                val amount = data.second
                appendLine(String.format("%-10s %3d %7.2f", station, count, amount))
            }

            appendLine()

            // 🟢 **Route Breakdown**
            appendLine("---- Routes ----")
            appendLine("From -> To     Qty   Total")
            appendLine("-".repeat(28))

            routeBreakdown.forEach { (start, destination, data) ->
                val count = data.first
                val amount = data.second
                appendLine(String.format("%-5s -> %-5s %3d %7.2f", start, destination, count, amount))
            }

            appendLine()

            // 🟢 **Total Sales & Expenses**
            appendLine("-".repeat(28))
            appendLine(String.format("%-12s %7.2f", "Total Sales:", totalSales))
            appendLine("-".repeat(28))

            appendLine("---- Expenses ----")
            appendLine("Type      Cost")
            appendLine("-".repeat(22))

            expenses.forEach { (type, amount) ->
                appendLine(String.format("%-10s %7.2f", type, amount))
            }

            appendLine("-".repeat(22))
            appendLine(String.format("%-10s %7.2f", "Total:", totalExpenses))
            appendLine()

            // 🟢 **Net Sales**
            appendLine("-".repeat(22))
            appendLine(String.format("%-10s %7.2f", "Net Sales:", netSales))
            appendLine("-".repeat(22))
        }
    }





    fun generateReportHeader(
        companyName: String,
        reportTitle: String,
        date: String,
        time: String,
        deviceId: String
    ): String {
        return buildString {
            append("\n")
            append(" $companyName \n")
            append("===========================\n")
            append(" $reportTitle \n")
            append("---------------------------\n")
            append("$date $time\n")
            append("Device-ID: $deviceId\n")
            append("===========================\n")
        }
    }

    */
/**
     * ✅ Generates the trip summary section
     *//*

    fun generateTripSummary(
        tripName: String,
        totalSales: Double,
        totalTickets: Int,
        tripSales: Double,
        tripTickets: Int
    ): String {
        return buildString {
            append("TRIP: $tripName\n")
            append("TOTAL-SALES: ${formatAmount(totalSales)}\n")
            append("TRIP-SALES:  ${formatAmount(tripSales)}\n")
            append("TOTAL-TICKETS: $totalTickets\n")
            append("TRIP-TICKETS: $tripTickets\n")
            append("\n")
        }
    }

    */
/**
     * ✅ Formats a double value to 2 decimal places
     *//*

    private fun formatAmount(amount: Double): String {
        return "%.2f".format(amount)
    }


    */
/**
     * ✅ Generates the ONBOARD NOW section
     *//*

    fun generateOnboardNow(
        currentLocation: String,
        adults: Int,
        children: Int,
        luggage: Int,
        passes: Int,
        totalTickets: Int
    ): String {
        return buildString {
            append("ONBOARD-NOW: *$currentLocation\n")
            append(" ADULTS:   $adults  CHILDREN: $children\n")
            append(" LUGGAGE:  $luggage  PASSES:   $passes\n")
            append(" TICKETS:  $totalTickets\n")
            append("\n")
        }
    }

    */
/**
     * ✅ Generates the DESTINATION DETAILS section
     *//*








}
*/
