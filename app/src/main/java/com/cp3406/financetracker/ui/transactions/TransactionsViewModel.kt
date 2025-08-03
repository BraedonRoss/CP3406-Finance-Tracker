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
import com.cp3406.financetracker.utils.UserUtils
import kotlinx.coroutines.launch
import java.util.*

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository
    private val _transactions = MediatorLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    init {
        val database = FinanceDatabase.getDatabase(application)
        repository = TransactionRepository(database.transactionDao())
        
        val currentUserId = UserUtils.getCurrentUserId()
        if (currentUserId != null) {
            val entityTransactions = repository.getAllTransactions(currentUserId)
            _transactions.addSource(entityTransactions) { entities ->
                _transactions.value = entities.map { it.toTransaction() }
            }
        } else {
            _transactions.value = emptyList()
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
            val currentUserId = UserUtils.getCurrentUserId() ?: return@launch
            val transaction = TransactionEntity(
                userId = currentUserId,
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
            val currentUserId = UserUtils.getCurrentUserId() ?: return@launch
            repository.deleteTransactionById(transactionId, currentUserId)
        }
    }
    
    fun deleteTransaction(transactionId: String) {
        deleteTransaction(transactionId.toLong())
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