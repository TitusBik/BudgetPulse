package com.example.budgetpulse.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.budgetpulse.data.model.TransactionItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(item: TransactionItem)

    @Query("SELECT * FROM transactionitem_table ORDER BY date DESC, time DESC")
    fun getAllTransactions(): Flow<List<TransactionItem>>

    @Update
    suspend fun updateTransaction(item: TransactionItem)

    @Delete
    suspend fun deleteTransaction(item: TransactionItem)
}
