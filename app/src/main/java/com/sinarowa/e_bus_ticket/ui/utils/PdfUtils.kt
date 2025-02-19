package com.sinarowa.e_bus_ticket.utils

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel
import java.io.File
import java.io.FileOutputStream
import android.util.Log
import com.lowagie.text.*
import com.lowagie.text.pdf.*
import java.io.IOException
import androidx.compose.ui.text.font.FontFamily



object PdfUtils {
    fun generateTripSalesPdf(context: Context, sales: List<TripViewModel.TripSale>) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint()

        var yPosition = 20f

        sales.forEach { sale ->
            canvas.drawText("Route: ${sale.routeName}", 10f, yPosition, paint)
            yPosition += 20
            canvas.drawText("Total Tickets Sold: ${sale.totalTickets}", 10f, yPosition, paint)
            yPosition += 20
            canvas.drawText("Total Sales: \$${sale.totalSales}", 10f, yPosition, paint)
            yPosition += 20
            canvas.drawText("Total Expenses: \$${sale.totalExpenses}", 10f, yPosition, paint)
            yPosition += 20
            canvas.drawText("Net Sales: \$${sale.netSales}", 10f, yPosition, paint)
            yPosition += 30

            // Route Breakdown
            sale.routeBreakdown.forEach { route ->
                canvas.drawText("From: ${route.fromCity} To: ${route.toCity}", 10f, yPosition, paint)
                yPosition += 20
                route.ticketBreakdown.forEach { ticket ->
                    canvas.drawText("  - ${ticket.type}: ${ticket.count} tickets (\$${ticket.amount})", 20f, yPosition, paint)
                    yPosition += 20
                }
            }
            yPosition += 30

            // Expense Breakdown
            canvas.drawText("Expense Breakdown:", 10f, yPosition, paint)
            yPosition += 20
            sale.expenseBreakdown.forEach { expense ->
                canvas.drawText("  - ${expense.type}: ${expense.count} entries (\$${expense.totalAmount})", 20f, yPosition, paint)
                yPosition += 20
            }
            yPosition += 40
        }

        pdfDocument.finishPage(page)
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "TripSalesReport.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
    }

    fun generateTicketSalesPdf(
        context: Context,
        stationSales: Map<String, Pair<Int, Double>>,
        breakdown: Map<Pair<String, String>, Pair<Int, Double>>
    ) {
        try {
            val pdfFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Ticket_Sales_Report.pdf"
            )

            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(pdfFile))
            document.open()

            // ✅ Title
            val titleFont = Font(Font.HELVETICA, 18f, Font.BOLD)
            document.add(Paragraph("Ticket Sales Report", titleFont))
            document.add(Paragraph("\n"))

            // ✅ Station Sales Table
            val stationTable = PdfPTable(3) // 3 Columns: Station, Count, Amount
            stationTable.addCell("Station")
            stationTable.addCell("Count")
            stationTable.addCell("Amount ($)")

            stationSales.forEach { (station, data) ->
                stationTable.addCell(station)
                stationTable.addCell(data.first.toString())
                stationTable.addCell(String.format("%.2f", data.second))
            }

            document.add(Paragraph("Station Sales Summary", titleFont))
            document.add(stationTable)
            document.add(Paragraph("\n"))

            // ✅ Breakdown Table
            val breakdownTable = PdfPTable(4) // 4 Columns: Start, Destination, Count, Amount
            breakdownTable.addCell("Start")
            breakdownTable.addCell("Destination")
            breakdownTable.addCell("Count")
            breakdownTable.addCell("Amount ($)")

            breakdown.forEach { (route, data) ->
                breakdownTable.addCell(route.first)
                breakdownTable.addCell(route.second)
                breakdownTable.addCell(data.first.toString())
                breakdownTable.addCell(String.format("%.2f", data.second))
            }

            document.add(Paragraph("Breakdown", titleFont))
            document.add(breakdownTable)

            document.close()

            Log.d("PDF_GENERATION", "✅ PDF saved at: ${pdfFile.absolutePath}")

        } catch (e: IOException) {
            Log.e("PDF_GENERATION", "❌ Error generating PDF: ${e.message}")
        }
    }
}
