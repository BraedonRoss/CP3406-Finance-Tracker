package com.cp3406.financetracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cp3406.financetracker.data.entity.BudgetEntity

@Dao
interface BudgetDao {
    
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    fun getBudgetsForMonth(month: Int, year: Int): LiveData<List<BudgetEntity>>
    
    @Query("SELECT * FROM budgets WHERE category = :category AND month = :month AND year = :year")
    suspend fun getBudgetByCategory(category: String, month: Int, year: Int): BudgetEntity?
    
    @Query("SELECT SUM(budgetAmount) FROM budgets WHERE month = :month AND year = :year")
    suspend fun getTotalBudgetForMonth(month: Int, year: Int): Double?
    
    @Query("SELECT SUM(spentAmount) FROM budgets WHERE month = :month AND year = :year")
    suspend fun getTotalSpentForMonth(month: Int, year: Int): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long
    
    @Update
    suspend fun updateBudget(budget: BudgetEntity)
    
    @Query("UPDATE budgets SET spentAmount = :spentAmount WHERE category = :category AND month = :month AND year = :year")
    suspend fun updateSpentAmount(category: String, month: Int, year: Int, spentAmount: Double)
    
    @Query("UPDATE budgets SET budgetAmount = :budgetAmount WHERE category = :category AND month = :month AND year = :year")
    suspend fun updateBudgetAmount(category: String, month: Int, year: Int, budgetAmount: Double)
    
    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)
    
    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudgetById(id: Long)
    
    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()
}