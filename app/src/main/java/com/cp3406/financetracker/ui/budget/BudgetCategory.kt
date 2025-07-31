package com.cp3406.financetracker.ui.budget

data class BudgetCategory(
    val id: String,
    val name: String,
    val budgetAmount: Double,
    val spentAmount: Double,
    val color: String
) {
    val remainingAmount: Double
        get() = budgetAmount - spentAmount
    
    val progressPercentage: Float
        get() = if (budgetAmount > 0) (spentAmount / budgetAmount * 100).toFloat() else 0f
    
    val isOverBudget: Boolean
        get() = spentAmount > budgetAmount
}