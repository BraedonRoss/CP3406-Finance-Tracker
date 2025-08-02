package com.cp3406.financetracker.ui.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.*

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    
    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    private val _preferences = MutableLiveData<UserPreferences>()
    val preferences: LiveData<UserPreferences> = _preferences

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        // Load preferences from SharedPreferences
        val savedPreferences = UserPreferences(
            notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", true),
            darkModeEnabled = sharedPrefs.getBoolean("dark_mode_enabled", false),
            selectedCurrency = sharedPrefs.getString("selected_currency", "USD") ?: "USD",
            biometricLockEnabled = sharedPrefs.getBoolean("biometric_lock_enabled", true)
        )
        
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
            preferences = savedPreferences
        )

        _userProfile.value = mockProfile
        _preferences.value = savedPreferences
    }

    fun updateNotificationSetting(enabled: Boolean) {
        _preferences.value?.let { currentPrefs ->
            val updatedPrefs = currentPrefs.copy(notificationsEnabled = enabled)
            _preferences.value = updatedPrefs
            
            // Save to SharedPreferences
            sharedPrefs.edit().putBoolean("notifications_enabled", enabled).apply()
            
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
            
            // Save to SharedPreferences
            sharedPrefs.edit().putBoolean("dark_mode_enabled", enabled).apply()
            
            _userProfile.value?.let { profile ->
                _userProfile.value = profile.copy(preferences = updatedPrefs)
            }
        }
    }

    fun updateBiometricSetting(enabled: Boolean) {
        _preferences.value?.let { currentPrefs ->
            val updatedPrefs = currentPrefs.copy(biometricLockEnabled = enabled)
            _preferences.value = updatedPrefs
            
            // Save to SharedPreferences
            sharedPrefs.edit().putBoolean("biometric_lock_enabled", enabled).apply()
            
            _userProfile.value?.let { profile ->
                _userProfile.value = profile.copy(preferences = updatedPrefs)
            }
        }
    }

    fun updateCurrency(currency: String) {
        _preferences.value?.let { currentPrefs ->
            val updatedPrefs = currentPrefs.copy(selectedCurrency = currency)
            _preferences.value = updatedPrefs
            
            // Save to SharedPreferences
            sharedPrefs.edit().putString("selected_currency", currency).apply()
            
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