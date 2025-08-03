package com.cp3406.financetracker.ui.transactions

import androidx.compose.foundation.background
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
import com.cp3406.financetracker.data.entity.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeTransactionsScreen(
    viewModel: TransactionsViewModel = viewModel()
) {
    val transactions by viewModel.transactions.observeAsState(emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(TransactionFilter.ALL) }

    val filteredTransactions = when (selectedFilter) {
        TransactionFilter.ALL -> transactions
        TransactionFilter.INCOME -> transactions.filter { it.isIncome }
        TransactionFilter.EXPENSE -> transactions.filter { it.isExpense }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Add Button and Filters
        item {
            TransactionHeaderCard(
                totalTransactions = transactions.size,
                totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount },
                totalExpenses = transactions.filter { it.isExpense }.sumOf { it.amount },
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it },
                onAddTransaction = { showAddDialog = true }
            )
        }

        // Transaction List
        if (filteredTransactions.isEmpty()) {
            item {
                EmptyTransactionsCard(
                    filter = selectedFilter,
                    onAddTransaction = { showAddDialog = true }
                )
            }
        } else {
            // Group transactions by date
            val groupedTransactions = filteredTransactions.groupBy { it.formattedDate }
            groupedTransactions.forEach { (date, transactionsForDate) ->
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(transactionsForDate) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onEdit = { /* TODO: Implement edit */ },
                        onDelete = { viewModel.deleteTransaction(transaction.id) }
                    )
                }
            }
        }
    }

    // Add Transaction Dialog
    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { description, amount, category, type, notes ->
                viewModel.addTransaction(description, amount, category, type, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TransactionHeaderCard(
    totalTransactions: Int,
    totalIncome: Double,
    totalExpenses: Double,
    selectedFilter: TransactionFilter,
    onFilterChange: (TransactionFilter) -> Unit,
    onAddTransaction: () -> Unit
) {
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
                    text = "Transactions",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                IconButton(onClick = onAddTransaction) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TransactionSummaryItem(
                    label = "Total",
                    value = "$totalTransactions",
                    icon = Icons.Default.List
                )

                TransactionSummaryItem(
                    label = "Income",
                    value = "$${String.format("%.0f", totalIncome)}",
                    icon = Icons.Default.Add,
                    color = Color(0xFF4CAF50)
                )

                TransactionSummaryItem(
                    label = "Expenses",
                    value = "$${String.format("%.0f", totalExpenses)}",
                    icon = Icons.Default.KeyboardArrowDown,
                    color = Color(0xFFF44336)
                )

                TransactionSummaryItem(
                    label = "Net",
                    value = "$${String.format("%.0f", totalIncome - totalExpenses)}",
                    icon = Icons.Default.AccountBox,
                    color = if (totalIncome >= totalExpenses) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransactionFilter.values().forEach { filter ->
                    FilterChip(
                        onClick = { onFilterChange(filter) },
                        label = { Text(filter.displayName) },
                        selected = selectedFilter == filter,
                        leadingIcon = if (selectedFilter == filter) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionSummaryItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${transaction.category} • ${transaction.formattedDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val amountColor = if (transaction.isIncome) {
                    Color(0xFF4CAF50)
                } else {
                    Color(0xFFF44336)
                }

                Text(
                    text = transaction.formattedAmount,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (transaction.isIncome)
                                Color(0xFF4CAF50).copy(alpha = 0.1f)
                            else
                                Color(0xFFF44336).copy(alpha = 0.1f)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (transaction.isIncome) "Income" else "Expense",
                        style = MaterialTheme.typography.labelSmall,
                        color = amountColor
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Transaction",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyTransactionsCard(
    filter: TransactionFilter,
    onAddTransaction: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (filter) {
                    TransactionFilter.ALL -> "No Transactions Yet"
                    TransactionFilter.INCOME -> "No Income Transactions"
                    TransactionFilter.EXPENSE -> "No Expense Transactions"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (filter) {
                    TransactionFilter.ALL -> "Start tracking your finances by adding your first transaction"
                    TransactionFilter.INCOME -> "Add income transactions to track your earnings"
                    TransactionFilter.EXPENSE -> "Add expense transactions to track your spending"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddTransaction,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add First Transaction")
            }
        }
    }
}

@Composable
private fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, String, TransactionType, String?) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Transaction", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Coffee, Salary") }
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Text("$") },
                    placeholder = { Text("0.00") }
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Food, Salary, Entertainment") }
                )

                // Transaction Type Selection
                Column {
                    Text(
                        text = "Type",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { transactionType = TransactionType.INCOME },
                            label = { Text("Income") },
                            selected = transactionType == TransactionType.INCOME,
                            leadingIcon = if (transactionType == TransactionType.INCOME) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                        FilterChip(
                            onClick = { transactionType = TransactionType.EXPENSE },
                            label = { Text("Expense") },
                            selected = transactionType == TransactionType.EXPENSE,
                            leadingIcon = if (transactionType == TransactionType.EXPENSE) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Additional details...") },
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val transactionAmount = amount.toDoubleOrNull()
                    if (description.isNotBlank() && transactionAmount != null && transactionAmount > 0 && category.isNotBlank()) {
                        onAdd(description, transactionAmount, category, transactionType, notes.ifBlank { null })
                    }
                },
                enabled = description.isNotBlank() && amount.toDoubleOrNull() != null && category.isNotBlank()
            ) {
                Text("Add Transaction")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

enum class TransactionFilter(val displayName: String) {
    ALL("All"),
    INCOME("Income"),
    EXPENSE("Expenses")
}