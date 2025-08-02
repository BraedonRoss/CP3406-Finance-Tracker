package com.cp3406.financetracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cp3406.financetracker.data.entity.GoalEntity
import com.cp3406.financetracker.data.entity.GoalCategory

@Dao
interface GoalDao {
    
    @Query("SELECT * FROM goals ORDER BY targetDate ASC")
    fun getAllGoals(): LiveData<List<GoalEntity>>
    
    @Query("SELECT * FROM goals WHERE isCompleted = 0 ORDER BY targetDate ASC")
    fun getActiveGoals(): LiveData<List<GoalEntity>>
    
    @Query("SELECT * FROM goals WHERE isCompleted = 1 ORDER BY targetDate DESC")
    fun getCompletedGoals(): LiveData<List<GoalEntity>>
    
    @Query("SELECT * FROM goals WHERE category = :category")
    fun getGoalsByCategory(category: GoalCategory): LiveData<List<GoalEntity>>
    
    @Insert
    suspend fun insertGoal(goal: GoalEntity): Long
    
    @Update
    suspend fun updateGoal(goal: GoalEntity)
    
    @Query("UPDATE goals SET currentAmount = :currentAmount WHERE id = :id")
    suspend fun updateGoalProgress(id: Long, currentAmount: Double)
    
    @Query("UPDATE goals SET isCompleted = 1 WHERE id = :id")
    suspend fun markGoalAsCompleted(id: Long)
    
    @Delete
    suspend fun deleteGoal(goal: GoalEntity)
    
    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)
    
    @Query("DELETE FROM goals")
    suspend fun deleteAllGoals()
}