package com.cp3406.financetracker.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    init {
        _currentUser.value = auth.currentUser
    }

    fun signInWithEmail(email: String, password: String) {
        _authState.value = AuthState.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    if (user != null) {
                        _currentUser.value = user
                        _authState.value = AuthState.Success(user)
                    } else {
                        _authState.value = AuthState.Error("Sign in failed")
                    }
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    val errorMessage = task.exception?.message ?: "Sign in failed"
                    _authState.value = AuthState.Error(getErrorMessage(errorMessage))
                }
            }
    }

    fun createUserWithEmail(email: String, password: String, firstName: String, lastName: String) {
        _authState.value = AuthState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    if (user != null) {
                        // Update user profile with name
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName("$firstName $lastName")
                            .build()

                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    Log.d(TAG, "User profile updated.")
                                    _currentUser.value = user
                                    _authState.value = AuthState.Success(user)
                                } else {
                                    Log.w(TAG, "Profile update failed", profileTask.exception)
                                    // Still consider registration successful
                                    _currentUser.value = user
                                    _authState.value = AuthState.Success(user)
                                }
                            }
                    } else {
                        _authState.value = AuthState.Error("Registration failed")
                    }
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    val errorMessage = task.exception?.message ?: "Registration failed"
                    _authState.value = AuthState.Error(getErrorMessage(errorMessage))
                }
            }
    }

    fun signInWithCredential(credential: AuthCredential) {
        _authState.value = AuthState.Loading

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    if (user != null) {
                        _currentUser.value = user
                        _authState.value = AuthState.Success(user)
                    } else {
                        _authState.value = AuthState.Error("Sign in failed")
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    val errorMessage = task.exception?.message ?: "Sign in failed"
                    _authState.value = AuthState.Error(getErrorMessage(errorMessage))
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    private fun getErrorMessage(firebaseError: String): String {
        return when {
            firebaseError.contains("EMAIL_NOT_FOUND") || 
            firebaseError.contains("user-not-found") -> "No account found with this email"
            
            firebaseError.contains("INVALID_PASSWORD") || 
            firebaseError.contains("wrong-password") -> "Incorrect password"
            
            firebaseError.contains("EMAIL_EXISTS") || 
            firebaseError.contains("email-already-in-use") -> "An account with this email already exists"
            
            firebaseError.contains("WEAK_PASSWORD") || 
            firebaseError.contains("weak-password") -> "Password is too weak"
            
            firebaseError.contains("INVALID_EMAIL") || 
            firebaseError.contains("invalid-email") -> "Invalid email address"
            
            firebaseError.contains("TOO_MANY_ATTEMPTS_TRY_LATER") || 
            firebaseError.contains("too-many-requests") -> "Too many failed attempts. Please try again later"
            
            firebaseError.contains("NETWORK_ERROR") -> "Network error. Please check your connection"
            
            else -> "Authentication failed. Please try again"
        }
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}