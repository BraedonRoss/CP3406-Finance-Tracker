package com.cp3406.financetracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cp3406.financetracker.ui.auth.AuthViewModel
import com.cp3406.financetracker.ui.auth.ComposeLoginScreen
import com.cp3406.financetracker.ui.dashboard.ComposeDashboardScreen
import com.cp3406.financetracker.ui.theme.FinanceTrackerTheme
import com.google.firebase.auth.FirebaseAuth

class FullComposeMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize dark mode before calling super.onCreate()
        initializeDarkMode()
        
        super.onCreate(savedInstanceState)

        setContent {
            val themeViewModel: com.cp3406.financetracker.ui.theme.ThemeViewModel = viewModel()
            
            FinanceTrackerTheme(darkTheme = themeViewModel.isDarkModeEnabled) {
                FullFinanceTrackerApp()
            }
        }
    }

    private fun initializeDarkMode() {
        val sharedPrefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val isDarkModeEnabled = sharedPrefs.getBoolean("dark_mode_enabled", false)
        
        val nightMode = if (isDarkModeEnabled) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}

@Composable
fun FullFinanceTrackerApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    
    // Check if user is authenticated
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) "main" else "login"
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            ComposeLoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    // For now, just show a message - can implement later
                },
                authViewModel = authViewModel
            )
        }
        
        composable("main") {
            FullMainAppScreen(
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullMainAppScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finance Tracker") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                fullBottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                ComposeDashboardScreen()
            }
            composable("budget") {
                com.cp3406.financetracker.ui.budget.ComposeBudgetScreen()
            }
            composable("transactions") {
                com.cp3406.financetracker.ui.transactions.ComposeTransactionsScreen()
            }
            composable("goals") {
                com.cp3406.financetracker.ui.goals.ComposeGoalsScreen()
            }
            composable("profile") {
                com.cp3406.financetracker.ui.profile.ComposeProfileScreen(
                    onSignOut = onLogout
                )
            }
        }
    }
}

@Composable
fun FullPlaceholderScreen(title: String, description: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "🚧 Feature in Development 🚧\n\nThis feature will be available in a future update. The dashboard already demonstrates the core functionality of your Finance Tracker app.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class FullBottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val fullBottomNavItems = listOf(
    FullBottomNavItem("dashboard", "Dashboard", Icons.Default.Home),
    FullBottomNavItem("budget", "Budget", Icons.Default.AccountBox),
    FullBottomNavItem("transactions", "History", Icons.Default.Menu),
    FullBottomNavItem("goals", "Goals", Icons.Default.Star),
    FullBottomNavItem("profile", "Profile", Icons.Default.Person)
)