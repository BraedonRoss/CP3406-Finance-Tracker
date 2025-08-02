package com.cp3406.financetracker.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeRateApi {
    
    @GET("latest")
    suspend fun getLatestRates(
        @Query("access_key") apiKey: String,
        @Query("base") baseCurrency: String = "USD",
        @Query("symbols") symbols: String? = null
    ): Response<ExchangeRateResponse>
    
    @GET("historical")
    suspend fun getHistoricalRates(
        @Query("access_key") apiKey: String,
        @Query("date") date: String,
        @Query("base") baseCurrency: String = "USD",
        @Query("symbols") symbols: String? = null
    ): Response<ExchangeRateResponse>
    
    companion object {
        const val BASE_URL = "https://api.exchangerate.host/"
    }
}

data class ExchangeRateResponse(
    val success: Boolean,
    val timestamp: Long,
    val base: String,
    val date: String,
    val rates: Map<String, Double>,
    val error: ApiError? = null
)

data class ApiError(
    val code: Int,
    val type: String,
    val info: String
)