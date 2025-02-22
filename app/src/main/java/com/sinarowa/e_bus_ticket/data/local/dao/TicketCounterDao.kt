package com.sinarowa.e_bus_ticket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinarowa.e_bus_ticket.data.local.entities.TicketCounter
import com.sinarowa.e_bus_ticket.data.local.entities.TicketSummary

@Dao
interface TicketCounterDao {

    @Query("SELECT lastTicketNumber FROM ticketcounter WHERE tripId = :tripId")
    fun getLastTicketNumber(tripId: String): Int?

    // ✅ Insert or update ticket counter for a trip
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(ticketCounter: TicketCounter)
}
