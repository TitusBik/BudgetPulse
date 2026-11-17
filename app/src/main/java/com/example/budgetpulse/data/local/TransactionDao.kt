package com.example.budgetpulse.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.budgetpulse.data.model.CategoryType
import com.example.budgetpulse.data.model.TransactionItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(item: TransactionItem): Long

    @Update
    suspend fun updateTransaction(item: TransactionItem)

    @Delete
    suspend fun deleteTransaction(item: TransactionItem)

    @Query("SELECT * FROM transactionitem_table ORDER BY date DESC, time DESC")
    fun getAllTransactions(): Flow<List<TransactionItem>>

    // Dates are stored as ISO text (yyyy-MM-dd), so BETWEEN works correctly on them.
    @Query(
        "SELECT * FROM transactionitem_table " +
            "WHERE date BETWEEN :startDate AND :endDate " +
            "ORDER BY date DESC, time DESC"
    )
    fun getTransactionsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<TransactionItem>>

    @Query("SELECT * FROM transactionitem_table WHERE date = :date ORDER BY time DESC")
    fun getTransactionsOnDate(date: LocalDate): Flow<List<TransactionItem>>

    @Query(
        "SELECT COALESCE(SUM(ABS(amount)), 0) FROM transactionitem_table " +
            "WHERE type = :type AND date BETWEEN :startDate AND :endDate"
    )
    fun getTotalByTypeBetween(type: CategoryType, startDate: LocalDate, endDate: LocalDate): Flow<Double>

    // Used by the Settings page
    @Query("SELECT COUNT(*) FROM transactionitem_table")
    fun getTransactionCount(): Flow<Int>

    @Query("SELECT DISTINCT date FROM transactionitem_table ORDER BY date DESC")
    fun getRecordedDates(): Flow<List<LocalDate>>
}
