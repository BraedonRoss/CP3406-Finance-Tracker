package com.cp3406.financetracker.ui.budget

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cp3406.financetracker.data.database.FinanceDatabase
import com.cp3406.financetracker.data.entity.TransactionType
import com.cp3406.financetracker.data.repository.TransactionRepository
import com.cp3406.financetracker.utils.UserUtils
import kotlinx.coroutines.launch
import java.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SafeBudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionRepository: TransactionRepository
    private val gson = Gson()
    
    // Use user-scoped SharedPreferences
    private fun getUserPreferences(): android.content.SharedPreferences {
        val currentUserId = UserUtils.getCurrentUserIdOrDefault()
        return getApplication<Application>().getSharedPreferences("budget_categories_$currentUserId", Context.MODE_PRIVATE)
    }
    private val _storedCategories = MutableLiveData<List<BudgetCategory>>(emptyList())
    
    private val _budgetCategories = MediatorLiveData<List<BudgetCategory>>()
    val budgetCategories: LiveData<List<BudgetCategory>> = _budgetCategories

    private val _totalBudget = MutableLiveData<Double>()
    val totalBudget: LiveData<Double> = _totalBudget

    private val _totalSpent = MutableLiveData<Double>()
    val totalSpent: LiveData<Double> = _totalSpent
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        val database = FinanceDatabase.getDatabase(application)
        transactionRepository = TransactionRepository(database.transactionDao())
        
        loadBudgetData()
        observeTransactions()
    }
    
    private fun observeTransactions() {
        val currentUserId = UserUtils.getCurrentUserIdOrDefault()
        val allTransactions = transactionRepository.getAllTransactions(currentUserId)
        
        _budgetCategories.addSource(_storedCategories) { categories ->
            updateBudgetCategoriesWithTransactions(categories, allTransactions.value ?: emptyList())
        }
        
        _budgetCategories.addSource(allTransactions) { transactions ->
            val categories = _storedCategories.value ?: emptyList()
            updateBudgetCategoriesWithTransactions(categories, transactions)
        }
    }
    
    private fun updateBudgetCategoriesWithTransactions(categories: List<BudgetCategory>, transactions: List<com.cp3406.financetracker.data.entity.TransactionEntity>) {
        val updatedCategories = categories.map { category ->
            val spentAmount = transactions
                .filter { 
                    it.type == TransactionType.EXPENSE && 
                    it.category == category.name 
                }
                .sumOf { it.amount }
            
            category.copy(spentAmount = spentAmount)
        }
        
        _budgetCategories.value = updatedCategories
        _totalBudget.value = updatedCategories.sumOf { it.budgetAmount }
        _totalSpent.value = updatedCategories.sumOf { it.spentAmount }
    }

    private fun loadBudgetData() {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val savedCategories = loadBudgetCategories()
                _storedCategories.value = savedCategories
                _totalBudget.value = savedCategories.sumOf { it.budgetAmount }
                _totalSpent.value = savedCategories.sumOf { it.spentAmount }
                
            } catch (e: Exception) {
                _storedCategories.value = emptyList()
                _totalBudget.value = 0.0
                _totalSpent.value = 0.0
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun loadBudgetCategories(): List<BudgetCategory> {
        return try {
            val userPrefs = getUserPreferences()
            val json = userPrefs.getString("budget_categories_list", null)
            if (json != null) {
                val type = object : TypeToken<List<BudgetCategory>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun saveBudgetCategories(categories: List<BudgetCategory>) {
        try {
            val json = gson.toJson(categories)
            val userPrefs = getUserPreferences()
            userPrefs.edit()
                .putString("budget_categories_list", json)
                .apply()
        } catch (e: Exception) {
            // Handle save error silently
        }
    }

    fun addBudgetCategory(name: String, amount: Double, color: String) {
        viewModelScope.launch {
            val currentCategories = _storedCategories.value ?: emptyList()
            val newCategory = BudgetCategory(
                id = UUID.randomUUID().toString(),
                name = name,
                budgetAmount = amount,
                spentAmount = 0.0,
                color = color
            )
            
            val updatedCategories = currentCategories + newCategory
            _storedCategories.value = updatedCategories
            saveBudgetCategories(updatedCategories)
        }
    }

    fun updateBudgetCategory(categoryId: String, newAmount: Double) {
        viewModelScope.launch {
            val currentCategories = _storedCategories.value ?: emptyList()
            val updatedCategories = currentCategories.map { category ->
                if (category.id == categoryId) {
                    category.copy(budgetAmount = newAmount)
                } else {
                    category
                }
            }
            
            _storedCategories.value = updatedCategories
            saveBudgetCategories(updatedCategories)
        }
    }

    fun deleteBudgetCategory(categoryId: String) {
        viewModelScope.launch {
            val currentCategories = _storedCategories.value ?: emptyList()
            val updatedCategories = currentCategories.filter { it.id != categoryId }
            
            _storedCategories.value = updatedCategories
            saveBudgetCategories(updatedCategories)
        }
    }

    fun refreshBudgetData() {
        loadBudgetData()
    }
}