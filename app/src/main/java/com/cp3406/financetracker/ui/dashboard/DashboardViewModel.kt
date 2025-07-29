package com.cp3406.financetracker.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    private val _welcomeMessage = MutableLiveData<String>()
    val welcomeMessage: LiveData<String> = _welcomeMessage
    
    private val _balance = MutableLiveData<String>()
    val balance: LiveData<String> = _balance
    
    init {
        loadDashboardData()
    }
    
    private fun loadDashboardData() {
        // Simulate loading user data
        _welcomeMessage.value = "Good morning, User"
        _balance.value = "$2,847.50"
    }
}