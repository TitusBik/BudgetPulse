package com.example.budgetpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.budgetpulse.data.model.CategoryIcons
import com.example.budgetpulse.data.model.CategoryType
import com.example.budgetpulse.data.model.DailySummary
import com.example.budgetpulse.data.model.TransactionItem
import com.example.budgetpulse.ui.theme.LocalAppCurrency
import com.example.budgetpulse.ui.viewmodel.CategoryTransactionGroup
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val IncomeGreen = Color(0xFF43A047)
private val ExpenseRed = Color(0xFFE53935)

private fun money(symbol: String, value: Double) =
    "$symbol${String.format(Locale.ENGLISH, "%,.2f", value)}"

/** Expense / Income switch + "showing date X" chip above the category list. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListHeader(
    listType: CategoryType,
    onListTypeChange: (CategoryType) -> Unit,
    filterBySelectedDate: Boolean,
    selectedDate: LocalDate,
    selectedDateSummary: DailySummary,
    visibleTotal: Double,
    onShowWholeMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val symbol = LocalAppCurrency.current.symbol

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "By Category",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = listType == CategoryType.EXPENSE,
                onClick = { onListTypeChange(CategoryType.EXPENSE) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("Expense") }
            SegmentedButton(
                selected = listType == CategoryType.INCOME,
                onClick = { onListTypeChange(CategoryType.INCOME) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("Income") }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (filterBySelectedDate) {
                // Specific date selected on the calendar
                AssistChip(
                    onClick = onShowWholeMonth,
                    label = {
                        Text(selectedDate.format(DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.ENGLISH)))
                    },
                    trailingIcon = {
                        Icon(Icons.Default.Close, contentDescription = "Show whole month", modifier = Modifier.size(16.dp))
                    }
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "IN ${money(symbol, selectedDateSummary.totalIncome)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                    Text(
                        text = "EX ${money(symbol, selectedDateSummary.totalExpense)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }
            } else {
                Text(
                    text = "Whole month • tap a date to filter",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = money(symbol, visibleTotal),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (listType == CategoryType.INCOME) IncomeGreen else ExpenseRed
                )
            }
        }
    }
}

/** One category card listing every transaction (with its description) under it. */
@Composable
fun CategoryTransactionGroupCard(
    group: CategoryTransactionGroup,
    sectionTotal: Double,
    modifier: Modifier = Modifier
) {
    val symbol = LocalAppCurrency.current.symbol
    val isIncome = group.type == CategoryType.INCOME
    val accent = if (isIncome) IncomeGreen else ExpenseRed
    val share = if (sectionTotal > 0) (group.total / sectionTotal).toFloat() else 0f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Category header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(group.colorHex))
                ) {
                    Icon(
                        imageVector = CategoryIcons.getIcon(group.iconName),
                        contentDescription = group.categoryName,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.categoryName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${group.transactions.size} transaction${if (group.transactions.size == 1) "" else "s"} • ${(share * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = (if (isIncome) "+" else "-") + money(symbol, group.total),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { share },
                color = accent,
                trackColor = accent.copy(alpha = 0.12f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Individual transactions with description
            group.transactions.forEachIndexed { index, transaction ->
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }
                CategoryTransactionRow(transaction = transaction, symbol = symbol, accent = accent)
            }
        }
    }
}

@Composable
private fun CategoryTransactionRow(transaction: TransactionItem, symbol: String, accent: Color) {
    val dateLabel = transaction.date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.note.ifBlank { "No description" },
                style = MaterialTheme.typography.bodyMedium,
                color = if (transaction.note.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$dateLabel • ${transaction.formattedTime}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = (if (transaction.type == CategoryType.INCOME) "+" else "-") + money(symbol, abs(transaction.amount)),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = accent
        )
    }
}
