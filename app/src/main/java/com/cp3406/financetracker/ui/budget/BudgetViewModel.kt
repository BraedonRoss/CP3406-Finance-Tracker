package com.cp3406.financetracker.ui.budget

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BudgetViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Budget Management"
    }
    val text: LiveData<String> = _text
}