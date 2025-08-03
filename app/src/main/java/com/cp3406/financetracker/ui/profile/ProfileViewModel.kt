package com.cp3406.financetracker.ui.profile

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cp3406.financetracker.data.database.FinanceDatabase
import com.cp3406.financetracker.data.repository.BudgetRepository
import com.cp3406.financetracker.data.repository.GoalRepository
import com.cp3406.financetracker.data.repository.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    
    private val transactionRepository: TransactionRepository
    private val budgetRepository: BudgetRepository
    private val goalRepository: GoalRepository
    
    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    private val _preferences = MutableLiveData<UserPreferences>()
    val preferences: LiveData<UserPreferences> = _preferences

    init {
        val database = FinanceDatabase.getDatabase(application)
        transactionRepository = TransactionRepository(database.transactionDao())
        budgetRepository = BudgetRepository(database.budgetDao())
        goalRepository = GoalRepository(database.goalDao())
        
        loadUserProfile()
        applyDarkModeFromPreferences()
    }

    private fun loadUserProfile() {
        // Load preferences from SharedPreferences
        val savedPreferences = UserPreferences(
            notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", true),
            darkModeEnabled = sharedPrefs.getBoolean("dark_mode_enabled", false),
            selectedCurrency = sharedPrefs.getString("selected_currency", "USD") ?: "USD",
            biometricLockEnabled = sharedPrefs.getBoolean("biometric_lock_enabled", true)
        )
        
        // Get user data from Firebase Authentication
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val displayName = currentUser.displayName ?: ""
            val nameParts = displayName.split(" ")
            val firstName = nameParts.firstOrNull() ?: "User"
            val lastName = nameParts.drop(1).joinToString(" ").ifEmpty { "" }
            val initials = "${firstName.firstOrNull() ?: "U"}${lastName.firstOrNull() ?: ""}"
            
            val profile = UserProfile(
                id = currentUser.uid,
                firstName = firstName,
                lastName = lastName,
                email = currentUser.email ?: "",
                avatarInitials = initials,
                memberSince = Date(currentUser.metadata?.creationTimestamp ?: System.currentTimeMillis()),
                preferences = savedPreferences
            )
            
            _userProfile.value = profile
        }
        // If no user is logged in, don't set any profile - let the auth system handle redirect
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
            
            // Apply dark mode immediately
            applyDarkMode(enabled)
            
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
    
    fun clearAllData() {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentUserId = currentUser?.uid ?: "default_user"
            
            // Clear only the current user's data from database tables
            transactionRepository.deleteAllUserTransactions(currentUserId)
            budgetRepository.deleteAllUserBudgets(currentUserId)
            goalRepository.deleteAllUserGoals(currentUserId)
            
            // Reset only dark mode preference to default but keep user-specific settings
            val currentDarkMode = sharedPrefs.getBoolean("dark_mode_enabled", false)
            val resetPreferences = UserPreferences(
                notificationsEnabled = true,
                darkModeEnabled = false,
                selectedCurrency = "USD",
                biometricLockEnabled = true
            )
            
            // Save reset preferences to SharedPreferences
            sharedPrefs.edit()
                .putBoolean("notifications_enabled", true)
                .putBoolean("dark_mode_enabled", false)
                .putString("selected_currency", "USD")
                .putBoolean("biometric_lock_enabled", true)
                .apply()
            
            // Apply light mode when data is cleared
            if (currentDarkMode) {
                applyDarkMode(false)
            }
            
            _preferences.value = resetPreferences
            
            _userProfile.value?.let { profile ->
                _userProfile.value = profile.copy(preferences = resetPreferences)
            }
        }
    }
    
    private fun applyDarkModeFromPreferences() {
        val isDarkModeEnabled = sharedPrefs.getBoolean("dark_mode_enabled", false)
        applyDarkMode(isDarkModeEnabled)
    }
    
    private fun applyDarkMode(enabled: Boolean) {
        val nightMode = if (enabled) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
    
    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }
}