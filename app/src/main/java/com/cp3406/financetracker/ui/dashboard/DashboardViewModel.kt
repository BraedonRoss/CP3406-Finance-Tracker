package com.cp3406.financetracker.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.cp3406.financetracker.data.database.FinanceDatabase
import com.cp3406.financetracker.data.entity.TransactionType
import com.cp3406.financetracker.data.repository.BudgetRepository
import com.cp3406.financetracker.data.repository.ExchangeRateRepository
import com.cp3406.financetracker.data.repository.GoalRepository
import com.cp3406.financetracker.data.repository.TransactionRepository
import com.cp3406.financetracker.utils.UserUtils
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import java.util.Calendar

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var budgetRepository: BudgetRepository
    private lateinit var goalRepository: GoalRepository
    private lateinit var exchangeRateRepository: ExchangeRateRepository
    private fun getCurrencyFormatter(): NumberFormat {
        val application = getApplication<Application>()
        val currencyPrefs = application.getSharedPreferences("currency_prefs", android.content.Context.MODE_PRIVATE)
        val selectedCurrency = currencyPrefs.getString("selected_currency", "AUD") ?: "AUD"
        
        val locale = when (selectedCurrency) {
            "USD" -> Locale("en", "US")
            "EUR" -> Locale("en", "EU")
            "GBP" -> Locale("en", "GB")
            "JPY" -> Locale("ja", "JP")
            "CAD" -> Locale("en", "CA")
            "AUD" -> Locale("en", "AU")
            "CHF" -> Locale("de", "CH")
            "CNY" -> Locale("zh", "CN")
            "INR" -> Locale("en", "IN")
            "BRL" -> Locale("pt", "BR")
            else -> Locale("en", "AU")
        }
        
        return NumberFormat.getCurrencyInstance(locale)
    }

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
    
    // Exchange Rate functionality for international users
    private val _exchangeRates = MediatorLiveData<Map<String, Double>>()
    val exchangeRates: LiveData<Map<String, Double>> = _exchangeRates
    
    private val _exchangeRateStatus = MediatorLiveData<String>()
    val exchangeRateStatus: LiveData<String> = _exchangeRateStatus
    
    init {
        try {
            val database = FinanceDatabase.getDatabase(application)
            transactionRepository = TransactionRepository(database.transactionDao())
            budgetRepository = BudgetRepository(database.budgetDao())
            goalRepository = GoalRepository(database.goalDao())
            exchangeRateRepository = ExchangeRateRepository()
            
            setupWelcomeMessage()
            observeFinancialData()
            fetchLatestExchangeRates()
        } catch (e: Exception) {
            // Fallback to default values if database initialization fails
            setupWelcomeMessage()
            setupDefaultValues()
        }
    }
    
    private fun setupDefaultValues() {
        try {
            _currentBalance.value = "$0.00"
            _monthlyIncome.value = "$0.00"
            _monthlyExpenses.value = "$0.00"
            _budgetProgress.value = 0
            _activeGoalsCount.value = "0"
            _totalSavingsProgress.value = "$0.00"
            _recentTransactions.value = emptyList()
            _exchangeRateStatus.value = "Exchange rates not available"
        } catch (e: Exception) {
            // If setting default values fails, log the error but don't crash
            android.util.Log.e("DashboardViewModel", "Failed to set default values", e)
        }
    }
    
    private fun setupWelcomeMessage() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
        _welcomeMessage.value = "$greeting!"
    }
    
    private fun observeFinancialData() {
        val currentUserId = UserUtils.getCurrentUserIdOrDefault()
        val allTransactions = transactionRepository.getAllTransactions(currentUserId)
        
        // Calculate current balance from all transactions
        _currentBalance.addSource(allTransactions) { transactions ->
            viewModelScope.launch {
                val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                val balance = totalIncome - totalExpenses
                _currentBalance.postValue(getCurrencyFormatter().format(balance))
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
                
                _monthlyIncome.postValue(getCurrencyFormatter().format(monthlyIncomeAmount))
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
                
                _monthlyExpenses.postValue(getCurrencyFormatter().format(monthlyExpenseAmount))
            }
        }
        
        // Observe budget data
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val budgets = budgetRepository.getBudgetsForMonth(currentUserId, currentMonth, currentYear)
        
        _budgetProgress.addSource(budgets) { budgetList ->
            viewModelScope.launch {
                if (budgetList.isNotEmpty()) {
                    val totalBudget = budgetList.sumOf { it.budgetAmount }
                    val totalSpent = budgetRepository.getTotalSpentForMonth(currentUserId, currentMonth, currentYear)
                    
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
        val allGoals = goalRepository.getAllGoals(currentUserId)
        _activeGoalsCount.addSource(allGoals) { goals ->
            val activeGoals = goals.filter { !it.isCompleted }
            _activeGoalsCount.postValue("${activeGoals.size} Active Goals")
        }
        
        _totalSavingsProgress.addSource(allGoals) { goals ->
            val totalSaved = goals.sumOf { it.currentAmount }
            _totalSavingsProgress.postValue(getCurrencyFormatter().format(totalSaved))
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
    
    private fun fetchLatestExchangeRates() {
        _exchangeRateStatus.value = "Fetching latest exchange rates..."
        
        viewModelScope.launch {
            try {
                // Get selected currency from SharedPreferences
                val application = getApplication<Application>()
                val currencyPrefs = application.getSharedPreferences("currency_prefs", android.content.Context.MODE_PRIVATE)
                val selectedCurrency = currencyPrefs.getString("selected_currency", "AUD") ?: "AUD"
                
                val result = exchangeRateRepository.getLatestExchangeRates(
                    baseCurrency = selectedCurrency,
                    targetCurrencies = ExchangeRateRepository.SUPPORTED_CURRENCIES
                )
                
                result.onSuccess { response ->
                    _exchangeRates.postValue(response.rates)
                    _exchangeRateStatus.postValue("Exchange rates updated: ${response.date}")
                }.onFailure { error ->
                    _exchangeRateStatus.postValue("Failed to fetch rates: ${error.message}")
                }
            } catch (e: Exception) {
                _exchangeRateStatus.postValue("Exchange rate service unavailable")
            }
        }
    }
    
    fun convertAmount(amount: Double, fromCurrency: String, toCurrency: String): Double {
        val rates = _exchangeRates.value ?: return amount
        return exchangeRateRepository.convertCurrency(amount, fromCurrency, toCurrency, rates)
    }
    
    fun refreshExchangeRates() {
        fetchLatestExchangeRates()
    }
    
    fun onCurrencyChanged() {
        fetchLatestExchangeRates()
    }
}