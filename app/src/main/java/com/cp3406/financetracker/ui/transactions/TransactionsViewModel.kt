package com.cp3406.financetracker.ui.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TransactionsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Transaction History"
    }
    val text: LiveData<String> = _text
}