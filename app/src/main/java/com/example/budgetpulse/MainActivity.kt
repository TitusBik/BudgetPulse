package com.example.budgetpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.budgetpulse.data.local.BudgetDatabase
import com.example.budgetpulse.ui.components.NavTab
import com.example.budgetpulse.ui.screens.BudgetScreen
import com.example.budgetpulse.ui.screens.HomeScreen
import com.example.budgetpulse.ui.screens.ProfileScreen
import com.example.budgetpulse.ui.screens.TransactionScreen
import com.example.budgetpulse.ui.theme.BudgetPulseTheme
import com.example.budgetpulse.ui.viewmodel.ProfileViewModel

class MainActivity : ComponentActivity() {

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database
        val database = BudgetDatabase.getDatabase(this)

        setContent {
            val profileState by profileViewModel.profileState.collectAsState()

            // Dynamic Font Scale Provider for all pages
            val currentDensity = LocalDensity.current
            val scaledDensity = Density(
                density = currentDensity.density,
                fontScale = profileState.fontScale
            )

            CompositionLocalProvider(LocalDensity provides scaledDensity) {
                BudgetPulseTheme(darkTheme = profileState.isDarkMode) {
                    var currentTab by remember { mutableStateOf(NavTab.SETTINGS) }

                    when (currentTab) {
                        NavTab.HOME -> HomeScreen(onNavigateTab = { currentTab = it })
                        NavTab.TRANSACTIONS -> TransactionScreen(onNavigateTab = { currentTab = it })
                        NavTab.BUDGET -> BudgetScreen(onNavigateTab = { currentTab = it })
                        NavTab.SETTINGS -> ProfileScreen(
                            onNavigateTab = { currentTab = it },
                            viewModel = profileViewModel
                        )
                    }
                }
            }
        }
    }
}
