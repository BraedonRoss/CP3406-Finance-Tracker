package com.cp3406.financetracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cp3406.financetracker.data.entity.BudgetEntity

@Dao
interface BudgetDao {
    
    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    fun getBudgetsForMonth(userId: String, month: Int, year: Int): LiveData<List<BudgetEntity>>
    
    @Query("SELECT * FROM budgets WHERE userId = :userId AND category = :category AND month = :month AND year = :year")
    suspend fun getBudgetByCategory(userId: String, category: String, month: Int, year: Int): BudgetEntity?
    
    @Query("SELECT SUM(budgetAmount) FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun getTotalBudgetForMonth(userId: String, month: Int, year: Int): Double?
    
    @Query("SELECT SUM(spentAmount) FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun getTotalSpentForMonth(userId: String, month: Int, year: Int): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long
    
    @Update
    suspend fun updateBudget(budget: BudgetEntity)
    
    @Query("UPDATE budgets SET spentAmount = :spentAmount WHERE userId = :userId AND category = :category AND month = :month AND year = :year")
    suspend fun updateSpentAmount(userId: String, category: String, month: Int, year: Int, spentAmount: Double)
    
    @Query("UPDATE budgets SET budgetAmount = :budgetAmount WHERE userId = :userId AND category = :category AND month = :month AND year = :year")
    suspend fun updateBudgetAmount(userId: String, category: String, month: Int, year: Int, budgetAmount: Double)
    
    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)
    
    @Query("DELETE FROM budgets WHERE id = :id AND userId = :userId")
    suspend fun deleteBudgetById(id: Long, userId: String)
    
    @Query("DELETE FROM budgets WHERE userId = :userId")
    suspend fun deleteAllUserBudgets(userId: String)
    
    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()
}