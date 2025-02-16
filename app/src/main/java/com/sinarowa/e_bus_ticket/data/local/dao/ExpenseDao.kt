package com.sinarowa.e_bus_ticket.data.local.dao
import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpenses(): List<Expense>  // ✅ Fetch all unsynced expenses

    @Query("SELECT * FROM expenses WHERE tripId = :tripId")
    fun getExpensesByTrip(tripId: String): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()
}
