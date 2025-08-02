package com.cp3406.financetracker.data.repository

import androidx.lifecycle.LiveData
import com.cp3406.financetracker.data.dao.TransactionDao
import com.cp3406.financetracker.data.entity.TransactionEntity
import com.cp3406.financetracker.data.entity.TransactionType
import java.util.Date

class TransactionRepository(private val transactionDao: TransactionDao) {
    
    fun getAllTransactions(): LiveData<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }
    
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): LiveData<List<TransactionEntity>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }
    
    fun getTransactionsByCategory(category: String): LiveData<List<TransactionEntity>> {
        return transactionDao.getTransactionsByCategory(category)
    }
    
    suspend fun getTotalAmountByTypeAndDateRange(type: TransactionType, startDate: Date, endDate: Date): Double {
        return transactionDao.getTotalAmountByTypeAndDateRange(type, startDate, endDate) ?: 0.0
    }
    
    suspend fun getSpentAmountByCategory(category: String, startDate: Date, endDate: Date): Double {
        return transactionDao.getSpentAmountByCategory(category, startDate, endDate) ?: 0.0
    }
    
    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }
    
    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }
    
    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }
    
    suspend fun deleteTransactionById(id: Long) {
        transactionDao.deleteTransactionById(id)
    }
    
    suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
    }
}