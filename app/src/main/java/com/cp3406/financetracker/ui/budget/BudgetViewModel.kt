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
import java.util.Date

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val budgetRepository: BudgetRepository
    private val transactionRepository: TransactionRepository
    
    private val _budgetCategories = MediatorLiveData<List<BudgetCategory>>()
    val budgetCategories: LiveData<List<BudgetCategory>> = _budgetCategories

    private val _totalBudget = MediatorLiveData<Double>()
    val totalBudget: LiveData<Double> = _totalBudget

    private val _totalSpent = MediatorLiveData<Double>()
    val totalSpent: LiveData<Double> = _totalSpent
    
    private var syncWithTransactions = true // Default to sync with actual transactions

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
        
        // Also observe transactions to sync budget spent amounts with actual transaction expenses
        val allTransactions = transactionRepository.getAllTransactions()
        _budgetCategories.addSource(allTransactions) { _ ->
            if (syncWithTransactions) {
                entityBudgets.value?.let { entities ->
                    syncBudgetWithTransactions(entities)
                }
            }
        }
        
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

    fun updateBudgetAmount(categoryId: String, newAmount: Double) {
        viewModelScope.launch {
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            
            val categoryName = _budgetCategories.value?.find { it.id == categoryId }?.name ?: return@launch
            budgetRepository.updateBudgetAmount(categoryName, currentMonth, currentYear, newAmount)
        }
    }

    fun updateSpentAmount(categoryId: String, newSpentAmount: Double) {
        viewModelScope.launch {
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            
            val categoryName = _budgetCategories.value?.find { it.id == categoryId }?.name ?: return@launch
            budgetRepository.updateSpentAmount(categoryName, currentMonth, currentYear, newSpentAmount)
        }
    }

    fun deleteBudgetCategory(categoryId: String) {
        viewModelScope.launch {
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            
            val categoryName = _budgetCategories.value?.find { it.id == categoryId }?.name ?: return@launch
            val budget = budgetRepository.getBudgetByCategory(categoryName, currentMonth, currentYear)
            
            budget?.let {
                budgetRepository.deleteBudget(it)
            }
        }
    }
    
    fun toggleTransactionSync(enabled: Boolean) {
        syncWithTransactions = enabled
        if (enabled) {
            // Force a sync when enabled
            _budgetCategories.value?.let { _ ->
                val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                viewModelScope.launch {
                    val entities = budgetRepository.getBudgetsForMonth(currentMonth, currentYear).value
                    entities?.let { syncBudgetWithTransactions(it) }
                }
            }
        }
    }
    
    fun addSpendingTransaction(category: String, amount: Double, description: String) {
        viewModelScope.launch {
            val transaction = com.cp3406.financetracker.data.entity.TransactionEntity(
                description = description,
                amount = amount,
                category = category,
                date = Date(),
                type = com.cp3406.financetracker.data.entity.TransactionType.EXPENSE,
                isRecurring = false,
                notes = "Budget spending"
            )
            transactionRepository.insertTransaction(transaction)
            
            // Small delay to ensure transaction is committed
            kotlinx.coroutines.delay(100)
            
            // Force refresh budget data to reflect the new transaction
            refreshBudgetData()
        }
    }
    
    private fun refreshBudgetData() {
        viewModelScope.launch {
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            
            // Force sync by updating spent amounts for all categories
            _budgetCategories.value?.forEach { budgetCategory ->
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
                
                val actualSpent = transactionRepository.getSpentAmountByCategory(
                    budgetCategory.name, startOfMonth, endOfMonth
                )
                budgetRepository.updateSpentAmount(
                    budgetCategory.name, 
                    currentMonth, 
                    currentYear, 
                    actualSpent
                )
            }
        }
    }

    private fun updateBudgetData(entities: List<BudgetEntity>) {
        val budgetCategories = entities.map { entity ->
            BudgetCategory(
                id = entity.id.toString(),
                name = entity.category,
                budgetAmount = entity.budgetAmount,
                spentAmount = entity.spentAmount,
                color = entity.color
            )
        }
        
        _budgetCategories.postValue(budgetCategories)
        _totalBudget.postValue(budgetCategories.sumOf { it.budgetAmount })
        _totalSpent.postValue(budgetCategories.sumOf { it.spentAmount })
    }
    
    private fun syncBudgetWithTransactions(entities: List<BudgetEntity>) {
        viewModelScope.launch {
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
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
            
            entities.forEach { entity ->
                val actualSpent = transactionRepository.getSpentAmountByCategory(
                    entity.category, startOfMonth, endOfMonth
                )
                budgetRepository.updateSpentAmount(
                    entity.category, 
                    currentMonth + 1, // Repository expects 1-based month
                    currentYear, 
                    actualSpent
                )
            }
        }
    }
}