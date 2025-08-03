package com.cp3406.financetracker.ui.theme

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val sharedPrefs = application.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    
    var isDarkModeEnabled by mutableStateOf(sharedPrefs.getBoolean("dark_mode_enabled", false))
        private set
    
    private val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "dark_mode_enabled") {
            isDarkModeEnabled = sharedPrefs.getBoolean("dark_mode_enabled", false)
        }
    }
    
    init {
        // Listen for preference changes
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
    }
    
    override fun onCleared() {
        super.onCleared()
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
    
    fun refreshTheme() {
        isDarkModeEnabled = sharedPrefs.getBoolean("dark_mode_enabled", false)
    }
}