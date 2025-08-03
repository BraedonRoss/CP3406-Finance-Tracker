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
import com.cp3406.financetracker.data.repository.TransactionRepository
import com.cp3406.financetracker.data.entity.TransactionEntity
import com.cp3406.financetracker.utils.UserUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GoalsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GoalRepository
    private val transactionRepository: TransactionRepository
    private val _goals = MediatorLiveData<List<FinancialGoal>>()
    val goals: LiveData<List<FinancialGoal>> = _goals

    private val _totalSaved = MediatorLiveData<Double>()
    val totalSaved: LiveData<Double> = _totalSaved

    private val _averageProgress = MediatorLiveData<Float>()
    val averageProgress: LiveData<Float> = _averageProgress

    init {
        val database = FinanceDatabase.getDatabase(application)
        repository = GoalRepository(database.goalDao())
        transactionRepository = TransactionRepository(database.transactionDao())
        
        val currentUserId = UserUtils.getCurrentUserId()
        if (currentUserId != null) {
            val entityGoals = repository.getAllGoals(currentUserId)
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
        } else {
            // No authenticated user - set empty defaults
            _goals.value = emptyList()
            _totalSaved.value = 0.0
            _averageProgress.value = 0f
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
            val currentUserId = UserUtils.getCurrentUserId() ?: return@launch
            val goal = GoalEntity(
                userId = currentUserId,
                title = title,
                description = description,
                targetAmount = targetAmount,
                targetDate = targetDate,
                category = category
            )
            repository.insertGoal(goal)
        }
    }

    fun updateGoal(updatedGoal: FinancialGoal) {
        viewModelScope.launch {
            val currentUserId = UserUtils.getCurrentUserId() ?: return@launch
            val goalEntity = GoalEntity(
                id = updatedGoal.id.toLong(),
                userId = currentUserId,
                title = updatedGoal.title,
                description = updatedGoal.description,
                targetAmount = updatedGoal.targetAmount,
                currentAmount = updatedGoal.currentAmount,
                targetDate = updatedGoal.targetDate,
                category = when(updatedGoal.category) {
                    com.cp3406.financetracker.ui.goals.GoalCategory.EMERGENCY_FUND -> GoalCategory.EMERGENCY_FUND
                    com.cp3406.financetracker.ui.goals.GoalCategory.VACATION -> GoalCategory.VACATION
                    com.cp3406.financetracker.ui.goals.GoalCategory.HOME -> GoalCategory.HOME
                    com.cp3406.financetracker.ui.goals.GoalCategory.CAR -> GoalCategory.CAR
                    com.cp3406.financetracker.ui.goals.GoalCategory.EDUCATION -> GoalCategory.EDUCATION
                    com.cp3406.financetracker.ui.goals.GoalCategory.RETIREMENT -> GoalCategory.RETIREMENT
                    com.cp3406.financetracker.ui.goals.GoalCategory.INVESTMENT -> GoalCategory.RETIREMENT
                    com.cp3406.financetracker.ui.goals.GoalCategory.OTHER -> GoalCategory.OTHER
                },
                isCompleted = updatedGoal.isCompleted
            )
            repository.updateGoal(goalEntity)
        }
    }

    fun updateGoalProgress(goalId: Long, newAmount: Double) {
        viewModelScope.launch {
            val currentUserId = UserUtils.getCurrentUserId() ?: return@launch
            repository.updateGoalProgress(goalId, currentUserId, newAmount)
        }
    }

    fun markGoalCompleted(goalId: Long) {
        viewModelScope.launch {
            val currentUserId = UserUtils.getCurrentUserId() ?: return@launch
            repository.markGoalAsCompleted(goalId, currentUserId)
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            val currentUserId = UserUtils.getCurrentUserId() ?: return@launch
            repository.deleteGoalById(goalId, currentUserId)
        }
    }
    
    fun deleteGoal(goalId: String) {
        deleteGoal(goalId.toLong())
    }
    
    fun addGoal(title: String, targetAmount: Double, targetDate: String, description: String) {
        viewModelScope.launch {
            val currentUserId = UserUtils.getCurrentUserId() ?: return@launch
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val parsedDate = try {
                dateFormat.parse(targetDate) ?: Date()
            } catch (e: Exception) {
                Date()
            }
            
            val goal = GoalEntity(
                userId = currentUserId,
                title = title,
                description = description,
                targetAmount = targetAmount,
                targetDate = parsedDate,
                category = com.cp3406.financetracker.data.entity.GoalCategory.OTHER
            )
            repository.insertGoal(goal)
        }
    }
    
    fun addProgressToGoal(goalId: String, amount: Double) {
        viewModelScope.launch {
            val currentUserId = UserUtils.getCurrentUserId() ?: return@launch
            val goalIdLong = goalId.toLong()
            val currentGoal = repository.getGoalById(goalIdLong, currentUserId)
            currentGoal?.let { goal ->
                val newAmount = goal.currentAmount + amount
                val isCompleted = newAmount >= goal.targetAmount
                repository.updateGoalProgress(goalIdLong, currentUserId, newAmount)
                if (isCompleted) {
                    repository.markGoalAsCompleted(goalIdLong, currentUserId)
                }
                
                // Create transaction entry for goal progress
                val transaction = TransactionEntity(
                    userId = currentUserId,
                    description = "Added to ${goal.title} goal",
                    amount = amount,
                    category = "Goals - ${goal.category.name}",
                    date = Date(),
                    type = com.cp3406.financetracker.data.entity.TransactionType.EXPENSE,
                    isRecurring = false,
                    notes = "Goal progress: $${String.format("%.2f", goal.currentAmount)} → $${String.format("%.2f", newAmount)}"
                )
                transactionRepository.insertTransaction(transaction)
            }
        }
    }
    
    fun removeProgressFromGoal(goalId: String, amount: Double) {
        viewModelScope.launch {
            val currentUserId = UserUtils.getCurrentUserId() ?: return@launch
            val goalIdLong = goalId.toLong()
            val currentGoal = repository.getGoalById(goalIdLong, currentUserId)
            currentGoal?.let { goal ->
                val newAmount = maxOf(0.0, goal.currentAmount - amount)
                repository.updateGoalProgress(goalIdLong, currentUserId, newAmount)
                // If goal was completed and now isn't, mark as incomplete
                if (goal.isCompleted && newAmount < goal.targetAmount) {
                    repository.markGoalAsIncomplete(goalIdLong, currentUserId)
                }
                
                // Create transaction entry for goal money removal
                val transaction = TransactionEntity(
                    userId = currentUserId,
                    description = "Removed from ${goal.title} goal",
                    amount = amount,
                    category = "Goals - ${goal.category.name}",
                    date = Date(),
                    type = com.cp3406.financetracker.data.entity.TransactionType.INCOME,
                    isRecurring = false,
                    notes = "Goal withdrawal: $${String.format("%.2f", goal.currentAmount)} → $${String.format("%.2f", newAmount)}"
                )
                transactionRepository.insertTransaction(transaction)
            }
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