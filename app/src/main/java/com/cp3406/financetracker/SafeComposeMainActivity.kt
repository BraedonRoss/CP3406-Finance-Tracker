package com.cp3406.financetracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
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
        
        // Initialize dark mode
        initializeDarkMode()

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
            FinanceTrackerTheme {
                SafeFinanceTrackerApp(
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
fun SafeFinanceTrackerApp(
    onSignOut: () -> Unit = {}
) {
    val navController = rememberNavController()
    var showAddIncomeDialog by remember { mutableStateOf(false) }
    val transactionsViewModel: TransactionsViewModel = viewModel()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
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
    transactionsViewModel: TransactionsViewModel = viewModel()
) {
    val transactions by transactionsViewModel.transactions.observeAsState(emptyList())
    
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
                        text = "$${String.format("%.2f", currentBalance)}",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
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
                                text = "0",
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
                                text = "$0.00",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
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
                        "+$${String.format("%.2f", transaction.amount)}" 
                    else 
                        "-$${String.format("%.2f", transaction.amount)}",
                    isIncome = transaction.type == com.cp3406.financetracker.ui.transactions.TransactionType.INCOME
                )
            }
        }

    }
}

@Composable
fun SafeBudgetScreen(
    viewModel: SafeBudgetViewModel = viewModel(),
    transactionsViewModel: TransactionsViewModel = viewModel()
) {
    val budgetCategories by viewModel.budgetCategories.observeAsState(emptyList())
    val totalBudget by viewModel.totalBudget.observeAsState(0.0)
    val totalSpent by viewModel.totalSpent.observeAsState(0.0)
    val isLoading by viewModel.isLoading.observeAsState(false)
    
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<BudgetCategory?>(null) }
    var showEditCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<BudgetCategory?>(null) }
    
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
                    text = "Budget Management",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = { viewModel.refreshBudgetData() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Budget Data"
                    )
                }
            }
        }
        
        // Monthly Budget Overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Monthly Budget Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val remaining = totalBudget - totalSpent
                    val progressPercentage = if (totalBudget > 0) (totalSpent / totalBudget * 100).toInt() else 0
                    
                    Text(
                        text = "Total Budget: $${String.format("%.2f", totalBudget)} | " +
                               "Used: $${String.format("%.2f", totalSpent)} | " +
                               "Remaining: $${String.format("%.2f", remaining)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = { progressPercentage / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (progressPercentage > 80) Color(0xFFF44336) else MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "$progressPercentage% of budget used",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        // Budget Categories Header with Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budget Categories",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                FloatingActionButton(
                    onClick = { showAddCategoryDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Budget Category"
                    )
                }
            }
        }
        
        // Loading Indicator
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        // Budget Categories List
        items(budgetCategories.size) { index ->
            val category = budgetCategories[index]
            SafeBudgetCategoryItem(
                category = category,
                onEdit = { categoryId, _ ->
                    categoryToEdit = category
                    showEditCategoryDialog = true
                },
                onDelete = { categoryId ->
                    viewModel.deleteBudgetCategory(categoryId)
                },
                onAddTransaction = {
                    selectedCategory = category
                    showAddTransactionDialog = true
                }
            )
        }
        
        // Empty State
        if (budgetCategories.isEmpty() && !isLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No budget categories yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start managing your finances by adding your first budget category",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
    
    // Add Category Dialog
    if (showAddCategoryDialog) {
        AddBudgetCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onAdd = { name: String, amount: Double, color: String ->
                viewModel.addBudgetCategory(name, amount, color)
                showAddCategoryDialog = false
            }
        )
    }
    
    // Add Transaction Dialog
    if (showAddTransactionDialog && selectedCategory != null) {
        AddTransactionDialog(
            category = selectedCategory!!,
            onDismiss = { 
                showAddTransactionDialog = false
                selectedCategory = null
            },
            onAdd = { description: String, amount: Double ->
                transactionsViewModel.addTransaction(
                    description = description,
                    amount = amount,
                    category = selectedCategory!!.name,
                    type = TransactionType.EXPENSE
                )
                showAddTransactionDialog = false
                selectedCategory = null
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
fun SafeBudgetCategoryItem(
    category: BudgetCategory,
    onEdit: (String, Double) -> Unit,
    onDelete: (String) -> Unit,
    onAddTransaction: () -> Unit = {}
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
                        text = "Budget: $${String.format("%.2f", category.budgetAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Spent: $${String.format("%.2f", category.spentAmount)}",
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
fun SafeTransactionsScreen(
    transactionsViewModel: TransactionsViewModel = viewModel()
) {
    val transactions by transactionsViewModel.transactions.observeAsState(emptyList())
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.headlineSmall
                )
                TextButton(onClick = { /* Add transaction */ }) {
                    Text("Add Transaction")
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
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "No transactions",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No transactions yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start tracking your income and expenses by adding transactions from the Budget tab",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(transactions.size) { index ->
                val transaction = transactions[index]
                TransactionItem(transaction = transaction)
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: com.cp3406.financetracker.ui.transactions.Transaction) {
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
            
            Text(
                text = if (transaction.type == com.cp3406.financetracker.ui.transactions.TransactionType.INCOME) 
                    "+$${String.format("%.2f", transaction.amount)}" 
                else 
                    "-$${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == com.cp3406.financetracker.ui.transactions.TransactionType.INCOME) 
                    Color(0xFF4CAF50) 
                else 
                    Color(0xFFF44336)
            )
        }
    }
}

@Composable
fun SafeGoalsScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Financial Goals",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Goal Progress Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Active Goals: 0 | Total Progress: $0 saved",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        item {
            Text(
                text = "Your Goals",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        // No goals yet
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No goals set yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Create financial goals to track your progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SafeProfileScreen(
    onSignOut: () -> Unit = {}
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showDataWipeDialog by remember { mutableStateOf(false) }
    var isDarkModeEnabled by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var selectedCurrency by remember { mutableStateOf("USD") }
    
    val context = LocalContext.current
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
                        icon = Icons.Default.Refresh,
                        title = "Dark Mode",
                        subtitle = if (isDarkModeEnabled) "Enabled" else "Disabled",
                        trailing = {
                            Switch(
                                checked = isDarkModeEnabled,
                                onCheckedChange = { isDarkModeEnabled = it }
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
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Notifications Toggle
                    ProfileSettingItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = if (notificationsEnabled) "Enabled" else "Disabled",
                        trailing = {
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it }
                            )
                        }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Privacy Settings
                    ProfileSettingItem(
                        icon = Icons.Default.Lock,
                        title = "Privacy & Security",
                        subtitle = "Manage your privacy settings",
                        onClick = { /* Handle privacy settings */ }
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
                        icon = Icons.Default.Info,
                        title = "Help & Support",
                        subtitle = "Get help or contact support",
                        onClick = { /* Handle help */ }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ProfileSettingItem(
                        icon = Icons.Default.Share,
                        title = "Share App",
                        subtitle = "Tell friends about Finance Tracker",
                        onClick = { /* Handle share */ }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ProfileSettingItem(
                        icon = Icons.Default.Star,
                        title = "Rate App",
                        subtitle = "Rate us on the app store",
                        onClick = { /* Handle rating */ }
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
                        icon = Icons.Default.Build,
                        title = "Export Data",
                        subtitle = "Download your financial data",
                        onClick = { /* Handle data export */ }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
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
                    text = "This will permanently delete all your financial data including transactions, budgets, and goals. This action cannot be undone.\n\nAre you sure you want to continue?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val database = FinanceDatabase.getDatabase(context)
                                database.clearAllTables()
                                // Clear SharedPreferences as well
                                val sharedPrefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
                                sharedPrefs.edit().clear().apply()
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
fun AddTransactionDialog(
    category: BudgetCategory,
    onDismiss: () -> Unit,
    onAdd: (String, Double) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Expense - ${category.name}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true,
                    placeholder = { Text("e.g., Grocery shopping, Gas, etc.") }
                )
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
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
                    val expenseAmount = amount.toDoubleOrNull()
                    if (description.isNotBlank() && expenseAmount != null && expenseAmount > 0) {
                        onAdd(description, expenseAmount)
                    }
                },
                enabled = description.isNotBlank() && amount.toDoubleOrNull() != null
            ) {
                Text("Add Expense")
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
fun AddIncomeDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Income",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Income Source") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true,
                    placeholder = { Text("e.g., Salary, Freelance, etc.") }
                )
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
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
                    val incomeAmount = amount.toDoubleOrNull()
                    if (description.isNotBlank() && incomeAmount != null && incomeAmount > 0) {
                        onAdd(description, incomeAmount)
                    }
                },
                enabled = description.isNotBlank() && amount.toDoubleOrNull() != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Add Income")
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
    onDelete: (String) -> Unit
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
                    text = "$${String.format("%.2f", category.spentAmount)} / $${String.format("%.2f", category.budgetAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Text(
                    text = if (category.isOverBudget) "Over Budget!" else "Remaining: $${String.format("%.2f", category.remainingAmount)}",
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
            onSave = { newAmount ->
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
private fun EditBudgetCategoryDialog(
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
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val budgetAmount = amount.toDoubleOrNull()
                    if (budgetAmount != null && budgetAmount > 0) {
                        onSave(budgetAmount)
                    }
                },
                enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
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

