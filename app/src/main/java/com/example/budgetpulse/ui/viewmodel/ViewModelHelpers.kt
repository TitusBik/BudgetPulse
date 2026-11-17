package com.example.budgetpulse.ui.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.budgetpulse.BudgetPulseApplication
import com.example.budgetpulse.data.repository.TransactionRepository
import java.util.Locale

/** Grabs the shared Room repository from the Application's AppContainer inside a ViewModel factory. */
internal fun CreationExtras.transactionRepository(): TransactionRepository =
    (this[APPLICATION_KEY] as BudgetPulseApplication).container.transactionRepository

internal fun formatRm(amount: Double): String =
    if (amount > 0) "RM${String.format(Locale.ENGLISH, "%,.2f", amount)}" else "RM0"
