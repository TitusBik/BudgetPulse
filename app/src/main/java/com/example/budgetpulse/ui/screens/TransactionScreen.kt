package com.example.budgetpulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgetpulse.data.model.CategoryType
import com.example.budgetpulse.ui.components.AppBottomNavigationBar
import com.example.budgetpulse.ui.components.CalendarView
import com.example.budgetpulse.ui.components.CategoryListHeader
import com.example.budgetpulse.ui.components.CategoryTransactionGroupCard
import com.example.budgetpulse.ui.components.FinancialSummaryCard
import com.example.budgetpulse.ui.components.NavTab
import com.example.budgetpulse.ui.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    onNavigateTab: (NavTab) -> Unit = {},
    viewModel: TransactionViewModel = viewModel(factory = TransactionViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        // Same navbar setup as Budget & Settings pages
        bottomBar = {
            AppBottomNavigationBar(
                currentTab = NavTab.TRANSACTIONS,
                onTabSelected = onNavigateTab
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Calendar: income / expense of every day this month. Tap a day to filter.
                item(key = "calendar") {
                    CalendarView(
                        selectedMonth = uiState.selectedMonth,
                        selectedDate = uiState.selectedDate,
                        dailySummaries = uiState.dailySummaries,
                        onDateSelected = { date -> viewModel.selectDate(date) },
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onTodayClick = { viewModel.goToToday() }
                    )
                }

                // 2. Monthly totals: income, expense, balance
                item(key = "summary") {
                    Spacer(modifier = Modifier.height(4.dp))
                    FinancialSummaryCard(
                        income = uiState.monthlyIncome,
                        expense = uiState.monthlyExpense,
                        balance = uiState.monthlyBalance
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // 3. Expense / Income categories with each transaction's description
                item(key = "category_header") {
                    CategoryListHeader(
                        listType = uiState.listType,
                        onListTypeChange = { viewModel.setListType(it) },
                        filterBySelectedDate = uiState.filterBySelectedDate,
                        selectedDate = uiState.selectedDate,
                        selectedDateSummary = uiState.selectedDateSummary,
                        visibleTotal = uiState.visibleTotal,
                        onShowWholeMonth = { viewModel.showWholeMonth() }
                    )
                }

                val groups = uiState.categoryGroups
                if (groups.isEmpty()) {
                    item(key = "empty") {
                        val typeLabel = if (uiState.listType == CategoryType.INCOME) "income" else "expenses"
                        val period = if (uiState.filterBySelectedDate) "on this date" else "this month"
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                        ) {
                            Text(
                                text = "No $typeLabel recorded $period.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    items(items = groups, key = { "cat_${it.type}_${it.categoryName}" }) { group ->
                        CategoryTransactionGroupCard(
                            group = group,
                            sectionTotal = uiState.visibleTotal
                        )
                    }
                }
            }

        }
    }
}
