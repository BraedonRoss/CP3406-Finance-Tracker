package com.cp3406.financetracker.data.repository

import androidx.lifecycle.LiveData
import com.cp3406.financetracker.data.dao.BudgetDao
import com.cp3406.financetracker.data.entity.BudgetEntity

class BudgetRepository(private val budgetDao: BudgetDao) {
    
    fun getBudgetsForMonth(month: Int, year: Int): LiveData<List<BudgetEntity>> {
        return budgetDao.getBudgetsForMonth(month, year)
    }
    
    suspend fun getBudgetByCategory(category: String, month: Int, year: Int): BudgetEntity? {
        return budgetDao.getBudgetByCategory(category, month, year)
    }
    
    suspend fun getTotalBudgetForMonth(month: Int, year: Int): Double {
        return budgetDao.getTotalBudgetForMonth(month, year) ?: 0.0
    }
    
    suspend fun getTotalSpentForMonth(month: Int, year: Int): Double {
        return budgetDao.getTotalSpentForMonth(month, year) ?: 0.0
    }
    
    suspend fun insertBudget(budget: BudgetEntity): Long {
        return budgetDao.insertBudget(budget)
    }
    
    suspend fun updateBudget(budget: BudgetEntity) {
        budgetDao.updateBudget(budget)
    }
    
    suspend fun updateSpentAmount(category: String, month: Int, year: Int, spentAmount: Double) {
        budgetDao.updateSpentAmount(category, month, year, spentAmount)
    }
    
    suspend fun updateBudgetAmount(category: String, month: Int, year: Int, budgetAmount: Double) {
        budgetDao.updateBudgetAmount(category, month, year, budgetAmount)
    }
    
    suspend fun deleteBudget(budget: BudgetEntity) {
        budgetDao.deleteBudget(budget)
    }
    
    suspend fun deleteBudgetById(id: Long) {
        budgetDao.deleteBudgetById(id)
    }
    
    suspend fun deleteAllBudgets() {
        budgetDao.deleteAllBudgets()
    }
}