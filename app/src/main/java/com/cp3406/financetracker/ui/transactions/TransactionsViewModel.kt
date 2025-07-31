package com.cp3406.financetracker.ui.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TransactionsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Transactions will appear here"
    }
    val text: LiveData<String> = _text
}