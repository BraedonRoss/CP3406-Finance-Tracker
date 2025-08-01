package com.cp3406.financetracker.ui.goals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class GoalsViewModel : ViewModel() {

    private val _goals = MutableLiveData<List<FinancialGoal>>()
    val goals: LiveData<List<FinancialGoal>> = _goals

    private val _totalSaved = MutableLiveData<Double>()
    val totalSaved: LiveData<Double> = _totalSaved

    private val _averageProgress = MutableLiveData<Float>()
    val averageProgress: LiveData<Float> = _averageProgress

    init {
        loadGoals()
    }

    private fun loadGoals() {
        val mockGoals = listOf(
            FinancialGoal(
                id = "1",
                title = "Emergency Fund",
                description = "6 months of living expenses",
                targetAmount = 15000.00,
                currentAmount = 8750.00,
                targetDate = Calendar.getInstance().apply { 
                    add(Calendar.MONTH, 8) 
                }.time,
                category = GoalCategory.EMERGENCY_FUND,
                priority = GoalPriority.HIGH
            ),
            FinancialGoal(
                id = "2",
                title = "Europe Vacation",
                description = "Two weeks touring Europe next summer",
                targetAmount = 5000.00,
                currentAmount = 3200.00,
                targetDate = Calendar.getInstance().apply { 
                    add(Calendar.MONTH, 6) 
                }.time,
                category = GoalCategory.VACATION,
                priority = GoalPriority.MEDIUM
            ),
            FinancialGoal(
                id = "3",
                title = "New Car",
                description = "Down payment for reliable vehicle",
                targetAmount = 12000.00,
                currentAmount = 4500.00,
                targetDate = Calendar.getInstance().apply { 
                    add(Calendar.MONTH, 10) 
                }.time,
                category = GoalCategory.CAR,
                priority = GoalPriority.HIGH
            ),
            FinancialGoal(
                id = "4",
                title = "Investment Portfolio",
                description = "Build diversified investment portfolio",
                targetAmount = 25000.00,
                currentAmount = 12800.00,
                targetDate = Calendar.getInstance().apply { 
                    add(Calendar.YEAR, 2) 
                }.time,
                category = GoalCategory.INVESTMENT,
                priority = GoalPriority.MEDIUM
            ),
            FinancialGoal(
                id = "5",
                title = "House Down Payment",
                description = "20% down payment for first home",
                targetAmount = 60000.00,
                currentAmount = 18500.00,
                targetDate = Calendar.getInstance().apply { 
                    add(Calendar.YEAR, 3) 
                }.time,
                category = GoalCategory.HOME,
                priority = GoalPriority.HIGH
            ),
            FinancialGoal(
                id = "6",
                title = "Professional Course",
                description = "Complete certification program",
                targetAmount = 2500.00,
                currentAmount = 2500.00,
                targetDate = Calendar.getInstance().apply { 
                    add(Calendar.MONTH, -1) 
                }.time,
                category = GoalCategory.EDUCATION,
                priority = GoalPriority.LOW,
                isCompleted = true
            )
        )

        _goals.value = mockGoals.sortedByDescending { it.priority.ordinal }
        
        // Calculate totals
        val totalSavedAmount = mockGoals.sumOf { it.currentAmount }
        _totalSaved.value = totalSavedAmount
        
        // Calculate average progress
        val activeGoals = mockGoals.filter { !it.isCompleted }
        val avgProgress = if (activeGoals.isNotEmpty()) {
            activeGoals.map { it.progressPercentage }.average().toFloat()
        } else 0f
        _averageProgress.value = avgProgress
    }
}