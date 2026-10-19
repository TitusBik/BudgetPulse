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
import com.example.budgetpulse.ui.components.FinancialSummaryCard
import com.example.budgetpulse.ui.components.NavTab
import com.example.budgetpulse.ui.components.TransactionGroupHeader
import com.example.budgetpulse.ui.components.TransactionItemCard
import com.example.budgetpulse.ui.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    onNavigateTab: (NavTab) -> Unit = {},
    viewModel: TransactionViewModel = viewModel()
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Requirement 1: Calendar View (Top Section)
                item {
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

                // Requirement 2: Financial Summary (Middle Section)
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    FinancialSummaryCard(
                        income = uiState.monthlyIncome,
                        expense = uiState.monthlyExpense,
                        balance = uiState.monthlyBalance
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Requirement 3: Transaction Records Grouped by Date (List Section)
                val grouped = uiState.groupedTransactions
                if (grouped.isEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                        ) {
                            Text(
                                text = "No transactions found for this month.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    grouped.forEach { (date, items) ->
                        val dayIncome = items.filter { it.type == CategoryType.INCOME }.sumOf { it.amount }
                        val dayExpense = items.filter { it.type == CategoryType.EXPENSE }.sumOf { Math.abs(it.amount) }

                        item(key = "header_${date}") {
                            TransactionGroupHeader(
                                date = date,
                                totalIncome = dayIncome,
                                totalExpense = dayExpense
                            )
                        }

                        items(items = items, key = { it.id }) { transaction ->
                            TransactionItemCard(transaction = transaction)
                        }
                    }
                }
            }

            // Requirement 4: Bottom Navigation Bar with "Transactions" active and highlighted
            AppBottomNavigationBar(
                currentTab = NavTab.TRANSACTIONS,
                onTabSelected = onNavigateTab,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
