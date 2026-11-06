package com.example.budgetpulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgetpulse.data.local.TransactionDao
import com.example.budgetpulse.data.model.BudgetViewMode
import com.example.budgetpulse.data.model.CategoryBudget
import com.example.budgetpulse.data.model.CategoryItem
import com.example.budgetpulse.data.model.CategoryType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

data class BudgetUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val totalBudget: Double = 2500.0,
    val viewMode: BudgetViewMode = BudgetViewMode.PLAN,
    val categoryBudgets: List<CategoryBudget> = emptyList(),
    val editingCategoryBudget: CategoryBudget? = null
) {
    val totalSpent: Double
        get() = categoryBudgets.sumOf { it.spentAmount }

    val daysInMonth: Int
        get() = selectedMonth.lengthOfMonth()

    val dailyAverage: Double
        get() = if (daysInMonth > 0) totalBudget / daysInMonth else 0.0

    val progressFraction: Float
        get() = if (totalBudget > 0) (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f) else 0f
}

class BudgetViewModel(private val transactionDao: TransactionDao) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    private val _viewMode = MutableStateFlow(BudgetViewMode.PLAN)
    private val _allocatedBudgets = MutableStateFlow<Map<String, Double?>>(emptyMap())
    private val _editingCategoryBudget = MutableStateFlow<CategoryBudget?>(null)

    private val allTransactions = transactionDao.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val defaultCategories = listOf(
        CategoryItem("exp_1", "Housing & Utilities", CategoryType.EXPENSE, "home", 0xFFFFF3E0),
        CategoryItem("exp_2", "Food & Drink", CategoryType.EXPENSE, "fastfood", 0xFFFFF8E1),
        CategoryItem("exp_3", "Transportation", CategoryType.EXPENSE, "directions_car", 0xFFE3F2FD),
        CategoryItem("exp_4", "Shopping", CategoryType.EXPENSE, "shopping_bag", 0xFFFFEBEE),
        CategoryItem("exp_5", "Other", CategoryType.EXPENSE, "build", 0xFFE0F2F1)
    )

    val uiState: StateFlow<BudgetUiState> = combine(
        _selectedMonth,
        _viewMode,
        _allocatedBudgets,
        _editingCategoryBudget,
        allTransactions
    ) { month, mode, allocMap, editing, transactions ->
        val monthTransactions = transactions.filter { YearMonth.from(it.date) == month && it.type == CategoryType.EXPENSE }

        val categoryBudgetsList = defaultCategories.map { category ->
            val spent = monthTransactions
                .filter { it.categoryName.equals(category.name, ignoreCase = true) }
                .sumOf { Math.abs(it.amount) }
            val alloc = allocMap[category.id]
            CategoryBudget(
                category = category,
                allocatedBudget = alloc,
                spentAmount = spent
            )
        }

        BudgetUiState(
            selectedMonth = month,
            totalBudget = 2500.0,
            viewMode = mode,
            categoryBudgets = categoryBudgetsList,
            editingCategoryBudget = editing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetUiState()
    )

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun setViewMode(mode: BudgetViewMode) {
        _viewMode.value = mode
    }

    fun startEditingBudget(item: CategoryBudget) {
        _editingCategoryBudget.value = item
    }

    fun dismissEditingBudget() {
        _editingCategoryBudget.value = null
    }

    fun updateAllocatedBudget(categoryId: String, newBudget: Double?) {
        val current = _allocatedBudgets.value.toMutableMap()
        current[categoryId] = newBudget
        _allocatedBudgets.value = current
        _editingCategoryBudget.value = null
    }
}

class BudgetViewModelFactory(private val transactionDao: TransactionDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(transactionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
