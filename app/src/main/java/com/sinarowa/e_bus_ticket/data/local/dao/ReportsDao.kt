package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.sinarowa.e_bus_ticket.reports.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportsDao {

    @Query("SELECT COUNT(*) FROM tickets WHERE tripId = :tripId")
    fun getTotalTicketsForTrip(tripId: Long): Flow<Int>

    @Query("SELECT SUM(amount) FROM tickets WHERE tripId = :tripId")
    fun getTotalSalesForTrip(tripId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE tripId = :tripId")
    fun getTotalExpensesForTrip(tripId: Long): Flow<Double?>

    /**
     * 🔹 Get sales summary for a trip (Total Tickets, Sales, Expenses, Net Profit)
     */
    @Query("""
        SELECT 
            trips.id AS tripId,
            trips.routeId AS routeId,
            (SELECT COUNT(*) FROM tickets WHERE tripId = trips.id) AS totalTickets,
            (SELECT SUM(amount) FROM tickets WHERE tripId = trips.id) AS totalSales,
            (SELECT SUM(amount) FROM expenses WHERE tripId = trips.id) AS totalExpenses,
            ((SELECT SUM(amount) FROM tickets WHERE tripId = trips.id) - 
             (SELECT SUM(amount) FROM expenses WHERE tripId = trips.id)) AS netSales
        FROM trips 
        WHERE trips.id = :tripId
    """)
    fun getTripSalesSummary(tripId: Long): Flow<TripSalesRaw>

    /**
     * 🔹 Breakdown of ticket sales by ticket type (Adult, Child, etc.)
     */
    @Query("""
        SELECT tickets.startStation AS startStation, 
               tickets.type AS type, 
               COUNT(*) AS count, 
               SUM(tickets.amount) AS amount 
        FROM tickets 
        WHERE tripId = :tripId 
        GROUP BY tickets.startStation, tickets.type
    """)
    fun getTripTicketBreakdown(tripId: Long): Flow<List<TripWithTicketBreakdown>>

    /**
     * 🔹 Breakdown of ticket sales by station (Passengers boarding at each stop)
     */
    @Query("""
        SELECT tickets.tripId AS tripId, 
               tickets.startStation AS station, 
               SUM(tickets.amount) AS totalAmount 
        FROM tickets 
        WHERE tripId = :tripId 
        GROUP BY tickets.startStation
    """)
    fun getTripStationBreakdown(tripId: Long): Flow<List<TripWithStationTotalsRaw>>

    /**
     * 🔹 Breakdown of ticket sales by start and stop stations
     */
    @Query("""
        SELECT tickets.startStation AS startStation, 
               tickets.stopStation AS stopStation,
               tickets.type AS type, 
               COUNT(*) AS count, 
               SUM(tickets.amount) AS amount
        FROM tickets 
        WHERE tripId = :tripId 
        GROUP BY tickets.startStation, tickets.stopStation, tickets.type
    """)
    fun getTripTicketBreakdownByStation(tripId: Long): Flow<List<TripWithTicketBreakdown>>

    /**
     * 🔹 Breakdown of expenses by category (Fuel, Food, etc.)
     */
    @Query("""
        SELECT expenses.type AS type, COUNT(*) AS count, SUM(expenses.amount) AS amount 
        FROM expenses 
        WHERE tripId = :tripId 
        GROUP BY expenses.type
    """)
    fun getTripExpenseBreakdown(tripId: Long): Flow<List<TripWithExpenseBreakdown>>
}
