package com.sinarowa.e_bus_ticket.domain.usecase

import com.sinarowa.e_bus_ticket.data.local.entities.Ticket
import com.sinarowa.e_bus_ticket.data.local.enums.SyncStatus
import com.sinarowa.e_bus_ticket.data.local.enums.TicketStatus
import com.sinarowa.e_bus_ticket.data.repository.PriceRepository
import com.sinarowa.e_bus_ticket.data.repository.TicketRepository
import com.sinarowa.e_bus_ticket.utils.DateTimeUtils
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class SellTicketUseCase @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val priceRepository: PriceRepository
) {
    suspend fun execute(tripId: String, fromStationId: String, toStationId: String, paymentCategory: String): Result<Unit> {
        return try {
            // Validate input data
            if (tripId.isBlank() || fromStationId.isBlank() || toStationId.isBlank()) {
                return Result.failure(IllegalArgumentException("Invalid trip or station details"))
            }

            // Fetch price entry
            val priceEntry = priceRepository.getPrice(fromStationId, toStationId)
                ?: return Result.failure(IllegalArgumentException("No price found for selected route"))

            // Generate a new Ticket ID
            val ticketId = UUID.randomUUID().toString()

            // Create Ticket
            val ticket = Ticket(
                ticketId = ticketId,
                tripId = tripId,
                priceId = priceEntry.priceId,
                paymentCategory = paymentCategory,
                creationTime = DateTimeUtils.getCurrentDateTime(),
                amount = priceEntry.amount,
                status = TicketStatus.VALID,
                syncStatus = SyncStatus.PENDING
            )

            // Insert ticket into the database
            ticketRepository.insertTicket(ticket)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}