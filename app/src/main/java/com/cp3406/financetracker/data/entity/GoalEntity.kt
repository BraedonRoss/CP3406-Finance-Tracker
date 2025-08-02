package com.cp3406.financetracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: Date,
    val category: GoalCategory,
    val isCompleted: Boolean = false,
    val createdDate: Date = Date()
) {
    val progressPercentage: Int
        get() = if (targetAmount > 0) ((currentAmount / targetAmount) * 100).toInt() else 0
    
    val isOverdue: Boolean
        get() = !isCompleted && Date().after(targetDate)
}

enum class GoalCategory {
    EMERGENCY_FUND,
    VACATION,
    HOME,
    CAR,
    EDUCATION,
    RETIREMENT,
    OTHER
}