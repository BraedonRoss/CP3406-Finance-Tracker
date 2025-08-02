package com.cp3406.financetracker.ui.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.*

class SafeBudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val _budgetCategories = MutableLiveData<List<BudgetCategory>>()
    val budgetCategories: LiveData<List<BudgetCategory>> = _budgetCategories

    private val _totalBudget = MutableLiveData<Double>()
    val totalBudget: LiveData<Double> = _totalBudget

    private val _totalSpent = MutableLiveData<Double>()
    val totalSpent: LiveData<Double> = _totalSpent
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadBudgetData()
    }

    private fun loadBudgetData() {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                // Sample budget data for demonstration
                val sampleCategories = listOf(
                    BudgetCategory(
                        id = "1",
                        name = "Food & Dining",
                        budgetAmount = 500.0,
                        spentAmount = 180.0,
                        color = "#FF5722"
                    ),
                    BudgetCategory(
                        id = "2",
                        name = "Transportation",
                        budgetAmount = 300.0,
                        spentAmount = 120.0,
                        color = "#2196F3"
                    ),
                    BudgetCategory(
                        id = "3",
                        name = "Entertainment",
                        budgetAmount = 200.0,
                        spentAmount = 85.0,
                        color = "#9C27B0"
                    ),
                    BudgetCategory(
                        id = "4",
                        name = "Utilities",
                        budgetAmount = 400.0,
                        spentAmount = 365.0,
                        color = "#FF9800"
                    ),
                    BudgetCategory(
                        id = "5",
                        name = "Shopping",
                        budgetAmount = 250.0,
                        spentAmount = 140.0,
                        color = "#4CAF50"
                    ),
                    BudgetCategory(
                        id = "6",
                        name = "Healthcare",
                        budgetAmount = 150.0,
                        spentAmount = 75.0,
                        color = "#607D8B"
                    )
                )
                
                _budgetCategories.value = sampleCategories
                _totalBudget.value = sampleCategories.sumOf { it.budgetAmount }
                _totalSpent.value = sampleCategories.sumOf { it.spentAmount }
                
            } catch (e: Exception) {
                // Handle error gracefully
                _budgetCategories.value = emptyList()
                _totalBudget.value = 0.0
                _totalSpent.value = 0.0
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addBudgetCategory(name: String, amount: Double, color: String) {
        viewModelScope.launch {
            val currentCategories = _budgetCategories.value ?: emptyList()
            val newCategory = BudgetCategory(
                id = UUID.randomUUID().toString(),
                name = name,
                budgetAmount = amount,
                spentAmount = 0.0,
                color = color
            )
            
            val updatedCategories = currentCategories + newCategory
            _budgetCategories.value = updatedCategories
            _totalBudget.value = updatedCategories.sumOf { it.budgetAmount }
            _totalSpent.value = updatedCategories.sumOf { it.spentAmount }
        }
    }

    fun updateBudgetCategory(categoryId: String, newAmount: Double) {
        viewModelScope.launch {
            val currentCategories = _budgetCategories.value ?: emptyList()
            val updatedCategories = currentCategories.map { category ->
                if (category.id == categoryId) {
                    category.copy(budgetAmount = newAmount)
                } else {
                    category
                }
            }
            
            _budgetCategories.value = updatedCategories
            _totalBudget.value = updatedCategories.sumOf { it.budgetAmount }
        }
    }

    fun deleteBudgetCategory(categoryId: String) {
        viewModelScope.launch {
            val currentCategories = _budgetCategories.value ?: emptyList()
            val updatedCategories = currentCategories.filter { it.id != categoryId }
            
            _budgetCategories.value = updatedCategories
            _totalBudget.value = updatedCategories.sumOf { it.budgetAmount }
            _totalSpent.value = updatedCategories.sumOf { it.spentAmount }
        }
    }

    fun refreshBudgetData() {
        loadBudgetData()
    }
}