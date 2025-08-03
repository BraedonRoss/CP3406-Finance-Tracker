package com.cp3406.financetracker.utils

import android.content.Context
import java.text.NumberFormat
import java.util.*

class CurrencyFormatter(private val context: Context) {
    
    private fun getSelectedCurrency(): String {
        val currencyPrefs = context.getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)
        return currencyPrefs.getString("selected_currency", "AUD") ?: "AUD"
    }
    
    private fun getCurrencySymbol(currencyCode: String): String {
        return when (currencyCode) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            "CAD" -> "C$"
            "AUD" -> "A$"
            "CHF" -> "CHF"
            "CNY" -> "¥"
            "INR" -> "₹"
            "BRL" -> "R$"
            else -> "$"
        }
    }
    
    private fun getCurrencyLocale(currencyCode: String): Locale {
        return when (currencyCode) {
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
    }
    
    fun formatAmount(
        amount: Double, 
        exchangeRates: Map<String, Double>? = null,
        baseCurrency: String = "AUD"
    ): String {
        val selectedCurrency = getSelectedCurrency()
        val convertedAmount = convertCurrency(amount, baseCurrency, selectedCurrency, exchangeRates)
        val symbol = getCurrencySymbol(selectedCurrency)
        
        return "$symbol${String.format("%.2f", convertedAmount)}"
    }
    
    fun formatAmountWithSymbol(
        amount: Double,
        exchangeRates: Map<String, Double>? = null,
        baseCurrency: String = "AUD"
    ): String {
        val selectedCurrency = getSelectedCurrency()
        val convertedAmount = convertCurrency(amount, baseCurrency, selectedCurrency, exchangeRates)
        
        try {
            val locale = getCurrencyLocale(selectedCurrency)
            val formatter = NumberFormat.getCurrencyInstance(locale)
            val currency = Currency.getInstance(selectedCurrency)
            formatter.currency = currency
            return formatter.format(convertedAmount)
        } catch (e: Exception) {
            // Fallback to manual formatting
            val symbol = getCurrencySymbol(selectedCurrency)
            return "$symbol${String.format("%.2f", convertedAmount)}"
        }
    }
    
    private fun convertCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        exchangeRates: Map<String, Double>?
    ): Double {
        if (fromCurrency == toCurrency || exchangeRates == null) return amount
        
        val fromRate = exchangeRates[fromCurrency] ?: 1.0
        val toRate = exchangeRates[toCurrency] ?: 1.0
        
        // Convert to base currency first, then to target currency
        val baseAmount = amount / fromRate
        return baseAmount * toRate
    }
    
    fun getSelectedCurrencyCode(): String {
        return getSelectedCurrency()
    }
    
    fun getSelectedCurrencySymbol(): String {
        return getCurrencySymbol(getSelectedCurrency())
    }
    
    companion object {
        fun getCurrencyDisplayName(currencyCode: String): String {
            return when (currencyCode) {
                "USD" -> "US Dollar ($)"
                "EUR" -> "Euro (€)"
                "GBP" -> "British Pound (£)"
                "JPY" -> "Japanese Yen (¥)"
                "CAD" -> "Canadian Dollar (C$)"
                "AUD" -> "Australian Dollar (A$)"
                "CHF" -> "Swiss Franc (CHF)"
                "CNY" -> "Chinese Yuan (¥)"
                "INR" -> "Indian Rupee (₹)"
                "BRL" -> "Brazilian Real (R$)"
                else -> "Unknown Currency"
            }
        }
    }
}