package com.sinarowa.e_bus_ticket.data.repository

import com.sinarowa.e_bus_ticket.data.local.dao.ReportsDao
import com.sinarowa.e_bus_ticket.reports.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ReportsRepository @Inject constructor(private val reportsDao: ReportsDao) {

    fun getTripSalesSummary(tripId: Long): Flow<TripSales> = flow {
        val tripSalesRaw = reportsDao.getTripSalesSummary(tripId).firstOrNull() ?: return@flow

        val stationTotalsRaw = reportsDao.getTripStationBreakdown(tripId).firstOrNull() ?: emptyList()
        val ticketBreakdownByStation = reportsDao.getTripTicketBreakdownByStation(tripId).firstOrNull() ?: emptyList()
        val ticketBreakdown = reportsDao.getTripTicketBreakdown(tripId).firstOrNull() ?: emptyList()
        val expenseBreakdown = reportsDao.getTripExpenseBreakdown(tripId).firstOrNull() ?: emptyList()

        // ✅ Convert raw data to structured report
        val stationBreakdown = stationTotalsRaw.map { station ->
            TripWithStationTotals(
                tripId = station.tripId,
                station = station.station,
                totalAmount = station.totalAmount,
                ticketBreakdown = ticketBreakdownByStation.filter { it.startStation == station.station }
            )
        }

        val detailedTripSales = TripSales(
            tripId = tripSalesRaw.tripId,
            routeId = tripSalesRaw.routeId,
            totalTickets = tripSalesRaw.totalTickets,
            totalSales = tripSalesRaw.totalSales ?: 0.0,
            totalExpenses = tripSalesRaw.totalExpenses ?: 0.0,
            netSales = tripSalesRaw.netSales ?: 0.0,
            stationBreakdown = stationBreakdown,
            expenseBreakdown = expenseBreakdown,
            ticketBreakdown = ticketBreakdown
        )

        emit(detailedTripSales)
    }
}
