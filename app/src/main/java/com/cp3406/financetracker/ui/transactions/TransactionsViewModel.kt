package com.cp3406.financetracker.ui.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.cp3406.financetracker.data.database.FinanceDatabase
import com.cp3406.financetracker.data.entity.TransactionEntity
import com.cp3406.financetracker.data.entity.TransactionType
import com.cp3406.financetracker.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.util.*

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository
    private val _transactions = MediatorLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    init {
        val database = FinanceDatabase.getDatabase(application)
        repository = TransactionRepository(database.transactionDao())
        
        val entityTransactions = repository.getAllTransactions()
        _transactions.addSource(entityTransactions) { entities ->
            _transactions.value = entities.map { it.toTransaction() }
        }
        
        // Initialize with sample data if database is empty
        initializeSampleData()
    }

    fun addTransaction(
        description: String,
        amount: Double,
        category: String,
        type: TransactionType,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val transaction = TransactionEntity(
                description = description,
                amount = amount,
                category = category,
                date = Date(),
                type = type,
                notes = notes
            )
            repository.insertTransaction(transaction)
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(transactionId)
        }
    }

    private fun initializeSampleData() {
        viewModelScope.launch {
            val existingTransactions = repository.getAllTransactions().value
            if (existingTransactions.isNullOrEmpty()) {
                val sampleTransactions = listOf(
                    TransactionEntity(
                        description = "Salary Deposit",
                        amount = 3200.00,
                        category = "Income",
                        date = Calendar.getInstance().apply { 
                            add(Calendar.DAY_OF_MONTH, -1)
                            set(Calendar.HOUR_OF_DAY, 9)
                        }.time,
                        type = TransactionType.INCOME
                    ),
                    TransactionEntity(
                        description = "Grocery Shopping",
                        amount = 127.45,
                        category = "Food & Dining",
                        date = Calendar.getInstance().apply { 
                            add(Calendar.DAY_OF_MONTH, 0)
                            set(Calendar.HOUR_OF_DAY, 14)
                        }.time,
                        type = TransactionType.EXPENSE
                    ),
                    TransactionEntity(
                        description = "Coffee",
                        amount = 5.20,
                        category = "Food & Dining",
                        date = Calendar.getInstance().apply { 
                            add(Calendar.DAY_OF_MONTH, 0)
                            set(Calendar.HOUR_OF_DAY, 10)
                        }.time,
                        type = TransactionType.EXPENSE
                    ),
                    TransactionEntity(
                        description = "Gas Station",
                        amount = 62.00,
                        category = "Transportation",
                        date = Calendar.getInstance().apply { 
                            add(Calendar.DAY_OF_MONTH, -1)
                            set(Calendar.HOUR_OF_DAY, 16)
                        }.time,
                        type = TransactionType.EXPENSE
                    ),
                    TransactionEntity(
                        description = "Netflix Subscription",
                        amount = 17.99,
                        category = "Entertainment",
                        date = Calendar.getInstance().apply { 
                            add(Calendar.DAY_OF_MONTH, -2)
                            set(Calendar.HOUR_OF_DAY, 12)
                        }.time,
                        type = TransactionType.EXPENSE
                    )
                )
                
                sampleTransactions.forEach { transaction ->
                    repository.insertTransaction(transaction)
                }
            }
        }
    }

    private fun TransactionEntity.toTransaction(): Transaction {
        return Transaction(
            id = this.id.toString(),
            description = this.description,
            amount = this.amount,
            category = this.category,
            date = this.date,
            type = when(this.type) {
                TransactionType.INCOME -> com.cp3406.financetracker.ui.transactions.TransactionType.INCOME
                TransactionType.EXPENSE -> com.cp3406.financetracker.ui.transactions.TransactionType.EXPENSE
            },
            merchantName = this.notes ?: ""
        )
    }
}