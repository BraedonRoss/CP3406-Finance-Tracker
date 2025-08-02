package com.cp3406.financetracker.ui.goals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.cp3406.financetracker.data.database.FinanceDatabase
import com.cp3406.financetracker.data.entity.GoalEntity
import com.cp3406.financetracker.data.entity.GoalCategory
import com.cp3406.financetracker.data.repository.GoalRepository
import kotlinx.coroutines.launch
import java.util.*

class GoalsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GoalRepository
    private val _goals = MediatorLiveData<List<FinancialGoal>>()
    val goals: LiveData<List<FinancialGoal>> = _goals

    private val _totalSaved = MediatorLiveData<Double>()
    val totalSaved: LiveData<Double> = _totalSaved

    private val _averageProgress = MediatorLiveData<Float>()
    val averageProgress: LiveData<Float> = _averageProgress

    init {
        val database = FinanceDatabase.getDatabase(application)
        repository = GoalRepository(database.goalDao())
        
        val entityGoals = repository.getAllGoals()
        _goals.addSource(entityGoals) { entities ->
            val financialGoals = entities.map { it.toFinancialGoal() }
            _goals.value = financialGoals
            
            // Calculate totals
            val totalSavedAmount = financialGoals.sumOf { it.currentAmount }
            _totalSaved.value = totalSavedAmount
            
            // Calculate average progress
            val activeGoals = financialGoals.filter { !it.isCompleted }
            val avgProgress = if (activeGoals.isNotEmpty()) {
                activeGoals.map { it.progressPercentage }.average().toFloat()
            } else 0f
            _averageProgress.value = avgProgress
        }
        
    }

    fun addGoal(
        title: String,
        description: String,
        targetAmount: Double,
        targetDate: Date,
        category: GoalCategory
    ) {
        viewModelScope.launch {
            val goal = GoalEntity(
                title = title,
                description = description,
                targetAmount = targetAmount,
                targetDate = targetDate,
                category = category
            )
            repository.insertGoal(goal)
        }
    }

    fun updateGoalProgress(goalId: Long, newAmount: Double) {
        viewModelScope.launch {
            repository.updateGoalProgress(goalId, newAmount)
        }
    }

    fun markGoalCompleted(goalId: Long) {
        viewModelScope.launch {
            repository.markGoalAsCompleted(goalId)
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            repository.deleteGoalById(goalId)
        }
    }

    private fun GoalEntity.toFinancialGoal(): FinancialGoal {
        return FinancialGoal(
            id = this.id.toString(),
            title = this.title,
            description = this.description,
            targetAmount = this.targetAmount,
            currentAmount = this.currentAmount,
            targetDate = this.targetDate,
            category = when(this.category) {
                GoalCategory.EMERGENCY_FUND -> com.cp3406.financetracker.ui.goals.GoalCategory.EMERGENCY_FUND
                GoalCategory.VACATION -> com.cp3406.financetracker.ui.goals.GoalCategory.VACATION
                GoalCategory.HOME -> com.cp3406.financetracker.ui.goals.GoalCategory.HOME
                GoalCategory.CAR -> com.cp3406.financetracker.ui.goals.GoalCategory.CAR
                GoalCategory.EDUCATION -> com.cp3406.financetracker.ui.goals.GoalCategory.EDUCATION
                GoalCategory.RETIREMENT -> com.cp3406.financetracker.ui.goals.GoalCategory.INVESTMENT
                GoalCategory.OTHER -> com.cp3406.financetracker.ui.goals.GoalCategory.OTHER
            },
            priority = when {
                this.category == GoalCategory.EMERGENCY_FUND -> GoalPriority.HIGH
                this.targetAmount > 50000 -> GoalPriority.HIGH
                this.targetAmount > 10000 -> GoalPriority.MEDIUM
                else -> GoalPriority.LOW
            },
            isCompleted = this.isCompleted
        )
    }
}