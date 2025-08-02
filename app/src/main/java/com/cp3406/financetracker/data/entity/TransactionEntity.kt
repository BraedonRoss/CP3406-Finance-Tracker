package com.cp3406.financetracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val amount: Double,
    val category: String,
    val date: Date,
    val type: TransactionType,
    val isRecurring: Boolean = false,
    val notes: String? = null
)

enum class TransactionType {
    INCOME,
    EXPENSE
}