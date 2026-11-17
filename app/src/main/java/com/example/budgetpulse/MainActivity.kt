package com.example.budgetpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.budgetpulse.ui.components.NavTab
import com.example.budgetpulse.ui.screens.BudgetScreen
import com.example.budgetpulse.ui.screens.HomeScreen
import com.example.budgetpulse.ui.screens.ProfileScreen
import com.example.budgetpulse.ui.screens.TransactionScreen
import com.example.budgetpulse.ui.screens.WelcomeScreen
import com.example.budgetpulse.ui.theme.BudgetPulseTheme
import com.example.budgetpulse.ui.theme.LocalAppCurrency
import com.example.budgetpulse.ui.viewmodel.ProfileViewModel

class MainActivity : ComponentActivity() {

    // Factory gives the ViewModel the Room repository (used for the transaction count on Settings)
    private val profileViewModel: ProfileViewModel by viewModels { ProfileViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Room database is created by BudgetPulseApplication -> AppContainer

        setContent {
            val profileState by profileViewModel.profileState.collectAsState()

            // Dynamic Font Scale Provider for all pages
            val currentDensity = LocalDensity.current
            val scaledDensity = Density(
                density = currentDensity.density,
                fontScale = profileState.fontScale
            )

            CompositionLocalProvider(
                LocalDensity provides scaledDensity,
                LocalAppCurrency provides profileState.selectedCurrency
            ) {
                BudgetPulseTheme(darkTheme = profileState.isDarkMode) {
                    // Welcome page is shown first every time the app opens
                    var showWelcome by rememberSaveable { mutableStateOf(true) }
                    var currentTab by rememberSaveable { mutableStateOf(NavTab.HOME) }

                    // Back button on any tab returns to the Welcome page
                    BackHandler(enabled = !showWelcome) {
                        showWelcome = true
                    }

                    if (showWelcome) {
                        WelcomeScreen(
                            profileData = profileState,
                            onSelectDestination = { destination ->
                                currentTab = destination
                                showWelcome = false
                            }
                        )
                    } else {
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
}
