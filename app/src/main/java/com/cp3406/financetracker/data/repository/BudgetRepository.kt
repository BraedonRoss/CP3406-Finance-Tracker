package com.cp3406.financetracker.data.repository

import androidx.lifecycle.LiveData
import com.cp3406.financetracker.data.dao.BudgetDao
import com.cp3406.financetracker.data.entity.BudgetEntity

class BudgetRepository(private val budgetDao: BudgetDao) {
    
    fun getBudgetsForMonth(userId: String, month: Int, year: Int): LiveData<List<BudgetEntity>> {
        return budgetDao.getBudgetsForMonth(userId, month, year)
    }
    
    suspend fun getBudgetByCategory(userId: String, category: String, month: Int, year: Int): BudgetEntity? {
        return budgetDao.getBudgetByCategory(userId, category, month, year)
    }
    
    suspend fun getTotalBudgetForMonth(userId: String, month: Int, year: Int): Double {
        return budgetDao.getTotalBudgetForMonth(userId, month, year) ?: 0.0
    }
    
    suspend fun getTotalSpentForMonth(userId: String, month: Int, year: Int): Double {
        return budgetDao.getTotalSpentForMonth(userId, month, year) ?: 0.0
    }
    
    suspend fun insertBudget(budget: BudgetEntity): Long {
        return budgetDao.insertBudget(budget)
    }
    
    suspend fun updateBudget(budget: BudgetEntity) {
        budgetDao.updateBudget(budget)
    }
    
    suspend fun updateSpentAmount(userId: String, category: String, month: Int, year: Int, spentAmount: Double) {
        budgetDao.updateSpentAmount(userId, category, month, year, spentAmount)
    }
    
    suspend fun updateBudgetAmount(userId: String, category: String, month: Int, year: Int, budgetAmount: Double) {
        budgetDao.updateBudgetAmount(userId, category, month, year, budgetAmount)
    }
    
    suspend fun deleteBudget(budget: BudgetEntity) {
        budgetDao.deleteBudget(budget)
    }
    
    suspend fun deleteBudgetById(id: Long, userId: String) {
        budgetDao.deleteBudgetById(id, userId)
    }
    
    suspend fun deleteAllUserBudgets(userId: String) {
        budgetDao.deleteAllUserBudgets(userId)
    }
    
    suspend fun deleteAllBudgets() {
        budgetDao.deleteAllBudgets()
    }
}