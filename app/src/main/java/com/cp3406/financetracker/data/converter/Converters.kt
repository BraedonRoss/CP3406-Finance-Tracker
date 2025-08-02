package com.cp3406.financetracker.data.converter

import androidx.room.TypeConverter
import com.cp3406.financetracker.data.entity.GoalCategory
import com.cp3406.financetracker.data.entity.TransactionType
import java.util.Date

class Converters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }
    
    @TypeConverter
    fun toTransactionType(type: String): TransactionType {
        return TransactionType.valueOf(type)
    }
    
    @TypeConverter
    fun fromGoalCategory(category: GoalCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toGoalCategory(category: String): GoalCategory {
        return GoalCategory.valueOf(category)
    }
}