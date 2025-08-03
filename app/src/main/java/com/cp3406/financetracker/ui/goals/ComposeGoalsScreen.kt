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