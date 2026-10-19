package com.example.budgetpulse.data.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class TransactionItem(
    val id: String,
    val categoryName: String,
    val categoryIconName: String,
    val categoryColorHex: Long,
    val amount: Double,
    val type: CategoryType,
    val date: LocalDate,
    val time: LocalTime,
    val note: String = ""
) {
    val formattedAmount: String
        get() = if (type == CategoryType.INCOME) {
            "+RM${String.format("%,.2f", amount)}"
        } else {
            "-RM${String.format("%,.2f", Math.abs(amount))}"
        }

    val formattedTime: String
        get() = time.format(DateTimeFormatter.ofPattern("HH:mm"))
}

data class DailySummary(
    val date: LocalDate,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0
) {
    val netTotal: Double get() = totalIncome - totalExpense
}
