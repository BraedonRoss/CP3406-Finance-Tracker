package com.cp3406.financetracker.utils

import com.google.firebase.auth.FirebaseAuth

object UserUtils {
    
    /**
     * Gets the current authenticated user's ID
     * @return User ID if authenticated, null otherwise
     */
    fun getCurrentUserId(): String? {
        return try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Gets the current authenticated user's ID with fallback
     * @param fallback Default value if user is not authenticated
     * @return User ID if authenticated, fallback otherwise
     */
    fun getCurrentUserIdOrDefault(fallback: String = "anonymous"): String {
        return getCurrentUserId() ?: fallback
    }
    
    /**
     * Checks if user is currently authenticated
     * @return true if user is authenticated, false otherwise
     */
    fun isUserAuthenticated(): Boolean {
        return getCurrentUserId() != null
    }
}