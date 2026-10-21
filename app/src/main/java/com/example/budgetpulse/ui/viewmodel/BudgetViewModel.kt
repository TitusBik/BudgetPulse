package com.example.budgetpulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.budgetpulse.data.model.BudgetViewMode
import com.example.budgetpulse.data.model.CategoryBudget
import com.example.budgetpulse.data.model.CategoryItem
import com.example.budgetpulse.data.model.CategoryType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

class BudgetViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadDefaultBudgetData()
    }

    private fun loadDefaultBudgetData() {
        val defaultCategories = listOf(
            CategoryBudget(
                category = CategoryItem("exp_1", "Housing & Utilities", CategoryType.EXPENSE, "home", 0xFFFFF3E0),
                allocatedBudget = null,
                spentAmount = 0.0
            ),
            CategoryBudget(
                category = CategoryItem("exp_2", "Food & Drink", CategoryType.EXPENSE, "fastfood", 0xFFFFF8E1),
                allocatedBudget = null,
                spentAmount = 45.50
            ),
            CategoryBudget(
                category = CategoryItem("exp_3", "Transportation", CategoryType.EXPENSE, "directions_car", 0xFFE3F2FD),
                allocatedBudget = null,
                spentAmount = 35.00
            ),
            CategoryBudget(
                category = CategoryItem("exp_4", "Shopping", CategoryType.EXPENSE, "shopping_bag", 0xFFFFEBEE),
                allocatedBudget = null,
                spentAmount = 120.00
            ),
            CategoryBudget(
                category = CategoryItem("exp_5", "Other", CategoryType.EXPENSE, "build", 0xFFE0F2F1),
                allocatedBudget = null,
                spentAmount = 0.0
            )
        )

        _uiState.update {
            it.copy(categoryBudgets = defaultCategories)
        }
    }

    fun previousMonth() {
        _uiState.update { it.copy(selectedMonth = it.selectedMonth.minusMonths(1)) }
    }

    fun nextMonth() {
        _uiState.update { it.copy(selectedMonth = it.selectedMonth.plusMonths(1)) }
    }

    fun setViewMode(mode: BudgetViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun startEditingBudget(item: CategoryBudget) {
        _uiState.update { it.copy(editingCategoryBudget = item) }
    }

    fun dismissEditingBudget() {
        _uiState.update { it.copy(editingCategoryBudget = null) }
    }

    fun updateAllocatedBudget(categoryId: String, newBudget: Double?) {
        _uiState.update { state ->
            val updated = state.categoryBudgets.map {
                if (it.category.id == categoryId) it.copy(allocatedBudget = newBudget) else it
            }
            state.copy(
                categoryBudgets = updated,
                editingCategoryBudget = null
            )
        }
    }
}
