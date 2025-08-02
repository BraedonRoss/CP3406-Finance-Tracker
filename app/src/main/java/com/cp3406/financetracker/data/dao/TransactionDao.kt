package com.cp3406.financetracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cp3406.financetracker.data.entity.TransactionEntity
import com.cp3406.financetracker.data.entity.TransactionType
import java.util.Date

@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): LiveData<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): LiveData<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): LiveData<List<TransactionEntity>>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalAmountByTypeAndDateRange(type: TransactionType, startDate: Date, endDate: Date): Double?
    
    @Query("SELECT SUM(amount) FROM transactions WHERE category = :category AND type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate")
    suspend fun getSpentAmountByCategory(category: String, startDate: Date, endDate: Date): Double?
    
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long
    
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
    
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
    
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}