package com.cp3406.financetracker.data.repository

import androidx.lifecycle.LiveData
import com.cp3406.financetracker.data.dao.TransactionDao
import com.cp3406.financetracker.data.entity.TransactionEntity
import com.cp3406.financetracker.data.entity.TransactionType
import java.util.Date

class TransactionRepository(private val transactionDao: TransactionDao) {
    
    fun getAllTransactions(userId: String): LiveData<List<TransactionEntity>> {
        return transactionDao.getAllTransactions(userId)
    }
    
    fun getTransactionsByDateRange(userId: String, startDate: Date, endDate: Date): LiveData<List<TransactionEntity>> {
        return transactionDao.getTransactionsByDateRange(userId, startDate, endDate)
    }
    
    fun getTransactionsByCategory(userId: String, category: String): LiveData<List<TransactionEntity>> {
        return transactionDao.getTransactionsByCategory(userId, category)
    }
    
    suspend fun getTotalAmountByTypeAndDateRange(userId: String, type: TransactionType, startDate: Date, endDate: Date): Double {
        return transactionDao.getTotalAmountByTypeAndDateRange(userId, type, startDate, endDate) ?: 0.0
    }
    
    suspend fun getSpentAmountByCategory(userId: String, category: String, startDate: Date, endDate: Date): Double {
        return transactionDao.getSpentAmountByCategory(userId, category, startDate, endDate) ?: 0.0
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
    
    suspend fun deleteTransactionById(id: Long, userId: String) {
        transactionDao.deleteTransactionById(id, userId)
    }
    
    suspend fun deleteAllUserTransactions(userId: String) {
        transactionDao.deleteAllUserTransactions(userId)
    }
    
    suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
    }
}