package com.cp3406.financetracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cp3406.financetracker.ui.auth.ComposeAuthActivity
import com.cp3406.financetracker.ui.budget.BudgetCategory
import com.cp3406.financetracker.ui.budget.SafeBudgetViewModel
import com.cp3406.financetracker.ui.theme.FinanceTrackerTheme
import com.cp3406.financetracker.data.database.FinanceDatabase
import com.cp3406.financetracker.data.entity.TransactionType
import com.cp3406.financetracker.ui.transactions.TransactionsViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.foundation.isSystemInDarkTheme
import java.util.UUID
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.cp3406.financetracker.ui.goals.FinancialGoal
import com.cp3406.financetracker.ui.goals.GoalsViewModel
import com.cp3406.financetracker.utils.UserUtils
import java.text.SimpleDateFormat
import java.util.Locale

data class SafeBottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)


val safeBottomNavItems = listOf(
    SafeBottomNavItem("dashboard", "Dashboard", Icons.Default.Home),
    SafeBottomNavItem("budget", "Budget", Icons.Default.AccountBox),
    SafeBottomNavItem("transactions", "History", Icons.Default.List),
    SafeBottomNavItem("goals", "Goals", Icons.Default.Star),
    SafeBottomNavItem("profile", "Profile", Icons.Default.Person)
)

class SafeComposeMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Check if user is authenticated
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                startActivity(Intent(this, ComposeAuthActivity::class.java))
                finish()
                return
            }
        } catch (e: Exception) {
            // If Firebase auth fails, go to login
            startActivity(Intent(this, ComposeAuthActivity::class.java))
            finish()
            return
        }

        setContent {
            val sharedPrefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
            var isDarkModeEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("dark_mode_enabled", false)) }
            
            FinanceTrackerTheme(
                darkTheme = isDarkModeEnabled
            ) {
                SafeFinanceTrackerApp(
                    isDarkModeEnabled = isDarkModeEnabled,
                    onDarkModeToggle = { enabled ->
                        isDarkModeEnabled = enabled
                        sharedPrefs.edit().putBoolean("dark_mode_enabled", enabled).apply()
                    },
                    onSignOut = {
                        try {
                            FirebaseAuth.getInstance().signOut()
                        } catch (e: Exception) {
                            // Handle error gracefully
                        }
                        val intent = Intent(this@SafeComposeMainActivity, ComposeAuthActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

}

@Composable
fun SafeFinanceTrackerApp(
    isDarkModeEnabled: Boolean = false,
    onDarkModeToggle: (Boolean) -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val navController = rememberNavController()
    var showAddIncomeDialog by remember { mutableStateOf(false) }
    val transactionsViewModel: TransactionsViewModel = viewModel()
    
    // Get current route to conditionally show floating action button
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showFab = currentRoute in listOf("dashboard", "budget", "transactions")
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentDestination = navBackStackEntry?.destination

                safeBottomNavItems.forEach { item ->
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
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { showAddIncomeDialog = true },
                    containerColor = Color(0xFF4CAF50),
                    modifier = Modifier.size(64.dp)
                ) {
                    Text(
                        text = "$",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                SafeDashboardScreen()
            }
            composable("budget") {
                SafeBudgetScreen()
            }
            composable("transactions") {
                SafeTransactionsScreen()
            }
            composable("goals") {
                SafeGoalsScreen()
            }
            composable("profile") {
                SafeProfileScreen(
                    isDarkModeEnabled = isDarkModeEnabled,
                    onDarkModeToggle = onDarkModeToggle,
                    onSignOut = onSignOut
                )
            }
        }
    }
    
    // Global Add Income Dialog
    if (showAddIncomeDialog) {
        AddIncomeDialog(
            onDismiss = { showAddIncomeDialog = false },
            onAdd = { description: String, amount: Double ->
                transactionsViewModel.addTransaction(
                    description = description,
                    amount = amount,
                    category = "Income",
                    type = TransactionType.INCOME
                )
                showAddIncomeDialog = false
            }
        )
    }
}

@Composable
fun SafeDashboardScreen(
    transactionsViewModel: TransactionsViewModel = viewModel(),
    dashboardViewModel: com.cp3406.financetracker.ui.dashboard.DashboardViewModel = viewModel(),
    budgetViewModel: SafeBudgetViewModel = viewModel(),
    goalsViewModel: GoalsViewModel = viewModel()
) {
    val context = LocalContext.current
    val currencyFormatter = remember { com.cp3406.financetracker.utils.CurrencyFormatter(context) }
    
    val transactions by transactionsViewModel.transactions.observeAsState(emptyList())
    val exchangeRates by dashboardViewModel.exchangeRates.observeAsState(emptyMap())
    val budgetCategories by budgetViewModel.budgetCategories.observeAsState(emptyList())
    val totalBudget by budgetViewModel.totalBudget.observeAsState(0.0)
    val totalSpent by budgetViewModel.totalSpent.observeAsState(0.0)
    val goals by goalsViewModel.goals.observeAsState(emptyList())
    val totalSaved by goalsViewModel.totalSaved.observeAsState(0.0)
    
    // Calculate real-time values
    val currentBalance = transactions.sumOf { 
        if (it.type == com.cp3406.financetracker.ui.transactions.TransactionType.INCOME) it.amount else -it.amount 
    }
    val monthlyIncome = transactions.filter { 
        it.type == com.cp3406.financetracker.ui.transactions.TransactionType.INCOME 
    }.sumOf { it.amount }
    val monthlyExpenses = transactions.filter { 
        it.type == com.cp3406.financetracker.ui.transactions.TransactionType.EXPENSE 
    }.sumOf { it.amount }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to Finance Tracker",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Current Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                    Text(
                        text = currencyFormatter.formatAmount(currentBalance, exchangeRates, "AUD"),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }


        // Budget Overview Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Budget Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val budgetUsedPercentage = if (totalBudget > 0) ((totalSpent / totalBudget) * 100).toInt() else 0
                    val remaining = totalBudget - totalSpent
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Total Budget",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = currencyFormatter.formatAmount(totalBudget, exchangeRates, "AUD"),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Spent",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = currencyFormatter.formatAmount(totalSpent, exchangeRates, "AUD"),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (budgetUsedPercentage > 80) Color(0xFFF44336) else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Remaining",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = currencyFormatter.formatAmount(remaining, exchangeRates, "AUD"),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (remaining < 0) Color(0xFFF44336) else Color(0xFF4CAF50)
                            )
                        }
                    }
                    
                    if (totalBudget > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LinearProgressIndicator(
                            progress = { (budgetUsedPercentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                            color = when {
                                budgetUsedPercentage > 100 -> Color(0xFFF44336)
                                budgetUsedPercentage > 80 -> Color(0xFFFF9800)
                                else -> Color(0xFF4CAF50)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "$budgetUsedPercentage% of budget used • ${budgetCategories.size} categories",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No budget categories set up yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Savings Progress Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Savings Progress",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val activeGoals = goals.filter { !it.isCompleted }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Active Goals",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${activeGoals.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Total Saved",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = currencyFormatter.formatAmount(totalSaved, exchangeRates, "AUD"),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Completed Goals",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${goals.count { it.isCompleted }}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            }
        }

        // Recent Transactions Section
        item {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Recent Transactions
        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No transactions yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Start tracking your finances by adding transactions from the Budget tab",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Show last 3 transactions
            val recentTransactions = transactions.take(3)
            items(recentTransactions.size) { index ->
                val transaction = recentTransactions[index]
                SafeTransactionItem(
                    description = transaction.description,
                    category = transaction.category,
                    date = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(transaction.date),
                    amount = if (transaction.type == com.cp3406.financetracker.ui.transactions.TransactionType.INCOME) 
                        "+${currencyFormatter.formatAmount(transaction.amount, exchangeRates, "AUD")}" 
                    else 
                        "-${currencyFormatter.formatAmount(transaction.amount, exchangeRates, "AUD")}",
                    isIncome = transaction.type == com.cp3406.financetracker.ui.transactions.TransactionType.INCOME
                )
            }
        }

    }
}


@Composable
fun SafeBudgetScreen(
    viewModel: SafeBudgetViewModel = viewModel(),
    dashboardViewModel: com.cp3406.financetracker.ui.dashboard.DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val currencyFormatter = remember { com.cp3406.financetracker.utils.CurrencyFormatter(context) }
    
    val budgetCategories by viewModel.budgetCategories.observeAsState(emptyList())
    val totalBudget by viewModel.totalBudget.observeAsState(0.0)
    val totalSpent by viewModel.totalSpent.observeAsState(0.0)
    val exchangeRates by dashboardViewModel.exchangeRates.observeAsState(emptyMap())
    
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showEditCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<BudgetCategory?>(null) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Budget Summary Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Budget Overview",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currencyFormatter.formatAmount(totalBudget, exchangeRates, "AUD"),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Total Budget",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currencyFormatter.formatAmount(totalSpent, exchangeRates, "AUD"),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Total Spent",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showAddCategoryDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Budget Category", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // Budget Categories List
        if (budgetCategories.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Budget Categories",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create budget categories to track your spending and stay within your financial goals.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(budgetCategories) { category ->
                SafeBudgetCategoryItem(
                    category = category,
                    onEdit = { id, amount ->
                        categoryToEdit = category
                        showEditCategoryDialog = true
                    },
                    onDelete = { id ->
                        viewModel.deleteBudgetCategory(id)
                    },
                    currencyFormatter = currencyFormatter,
                    exchangeRates = exchangeRates
                )
            }
        }
    }
    
    // Add Category Dialog
    if (showAddCategoryDialog) {
        AddBudgetCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onAdd = { name, amount, color ->
                viewModel.addBudgetCategory(name, amount, color)
                showAddCategoryDialog = false
            }
        )
    }
    
    // Edit Category Dialog
    if (showEditCategoryDialog && categoryToEdit != null) {
        EditBudgetCategoryDialog(
            category = categoryToEdit!!,
            onDismiss = {
                showEditCategoryDialog = false
                categoryToEdit = null
            },
            onSave = { newAmount ->
                viewModel.updateBudgetCategory(categoryToEdit!!.id, newAmount)
                showEditCategoryDialog = false
                categoryToEdit = null
            }
        )
    }
}

@Composable
fun SafeTransactionsScreen(
    transactionsViewModel: TransactionsViewModel = viewModel(),
    dashboardViewModel: com.cp3406.financetracker.ui.dashboard.DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val currencyFormatter = remember { com.cp3406.financetracker.utils.CurrencyFormatter(context) }
    
    val transactions by transactionsViewModel.transactions.observeAsState(emptyList())
    val exchangeRates by dashboardViewModel.exchangeRates.observeAsState(emptyMap())
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Transaction History",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${transactions.size} Transactions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        // Transactions List
        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Transactions Yet",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your transaction history will appear here once you start adding income and expenses.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(transactions) { transaction ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Transaction Type Icon
                        val (icon, color) = when (transaction.type) {
                            com.cp3406.financetracker.ui.transactions.TransactionType.INCOME -> 
                                Icons.Default.Add to Color(0xFF4CAF50)
                            com.cp3406.financetracker.ui.transactions.TransactionType.EXPENSE -> 
                                Icons.Default.Delete to MaterialTheme.colorScheme.error
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = color.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Transaction Details
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = transaction.description,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = transaction.category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(transaction.date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        
                        // Amount
                        Text(
                            text = "${if (transaction.type == com.cp3406.financetracker.ui.transactions.TransactionType.INCOME) "+" else "-"}${currencyFormatter.formatAmount(transaction.amount, exchangeRates, "AUD")}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (transaction.type) {
                                com.cp3406.financetracker.ui.transactions.TransactionType.INCOME -> Color(0xFF4CAF50)
                                com.cp3406.financetracker.ui.transactions.TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SafeBudgetCategoryItem(
    category: BudgetCategory,
    onEdit: (String, Double) -> Unit,
    onDelete: (String) -> Unit,
    onAddTransaction: () -> Unit = {},
    currencyFormatter: com.cp3406.financetracker.utils.CurrencyFormatter,
    exchangeRates: Map<String, Double> = emptyMap()
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Budget: ${currencyFormatter.formatAmount(category.budgetAmount, exchangeRates, "AUD")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Spent: ${currencyFormatter.formatAmount(category.spentAmount, exchangeRates, "AUD")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (category.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                
                Row {
                    IconButton(onClick = { onEdit(category.id, category.budgetAmount) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { onDelete(category.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { (category.spentAmount / category.budgetAmount).coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = if (category.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onAddTransaction,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Expense")
            }
        }
    }
}



@Composable
fun TempGoalsScreen() {
    val context = LocalContext.current
    val gson = remember { Gson() }
    val transactionsViewModel: TransactionsViewModel = viewModel()
    val dashboardViewModel: com.cp3406.financetracker.ui.dashboard.DashboardViewModel = viewModel()
    val currencyFormatter = remember { com.cp3406.financetracker.utils.CurrencyFormatter(context) }
    
    val exchangeRates by dashboardViewModel.exchangeRates.observeAsState(emptyMap())
    var goals by remember { mutableStateOf<List<FinancialGoal>>(emptyList()) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showEditGoalDialog by remember { mutableStateOf(false) }
    var selectedGoal by remember { mutableStateOf<FinancialGoal?>(null) }
    var showAddProgressDialog by remember { mutableStateOf(false) }
    var showRemoveProgressDialog by remember { mutableStateOf(false) }
    
    // Load goals on first composition
    LaunchedEffect(Unit) {
        val sharedPrefs = context.getSharedPreferences("goals_prefs", Context.MODE_PRIVATE)
        val goalsJson = sharedPrefs.getString("saved_goals", null)
        if (goalsJson != null) {
            try {
                val type = object : TypeToken<List<FinancialGoal>>() {}.type
                val savedGoals = gson.fromJson<List<FinancialGoal>>(goalsJson, type) ?: emptyList()
                goals = savedGoals
            } catch (e: Exception) {
                goals = emptyList()
            }
        }
    }
    
    // Save goals whenever they change
    val saveGoals = { goalsList: List<FinancialGoal> ->
        val sharedPrefs = context.getSharedPreferences("goals_prefs", Context.MODE_PRIVATE)
        val goalsJson = gson.toJson(goalsList)
        sharedPrefs.edit().putString("saved_goals", goalsJson).apply()
    }
    
    // Calculate summary statistics
    val activeGoals = goals.filter { !it.isCompleted }
    val totalSaved = goals.sumOf { it.currentAmount }
    val totalTarget = goals.sumOf { it.targetAmount }
    val overallProgress = if (totalTarget > 0) ((totalSaved / totalTarget) * 100).toInt() else 0
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Financial Goals",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                
                FloatingActionButton(
                    onClick = { showAddGoalDialog = true },
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Goal",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        
        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Goal Progress Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        GoalSummaryItem(
                            icon = Icons.Default.Star,
                            value = "${activeGoals.size}",
                            label = "Active Goals"
                        )
                        
                        GoalSummaryItem(
                            icon = Icons.Default.CheckCircle,
                            value = "${goals.size - activeGoals.size}",
                            label = "Completed"
                        )
                        
                        GoalSummaryItem(
                            icon = Icons.Default.KeyboardArrowUp,
                            value = "$overallProgress%",
                            label = "Overall Progress"
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Total Saved: ${currencyFormatter.formatAmount(totalSaved, exchangeRates, "AUD")} of ${currencyFormatter.formatAmount(totalTarget, exchangeRates, "AUD")}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        // Goals List Header
        if (goals.isNotEmpty()) {
            item {
                Text(
                    text = "Your Goals",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Goals List
        if (goals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "No goals",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No goals set yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create your first financial goal to start tracking your progress toward financial freedom!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddGoalDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Goal")
                        }
                    }
                }
            }
        } else {
            items(goals.size) { index ->
                val goal = goals[index]
                GoalItem(
                    goal = goal,
                    onEdit = {
                        selectedGoal = goal
                        showEditGoalDialog = true
                    },
                    onAddProgress = {
                        selectedGoal = goal
                        showAddProgressDialog = true
                    },
                    onRemoveProgress = {
                        selectedGoal = goal
                        showRemoveProgressDialog = true
                    },
                    onDelete = {
                        val updatedGoals = goals.filter { it.id != goal.id }
                        goals = updatedGoals
                        saveGoals(updatedGoals)
                    },
                    exchangeRates = exchangeRates
                )
            }
        }
    }
    
    // Add Goal Dialog
    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onAdd = { title, target, date, description ->
                val newGoal = FinancialGoal(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    targetAmount = target,
                    currentAmount = 0.0,
                    targetDate = java.util.Date(),
                    description = description,
                    category = com.cp3406.financetracker.ui.goals.GoalCategory.OTHER
                )
                val updatedGoals = goals + newGoal
                goals = updatedGoals
                saveGoals(updatedGoals)
                showAddGoalDialog = false
            }
        )
    }
    
    // Edit Goal Dialog
    if (showEditGoalDialog && selectedGoal != null) {
        EditGoalDialog(
            goal = selectedGoal!!,
            onDismiss = {
                showEditGoalDialog = false
                selectedGoal = null
            },
            onSave = { updatedGoal ->
                val updatedGoals = goals.map { if (it.id == updatedGoal.id) updatedGoal else it }
                goals = updatedGoals
                saveGoals(updatedGoals)
                showEditGoalDialog = false
                selectedGoal = null
            }
        )
    }
    
    // Add Progress Dialog
    if (showAddProgressDialog && selectedGoal != null) {
        AddProgressDialog(
            goal = selectedGoal!!,
            onDismiss = {
                showAddProgressDialog = false
                selectedGoal = null
            },
            exchangeRates = exchangeRates,
            onAdd = { amount ->
                val updatedGoal = selectedGoal!!.copy(
                    currentAmount = selectedGoal!!.currentAmount + amount,
                    isCompleted = (selectedGoal!!.currentAmount + amount) >= selectedGoal!!.targetAmount
                )
                val updatedGoals = goals.map { if (it.id == updatedGoal.id) updatedGoal else it }
                goals = updatedGoals
                saveGoals(updatedGoals)
                
                // Add transaction to history
                transactionsViewModel.addTransaction(
                    description = "Goal Progress: ${selectedGoal!!.title}",
                    amount = amount,
                    category = "Goals",
                    type = TransactionType.EXPENSE,
                    notes = "Added ${currencyFormatter.formatAmount(amount, exchangeRates, "AUD")} to goal '${selectedGoal!!.title}'"
                )
                
                showAddProgressDialog = false
                selectedGoal = null
            }
        )
    }
    
    // Remove Progress Dialog
    if (showRemoveProgressDialog && selectedGoal != null) {
        RemoveProgressDialog(
            goal = selectedGoal!!,
            onDismiss = {
                showRemoveProgressDialog = false
                selectedGoal = null
            },
            exchangeRates = exchangeRates,
            onRemove = { amount: Double ->
                val newAmount = maxOf(0.0, selectedGoal!!.currentAmount - amount)
                val updatedGoal = selectedGoal!!.copy(
                    currentAmount = newAmount,
                    isCompleted = false // Reset completion status when removing money
                )
                val updatedGoals = goals.map { if (it.id == updatedGoal.id) updatedGoal else it }
                goals = updatedGoals
                saveGoals(updatedGoals)
                
                // Add transaction to history as income (money taken out of goal)
                transactionsViewModel.addTransaction(
                    description = "Goal Withdrawal: ${selectedGoal!!.title}",
                    amount = amount,
                    category = "Goals",
                    type = TransactionType.INCOME,
                    notes = "Removed ${currencyFormatter.formatAmount(amount, exchangeRates, "AUD")} from goal '${selectedGoal!!.title}'"
                )
                
                showRemoveProgressDialog = false
                selectedGoal = null
            }
        )
    }
}

@Composable
fun SafeProfileScreen(
    isDarkModeEnabled: Boolean = false,
    onDarkModeToggle: (Boolean) -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showDataWipeDialog by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf("AUD") }
    
    val context = LocalContext.current
    
    // Load saved currency preference
    LaunchedEffect(Unit) {
        val currencyPrefs = context.getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)
        selectedCurrency = currencyPrefs.getString("selected_currency", "AUD") ?: "AUD"
    }
    val coroutineScope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Avatar
                    Card(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Avatar",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = try { 
                            FirebaseAuth.getInstance().currentUser?.displayName ?: "User" 
                        } catch (e: Exception) { 
                            "User" 
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = try { 
                            FirebaseAuth.getInstance().currentUser?.email ?: "user@example.com" 
                        } catch (e: Exception) { 
                            "user@example.com" 
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.fillMaxWidth(0.6f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile")
                    }
                }
            }
        }
        
        
        // Settings Section
        item {
            Text(
                text = "Settings & Preferences",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        // App Preferences
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Dark Mode Toggle
                    ProfileSettingItem(
                        icon = Icons.Default.Settings,
                        title = "Dark Mode",
                        subtitle = if (isDarkModeEnabled) "Enabled" else "Disabled",
                        trailing = {
                            Switch(
                                checked = isDarkModeEnabled,
                                onCheckedChange = onDarkModeToggle
                            )
                        }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Currency Setting
                    ProfileSettingItem(
                        icon = Icons.Default.Menu,
                        title = "Currency",
                        subtitle = "$selectedCurrency",
                        onClick = { showCurrencyDialog = true }
                    )
                    
                }
            }
        }
        
        // Quick Actions
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    ProfileSettingItem(
                        icon = Icons.Default.Add,
                        title = "Export Data",
                        subtitle = "Export your financial data to CSV",
                        onClick = { 
                            // Export data functionality
                            try {
                                // This would normally export data to CSV
                                android.widget.Toast.makeText(context, "Data export feature coming soon!", android.widget.Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Export failed", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ProfileSettingItem(
                        icon = Icons.Default.Share,
                        title = "Share App",
                        subtitle = "Tell friends about Finance Tracker",
                        onClick = { 
                            try {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Check out Finance Tracker - a great app for managing your finances!")
                                    putExtra(Intent.EXTRA_SUBJECT, "Finance Tracker App")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Finance Tracker"))
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Sharing failed", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ProfileSettingItem(
                        icon = Icons.Default.Info,
                        title = "Help & Support",
                        subtitle = "Get help or contact support",
                        onClick = { 
                            try {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = android.net.Uri.parse("mailto:support@financetracker.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "Finance Tracker Support Request")
                                    putExtra(Intent.EXTRA_TEXT, "Hi,\n\nI need help with the Finance Tracker app.\n\nIssue: \n\nDevice: Android\nApp Version: 1.0.0\n\nThanks!")
                                }
                                context.startActivity(emailIntent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Please install an email app", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    
                }
            }
        }
        
        // Account Management
        item {
            Text(
                text = "Account Management",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    ProfileSettingItem(
                        icon = Icons.Default.Refresh,
                        title = "Backup & Sync",
                        subtitle = "Last backup: Today at 3:42 PM",
                        onClick = { /* Handle backup */ }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ProfileSettingItem(
                        icon = Icons.Default.Delete,
                        title = "Clear All Data",
                        subtitle = "Permanently delete all financial data",
                        onClick = { showDataWipeDialog = true }
                    )
                }
            }
        }
        
        // Sign Out Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Sign Out"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold)
            }
        }
        
        // App Version
        item {
            Text(
                text = "Finance Tracker v1.0.0 • Built with Jetpack Compose",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    
    // Edit Profile Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            onDismiss = { showEditProfileDialog = false }
        )
    }
    
    // Currency Selection Dialog
    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currentCurrency = selectedCurrency,
            onDismiss = { showCurrencyDialog = false },
            onCurrencySelected = { currency ->
                selectedCurrency = currency
                // Save currency preference
                val currencyPrefs = context.getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)
                currencyPrefs.edit().putString("selected_currency", currency).apply()
                // Refresh exchange rates for the new currency
                // Note: Exchange rates will be refreshed on next screen load
                showCurrencyDialog = false
            }
        )
    }
    
    // Data Wipe Confirmation Dialog
    if (showDataWipeDialog) {
        AlertDialog(
            onDismissRequest = { showDataWipeDialog = false },
            title = {
                Text(
                    text = "Clear All Data",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will permanently delete all your financial data including transactions, budgets, and goals. This action only affects your current account and will not impact other users' data. This cannot be undone.\n\nAre you sure you want to continue?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val currentUserId = UserUtils.getCurrentUserId()
                                if (currentUserId != null) {
                                    val database = FinanceDatabase.getDatabase(context)
                                    
                                    // Clear database tables for current user only
                                    database.transactionDao().deleteAllUserTransactions(currentUserId)
                                    database.budgetDao().deleteAllUserBudgets(currentUserId)
                                    database.goalDao().deleteAllUserGoals(currentUserId)
                                    
                                    // Clear user-scoped SharedPreferences data
                                    val userPrefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
                                    userPrefs.edit().clear().apply()
                                    
                                    // Clear user-scoped budget categories
                                    val budgetPrefs = context.getSharedPreferences("budget_categories_$currentUserId", Context.MODE_PRIVATE)
                                    budgetPrefs.edit().clear().apply()
                                    
                                    val goalsPrefs = context.getSharedPreferences("goals_prefs", Context.MODE_PRIVATE)
                                    goalsPrefs.edit().clear().apply()
                                    
                                    val currencyPrefs = context.getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)
                                    currencyPrefs.edit().clear().apply()
                                    
                                    // Clear any other app-specific preferences
                                    val appPrefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                                    appPrefs.edit().clear().apply()
                                }
                                showDataWipeDialog = false
                            } catch (e: Exception) {
                                // Handle error
                                showDataWipeDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete All Data", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDataWipeDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddBudgetCategoryDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var budgetAmount by remember { mutableStateOf("") }
    
    val predefinedCategories = listOf(
        "Food & Dining", "Transportation", "Entertainment", "Utilities", 
        "Shopping", "Healthcare", "Education", "Travel", "Insurance"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Budget Category",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Select or enter a category:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(predefinedCategories.size) { index ->
                        val category = predefinedCategories[index]
                        FilterChip(
                            onClick = { categoryName = category },
                            label = { Text(category, fontSize = 12.sp) },
                            selected = categoryName == category
                        )
                    }
                }
                
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = budgetAmount,
                    onValueChange = { budgetAmount = it },
                    label = { Text("Budget Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = {
                        Text("$", style = MaterialTheme.typography.bodyLarge)
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = budgetAmount.toDoubleOrNull()
                    if (categoryName.isNotBlank() && amount != null && amount > 0) {
                        onAdd(categoryName, amount, "#2196F3")
                    }
                },
                enabled = categoryName.isNotBlank() && budgetAmount.toDoubleOrNull() != null
            ) {
                Text("Add Category")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



@Composable
fun EditProfileDialog(
    onDismiss: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    // Initialize with current user data
    LaunchedEffect(Unit) {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val displayName = currentUser?.displayName ?: ""
            val nameParts = displayName.split(" ")
            if (nameParts.isNotEmpty()) {
                firstName = nameParts[0]
                if (nameParts.size > 1) {
                    lastName = nameParts.drop(1).joinToString(" ")
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Profile",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true,
                    enabled = !isLoading
                )
                
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    try {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName("$firstName $lastName".trim())
                            .build()
                        
                        currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                onDismiss()
                            }
                        }
                    } catch (e: Exception) {
                        isLoading = false
                        onDismiss()
                    }
                },
                enabled = !isLoading && firstName.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Changes")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

// Helper Composables
@Composable
private fun SafeStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = backgroundColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = backgroundColor
            )
        }
    }
}

@Composable
private fun SafeTransactionItem(
    description: String,
    category: String,
    date: String,
    amount: String,
    isIncome: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$category • $date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}

@Composable
private fun RealBudgetCategoryItem(
    category: BudgetCategory,
    onEdit: (String, Double) -> Unit,
    onDelete: (String) -> Unit,
    currencyFormatter: com.cp3406.financetracker.utils.CurrencyFormatter,
    exchangeRates: Map<String, Double> = emptyMap()
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    IconButton(
                        onClick = { showEditDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Category"
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Category",
                            tint = Color(0xFFF44336)
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currencyFormatter.formatAmount(category.spentAmount, exchangeRates, "AUD")} / ${currencyFormatter.formatAmount(category.budgetAmount, exchangeRates, "AUD")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Text(
                    text = if (category.isOverBudget) "Over Budget!" else "Remaining: ${currencyFormatter.formatAmount(category.remainingAmount, exchangeRates, "AUD")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (category.isOverBudget) Color(0xFFF44336) else Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = minOf(category.progressPercentage / 100f, 1f),
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    category.isOverBudget -> Color(0xFFF44336)
                    category.progressPercentage > 80 -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                }
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${category.progressPercentage.toInt()}% used",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
    
    // Edit Dialog
    if (showEditDialog) {
        EditBudgetCategoryDialog(
            category = category,
            onDismiss = { showEditDialog = false },
            onSave = { newAmount: Double ->
                onEdit(category.id, newAmount)
                showEditDialog = false
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Budget Category") },
            text = { Text("Are you sure you want to delete '${category.name}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(category.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color(0xFFF44336))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}



@Composable
private fun SafeGoalItem(
    goalName: String,
    targetAmount: String,
    currentAmount: String,
    percentage: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goalName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$currentAmount / $targetAmount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "$percentage% complete",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// Profile Helper Composables
@Composable
private fun ProfileStatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProfileSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val modifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}


@Composable
private fun CurrencySelectionDialog(
    currentCurrency: String,
    onDismiss: () -> Unit,
    onCurrencySelected: (String) -> Unit
) {
    val currencies = listOf(
        "USD" to "US Dollar ($)",
        "EUR" to "Euro (€)",
        "GBP" to "British Pound (£)",
        "JPY" to "Japanese Yen (¥)",
        "CAD" to "Canadian Dollar (C$)",
        "AUD" to "Australian Dollar (A$)",
        "CHF" to "Swiss Franc (CHF)",
        "CNY" to "Chinese Yuan (¥)",
        "INR" to "Indian Rupee (₹)"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency") },
        text = {
            LazyColumn {
                items(currencies.size) { index ->
                    val (code, name) = currencies[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCurrencySelected(code) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentCurrency == code,
                            onClick = { onCurrencySelected(code) }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = code,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

// Missing Dialog Components

@Composable
fun AddIncomeDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Income") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (description.isNotBlank() && amountValue != null && amountValue > 0) {
                        onAdd(description, amountValue)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddTransactionDialog(
    category: BudgetCategory,
    onDismiss: () -> Unit,
    onAdd: (String, Double) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense to ${category.name}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (description.isNotBlank() && amountValue != null && amountValue > 0) {
                        onAdd(description, amountValue)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditBudgetCategoryDialog(
    category: BudgetCategory,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var amount by remember { mutableStateOf(category.budgetAmount.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${category.name}") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Budget Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val budgetAmount = amount.toDoubleOrNull()
                    if (budgetAmount != null && budgetAmount > 0) {
                        onSave(budgetAmount)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SafeGoalsScreen(
    goalsViewModel: GoalsViewModel = viewModel(),
    dashboardViewModel: com.cp3406.financetracker.ui.dashboard.DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val currencyFormatter = remember { com.cp3406.financetracker.utils.CurrencyFormatter(context) }
    
    val goals by goalsViewModel.goals.observeAsState(emptyList())
    val totalSaved by goalsViewModel.totalSaved.observeAsState(0.0)
    val averageProgress by goalsViewModel.averageProgress.observeAsState(0f)
    val exchangeRates by dashboardViewModel.exchangeRates.observeAsState(emptyMap())
    
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showEditGoalDialog by remember { mutableStateOf(false) }
    var showAddProgressDialog by remember { mutableStateOf(false) }
    var showRemoveProgressDialog by remember { mutableStateOf(false) }
    var selectedGoal by remember { mutableStateOf<FinancialGoal?>(null) }
    
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Goals Summary Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Financial Goals",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        GoalSummaryItem(
                            icon = Icons.Default.Star,
                            value = "${goals.size}",
                            label = "Total Goals"
                        )
                        
                        GoalSummaryItem(
                            icon = Icons.Default.CheckCircle,
                            value = "${goals.count { it.isCompleted }}",
                            label = "Completed"
                        )
                        
                        GoalSummaryItem(
                            icon = Icons.Default.AccountBox,
                            value = currencyFormatter.formatAmount(totalSaved, exchangeRates, "AUD"),
                            label = "Total Saved"
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Add Goal Button
                    Button(
                        onClick = { showAddGoalDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create New Goal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // Goals List
        if (goals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Goals Yet",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start your financial journey by creating your first goal. Whether it's an emergency fund, vacation, or major purchase, every goal gets you closer to financial freedom.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(goals) { goal ->
                GoalItem(
                    goal = goal,
                    onEdit = {
                        selectedGoal = goal
                        showEditGoalDialog = true
                    },
                    onAddProgress = {
                        selectedGoal = goal
                        showAddProgressDialog = true
                    },
                    onRemoveProgress = {
                        selectedGoal = goal
                        showRemoveProgressDialog = true
                    },
                    onDelete = {
                        goalsViewModel.deleteGoal(goal.id.toLong())
                    },
                    exchangeRates = exchangeRates
                )
            }
        }
    }
    
    // Add Goal Dialog
    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onAdd = { title, targetAmount, targetDate, description ->
                try {
                    val date = dateFormatter.parse(targetDate) ?: java.util.Date()
                    goalsViewModel.addGoal(
                        title = title,
                        description = description,
                        targetAmount = targetAmount,
                        targetDate = date,
                        category = com.cp3406.financetracker.data.entity.GoalCategory.OTHER
                    )
                    showAddGoalDialog = false
                } catch (e: Exception) {
                    // Handle date parsing error
                    showAddGoalDialog = false
                }
            }
        )
    }
    
    // Edit Goal Dialog
    if (showEditGoalDialog && selectedGoal != null) {
        EditGoalDialog(
            goal = selectedGoal!!,
            onDismiss = {
                showEditGoalDialog = false
                selectedGoal = null
            },
            onSave = { updatedGoal ->
                goalsViewModel.updateGoal(updatedGoal)
                showEditGoalDialog = false
                selectedGoal = null
            }
        )
    }
    
    // Add Progress Dialog
    if (showAddProgressDialog && selectedGoal != null) {
        AddProgressDialog(
            goal = selectedGoal!!,
            onDismiss = {
                showAddProgressDialog = false
                selectedGoal = null
            },
            onAdd = { amount ->
                val newAmount = selectedGoal!!.currentAmount + amount
                goalsViewModel.updateGoalProgress(selectedGoal!!.id.toLong(), newAmount)
                showAddProgressDialog = false
                selectedGoal = null
            },
            exchangeRates = exchangeRates
        )
    }
    
    // Remove Progress Dialog
    if (showRemoveProgressDialog && selectedGoal != null) {
        RemoveProgressDialog(
            goal = selectedGoal!!,
            onDismiss = {
                showRemoveProgressDialog = false
                selectedGoal = null
            },
            onRemove = { amount ->
                val newAmount = maxOf(0.0, selectedGoal!!.currentAmount - amount)
                goalsViewModel.updateGoalProgress(selectedGoal!!.id.toLong(), newAmount)
                showRemoveProgressDialog = false
                selectedGoal = null
            },
            exchangeRates = exchangeRates
        )
    }
}

