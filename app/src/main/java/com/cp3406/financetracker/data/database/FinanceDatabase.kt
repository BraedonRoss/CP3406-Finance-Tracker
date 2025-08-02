package com.cp3406.financetracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cp3406.financetracker.data.converter.Converters
import com.cp3406.financetracker.data.dao.BudgetDao
import com.cp3406.financetracker.data.dao.GoalDao
import com.cp3406.financetracker.data.dao.TransactionDao
import com.cp3406.financetracker.data.entity.BudgetEntity
import com.cp3406.financetracker.data.entity.GoalEntity
import com.cp3406.financetracker.data.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, BudgetEntity::class, GoalEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FinanceDatabase : RoomDatabase() {
    
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    
    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null
        
        fun getDatabase(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}