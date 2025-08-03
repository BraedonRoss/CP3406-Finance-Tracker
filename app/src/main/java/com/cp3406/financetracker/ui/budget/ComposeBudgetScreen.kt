package com.cp3406.financetracker.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeBudgetScreen(
    viewModel: BudgetViewModel = viewModel()
) {
    val budgetCategories by viewModel.budgetCategories.observeAsState(emptyList())
    val totalBudget by viewModel.totalBudget.observeAsState(0.0)
    val totalSpent by viewModel.totalSpent.observeAsState(0.0)
    
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<BudgetCategory?>(null) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Budget Overview Header
        item {
            BudgetOverviewCard(
                totalBudget = totalBudget,
                totalSpent = totalSpent,
                onAddBudget = { showAddBudgetDialog = true }
            )
        }
        
        // Budget Categories
        if (budgetCategories.isEmpty()) {
            item {
                EmptyBudgetCard(
                    onCreateBudget = { showAddBudgetDialog = true }
                )
            }
        } else {
            items(budgetCategories) { category ->
                BudgetCategoryCard(
                    category = category,
                    onAddExpense = {
                        selectedCategory = category
                        showAddExpenseDialog = true
                    },
                    onEdit = { /* TODO: Implement edit */ },
                    onDelete = {
                        viewModel.deleteBudgetCategory(category.id)
                    }
                )
            }
        }
    }
    
    // Dialogs
    if (showAddBudgetDialog) {
        AddBudgetCategoryDialog(
            onDismiss = { showAddBudgetDialog = false },
            onAdd = { name, amount ->
                viewModel.addBudgetCategory(name, amount)
                showAddBudgetDialog = false
            }
        )
    }
    
    selectedCategory?.let { category ->
        if (showAddExpenseDialog) {
            AddExpenseDialog(
                category = category,
                onDismiss = { 
                    showAddExpenseDialog = false
                    selectedCategory = null
                },
                onAdd = { amount, description ->
                    viewModel.addExpenseToCategory(category.id, amount, description)
                    showAddExpenseDialog = false
                    selectedCategory = null
                }
            )
        }
    }
}

@Composable
private fun BudgetOverviewCard(
    totalBudget: Double,
    totalSpent: Double,
    onAddBudget: () -> Unit
) {
    val remainingBudget = totalBudget - totalSpent
    val spentPercentage = if (totalBudget > 0) (totalSpent / totalBudget * 100).toFloat() else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Budget",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                IconButton(onClick = onAddBudget) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Budget Category",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BudgetSummaryItem(
                    label = "Total Budget",
                    value = "$${String.format("%.0f", totalBudget)}",
                    icon = Icons.Default.AccountBox
                )
                
                BudgetSummaryItem(
                    label = "Spent",
                    value = "$${String.format("%.0f", totalSpent)}",
                    icon = Icons.Default.KeyboardArrowDown
                )
                
                BudgetSummaryItem(
                    label = "Remaining",
                    value = "$${String.format("%.0f", remainingBudget)}",
                    icon = Icons.Default.Star
                )
                
                BudgetSummaryItem(
                    label = "Used",
                    value = "${String.format("%.0f", spentPercentage)}%",
                    icon = Icons.Default.Info
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { minOf(spentPercentage / 100f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    spentPercentage >= 100 -> Color(0xFFF44336)
                    spentPercentage >= 80 -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                }
            )
        }
    }
}

@Composable
private fun BudgetSummaryItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BudgetCategoryCard(
    category: BudgetCategory,
    onAddExpense: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val spentPercentage = if (category.budgetAmount > 0) {
        (category.spentAmount / category.budgetAmount * 100).toFloat()
    } else 0f
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Spent: $${String.format("%.2f", category.spentAmount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Budget: $${String.format("%.2f", category.budgetAmount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Category"
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Category",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { minOf(spentPercentage / 100f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    spentPercentage >= 100 -> Color(0xFFF44336)
                    spentPercentage >= 80 -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${String.format("%.1f", spentPercentage)}% used",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        spentPercentage >= 100 -> Color(0xFFF44336)
                        spentPercentage >= 80 -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    }
                )
                
                Button(
                    onClick = onAddExpense,
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Expense", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun EmptyBudgetCard(
    onCreateBudget: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Budget Categories",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Create budget categories to track your monthly spending and stay on target!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onCreateBudget,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create First Budget Category")
            }
        }
    }
}

@Composable
private fun AddBudgetCategoryDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var budgetAmount by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Budget Category", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Groceries, Entertainment") }
                )
                
                OutlinedTextField(
                    value = budgetAmount,
                    onValueChange = { budgetAmount = it },
                    label = { Text("Monthly Budget") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Text("$") },
                    placeholder = { Text("0.00") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = budgetAmount.toDoubleOrNull()
                    if (categoryName.isNotBlank() && amount != null && amount > 0) {
                        onAdd(categoryName, amount)
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
private fun AddExpenseDialog(
    category: BudgetCategory,
    onDismiss: () -> Unit,
    onAdd: (Double, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense to ${category.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Budget: $${String.format("%.2f", category.budgetAmount)}\nSpent: $${String.format("%.2f", category.spentAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Expense Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Text("$") },
                    placeholder = { Text("0.00") }
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("What was this expense for?") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val expenseAmount = amount.toDoubleOrNull()
                    if (expenseAmount != null && expenseAmount > 0) {
                        onAdd(expenseAmount, description)
                    }
                },
                enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
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