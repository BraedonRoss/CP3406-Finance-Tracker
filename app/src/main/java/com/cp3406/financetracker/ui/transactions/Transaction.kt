package com.cp3406.financetracker.ui.transactions

import java.util.Date

data class Transaction(
    val id: String = "",
    val description: String,
    val amount: Double,
    val category: String,
    val date: Date,
    val type: TransactionType
) {
    val isExpense: Boolean
        get() = type == TransactionType.EXPENSE
}

enum class TransactionType {
    INCOME, EXPENSE
}