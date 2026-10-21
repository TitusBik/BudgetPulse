package com.example.budgetpulse.data.model

import java.time.YearMonth

enum class BudgetViewMode {
    PLAN,
    REMAIN
}

data class CategoryBudget(
    val category: CategoryItem,
    val allocatedBudget: Double? = null, // null represents "None"
    val spentAmount: Double = 0.0
) {
    val remainingAmount: Double
        get() = (allocatedBudget ?: 0.0) - spentAmount
}

data class MonthlyBudgetSummary(
    val month: YearMonth = YearMonth.now(),
    val totalBudget: Double = 2500.0,
    val categoryBudgets: List<CategoryBudget> = emptyList()
) {
    val totalSpent: Double
        get() = categoryBudgets.sumOf { it.spentAmount }

    val daysInMonth: Int
        get() = month.lengthOfMonth()

    val dailyAverage: Double
        get() = if (daysInMonth > 0) totalBudget / daysInMonth else 0.0

    val progressFraction: Float
        get() = if (totalBudget > 0) (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f) else 0f
}
