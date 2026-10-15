package com.example.budgetpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import com.example.budgetpulse.ui.screens.HomeScreen
import com.example.budgetpulse.ui.theme.BudgetPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BudgetPulseTheme {
                HomeScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    BudgetPulseTheme {
        HomeScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun EditModePreview() {
    val sampleViewModel = com.example.budgetpulse.ui.viewmodel.MainViewModel().apply {
        setEditMode(true)
    }
    BudgetPulseTheme {
        HomeScreen(viewModel = sampleViewModel)
    }
}

@Preview(showBackground = true)
@Composable
fun AddCategorySheetPreview() {
    val sampleViewModel = com.example.budgetpulse.ui.viewmodel.MainViewModel()
    val uiState = sampleViewModel.uiState.collectAsState().value
    BudgetPulseTheme {
        com.example.budgetpulse.ui.components.AddCategorySheet(
            activeCategories = uiState.activeCategories,
            availableCategories = uiState.availableCategories,
            onAddCategory = {},
            onCustomClick = {},
            onDismiss = {}
        )
    }
}
