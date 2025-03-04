package com.sinarowa.e_bus_ticket.data.local.dao


import androidx.room.*
import com.sinarowa.e_bus_ticket.data.local.entities.Expense
import com.sinarowa.e_bus_ticket.data.local.enums.SyncStatus
import kotlinx.coroutines.flow.Flow


@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE expense_tripId = :tripId")
    fun getExpensesForTrip(tripId: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE expense_tripId = :tripId")
    fun getTotalExpensesForTrip(tripId: Long): Flow<Double?>

    @Query("SELECT * FROM expenses WHERE expense_tripId = :tripId")
    suspend fun getExpensesForTrip(tripId: String): List<Expense>

    @Query("SELECT * FROM expenses WHERE expense_tripId = :tripId AND syncStatus = :status")
    suspend fun getUnsyncedExpenses(tripId: String, status: SyncStatus): List<Expense>

    @Update
    suspend fun updateExpenseSyncStatus(expense: Expense)
}
