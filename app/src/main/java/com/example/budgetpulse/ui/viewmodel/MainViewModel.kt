package com.example.budgetpulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgetpulse.data.local.TransactionDao
import com.example.budgetpulse.data.model.CategoryItem
import com.example.budgetpulse.data.model.CategoryType
import com.example.budgetpulse.data.model.TransactionItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class UiState(
    val isEditMode: Boolean = false,
    val activeCategories: List<CategoryItem> = emptyList(),
    val availableCategories: List<CategoryItem> = emptyList(),
    val showAddCategorySheet: Boolean = false,
    val showCustomCategoryDialog: Boolean = false,
    val editingCategory: CategoryItem? = null
)

class MainViewModel(private val transactionDao: TransactionDao) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val allTransactions = transactionDao.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadInitialData()
        observeTransactions()
    }

    private fun loadInitialData() {
        val defaultExpense = listOf(
            CategoryItem("exp_1", "Housing & Utilities", CategoryType.EXPENSE, "home", 0xFFE3F2FD, "RM0", true),
            CategoryItem("exp_2", "Food & Drink", CategoryType.EXPENSE, "fastfood", 0xFFFFF3E0, "RM0", true),
            CategoryItem("exp_3", "Transportation", CategoryType.EXPENSE, "directions_car", 0xFFE8F5E9, "RM0", true),
            CategoryItem("exp_4", "Shopping", CategoryType.EXPENSE, "shopping_cart", 0xFFF3E5F5, "RM0", true),
            CategoryItem("exp_5", "Other", CategoryType.EXPENSE, "more_horiz", 0xFFE0F2F1, "RM0", true)
        )

        val defaultIncome = listOf(
            CategoryItem("inc_1", "Salary", CategoryType.INCOME, "attach_money", 0xFFFFF8E1, "RM0", true)
        )

        val presetAvailable = listOf(
            CategoryItem("preset_1", "Entertainment", CategoryType.EXPENSE, "movie", 0xFFEDE7F6),
            CategoryItem("preset_2", "Education", CategoryType.EXPENSE, "school", 0xFFE8EAF6),
            CategoryItem("preset_3", "Healthcare", CategoryType.EXPENSE, "medical_services", 0xFFFFEBEE),
            CategoryItem("preset_4", "Investment", CategoryType.EXPENSE, "trending_up", 0xFFE0F7FA),
            CategoryItem("preset_5", "Travel", CategoryType.EXPENSE, "flight", 0xFFE1F5FE),
            CategoryItem("preset_6", "Pets", CategoryType.EXPENSE, "pets", 0xFFFBE9E7),
            CategoryItem("preset_7", "Fitness", CategoryType.EXPENSE, "fitness_center", 0xFFF3E5F5),
            CategoryItem("preset_8", "Subscriptions", CategoryType.EXPENSE, "subscriptions", 0xFFE8EAF6),
            CategoryItem("preset_9", "Beauty & Care", CategoryType.EXPENSE, "spa", 0xFFFCE4EC),
            CategoryItem("preset_10", "Gifts & Donation", CategoryType.EXPENSE, "card_giftcard", 0xFFFFF3E0),

            CategoryItem("preset_inc_1", "Bonus", CategoryType.INCOME, "card_giftcard", 0xFFFFF8E1),
            CategoryItem("preset_inc_2", "Investment Returns", CategoryType.INCOME, "show_chart", 0xFFE8F5E9),
            CategoryItem("preset_inc_3", "Freelance", CategoryType.INCOME, "work", 0xFFE0F2F1),
            CategoryItem("preset_inc_4", "Rental Income", CategoryType.INCOME, "real_estate_agent", 0xFFE3F2FD),
            CategoryItem("preset_inc_5", "Side Business", CategoryType.INCOME, "store", 0xFFEDE7F6),
            CategoryItem("preset_inc_6", "Gift Income", CategoryType.INCOME, "redeem", 0xFFFFF3E0)
        )

        _uiState.update {
            it.copy(
                activeCategories = defaultExpense + defaultIncome,
                availableCategories = presetAvailable
            )
        }
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            allTransactions.collect { transactions ->
                updateCategoryAmountsFromTransactions(transactions)
            }
        }
    }

    private fun updateCategoryAmountsFromTransactions(transactions: List<TransactionItem>) {
        _uiState.update { state ->
            val updatedActive = state.activeCategories.map { category ->
                val totalForCategory = transactions
                    .filter { it.categoryName.equals(category.name, ignoreCase = true) && it.type == category.type }
                    .sumOf { Math.abs(it.amount) }
                val formatted = if (totalForCategory > 0) "RM${String.format("%,.2f", totalForCategory)}" else "RM0"
                category.copy(amount = formatted)
            }
            state.copy(activeCategories = updatedActive)
        }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun setEditMode(enabled: Boolean) {
        _uiState.update { it.copy(isEditMode = enabled) }
    }

    fun deleteCategory(categoryId: String) {
        _uiState.update { state ->
            state.copy(activeCategories = state.activeCategories.filterNot { it.id == categoryId })
        }
    }

    fun addCategoryToActive(category: CategoryItem) {
        _uiState.update { state ->
            val exists = state.activeCategories.any {
                it.name.equals(category.name, ignoreCase = true) && it.type == category.type
            }
            if (exists) {
                state
            } else {
                val newCategory = category.copy(
                    id = UUID.randomUUID().toString(),
                    amount = "RM0"
                )
                val newActive = state.activeCategories + newCategory
                state.copy(activeCategories = newActive)
            }
        }
        updateCategoryAmountsFromTransactions(allTransactions.value)
    }

    fun addCustomCategory(name: String, type: CategoryType, iconName: String, colorHex: Long) {
        val newCategory = CategoryItem(
            id = UUID.randomUUID().toString(),
            name = name,
            type = type,
            iconName = iconName,
            colorHex = colorHex,
            amount = "RM0"
        )
        _uiState.update { state ->
            state.copy(
                activeCategories = state.activeCategories + newCategory,
                availableCategories = state.availableCategories + newCategory,
                showCustomCategoryDialog = false
            )
        }
        updateCategoryAmountsFromTransactions(allTransactions.value)
    }

    fun updateCategoryAmount(categoryId: String, newAmount: String) {
        val cleanAmount = newAmount.trim()
        val formattedAmount = when {
            cleanAmount.startsWith("RM", ignoreCase = true) -> cleanAmount
            cleanAmount.isEmpty() -> "RM0"
            else -> "RM$cleanAmount"
        }
        _uiState.update { state ->
            state.copy(
                activeCategories = state.activeCategories.map {
                    if (it.id == categoryId) it.copy(amount = formattedAmount) else it
                },
                editingCategory = null
            )
        }
    }

    fun openAddCategorySheet() {
        _uiState.update { it.copy(showAddCategorySheet = true) }
    }

    fun closeAddCategorySheet() {
        _uiState.update { it.copy(showAddCategorySheet = false) }
    }

    fun openCustomCategoryDialog() {
        _uiState.update { it.copy(showCustomCategoryDialog = true) }
    }

    fun closeCustomCategoryDialog() {
        _uiState.update { it.copy(showCustomCategoryDialog = false) }
    }

    fun startEditingAmount(category: CategoryItem) {
        _uiState.update { it.copy(editingCategory = category) }
    }

    fun dismissEditingAmount() {
        _uiState.update { it.copy(editingCategory = null) }
    }
}

class MainViewModelFactory(private val transactionDao: TransactionDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(transactionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
