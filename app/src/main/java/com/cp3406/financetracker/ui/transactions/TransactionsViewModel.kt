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