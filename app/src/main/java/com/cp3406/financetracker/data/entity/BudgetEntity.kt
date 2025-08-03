package com.cp3406.financetracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val category: String,
    val budgetAmount: Double,
    val spentAmount: Double = 0.0,
    val month: Int,
    val year: Int,
    val icon: String,
    val color: String = "#4CAF50"
) {
    val remainingAmount: Double
        get() = budgetAmount - spentAmount
    
    val progressPercentage: Int
        get() = if (budgetAmount > 0) ((spentAmount / budgetAmount) * 100).toInt() else 0
}