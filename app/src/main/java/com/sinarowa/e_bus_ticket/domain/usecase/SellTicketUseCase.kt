package com.sinarowa.e_bus_ticket.domain.usecase

import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.enums.SyncStatus
import com.sinarowa.e_bus_ticket.data.local.enums.TicketStatus
import com.sinarowa.e_bus_ticket.data.repository.PriceRepository
import com.sinarowa.e_bus_ticket.data.repository.TicketRepository
import com.sinarowa.e_bus_ticket.utils.DateTimeUtils
import javax.inject.Inject

class SellTicketUseCase @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val priceRepository: PriceRepository
) {
    suspend fun execute(
        tripId: String,
        fromStationId: String,
        toStationId: String,
        paymentCategory: String,
        amount: Double,
        description: String? = null // Add optional description parameter
    ): Result<Unit> {
        return try {
            // Validate input data
            if (tripId.isBlank() || fromStationId.isBlank() || toStationId.isBlank()) {
                return Result.failure(IllegalArgumentException("Invalid trip or station details"))
            }
            if (amount <= 0) {
                return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
            }

            // Fetch price entry only for non-luggage tickets
            val priceEntry = if (paymentCategory == "Luggage") {
                null // No price entry needed for luggage since price is manual
            } else {
                priceRepository.getPrice(fromStationId, toStationId)
                    ?: return Result.failure(IllegalArgumentException("No price found for selected route"))
            }

            // Get the count of existing tickets for the trip
            val lastTicketNumber = ticketRepository.getLastTicketNumberForTrip(tripId) ?: 0
            val newTicketNumber = lastTicketNumber + 1

            // Format ticket number as 4 digits (e.g., 0001, 0025, 0120)
            val formattedTicketId = newTicketNumber.toString().padStart(4, '0')

            // Create Ticket
            val ticket = Ticket(
                ticketId = formattedTicketId,
                tripId = tripId,
                priceId = priceEntry?.priceId, // Null for luggage tickets
                paymentCategory = paymentCategory,
                creationTime = DateTimeUtils.getCurrentDateTime(),
                amount = amount,
                status = TicketStatus.VALID,
                syncStatus = SyncStatus.PENDING,
                luggageDescription = description // Store description
            )

            // Insert ticket into the database
            ticketRepository.insertTicket(ticket)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}