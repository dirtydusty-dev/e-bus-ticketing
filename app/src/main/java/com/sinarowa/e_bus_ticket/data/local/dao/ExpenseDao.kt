package com.sinarowa.e_bus_ticket.data.local.dao


import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Expense
import kotlinx.coroutines.flow.Flow


@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE tripId = :tripId")
    fun getExpensesForTrip(tripId: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE tripId = :tripId")
    fun getTotalExpensesForTrip(tripId: Long): Flow<Double?>
}
