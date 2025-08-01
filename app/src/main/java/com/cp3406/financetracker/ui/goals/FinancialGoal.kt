package com.cp3406.financetracker.ui.goals

import java.util.Date

data class FinancialGoal(
    val id: String,
    val title: String,
    val description: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: Date,
    val category: GoalCategory,
    val priority: GoalPriority = GoalPriority.MEDIUM,
    val isCompleted: Boolean = false
) {
    val progressPercentage: Float
        get() = if (targetAmount > 0) (currentAmount / targetAmount * 100).toFloat() else 0f
    
    val remainingAmount: Double
        get() = targetAmount - currentAmount
    
    val isOverdue: Boolean
        get() = Date().after(targetDate) && !isCompleted
        
    val daysTillTarget: Long
        get() {
            val diff = targetDate.time - Date().time
            return diff / (1000 * 60 * 60 * 24)
        }
}

enum class GoalCategory(val displayName: String, val emoji: String) {
    EMERGENCY_FUND("Emergency Fund", "🛡️"),
    VACATION("Vacation", "✈️"),
    HOME("Home", "🏠"),
    CAR("Car", "🚗"),
    EDUCATION("Education", "🎓"),
    RETIREMENT("Retirement", "👴"),
    INVESTMENT("Investment", "📈"),
    OTHER("Other", "💰")
}

enum class GoalPriority(val displayName: String) {
    HIGH("High"),
    MEDIUM("Medium"), 
    LOW("Low")
}