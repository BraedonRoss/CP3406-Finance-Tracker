package com.cp3406.financetracker.ui.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cp3406.financetracker.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeGoalsScreen(
    viewModel: GoalsViewModel = viewModel()
) {
    val goals by viewModel.goals.observeAsState(emptyList())
    val totalSaved by viewModel.totalSaved.observeAsState(0.0)
    val averageProgress by viewModel.averageProgress.observeAsState(0f)
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddProgressDialog by remember { mutableStateOf(false) }
    var showRemoveProgressDialog by remember { mutableStateOf(false) }
    var selectedGoal by remember { mutableStateOf<FinancialGoal?>(null) }
    
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
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Financial Goals",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        IconButton(
                            onClick = { showAddDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Goal",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
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
                            value = "$${String.format("%.0f", totalSaved)}",
                            label = "Total Saved"
                        )
                        
                        GoalSummaryItem(
                            icon = Icons.Default.Info,
                            value = "${String.format("%.0f", averageProgress)}%",
                            label = "Avg Progress"
                        )
                    }
                }
            }
        }
        
        // Goals List
        if (goals.isEmpty()) {
            item {
                EmptyGoalsCard(
                    onCreateGoal = { showAddDialog = true }
                )
            }
        } else {
            items(goals) { goal ->
                GoalItem(
                    goal = goal,
                    onEdit = {
                        selectedGoal = goal
                        showEditDialog = true
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
                        viewModel.deleteGoal(goal.id)
                    }
                )
            }
        }
    }
    
    // Dialogs
    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, targetAmount, targetDate, description ->
                viewModel.addGoal(title, targetAmount, targetDate, description)
                showAddDialog = false
            }
        )
    }
    
    selectedGoal?.let { goal ->
        if (showEditDialog) {
            EditGoalDialog(
                goal = goal,
                onDismiss = { 
                    showEditDialog = false
                    selectedGoal = null
                },
                onSave = { updatedGoal ->
                    viewModel.updateGoal(updatedGoal)
                    showEditDialog = false
                    selectedGoal = null
                }
            )
        }
        
        if (showAddProgressDialog) {
            AddProgressDialog(
                goal = goal,
                onDismiss = { 
                    showAddProgressDialog = false
                    selectedGoal = null
                },
                onAdd = { amount ->
                    viewModel.addProgressToGoal(goal.id, amount)
                    showAddProgressDialog = false
                    selectedGoal = null
                }
            )
        }
        
        if (showRemoveProgressDialog) {
            RemoveProgressDialog(
                goal = goal,
                onDismiss = { 
                    showRemoveProgressDialog = false
                    selectedGoal = null
                },
                onRemove = { amount ->
                    viewModel.removeProgressFromGoal(goal.id, amount)
                    showRemoveProgressDialog = false
                    selectedGoal = null
                }
            )
        }
    }
}

@Composable
private fun EmptyGoalsCard(
    onCreateGoal: () -> Unit
) {
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
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Financial Goals Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Create your first financial goal to start tracking your progress towards your dreams!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onCreateGoal,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Your First Goal")
            }
        }
    }
}

@Composable
private fun GoalSummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun GoalItem(
    goal: FinancialGoal,
    onEdit: () -> Unit,
    onAddProgress: () -> Unit,
    onRemoveProgress: () -> Unit,
    onDelete: () -> Unit
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
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            
            if (goal.description.isNotEmpty()) {
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            val progress = if (goal.targetAmount > 0) {
                (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
            } else 0f
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$${String.format("%.2f", goal.currentAmount)} / $${String.format("%.2f", goal.targetAmount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "Target: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(goal.targetDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddProgress,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
                
                if (goal.currentAmount > 0) {
                    OutlinedButton(
                        onClick = onRemoveProgress,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("Dec 31, 2024") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Goal") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it },
                    label = { Text("Target Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = targetAmount.toDoubleOrNull() ?: 0.0
                    if (title.isNotEmpty() && amount > 0) {
                        onAdd(title, amount, targetDate, description)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditGoalDialog(
    goal: FinancialGoal,
    onDismiss: () -> Unit,
    onSave: (FinancialGoal) -> Unit
) {
    var title by remember { mutableStateOf(goal.title) }
    var targetAmount by remember { mutableStateOf(goal.targetAmount.toString()) }
    var description by remember { mutableStateOf(goal.description) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Goal") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it },
                    label = { Text("Target Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = targetAmount.toDoubleOrNull() ?: goal.targetAmount
                    if (title.isNotEmpty()) {
                        onSave(goal.copy(title = title, targetAmount = amount, description = description))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProgressDialog(
    goal: FinancialGoal,
    onDismiss: () -> Unit,
    onAdd: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Progress to ${goal.title}") },
        text = {
            Column {
                Text("Current: $${String.format("%.2f", goal.currentAmount)}")
                Text("Target: $${String.format("%.2f", goal.targetAmount)}")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount to Add") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val addAmount = amount.toDoubleOrNull() ?: 0.0
                    if (addAmount > 0) {
                        onAdd(addAmount)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemoveProgressDialog(
    goal: FinancialGoal,
    onDismiss: () -> Unit,
    onRemove: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove Progress from ${goal.title}") },
        text = {
            Column {
                Text("Current: $${String.format("%.2f", goal.currentAmount)}")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount to Remove") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val removeAmount = amount.toDoubleOrNull() ?: 0.0
                    if (removeAmount > 0 && removeAmount <= goal.currentAmount) {
                        onRemove(removeAmount)
                    }
                }
            ) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}