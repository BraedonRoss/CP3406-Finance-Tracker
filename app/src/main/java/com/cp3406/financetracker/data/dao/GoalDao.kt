package com.cp3406.financetracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cp3406.financetracker.data.entity.GoalEntity
import com.cp3406.financetracker.data.entity.GoalCategory

@Dao
interface GoalDao {
    
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY targetDate ASC")
    fun getAllGoals(userId: String): LiveData<List<GoalEntity>>
    
    @Query("SELECT * FROM goals WHERE userId = :userId AND isCompleted = 0 ORDER BY targetDate ASC")
    fun getActiveGoals(userId: String): LiveData<List<GoalEntity>>
    
    @Query("SELECT * FROM goals WHERE userId = :userId AND isCompleted = 1 ORDER BY targetDate DESC")
    fun getCompletedGoals(userId: String): LiveData<List<GoalEntity>>
    
    @Query("SELECT * FROM goals WHERE userId = :userId AND category = :category")
    fun getGoalsByCategory(userId: String, category: GoalCategory): LiveData<List<GoalEntity>>
    
    @Insert
    suspend fun insertGoal(goal: GoalEntity): Long
    
    @Update
    suspend fun updateGoal(goal: GoalEntity)
    
    @Query("UPDATE goals SET currentAmount = :currentAmount WHERE id = :id AND userId = :userId")
    suspend fun updateGoalProgress(id: Long, userId: String, currentAmount: Double)
    
    @Query("UPDATE goals SET isCompleted = 1 WHERE id = :id AND userId = :userId")
    suspend fun markGoalAsCompleted(id: Long, userId: String)
    
    @Query("UPDATE goals SET isCompleted = 0 WHERE id = :id AND userId = :userId")
    suspend fun markGoalAsIncomplete(id: Long, userId: String)
    
    @Query("SELECT * FROM goals WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getGoalById(id: Long, userId: String): GoalEntity?
    
    @Delete
    suspend fun deleteGoal(goal: GoalEntity)
    
    @Query("DELETE FROM goals WHERE id = :id AND userId = :userId")
    suspend fun deleteGoalById(id: Long, userId: String)
    
    @Query("DELETE FROM goals WHERE userId = :userId")
    suspend fun deleteAllUserGoals(userId: String)
    
    @Query("DELETE FROM goals")
    suspend fun deleteAllGoals()
}