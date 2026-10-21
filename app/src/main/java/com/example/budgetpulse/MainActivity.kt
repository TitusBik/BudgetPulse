package com.example.budgetpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.budgetpulse.ui.components.NavTab
import com.example.budgetpulse.ui.screens.BudgetScreen
import com.example.budgetpulse.ui.screens.HomeScreen
import com.example.budgetpulse.ui.screens.TransactionScreen
import com.example.budgetpulse.ui.theme.BudgetPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BudgetPulseTheme {
                var currentTab by remember { mutableStateOf(NavTab.BUDGET) }

                when (currentTab) {
                    NavTab.HOME -> HomeScreen(onNavigateTab = { currentTab = it })
                    NavTab.TRANSACTIONS -> TransactionScreen(onNavigateTab = { currentTab = it })
                    NavTab.BUDGET -> BudgetScreen(onNavigateTab = { currentTab = it })
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetScreenPreview() {
    BudgetPulseTheme {
        BudgetScreen()
    }
}
