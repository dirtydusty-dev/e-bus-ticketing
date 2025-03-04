package com.sinarowa.e_bus_ticket.domain.usecase

import com.sinarowa.e_bus_ticket.data.local.enums.SyncStatus
import com.sinarowa.e_bus_ticket.data.local.enums.TripStatus
import com.sinarowa.e_bus_ticket.data.repository.ExpenseRepository
import com.sinarowa.e_bus_ticket.data.repository.TicketRepository
import com.sinarowa.e_bus_ticket.data.repository.TripRepository
import com.sinarowa.e_bus_ticket.utils.DateTimeUtils
import javax.inject.Inject

class EndTripUseCase @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val expenseRepository: ExpenseRepository,
    private val tripRepository: TripRepository,
) {

    suspend fun execute(tripId: String): Result<Unit> {
        // Check if there are any unsynced tickets
        val unsyncedTickets = ticketRepository.getUnsyncedTickets(tripId, SyncStatus.PENDING)
        if (unsyncedTickets.isNotEmpty()) {
            return Result.failure(Exception("There are unsynced tickets. Please sync before ending the trip."))
        }

        // Check if there are any unsynced expenses
        val unsyncedExpenses = expenseRepository.getUnsyncedExpenses(tripId, SyncStatus.PENDING)
        if (unsyncedExpenses.isNotEmpty()) {
            return Result.failure(Exception("There are unsynced expenses. Please sync before ending the trip."))
        }

        // Check if the trip itself is unsynced
        val trip = tripRepository.getTripById(tripId)
        if (trip?.syncStatus == SyncStatus.PENDING) {
            return Result.failure(Exception("The trip is unsynced. Please sync before ending the trip."))
        }

        // If everything is synced, proceed to update the trip status to COMPLETED
        return try {
            trip?.let {
                // Update trip status to COMPLETED
                it.status = TripStatus.COMPLETED
                it.endTime = DateTimeUtils.getCurrentDateTime()
                tripRepository.updateTripStatus(it)
                Result.success(Unit)
            } ?: Result.failure(Exception("Trip not found"))
        } catch (e: Exception) {
            Result.failure(Exception("Error ending the trip: ${e.message}"))
        }
    }
}
