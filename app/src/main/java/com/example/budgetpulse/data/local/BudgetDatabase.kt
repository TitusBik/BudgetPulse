package com.example.budgetpulse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.budgetpulse.data.model.TransactionItem

@Database(entities = [TransactionItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BudgetDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: BudgetDatabase? = null

        fun getDatabase(context: Context): BudgetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BudgetDatabase::class.java,
                    "budget_pulse_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
