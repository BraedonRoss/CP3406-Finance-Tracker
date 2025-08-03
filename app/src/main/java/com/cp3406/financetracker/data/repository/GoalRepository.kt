package com.cp3406.financetracker.data.repository

import androidx.lifecycle.LiveData
import com.cp3406.financetracker.data.dao.GoalDao
import com.cp3406.financetracker.data.entity.GoalEntity
import com.cp3406.financetracker.data.entity.GoalCategory

class GoalRepository(private val goalDao: GoalDao) {
    
    fun getAllGoals(userId: String): LiveData<List<GoalEntity>> {
        return goalDao.getAllGoals(userId)
    }
    
    fun getActiveGoals(userId: String): LiveData<List<GoalEntity>> {
        return goalDao.getActiveGoals(userId)
    }
    
    fun getCompletedGoals(userId: String): LiveData<List<GoalEntity>> {
        return goalDao.getCompletedGoals(userId)
    }
    
    fun getGoalsByCategory(userId: String, category: GoalCategory): LiveData<List<GoalEntity>> {
        return goalDao.getGoalsByCategory(userId, category)
    }
    
    suspend fun insertGoal(goal: GoalEntity): Long {
        return goalDao.insertGoal(goal)
    }
    
    suspend fun updateGoal(goal: GoalEntity) {
        goalDao.updateGoal(goal)
    }
    
    suspend fun updateGoalProgress(id: Long, userId: String, currentAmount: Double) {
        goalDao.updateGoalProgress(id, userId, currentAmount)
    }
    
    suspend fun markGoalAsCompleted(id: Long, userId: String) {
        goalDao.markGoalAsCompleted(id, userId)
    }
    
    suspend fun markGoalAsIncomplete(id: Long, userId: String) {
        goalDao.markGoalAsIncomplete(id, userId)
    }
    
    suspend fun getGoalById(id: Long, userId: String): GoalEntity? {
        return goalDao.getGoalById(id, userId)
    }
    
    suspend fun deleteGoal(goal: GoalEntity) {
        goalDao.deleteGoal(goal)
    }
    
    suspend fun deleteGoalById(id: Long, userId: String) {
        goalDao.deleteGoalById(id, userId)
    }
    
    suspend fun deleteAllUserGoals(userId: String) {
        goalDao.deleteAllUserGoals(userId)
    }
    
    suspend fun deleteAllGoals() {
        goalDao.deleteAllGoals()
    }
}