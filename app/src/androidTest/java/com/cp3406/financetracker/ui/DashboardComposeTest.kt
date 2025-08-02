package com.cp3406.financetracker.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cp3406.financetracker.ui.dashboard.DashboardScreen
import com.cp3406.financetracker.ui.theme.FinanceTrackerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboardScreen_displaysWelcomeSection() {
        composeTestRule.setContent {
            FinanceTrackerTheme {
                DashboardScreen()
            }
        }

        // Verify welcome section is displayed
        composeTestRule.onNodeWithText("Current Balance").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_displaysQuickStats() {
        composeTestRule.setContent {
            FinanceTrackerTheme {
                DashboardScreen()
            }
        }

        // Verify quick stats cards are displayed
        composeTestRule.onNodeWithText("Monthly Income").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monthly Expenses").assertIsDisplayed()
        composeTestRule.onNodeWithText("Budget Used").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_displaysSavingsProgress() {
        composeTestRule.setContent {
            FinanceTrackerTheme {
                DashboardScreen()
            }
        }

        // Verify savings progress section
        composeTestRule.onNodeWithText("Savings Progress").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active Goals").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total Saved").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_displaysRecentTransactionsSection() {
        composeTestRule.setContent {
            FinanceTrackerTheme {
                DashboardScreen()
            }
        }

        // Verify recent transactions section
        composeTestRule.onNodeWithText("Recent Transactions").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_showsEmptyStateWhenNoTransactions() {
        composeTestRule.setContent {
            FinanceTrackerTheme {
                DashboardScreen()
            }
        }

        // When there are no transactions, should show empty state
        composeTestRule.onNodeWithText("No transactions yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start tracking your finances by adding your first transaction")
            .assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_statsCardsHaveCorrectContent() {
        composeTestRule.setContent {
            FinanceTrackerTheme {
                DashboardScreen()
            }
        }

        // Test that all stat cards contain percentage symbol for budget or currency symbols
        composeTestRule.onNodeWithText("Monthly Income").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monthly Expenses").assertIsDisplayed()
        
        // Budget progress should show a percentage
        composeTestRule.onNode(hasText("%")).assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_hasScrollableContent() {
        composeTestRule.setContent {
            FinanceTrackerTheme {
                DashboardScreen()
            }
        }

        // Verify the main content is scrollable by checking LazyColumn exists
        composeTestRule.onRoot().performScrollDown()
        
        // After scrolling, content should still be visible
        composeTestRule.onNodeWithText("Current Balance").assertIsDisplayed()
    }
}