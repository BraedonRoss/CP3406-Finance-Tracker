package com.cp3406.financetracker.ui.budget

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BudgetViewModel : ViewModel() {

    private val _budgetCategories = MutableLiveData<List<BudgetCategory>>()
    val budgetCategories: LiveData<List<BudgetCategory>> = _budgetCategories

    private val _totalBudget = MutableLiveData<Double>()
    val totalBudget: LiveData<Double> = _totalBudget

    private val _totalSpent = MutableLiveData<Double>()
    val totalSpent: LiveData<Double> = _totalSpent

    init {
        loadBudgetData()
    }

    private fun loadBudgetData() {
        val categories = listOf(
            BudgetCategory(
                id = "1",
                name = "Food & Dining",
                budgetAmount = 500.0,
                spentAmount = 335.0,
                color = "#4CAF50"
            ),
            BudgetCategory(
                id = "2", 
                name = "Transportation",
                budgetAmount = 300.0,
                spentAmount = 180.0,
                color = "#2196F3"
            ),
            BudgetCategory(
                id = "3",
                name = "Entertainment", 
                budgetAmount = 200.0,
                spentAmount = 245.0,
                color = "#FF9800"
            ),
            BudgetCategory(
                id = "4",
                name = "Shopping",
                budgetAmount = 400.0,
                spentAmount = 287.0,
                color = "#9C27B0"
            ),
            BudgetCategory(
                id = "5",
                name = "Bills & Utilities",
                budgetAmount = 800.0,
                spentAmount = 750.0,
                color = "#F44336"
            ),
            BudgetCategory(
                id = "6",
                name = "Health & Fitness",
                budgetAmount = 150.0,
                spentAmount = 50.0,
                color = "#009688"
            )
        )
        
        _budgetCategories.value = categories
        _totalBudget.value = categories.sumOf { it.budgetAmount }
        _totalSpent.value = categories.sumOf { it.spentAmount }
    }
}