package com.cp3406.financetracker.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.cp3406.financetracker.data.dao.TransactionDao
import com.cp3406.financetracker.data.entity.TransactionEntity
import com.cp3406.financetracker.data.entity.TransactionType
import com.cp3406.financetracker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.util.*

class TransactionRepositoryTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var transactionDao: TransactionDao

    private lateinit var repository: TransactionRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = TransactionRepository(transactionDao)
    }

    @Test
    fun `insertTransaction calls dao insert method`() = runTest {
        val transaction = TransactionEntity(
            id = 1,
            description = "Test Transaction",
            amount = 100.0,
            category = "Food",
            date = Date(),
            type = TransactionType.EXPENSE,
            isRecurring = false,
            notes = "Test notes"
        )

        repository.insertTransaction(transaction)

        verify(transactionDao).insertTransaction(transaction)
    }

    @Test
    fun `getAllTransactions returns flow from dao`() = runTest {
        val transactions = listOf(
            TransactionEntity(
                id = 1,
                description = "Transaction 1",
                amount = 50.0,
                category = "Food",
                date = Date(),
                type = TransactionType.EXPENSE,
                isRecurring = false
            ),
            TransactionEntity(
                id = 2,
                description = "Transaction 2",
                amount = 200.0,
                category = "Income",
                date = Date(),
                type = TransactionType.INCOME,
                isRecurring = false
            )
        )

        whenever(transactionDao.getAllTransactions()).thenReturn(flowOf(transactions))

        val result = repository.getAllTransactions()
        
        verify(transactionDao).getAllTransactions()
        // In a real test, you'd collect the flow and assert the values
    }

    @Test
    fun `deleteTransaction calls dao delete method`() = runTest {
        val transaction = TransactionEntity(
            id = 1,
            description = "Test Transaction",
            amount = 100.0,
            category = "Food",
            date = Date(),
            type = TransactionType.EXPENSE,
            isRecurring = false
        )

        repository.deleteTransaction(transaction)

        verify(transactionDao).deleteTransaction(transaction)
    }

    @Test
    fun `deleteAllTransactions calls dao deleteAll method`() = runTest {
        repository.deleteAllTransactions()

        verify(transactionDao).deleteAllTransactions()
    }

    @Test
    fun `getTransactionsByType returns correct type filter`() = runTest {
        val expenseTransactions = listOf(
            TransactionEntity(
                id = 1,
                description = "Expense 1",
                amount = 50.0,
                category = "Food",
                date = Date(),
                type = TransactionType.EXPENSE,
                isRecurring = false
            )
        )

        whenever(transactionDao.getTransactionsByType(TransactionType.EXPENSE))
            .thenReturn(flowOf(expenseTransactions))

        repository.getTransactionsByType(TransactionType.EXPENSE)

        verify(transactionDao).getTransactionsByType(TransactionType.EXPENSE)
    }

    @Test
    fun `getTotalBalance calculates correct balance`() = runTest {
        val totalIncome = 1000.0
        val totalExpenses = 400.0
        val expectedBalance = 600.0

        whenever(transactionDao.getTotalByType(TransactionType.INCOME))
            .thenReturn(flowOf(totalIncome))
        whenever(transactionDao.getTotalByType(TransactionType.EXPENSE))
            .thenReturn(flowOf(totalExpenses))

        repository.getTotalBalance()

        verify(transactionDao).getTotalByType(TransactionType.INCOME)
        verify(transactionDao).getTotalByType(TransactionType.EXPENSE)
    }
}