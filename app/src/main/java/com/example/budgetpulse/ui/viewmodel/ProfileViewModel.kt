package com.example.budgetpulse.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.budgetpulse.data.model.AppCurrency
import com.example.budgetpulse.data.model.ProfileData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileData())
    val profileState: StateFlow<ProfileData> = _profileState.asStateFlow()

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
}
