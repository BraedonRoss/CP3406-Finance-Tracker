package com.cp3406.financetracker.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class ProfileViewModel : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    private val _preferences = MutableLiveData<UserPreferences>()
    val preferences: LiveData<UserPreferences> = _preferences

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        // Mock user data - in real app this would come from authentication/database
        val mockProfile = UserProfile(
            id = "user123",
            firstName = "John",
            lastName = "Doe", 
            email = "john.doe@email.com",
            avatarInitials = "JD",
            memberSince = Calendar.getInstance().apply {
                set(2024, Calendar.JANUARY, 15)
            }.time,
            preferences = UserPreferences(
                notificationsEnabled = true,
                darkModeEnabled = false,
                selectedCurrency = "USD",
                biometricLockEnabled = true
            )
        )

        _userProfile.value = mockProfile
        _preferences.value = mockProfile.preferences
    }

    fun updateNotificationSetting(enabled: Boolean) {
        _preferences.value?.let { currentPrefs ->
            val updatedPrefs = currentPrefs.copy(notificationsEnabled = enabled)
            _preferences.value = updatedPrefs
            
            // Update the profile with new preferences
            _userProfile.value?.let { profile ->
                _userProfile.value = profile.copy(preferences = updatedPrefs)
            }
        }
    }

    fun updateDarkModeSetting(enabled: Boolean) {
        _preferences.value?.let { currentPrefs ->
            val updatedPrefs = currentPrefs.copy(darkModeEnabled = enabled)
            _preferences.value = updatedPrefs
            
            _userProfile.value?.let { profile ->
                _userProfile.value = profile.copy(preferences = updatedPrefs)
            }
        }
    }

    fun updateBiometricSetting(enabled: Boolean) {
        _preferences.value?.let { currentPrefs ->
            val updatedPrefs = currentPrefs.copy(biometricLockEnabled = enabled)
            _preferences.value = updatedPrefs
            
            _userProfile.value?.let { profile ->
                _userProfile.value = profile.copy(preferences = updatedPrefs)
            }
        }
    }

    fun updateCurrency(currency: String) {
        _preferences.value?.let { currentPrefs ->
            val updatedPrefs = currentPrefs.copy(selectedCurrency = currency)
            _preferences.value = updatedPrefs
            
            _userProfile.value?.let { profile ->
                _userProfile.value = profile.copy(preferences = updatedPrefs)
            }
        }
    }

    fun formatMemberSince(date: Date): String {
        val formatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        return "Member since ${formatter.format(date)}"
    }

    fun getCurrencyDisplayName(currencyCode: String): String {
        return when (currencyCode) {
            "USD" -> "USD ($)"
            "EUR" -> "EUR (€)"
            "GBP" -> "GBP (£)"
            "JPY" -> "JPY (¥)"
            "AUD" -> "AUD (A$)"
            "CAD" -> "CAD (C$)"
            else -> currencyCode
        }
    }
}