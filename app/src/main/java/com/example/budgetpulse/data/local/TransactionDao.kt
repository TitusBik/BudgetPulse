package com.example.budgetpulse.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.budgetpulse.data.model.TransactionItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(item: TransactionItem): Long

    // Dates are stored as ISO text (yyyy-MM-dd), so BETWEEN works correctly on them.
    @Query(
        "SELECT * FROM transactionitem_table " +
            "WHERE date BETWEEN :startDate AND :endDate " +
            "ORDER BY date DESC, time DESC"
    )
    fun getTransactionsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<TransactionItem>>

    // Used by the Settings page
    @Query("SELECT COUNT(*) FROM transactionitem_table")
    fun getTransactionCount(): Flow<Int>

    @Query("SELECT DISTINCT date FROM transactionitem_table ORDER BY date DESC")
    fun getRecordedDates(): Flow<List<LocalDate>>
}
