package com.cp3406.financetracker.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cp3406.financetracker.FullComposeMainActivity
import com.cp3406.financetracker.ui.theme.FinanceTrackerTheme

class ComposeAuthActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            FinanceTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthNavigationFlow()
                }
            }
        }
    }
    
    @Composable
    private fun AuthNavigationFlow() {
        var showRegistration by remember { mutableStateOf(false) }
        val authViewModel: AuthViewModel = viewModel()
        
        if (showRegistration) {
            ComposeRegisterScreen(
                onRegisterSuccess = {
                    // Navigate to main app after successful registration
                    val intent = Intent(this@ComposeAuthActivity, FullComposeMainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                },
                onNavigateToLogin = {
                    showRegistration = false
                    authViewModel.resetAuthState()
                },
                authViewModel = authViewModel
            )
        } else {
            ComposeLoginScreen(
                onLoginSuccess = {
                    // Navigate to main app after successful login
                    val intent = Intent(this@ComposeAuthActivity, FullComposeMainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                },
                onNavigateToRegister = {
                    showRegistration = true
                    authViewModel.resetAuthState()
                },
                authViewModel = authViewModel
            )
        }
    }
}