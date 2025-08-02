package com.cp3406.financetracker.data.repository

import android.util.Log
import com.cp3406.financetracker.data.api.ExchangeRateApi
import com.cp3406.financetracker.data.api.ExchangeRateResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ExchangeRateRepository {

    private val api: ExchangeRateApi

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(ExchangeRateApi.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ExchangeRateApi::class.java)
    }

    suspend fun getLatestExchangeRates(
        baseCurrency: String = "USD",
        targetCurrencies: List<String>? = null
    ): Result<ExchangeRateResponse> = withContext(Dispatchers.IO) {
        try {
            // Using exchangerate.host which is free and doesn't require API key
            val symbols = targetCurrencies?.joinToString(",")
            val response = api.getLatestRates(
                apiKey = "", // Not required for exchangerate.host
                baseCurrency = baseCurrency,
                symbols = symbols
            )

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        Result.success(body)
                    } else {
                        Result.failure(Exception(body.error?.info ?: "Unknown API error"))
                    }
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ExchangeRateRepository", "Error fetching exchange rates", e)
            Result.failure(e)
        }
    }

    suspend fun getHistoricalRates(
        date: String, // Format: YYYY-MM-DD
        baseCurrency: String = "USD",
        targetCurrencies: List<String>? = null
    ): Result<ExchangeRateResponse> = withContext(Dispatchers.IO) {
        try {
            val symbols = targetCurrencies?.joinToString(",")
            val response = api.getHistoricalRates(
                apiKey = "", // Not required for exchangerate.host
                date = date,
                baseCurrency = baseCurrency,
                symbols = symbols
            )

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        Result.success(body)
                    } else {
                        Result.failure(Exception(body.error?.info ?: "Unknown API error"))
                    }
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ExchangeRateRepository", "Error fetching historical rates", e)
            Result.failure(e)
        }
    }

    fun convertCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        exchangeRates: Map<String, Double>
    ): Double {
        if (fromCurrency == toCurrency) return amount
        
        val fromRate = exchangeRates[fromCurrency] ?: 1.0
        val toRate = exchangeRates[toCurrency] ?: 1.0
        
        // Convert to base currency first, then to target currency
        val baseAmount = amount / fromRate
        return baseAmount * toRate
    }

    companion object {
        const val TAG = "ExchangeRateRepository"
        
        // Common currency codes for the finance app
        val SUPPORTED_CURRENCIES = listOf(
            "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "INR", "BRL"
        )
    }
}