package com.cp3406.financetracker.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.cp3406.financetracker.data.database.FinanceDatabase
import com.cp3406.financetracker.data.entity.TransactionType
import com.cp3406.financetracker.data.repository.BudgetRepository
import com.cp3406.financetracker.data.repository.GoalRepository
import com.cp3406.financetracker.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionRepository: TransactionRepository
    private val budgetRepository: BudgetRepository
    private val goalRepository: GoalRepository
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    private val _welcomeMessage = MediatorLiveData<String>()
    val welcomeMessage: LiveData<String> = _welcomeMessage
    
    private val _currentBalance = MediatorLiveData<String>()
    val currentBalance: LiveData<String> = _currentBalance
    
    private val _monthlyIncome = MediatorLiveData<String>()
    val monthlyIncome: LiveData<String> = _monthlyIncome
    
    private val _monthlyExpenses = MediatorLiveData<String>()
    val monthlyExpenses: LiveData<String> = _monthlyExpenses
    
    private val _budgetProgress = MediatorLiveData<Int>()
    val budgetProgress: LiveData<Int> = _budgetProgress
    
    private val _activeGoalsCount = MediatorLiveData<String>()
    val activeGoalsCount: LiveData<String> = _activeGoalsCount
    
    private val _totalSavingsProgress = MediatorLiveData<String>()
    val totalSavingsProgress: LiveData<String> = _totalSavingsProgress
    
    private val _recentTransactions = MediatorLiveData<List<com.cp3406.financetracker.ui.transactions.Transaction>>()
    val recentTransactions: LiveData<List<com.cp3406.financetracker.ui.transactions.Transaction>> = _recentTransactions
    
    init {
        val database = FinanceDatabase.getDatabase(application)
        transactionRepository = TransactionRepository(database.transactionDao())
        budgetRepository = BudgetRepository(database.budgetDao())
        goalRepository = GoalRepository(database.goalDao())
        
        setupWelcomeMessage()
        observeFinancialData()
    }
    
    private fun setupWelcomeMessage() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
        _welcomeMessage.value = "$greeting, John"
    }
    
    private fun observeFinancialData() {
        val allTransactions = transactionRepository.getAllTransactions()
        
        // Calculate current balance from all transactions
        _currentBalance.addSource(allTransactions) { transactions ->
            viewModelScope.launch {
                val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                val balance = totalIncome - totalExpenses
                _currentBalance.postValue(currencyFormatter.format(balance))
            }
        }
        
        // Calculate monthly income and expenses
        _monthlyIncome.addSource(allTransactions) { transactions ->
            viewModelScope.launch {
                val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                
                val monthlyIncomeAmount = transactions.filter { transaction ->
                    val transactionCal = Calendar.getInstance().apply { time = transaction.date }
                    transaction.type == TransactionType.INCOME &&
                    transactionCal.get(Calendar.MONTH) == currentMonth &&
                    transactionCal.get(Calendar.YEAR) == currentYear
                }.sumOf { it.amount }
                
                _monthlyIncome.postValue(currencyFormatter.format(monthlyIncomeAmount))
            }
        }
        
        _monthlyExpenses.addSource(allTransactions) { transactions ->
            viewModelScope.launch {
                val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                
                val monthlyExpenseAmount = transactions.filter { transaction ->
                    val transactionCal = Calendar.getInstance().apply { time = transaction.date }
                    transaction.type == TransactionType.EXPENSE &&
                    transactionCal.get(Calendar.MONTH) == currentMonth &&
                    transactionCal.get(Calendar.YEAR) == currentYear
                }.sumOf { it.amount }
                
                _monthlyExpenses.postValue(currencyFormatter.format(monthlyExpenseAmount))
            }
        }
        
        // Observe budget data
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val budgets = budgetRepository.getBudgetsForMonth(currentMonth, currentYear)
        
        _budgetProgress.addSource(budgets) { budgetList ->
            viewModelScope.launch {
                if (budgetList.isNotEmpty()) {
                    val totalBudget = budgetList.sumOf { it.budgetAmount }
                    val totalSpent = budgetRepository.getTotalSpentForMonth(currentMonth, currentYear)
                    
                    val progress = if (totalBudget > 0) {
                        ((totalSpent / totalBudget) * 100).toInt()
                    } else 0
                    
                    _budgetProgress.postValue(progress)
                } else {
                    _budgetProgress.postValue(0)
                }
            }
        }
        
        // Observe goals data
        val allGoals = goalRepository.getAllGoals()
        _activeGoalsCount.addSource(allGoals) { goals ->
            val activeGoals = goals.filter { !it.isCompleted }
            _activeGoalsCount.postValue("${activeGoals.size} Active Goals")
        }
        
        _totalSavingsProgress.addSource(allGoals) { goals ->
            val totalSaved = goals.sumOf { it.currentAmount }
            _totalSavingsProgress.postValue(currencyFormatter.format(totalSaved))
        }
        
        // Observe recent transactions (last 5)
        _recentTransactions.addSource(allTransactions) { transactions ->
            val recentList = transactions.take(5).map { entity ->
                com.cp3406.financetracker.ui.transactions.Transaction(
                    id = entity.id.toString(),
                    description = entity.description,
                    amount = entity.amount,
                    category = entity.category,
                    date = entity.date,
                    type = when(entity.type) {
                        com.cp3406.financetracker.data.entity.TransactionType.INCOME -> 
                            com.cp3406.financetracker.ui.transactions.TransactionType.INCOME
                        com.cp3406.financetracker.data.entity.TransactionType.EXPENSE -> 
                            com.cp3406.financetracker.ui.transactions.TransactionType.EXPENSE
                    },
                    merchantName = entity.notes ?: ""
                )
            }
            _recentTransactions.postValue(recentList)
        }
    }
}