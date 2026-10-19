package com.example.budgetpulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.budgetpulse.data.model.CategoryType
import com.example.budgetpulse.data.model.DailySummary
import com.example.budgetpulse.data.model.TransactionItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

data class TransactionUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val transactions: List<TransactionItem> = emptyList()
) {
    val currentMonthTransactions: List<TransactionItem>
        get() = transactions.filter { YearMonth.from(it.date) == selectedMonth }

    val monthlyIncome: Double
        get() = currentMonthTransactions.filter { it.type == CategoryType.INCOME }.sumOf { it.amount }

    val monthlyExpense: Double
        get() = currentMonthTransactions.filter { it.type == CategoryType.EXPENSE }.sumOf { Math.abs(it.amount) }

    val monthlyBalance: Double
        get() = monthlyIncome - monthlyExpense

    val dailySummaries: Map<LocalDate, DailySummary>
        get() {
            return currentMonthTransactions.groupBy { it.date }.mapValues { (date, items) ->
                val inc = items.filter { it.type == CategoryType.INCOME }.sumOf { it.amount }
                val exp = items.filter { it.type == CategoryType.EXPENSE }.sumOf { Math.abs(it.amount) }
                DailySummary(date, inc, exp)
            }
        }

    val groupedTransactions: Map<LocalDate, List<TransactionItem>>
        get() {
            return currentMonthTransactions
                .sortedByDescending { it.time }
                .groupBy { it.date }
                .toSortedMap(comparator = reverseOrder())
        }
}

class TransactionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        generateInitialData()
    }

    private fun generateInitialData() {
        val today = LocalDate.now()

        // Generate sample transactions dynamically centered around system date
        val sampleList = listOf(
            TransactionItem(
                id = "tx1",
                categoryName = "Salary",
                categoryIconName = "attach_money",
                categoryColorHex = 0xFFFFF8E1,
                amount = 2500.00,
                type = CategoryType.INCOME,
                date = today,
                time = LocalTime.of(13, 7),
                note = "Monthly Paycheck"
            ),
            TransactionItem(
                id = "tx2",
                categoryName = "Food & Drink",
                categoryIconName = "fastfood",
                categoryColorHex = 0xFFFFF3E0,
                amount = -45.50,
                type = CategoryType.EXPENSE,
                date = today,
                time = LocalTime.of(12, 15),
                note = "Lunch with Team"
            ),
            TransactionItem(
                id = "tx3",
                categoryName = "Shopping",
                categoryIconName = "shopping_cart",
                categoryColorHex = 0xFFF3E5F5,
                amount = -120.00,
                type = CategoryType.EXPENSE,
                date = today.minusDays(1),
                time = LocalTime.of(16, 30),
                note = "Supermarket Grocery"
            ),
            TransactionItem(
                id = "tx4",
                categoryName = "Transportation",
                categoryIconName = "directions_car",
                categoryColorHex = 0xFFE8F5E9,
                amount = -35.00,
                type = CategoryType.EXPENSE,
                date = today.minusDays(1),
                time = LocalTime.of(8, 45),
                note = "Petrol Refill"
            ),
            TransactionItem(
                id = "tx5",
                categoryName = "Freelance",
                categoryIconName = "work",
                categoryColorHex = 0xFFE0F2F1,
                amount = 850.00,
                type = CategoryType.INCOME,
                date = today.minusDays(3),
                time = LocalTime.of(10, 0),
                note = "Side Project Bonus"
            ),
            TransactionItem(
                id = "tx6",
                categoryName = "Housing & Utilities",
                categoryIconName = "home",
                categoryColorHex = 0xFFE3F2FD,
                amount = -350.00,
                type = CategoryType.EXPENSE,
                date = today.minusDays(5),
                time = LocalTime.of(9, 20),
                note = "Electricity Bill"
            ),
            TransactionItem(
                id = "tx7",
                categoryName = "Entertainment",
                categoryIconName = "movie",
                categoryColorHex = 0xFFEDE7F6,
                amount = -60.00,
                type = CategoryType.EXPENSE,
                date = today.minusDays(7),
                time = LocalTime.of(20, 15),
                note = "Cinema Tickets"
            )
        )

        _uiState.update {
            it.copy(
                selectedMonth = YearMonth.from(today),
                selectedDate = today,
                transactions = sampleList
            )
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                selectedMonth = YearMonth.from(date)
            )
        }
    }

    fun previousMonth() {
        _uiState.update {
            val prev = it.selectedMonth.minusMonths(1)
            it.copy(
                selectedMonth = prev,
                selectedDate = prev.atDay(1)
            )
        }
    }

    fun nextMonth() {
        _uiState.update {
            val next = it.selectedMonth.plusMonths(1)
            it.copy(
                selectedMonth = next,
                selectedDate = next.atDay(1)
            )
        }
    }

    fun goToToday() {
        val today = LocalDate.now()
        _uiState.update {
            it.copy(
                selectedDate = today,
                selectedMonth = YearMonth.from(today)
            )
        }
    }
}
