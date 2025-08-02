package com.cp3406.financetracker.ui.transactions

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Transaction(
    val id: String,
    val description: String,
    val amount: Double,
    val category: String,
    val date: Date,
    val type: TransactionType,
    val merchantName: String? = null
) {
    val isExpense: Boolean
        get() = type == TransactionType.EXPENSE
    
    val isIncome: Boolean
        get() = type == TransactionType.INCOME
        
    val displayAmount: Double
        get() = if (isExpense) -amount else amount
    
    val formattedAmount: String
        get() {
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            val prefix = if (isIncome) "+" else "-"
            return "$prefix${formatter.format(amount)}"
        }
    
    val formattedDate: String
        get() {
            val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
            return formatter.format(date)
        }
}

enum class TransactionType {
    INCOME, EXPENSE
}