package com.example.budgetpulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.budgetpulse.data.model.CategoryType
import com.example.budgetpulse.data.model.DailySummary
import com.example.budgetpulse.data.model.TransactionItem
import com.example.budgetpulse.data.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs

/** One category block in the bottom list of the Transactions page. */
data class CategoryTransactionGroup(
    val categoryName: String,
    val iconName: String,
    val colorHex: Long,
    val type: CategoryType,
    val total: Double,
    val transactions: List<TransactionItem>
)

data class TransactionUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    // All transactions of selectedMonth (loaded from Room by date range)
    val transactions: List<TransactionItem> = emptyList(),
    // true -> bottom list only shows the tapped calendar date
    val filterBySelectedDate: Boolean = false,
    // Expense / Income tab for the bottom category list
    val listType: CategoryType = CategoryType.EXPENSE
) {
    val monthlyIncome: Double
        get() = transactions.filter { it.type == CategoryType.INCOME }.sumOf { abs(it.amount) }

    val monthlyExpense: Double
        get() = transactions.filter { it.type == CategoryType.EXPENSE }.sumOf { abs(it.amount) }

    val monthlyBalance: Double
        get() = monthlyIncome - monthlyExpense

    /** Income / expense per day -> shown in each calendar cell. */
    val dailySummaries: Map<LocalDate, DailySummary>
        get() = transactions.groupBy { it.date }.mapValues { (date, items) ->
            DailySummary(
                date = date,
                totalIncome = items.filter { it.type == CategoryType.INCOME }.sumOf { abs(it.amount) },
                totalExpense = items.filter { it.type == CategoryType.EXPENSE }.sumOf { abs(it.amount) }
            )
        }

    val selectedDateSummary: DailySummary
        get() = dailySummaries[selectedDate] ?: DailySummary(selectedDate)

    /** Transactions used by the bottom list (whole month, or just the selected date). */
    val visibleTransactions: List<TransactionItem>
        get() = if (filterBySelectedDate) transactions.filter { it.date == selectedDate } else transactions

    /** Bottom list: transactions grouped by category, biggest total first. */
    val categoryGroups: List<CategoryTransactionGroup>
        get() = visibleTransactions
            .filter { it.type == listType }
            .groupBy { it.categoryName.lowercase() }
            .map { (_, items) ->
                val first = items.first()
                CategoryTransactionGroup(
                    categoryName = first.categoryName,
                    iconName = first.categoryIconName,
                    colorHex = first.categoryColorHex,
                    type = first.type,
                    total = items.sumOf { abs(it.amount) },
                    transactions = items.sortedWith(compareByDescending<TransactionItem> { it.date }.thenByDescending { it.time })
                )
            }
            .sortedByDescending { it.total }

    val visibleTotal: Double
        get() = categoryGroups.sumOf { it.total }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _filterBySelectedDate = MutableStateFlow(false)
    private val _listType = MutableStateFlow(CategoryType.EXPENSE)

    // Re-queries Room whenever the month changes
    private val monthTransactions = _selectedMonth.flatMapLatest { month ->
        repository.transactionsForMonth(month)
    }

    val uiState: StateFlow<TransactionUiState> = combine(
        _selectedMonth,
        _selectedDate,
        _filterBySelectedDate,
        _listType,
        monthTransactions
    ) { month, date, filterByDate, listType, transactions ->
        TransactionUiState(
            selectedMonth = month,
            selectedDate = date,
            transactions = transactions,
            filterBySelectedDate = filterByDate,
            listType = listType
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionUiState()
    )

    /** Tapping a calendar day shows only that day's transactions in the bottom list. */
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _selectedMonth.value = YearMonth.from(date)
        _filterBySelectedDate.value = true
    }

    fun showWholeMonth() {
        _filterBySelectedDate.value = false
    }

    fun setListType(type: CategoryType) {
        _listType.value = type
    }

    fun previousMonth() {
        val prev = _selectedMonth.value.minusMonths(1)
        _selectedMonth.value = prev
        _selectedDate.value = prev.atDay(1)
        _filterBySelectedDate.value = false
    }

    fun nextMonth() {
        val next = _selectedMonth.value.plusMonths(1)
        _selectedMonth.value = next
        _selectedDate.value = next.atDay(1)
        _filterBySelectedDate.value = false
    }

    fun goToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        _selectedMonth.value = YearMonth.from(today)
        _filterBySelectedDate.value = false
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { TransactionViewModel(transactionRepository()) }
        }
    }
}
