package com.sinarowa.e_bus_ticket.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.itextpdf.kernel.pdf.PdfDocument
import android.os.Environment
import android.provider.MediaStore
import com.sinarowa.e_bus_ticket.viewmodel.TripViewModel

import android.util.Log
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Table
import com.sinarowa.e_bus_ticket.R


object PdfUtils {



    fun generateTripSalesPdf(context: Context,
                             sales: List<TripViewModel.TripSale>,
                             companyName: String,
                             companyContact: String,
                             conductorName: String,
                             tripStartTime: String,
                             tripDate: String,
                             busReg: String,   // ✅ Bus Registration
                             busName: String )  // ✅ Bus Name
    {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "Trip_Sales_Report.pdf")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val contentResolver = context.contentResolver
            val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

            if (uri != null) {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val pdfWriter = PdfWriter(outputStream)
                    val pdfDocument = PdfDocument(pdfWriter)
                    val document = Document(pdfDocument)

                    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    val imageData = ImageDataFactory.create(byteArrayOutputStream.toByteArray())
                    val image = Image(imageData).scaleToFit(100f, 100f)



                    document.add(Paragraph(companyName).setBold().setFontSize(20f))
                    document.add(Paragraph("Contact: $companyContact").setFontSize(12f))
                    document.add(Paragraph("\nTrip Sales Report").setBold().setFontSize(18f))

// ✅ Add Bus Information
                    document.add(Paragraph("Bus Name: $busName"))
                    document.add(Paragraph("Bus Registration: $busReg"))

// ✅ Add Trip Information
                    document.add(Paragraph("Trip Date: $tripDate"))
                    document.add(Paragraph("Conductor: $conductorName"))
                    document.add(Paragraph("Start Time: $tripStartTime"))
                    document.add(Paragraph("Report Generated: ${java.time.LocalDateTime.now()}"))

                    document.add(Paragraph("\n--------------------------------------------------------\n"))



                    // ✅ Title
                    document.add(Paragraph("Trip Sales Report").setBold().setFontSize(18f))
                    document.add(Paragraph("\n"))

                    // ✅ Sales Summary Table
                    val summaryTable = Table(floatArrayOf(2f, 1f, 1f, 1f, 1f))
                    summaryTable.addCell("Route")
                    summaryTable.addCell("Tickets Sold")
                    summaryTable.addCell("Total Sales ($)")
                    summaryTable.addCell("Total Expenses ($)")
                    summaryTable.addCell("Net Sales ($)")

                    sales.forEach { sale ->
                        summaryTable.addCell(sale.routeName)
                        summaryTable.addCell(sale.totalTickets.toString())
                        summaryTable.addCell(String.format("%.2f", sale.totalSales))
                        summaryTable.addCell(String.format("%.2f", sale.totalExpenses))
                        summaryTable.addCell(String.format("%.2f", sale.netSales))
                    }
                    document.add(summaryTable)

                    document.add(Paragraph("\n"))

                    // ✅ Route Breakdown
                    sales.forEach { sale ->
                        sale.routeBreakdown.forEach { route ->
                            // ✅ Add Route Header
                            document.add(Paragraph("From: ${route.fromCity} To: ${route.toCity}").setBold())

                            // ✅ Add Ticket Breakdown
                            route.ticketBreakdown.forEach { ticket ->
                                document.add(Paragraph("  - ${ticket.type}: ${ticket.count} ticket(s) ($${String.format("%.1f", ticket.amount)})"))
                            }

                            document.add(Paragraph("\n")) // ✅ Space between routes
                        }
                    }


                    // ✅ Expense Breakdown
                    document.add(Paragraph("Expense Breakdown").setBold())

                    val expenseTable = Table(floatArrayOf(2f, 1f, 1f))
                    expenseTable.addCell("Expense Type")
                    expenseTable.addCell("Count")
                    expenseTable.addCell("Total Amount ($)")

                    sales.forEach { sale ->
                        sale.expenseBreakdown.forEach { expense ->
                            expenseTable.addCell(expense.type)
                            expenseTable.addCell(expense.count.toString())
                            expenseTable.addCell(String.format("%.2f", expense.totalAmount))
                        }
                    }
                    document.add(expenseTable)

                    // ✅ Close document
                    document.close()
                    Log.d("PDF_GENERATION", "✅ PDF saved at: ${uri.path}")
                }
            } else {
                Log.e("PDF_GENERATION", "❌ Failed to create PDF in Downloads")
            }
        } catch (e: Exception) {
            Log.e("PDF_GENERATION", "❌ Error generating PDF: ${e.message}")
        }
    }


    fun generateTicketSalesPdf(
        context: Context,
        stationSales: Map<String, Pair<Int, Double>>,
        breakdown: Map<Pair<String, String>, Pair<Int, Double>>
    ) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "Ticket_Sales_Report.pdf")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val contentResolver = context.contentResolver
            val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

            if (uri != null) {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val writer = PdfWriter(outputStream)
                    val pdf = PdfDocument(writer)
                    val document = Document(pdf)

                    // Title
                    document.add(Paragraph("Ticket Sales Report").setBold().setFontSize(18f))

                    // Station Sales Table
                    val stationTable = Table(floatArrayOf(2f, 1f, 1f))
                    stationTable.addCell("Station")
                    stationTable.addCell("Count")
                    stationTable.addCell("Amount ($)")

                    stationSales.forEach { (station, data) ->
                        stationTable.addCell(station)
                        stationTable.addCell(data.first.toString())
                        stationTable.addCell(String.format("%.2f", data.second))
                    }
                    document.add(stationTable)

                    // Breakdown Table
                    document.add(Paragraph("\nBreakdown").setBold())
                    val breakdownTable = Table(floatArrayOf(2f, 2f, 1f, 1f))
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
                    document.add(breakdownTable)

                    document.close()
                    Log.d("PDF_GENERATION", "✅ PDF saved at: ${uri.path}")
                }
            } else {
                Log.e("PDF_GENERATION", "❌ Failed to create PDF in Downloads")
            }
        } catch (e: Exception) {
            Log.e("PDF_GENERATION", "❌ Error generating PDF: ${e.message}")
        }
    }


}
