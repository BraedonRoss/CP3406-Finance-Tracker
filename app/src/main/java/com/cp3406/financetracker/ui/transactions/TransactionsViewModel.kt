package com.cp3406.financetracker.ui.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class TransactionsViewModel : ViewModel() {

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    init {
        loadMockTransactions()
    }

    private fun loadMockTransactions() {
        val mockData = listOf(
            Transaction(
                id = "1",
                description = "Grocery Shopping",
                amount = 85.32,
                category = "Food & Dining",
                date = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.time,
                type = TransactionType.EXPENSE
            ),
            Transaction(
                id = "2", 
                description = "Salary Deposit",
                amount = 2500.00,
                category = "Income",
                date = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -3) }.time,
                type = TransactionType.INCOME
            ),
            Transaction(
                id = "3",
                description = "Coffee Shop",
                amount = 4.50,
                category = "Food & Dining", 
                date = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -2) }.time,
                type = TransactionType.EXPENSE
            ),
            Transaction(
                id = "4",
                description = "Gas Station",
                amount = 45.00,
                category = "Transportation",
                date = Calendar.getInstance().time,
                type = TransactionType.EXPENSE
            )
        )
        _transactions.value = mockData.sortedByDescending { it.date }
    }
}