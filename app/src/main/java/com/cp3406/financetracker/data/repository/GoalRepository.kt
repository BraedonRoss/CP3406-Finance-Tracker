package com.cp3406.financetracker.data.repository

import androidx.lifecycle.LiveData
import com.cp3406.financetracker.data.dao.GoalDao
import com.cp3406.financetracker.data.entity.GoalEntity
import com.cp3406.financetracker.data.entity.GoalCategory

class GoalRepository(private val goalDao: GoalDao) {
    
    fun getAllGoals(): LiveData<List<GoalEntity>> {
        return goalDao.getAllGoals()
    }
    
    fun getActiveGoals(): LiveData<List<GoalEntity>> {
        return goalDao.getActiveGoals()
    }
    
    fun getCompletedGoals(): LiveData<List<GoalEntity>> {
        return goalDao.getCompletedGoals()
    }
    
    fun getGoalsByCategory(category: GoalCategory): LiveData<List<GoalEntity>> {
        return goalDao.getGoalsByCategory(category)
    }
    
    suspend fun insertGoal(goal: GoalEntity): Long {
        return goalDao.insertGoal(goal)
    }
    
    suspend fun updateGoal(goal: GoalEntity) {
        goalDao.updateGoal(goal)
    }
    
    suspend fun updateGoalProgress(id: Long, currentAmount: Double) {
        goalDao.updateGoalProgress(id, currentAmount)
    }
    
    suspend fun markGoalAsCompleted(id: Long) {
        goalDao.markGoalAsCompleted(id)
    }
    
    suspend fun deleteGoal(goal: GoalEntity) {
        goalDao.deleteGoal(goal)
    }
    
    suspend fun deleteGoalById(id: Long) {
        goalDao.deleteGoalById(id)
    }
}