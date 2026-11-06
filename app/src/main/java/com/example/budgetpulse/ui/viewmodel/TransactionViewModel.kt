package com.example.budgetpulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgetpulse.data.local.TransactionDao
import com.example.budgetpulse.data.model.CategoryType
import com.example.budgetpulse.data.model.DailySummary
import com.example.budgetpulse.data.model.TransactionItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

class TransactionViewModel(private val transactionDao: TransactionDao) : ViewModel() {

    val allTransactionsList: StateFlow<List<TransactionItem>> =
        transactionDao.getAllTransactions()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<TransactionUiState> = combine(
        _selectedMonth,
        _selectedDate,
        allTransactionsList
    ) { month, date, transactions ->
        TransactionUiState(
            selectedMonth = month,
            selectedDate = date,
            transactions = transactions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionUiState()
    )

    init {
        viewModelScope.launch {
            allTransactionsList.collect { list ->
                if (list.isEmpty()) {
                    seedInitialData()
                }
            }
        }
    }

    private suspend fun seedInitialData() {
        val today = LocalDate.now()
        val sampleList = listOf(
            TransactionItem(
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
        sampleList.forEach { transactionDao.insertTransaction(it) }
    }

    fun addNewTransaction(item: TransactionItem) {
        viewModelScope.launch {
            transactionDao.insertTransaction(item)
        }
    }

    fun updateTransactionItem(item: TransactionItem) {
        viewModelScope.launch {
            transactionDao.updateTransaction(item)
        }
    }

    fun deleteTransactionItem(item: TransactionItem) {
        viewModelScope.launch {
            transactionDao.deleteTransaction(item)
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _selectedMonth.value = YearMonth.from(date)
    }

    fun previousMonth() {
        val prev = _selectedMonth.value.minusMonths(1)
        _selectedMonth.value = prev
        _selectedDate.value = prev.atDay(1)
    }

    fun nextMonth() {
        val next = _selectedMonth.value.plusMonths(1)
        _selectedMonth.value = next
        _selectedDate.value = next.atDay(1)
    }

    fun goToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        _selectedMonth.value = YearMonth.from(today)
    }
}

class TransactionViewModelFactory(private val transactionDao: TransactionDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(transactionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
