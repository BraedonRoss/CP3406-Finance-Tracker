package com.cp3406.financetracker.ui.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class TransactionsViewModel : ViewModel() {

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        val mockTransactions = listOf(
            Transaction(
                id = "1",
                description = "Salary Deposit",
                amount = 3200.00,
                category = "Income",
                date = Calendar.getInstance().apply { 
                    add(Calendar.DAY_OF_MONTH, -1)
                    set(Calendar.HOUR_OF_DAY, 9)
                }.time,
                type = TransactionType.INCOME,
                merchantName = "ABC Company Ltd"
            ),
            Transaction(
                id = "2",
                description = "Grocery Shopping",
                amount = 127.45,
                category = "Food & Dining",
                date = Calendar.getInstance().apply { 
                    add(Calendar.DAY_OF_MONTH, 0)
                    set(Calendar.HOUR_OF_DAY, 14)
                }.time,
                type = TransactionType.EXPENSE,
                merchantName = "Woolworths"
            ),
            Transaction(
                id = "3",
                description = "Coffee",
                amount = 5.20,
                category = "Food & Dining",
                date = Calendar.getInstance().apply { 
                    add(Calendar.DAY_OF_MONTH, 0)
                    set(Calendar.HOUR_OF_DAY, 10)
                }.time,
                type = TransactionType.EXPENSE,
                merchantName = "Starbucks"
            ),
            Transaction(
                id = "4",
                description = "Gas Station",
                amount = 62.00,
                category = "Transportation",
                date = Calendar.getInstance().apply { 
                    add(Calendar.DAY_OF_MONTH, -1)
                    set(Calendar.HOUR_OF_DAY, 16)
                }.time,
                type = TransactionType.EXPENSE,
                merchantName = "Shell Service Station"
            ),
            Transaction(
                id = "5",
                description = "Netflix Subscription",
                amount = 17.99,
                category = "Entertainment",
                date = Calendar.getInstance().apply { 
                    add(Calendar.DAY_OF_MONTH, -2)
                    set(Calendar.HOUR_OF_DAY, 12)
                }.time,
                type = TransactionType.EXPENSE,
                merchantName = "Netflix"
            ),
            Transaction(
                id = "6",
                description = "ATM Withdrawal",
                amount = 100.00,
                category = "Cash & ATM",
                date = Calendar.getInstance().apply { 
                    add(Calendar.DAY_OF_MONTH, -3)
                    set(Calendar.HOUR_OF_DAY, 18)
                }.time,
                type = TransactionType.EXPENSE,
                merchantName = "ANZ ATM"
            ),
            Transaction(
                id = "7",
                description = "Freelance Payment",
                amount = 450.00,
                category = "Income",
                date = Calendar.getInstance().apply { 
                    add(Calendar.DAY_OF_MONTH, -4)
                    set(Calendar.HOUR_OF_DAY, 11)
                }.time,
                type = TransactionType.INCOME,
                merchantName = "XYZ Digital Agency"
            ),
            Transaction(
                id = "8",
                description = "Uber Ride",
                amount = 23.50,
                category = "Transportation",
                date = Calendar.getInstance().apply { 
                    add(Calendar.DAY_OF_MONTH, -2)
                    set(Calendar.HOUR_OF_DAY, 22)
                }.time,
                type = TransactionType.EXPENSE,
                merchantName = "Uber"
            )
        )
        
        // Sort by date descending (most recent first)
        _transactions.value = mockTransactions.sortedByDescending { it.date }
    }
}