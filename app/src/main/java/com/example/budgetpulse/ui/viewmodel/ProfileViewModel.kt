package com.example.budgetpulse.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.budgetpulse.data.model.AppCurrency
import com.example.budgetpulse.data.model.ProfileData
import com.example.budgetpulse.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class ProfileViewModel(repository: TransactionRepository) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileData())

    /**
     * Profile settings + live stats from Room:
     *  - transactionsCount: number of transactions the user has recorded
     *  - totalDays: number of different days with at least one record
     *  - currentStreak: consecutive days with a record, up to today (or yesterday)
     */
    val profileState: StateFlow<ProfileData> = combine(
        _profileState,
        repository.transactionCount,
        repository.recordedDates
    ) { profile, count, dates ->
        profile.copy(
            transactionsCount = count,
            totalDays = dates.size,
            currentStreak = calculateStreak(dates)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ProfileData()
    )

    private fun calculateStreak(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        val dateSet = dates.toSet()
        val today = LocalDate.now()
        var day = when {
            today in dateSet -> today
            today.minusDays(1) in dateSet -> today.minusDays(1)
            else -> return 0
        }
        var streak = 0
        while (day in dateSet) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }

    fun updateNickname(newName: String) {
        if (newName.isNotBlank()) {
            _profileState.update { it.copy(nickname = newName.trim()) }
        }
    }

    fun updateProfileImage(uri: Uri?) {
        _profileState.update { it.copy(profileImageUri = uri) }
    }

    fun toggleTheme(isDark: Boolean) {
        _profileState.update { it.copy(isDarkMode = isDark) }
    }

    fun updateFontScale(scale: Float) {
        _profileState.update { it.copy(fontScale = scale) }
    }

    fun updateCurrency(currency: AppCurrency) {
        _profileState.update { it.copy(selectedCurrency = currency) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { ProfileViewModel(transactionRepository()) }
        }
    }
}
