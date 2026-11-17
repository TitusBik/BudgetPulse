package com.example.budgetpulse.data.repository

import com.example.budgetpulse.data.local.CategoryDao
import com.example.budgetpulse.data.local.TransactionDao
import com.example.budgetpulse.data.model.CategoryEntity
import com.example.budgetpulse.data.model.CategoryItem
import com.example.budgetpulse.data.model.CategoryType
import com.example.budgetpulse.data.model.DefaultCategories
import com.example.budgetpulse.data.model.TransactionItem
import com.example.budgetpulse.data.model.toEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.util.UUID
import kotlin.math.abs

/**
 * Single place the ViewModels talk to for anything stored in Room.
 */
class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {

    // ---------- Transactions ----------

    val allTransactions: Flow<List<TransactionItem>> = transactionDao.getAllTransactions()

    val transactionCount: Flow<Int> = transactionDao.getTransactionCount()

    val recordedDates: Flow<List<LocalDate>> = transactionDao.getRecordedDates()

    fun transactionsForMonth(month: YearMonth): Flow<List<TransactionItem>> =
        transactionDao.getTransactionsBetween(month.atDay(1), month.atEndOfMonth())

    fun transactionsOnDate(date: LocalDate): Flow<List<TransactionItem>> =
        transactionDao.getTransactionsOnDate(date)

    fun totalForMonth(type: CategoryType, month: YearMonth): Flow<Double> =
        transactionDao.getTotalByTypeBetween(type, month.atDay(1), month.atEndOfMonth())

    /**
     * Saves a transaction for [category]. Expenses are stored as negative numbers,
     * income as positive, so the existing formatting code keeps working.
     */
    suspend fun addTransaction(
        category: CategoryItem,
        amount: Double,
        description: String,
        date: LocalDate = LocalDate.now(),
        time: LocalTime = LocalTime.now().withNano(0)
    ) {
        val signedAmount = if (category.type == CategoryType.EXPENSE) -abs(amount) else abs(amount)
        transactionDao.insertTransaction(
            TransactionItem(
                categoryName = category.name,
                categoryIconName = category.iconName,
                categoryColorHex = category.colorHex,
                amount = signedAmount,
                type = category.type,
                date = date,
                time = time,
                note = description.trim()
            )
        )
    }

    suspend fun updateTransaction(item: TransactionItem) = transactionDao.updateTransaction(item)

    suspend fun deleteTransaction(item: TransactionItem) = transactionDao.deleteTransaction(item)

    // ---------- Categories ----------

    val categories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    fun categoriesByType(type: CategoryType): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByType(type)

    /** Inserts the 5 expense + 1 income default cards the very first time the app runs. */
    suspend fun ensureDefaultCategories() {
        if (categoryDao.getCategoryCount() == 0) {
            categoryDao.insertAll(
                DefaultCategories.active.mapIndexed { index, item -> item.toEntity(sortOrder = index.toLong()) }
            )
        }
    }

    /** Returns false if a category with the same name and type already exists. */
    suspend fun addCategory(category: CategoryItem): Boolean {
        if (categoryDao.categoryExists(category.name, category.type)) return false
        categoryDao.insertCategory(
            category.copy(id = UUID.randomUUID().toString(), isDefault = false).toEntity()
        )
        return true
    }

    suspend fun deleteCategory(categoryId: String) = categoryDao.deleteCategoryById(categoryId)

    suspend fun updateCategoryBudget(categoryId: String, budget: Double?) =
        categoryDao.updateMonthlyBudget(categoryId, budget)
}
