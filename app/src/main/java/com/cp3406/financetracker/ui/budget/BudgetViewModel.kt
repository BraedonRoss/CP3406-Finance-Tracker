package com.cp3406.financetracker.ui.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.cp3406.financetracker.data.database.FinanceDatabase
import com.cp3406.financetracker.data.entity.BudgetEntity
import com.cp3406.financetracker.data.entity.TransactionType
import com.cp3406.financetracker.data.repository.BudgetRepository
import com.cp3406.financetracker.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.util.*

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val budgetRepository: BudgetRepository
    private val transactionRepository: TransactionRepository
    
    private val _budgetCategories = MediatorLiveData<List<BudgetCategory>>()
    val budgetCategories: LiveData<List<BudgetCategory>> = _budgetCategories

    private val _totalBudget = MediatorLiveData<Double>()
    val totalBudget: LiveData<Double> = _totalBudget

    private val _totalSpent = MediatorLiveData<Double>()
    val totalSpent: LiveData<Double> = _totalSpent

    init {
        val database = FinanceDatabase.getDatabase(application)
        budgetRepository = BudgetRepository(database.budgetDao())
        transactionRepository = TransactionRepository(database.transactionDao())
        
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        
        val entityBudgets = budgetRepository.getBudgetsForMonth(currentMonth, currentYear)
        _budgetCategories.addSource(entityBudgets) { entities ->
            updateBudgetData(entities)
        }
        
        // Also observe transactions to update spent amounts when transactions change
        val allTransactions = transactionRepository.getAllTransactions()
        _budgetCategories.addSource(allTransactions) { _ ->
            entityBudgets.value?.let { entities ->
                updateBudgetData(entities)
            }
        }
        
        // Initialize with sample data if database is empty
        initializeSampleData()
    }

    fun addBudget(category: String, budgetAmount: Double, icon: String, color: String = "#4CAF50") {
        viewModelScope.launch {
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            
            val budget = BudgetEntity(
                category = category,
                budgetAmount = budgetAmount,
                month = currentMonth,
                year = currentYear,
                icon = icon,
                color = color
            )
            budgetRepository.insertBudget(budget)
        }
    }

    fun updateBudgetAmount(categoryId: Long, newAmount: Double) {
        viewModelScope.launch {
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            
            val budget = budgetRepository.getBudgetByCategory(
                _budgetCategories.value?.find { it.id == categoryId.toString() }?.name ?: "",
                currentMonth, currentYear
            )
            
            budget?.let {
                val updatedBudget = it.copy(budgetAmount = newAmount)
                budgetRepository.updateBudget(updatedBudget)
            }
        }
    }

    private fun updateBudgetData(entities: List<BudgetEntity>) {
        viewModelScope.launch {
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            
            val startOfMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            val endOfMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time
            
            val budgetCategories = entities.map { entity ->
                val spentAmount = transactionRepository.getSpentAmountByCategory(
                    entity.category, startOfMonth, endOfMonth
                )
                
                // Update the database with the calculated spent amount
                budgetRepository.updateSpentAmount(entity.category, currentMonth, currentYear, spentAmount)
                
                BudgetCategory(
                    id = entity.id.toString(),
                    name = entity.category,
                    budgetAmount = entity.budgetAmount,
                    spentAmount = spentAmount,
                    color = entity.color
                )
            }
            
            _budgetCategories.postValue(budgetCategories)
            _totalBudget.postValue(budgetCategories.sumOf { it.budgetAmount })
            _totalSpent.postValue(budgetCategories.sumOf { it.spentAmount })
        }
    }

    private fun initializeSampleData() {
        viewModelScope.launch {
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            
            val existingBudgets = budgetRepository.getBudgetsForMonth(currentMonth, currentYear).value
            if (existingBudgets.isNullOrEmpty()) {
                val sampleBudgets = listOf(
                    BudgetEntity(
                        category = "Food & Dining",
                        budgetAmount = 500.0,
                        month = currentMonth,
                        year = currentYear,
                        icon = "🍽️",
                        color = "#4CAF50"
                    ),
                    BudgetEntity(
                        category = "Transportation",
                        budgetAmount = 300.0,
                        month = currentMonth,
                        year = currentYear,
                        icon = "🚗",
                        color = "#2196F3"
                    ),
                    BudgetEntity(
                        category = "Entertainment",
                        budgetAmount = 200.0,
                        month = currentMonth,
                        year = currentYear,
                        icon = "🎬",
                        color = "#FF9800"
                    ),
                    BudgetEntity(
                        category = "Shopping",
                        budgetAmount = 400.0,
                        month = currentMonth,
                        year = currentYear,
                        icon = "🛍️",
                        color = "#9C27B0"
                    ),
                    BudgetEntity(
                        category = "Bills & Utilities",
                        budgetAmount = 800.0,
                        month = currentMonth,
                        year = currentYear,
                        icon = "💡",
                        color = "#F44336"
                    )
                )
                
                sampleBudgets.forEach { budget ->
                    budgetRepository.insertBudget(budget)
                }
            }
        }
    }
}