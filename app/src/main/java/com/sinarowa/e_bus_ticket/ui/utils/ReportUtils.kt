package com.sinarowa.e_bus_ticket.utils

import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel

object ReportUtils {


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

    /**
     * ✅ Generates the trip summary section
     */
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

    /**
     * ✅ Formats a double value to 2 decimal places
     */
    private fun formatAmount(amount: Double): String {
        return "%.2f".format(amount)
    }


    /**
     * ✅ Generates the ONBOARD NOW section
     */
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

    /**
     * ✅ Generates the DESTINATION DETAILS section
     */
    fun generateDestinationDetails(routeBreakdown: List<TripViewModel.RouteBreakdown>): String {
        return buildString {
            append(" DESTINATION DETAILS\n")
            append(" FROM-TO        A  C  L  P  AMOUNT\n")

            routeBreakdown.forEach { route ->
                val totalAdults = route.ticketBreakdown.find { it.type == "Adult" }?.count ?: 0
                val totalChildren = route.ticketBreakdown.find { it.type == "Child" }?.count ?: 0
                val totalLuggage = route.ticketBreakdown.find { it.type == "Luggage" }?.count ?: 0
                val totalPasses = route.ticketBreakdown.find { it.type == "Pass" }?.count ?: 0
                val totalAmount = route.ticketBreakdown.sumOf { it.amount }

                // Format each route breakdown entry
                append("${route.fromCity.take(5)}-${route.toCity.take(5)}   ")
                append("$totalAdults  $totalChildren  $totalLuggage  $totalPasses  ")
                append("${"%.2f".format(totalAmount)}\n")
            }

            append("\n A-Adult  C-Child  L-Luggage  P-Pass\n")
        }
    }

    fun generateTicketDetails(tripSales: List<TripViewModel.TripSale>): String {
        val lastTickets = tripSales
            .flatMap { sale ->
                sale.routeBreakdown.flatMap { route ->
                    route.ticketBreakdown.map { ticket ->
                        TripViewModel.TicketBreakdown(
                            type = ticket.type,
                            count = ticket.count,
                            amount = ticket.amount
                        )
                    }
                }
            }
            .takeLast(20) // ✅ Get only the last 20 tickets

        return buildString {
            append(" LAST 20 TICKETS\n")
            append(" FROM-TO      TYPE      AMOUNT\n")

            lastTickets.forEach { ticket ->
                append("${ticket.type.padEnd(10)}  ${"%.2f".format(ticket.amount)}\n")
            }

            append("\n")
        }
    }


    fun generatePaymentDetails(tripSales: List<TripViewModel.TripSale>): String {
        val paymentMethods = mapOf(
            "Cash" to tripSales.sumOf { it.totalSales * 0.6 }, // Example: Assume 60% cash
            "Card" to tripSales.sumOf { it.totalSales * 0.3 }, // Example: Assume 30% card
            "Mobile" to tripSales.sumOf { it.totalSales * 0.1 } // Example: Assume 10% mobile
        )

        return buildString {
            append(" PAYMENT DETAILS\n")
            append(" METHOD        AMOUNT\n")

            paymentMethods.forEach { (method, amount) ->
                append("${method.padEnd(10)}  ${"%.2f".format(amount)}\n")
            }

            append("\n")
        }
    }

    fun generateExpenseDetails(tripSales: List<TripViewModel.TripSale>): String {
        val expenseSummary = tripSales.flatMap { it.expenseBreakdown }
            .groupBy { it.type }
            .mapValues { entry -> entry.value.sumOf { it.totalAmount } }

        return buildString {
            append(" EXPENSE DETAILS\n")
            append(" TYPE          AMOUNT\n")

            expenseSummary.forEach { (type, amount) ->
                append("${type.padEnd(10)}  ${"%.2f".format(amount)}\n")
            }

            append("\n")
        }
    }




}
