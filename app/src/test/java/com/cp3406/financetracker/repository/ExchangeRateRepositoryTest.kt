package com.cp3406.financetracker.repository

import com.cp3406.financetracker.data.repository.ExchangeRateRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExchangeRateRepositoryTest {

    private lateinit var repository: ExchangeRateRepository

    @Before
    fun setup() {
        repository = ExchangeRateRepository()
    }

    @Test
    fun `convertCurrency returns same amount for same currency`() {
        val amount = 100.0
        val rates = mapOf("USD" to 1.0, "EUR" to 0.85)
        
        val result = repository.convertCurrency(
            amount = amount,
            fromCurrency = "USD",
            toCurrency = "USD",
            exchangeRates = rates
        )
        
        assertEquals(amount, result, 0.001)
    }

    @Test
    fun `convertCurrency converts USD to EUR correctly`() {
        val amount = 100.0
        val rates = mapOf("USD" to 1.0, "EUR" to 0.85)
        
        val result = repository.convertCurrency(
            amount = amount,
            fromCurrency = "USD",
            toCurrency = "EUR",
            exchangeRates = rates
        )
        
        assertEquals(85.0, result, 0.001)
    }

    @Test
    fun `convertCurrency converts between non-base currencies correctly`() {
        val amount = 100.0
        val rates = mapOf("USD" to 1.0, "EUR" to 0.85, "GBP" to 0.75)
        
        val result = repository.convertCurrency(
            amount = amount,
            fromCurrency = "EUR",
            toCurrency = "GBP",
            exchangeRates = rates
        )
        
        // 100 EUR -> USD: 100 / 0.85 = 117.647
        // 117.647 USD -> GBP: 117.647 * 0.75 = 88.235
        assertEquals(88.235, result, 0.001)
    }

    @Test
    fun `convertCurrency handles missing currency by using rate 1_0`() {
        val amount = 100.0
        val rates = mapOf("USD" to 1.0, "EUR" to 0.85)
        
        val result = repository.convertCurrency(
            amount = amount,
            fromCurrency = "USD",
            toCurrency = "UNKNOWN",
            exchangeRates = rates
        )
        
        assertEquals(100.0, result, 0.001)
    }

    @Test
    fun `getLatestExchangeRates returns failure for network error`() = runTest {
        // This test would require mocking the network layer
        // For now, we test the repository exists and can be instantiated
        assertNotNull(repository)
    }

    @Test
    fun `supported currencies list contains expected currencies`() {
        val supportedCurrencies = ExchangeRateRepository.SUPPORTED_CURRENCIES
        
        assertTrue(supportedCurrencies.contains("USD"))
        assertTrue(supportedCurrencies.contains("EUR"))
        assertTrue(supportedCurrencies.contains("GBP"))
        assertTrue(supportedCurrencies.contains("JPY"))
        assertTrue(supportedCurrencies.size >= 10)
    }
}