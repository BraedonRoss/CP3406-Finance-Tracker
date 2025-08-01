package com.cp3406.financetracker.ui.profile

import java.util.Date

data class UserProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val avatarInitials: String,
    val memberSince: Date,
    val preferences: UserPreferences
) {
    val fullName: String
        get() = "$firstName $lastName"
}

data class UserPreferences(
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val selectedCurrency: String = "USD",
    val biometricLockEnabled: Boolean = true
)