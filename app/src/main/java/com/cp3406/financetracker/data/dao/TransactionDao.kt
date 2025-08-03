package com.cp3406.financetracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cp3406.financetracker.data.entity.TransactionEntity
import com.cp3406.financetracker.data.entity.TransactionType
import java.util.Date

@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getAllTransactions(userId: String): LiveData<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(userId: String, startDate: Date, endDate: Date): LiveData<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(userId: String, category: String): LiveData<List<TransactionEntity>>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalAmountByTypeAndDateRange(userId: String, type: TransactionType, startDate: Date, endDate: Date): Double?
    
    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND category = :category AND type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate")
    suspend fun getSpentAmountByCategory(userId: String, category: String, startDate: Date, endDate: Date): Double?
    
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long
    
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
    
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
    
    @Query("DELETE FROM transactions WHERE id = :id AND userId = :userId")
    suspend fun deleteTransactionById(id: Long, userId: String)
    
    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllUserTransactions(userId: String)
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}