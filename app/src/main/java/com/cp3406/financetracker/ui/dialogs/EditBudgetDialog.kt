package com.cp3406.financetracker.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.*
import com.cp3406.financetracker.R
import com.cp3406.financetracker.ui.budget.BudgetCategory
import java.text.NumberFormat
import java.util.*

class EditBudgetDialog(
    private val context: Context,
    private val budgetCategory: BudgetCategory,
    private val onBudgetUpdated: (String, Double) -> Unit,
    private val onSpentUpdated: (String, Double) -> Unit,
    private val onBudgetDeleted: (String) -> Unit,
    private val onSpendingAdded: (String, Double, String) -> Unit // category, amount, description
) {
    
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    
    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_budget, null)
        
        val mainDialog = AlertDialog.Builder(context)
            .setTitle("Edit Budget: ${budgetCategory.name}")
            .setView(dialogView)
            .setPositiveButton("Update Budget", null) // Set to null initially
            .setNeutralButton("Delete Budget", null)
            .setNegativeButton("Cancel", null)
            .create()
        
        val categoryNameText = dialogView.findViewById<TextView>(R.id.text_category_name)
        val budgetAmountInput = dialogView.findViewById<EditText>(R.id.edit_budget_amount)
        val spentAmountInput = dialogView.findViewById<EditText>(R.id.edit_spent_amount)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.budget_progress_bar)
        val progressText = dialogView.findViewById<TextView>(R.id.budget_progress_text)
        val remainingText = dialogView.findViewById<TextView>(R.id.text_remaining_amount)
        val quickAmountButtons = dialogView.findViewById<LinearLayout>(R.id.quick_amount_buttons)
        val customSpendingButton = dialogView.findViewById<Button>(R.id.btn_add_custom_spending)
        
        // Initialize views
        categoryNameText.text = budgetCategory.name
        budgetAmountInput.setText(budgetCategory.budgetAmount.toString())
        spentAmountInput.setText(budgetCategory.spentAmount.toString())
        
        updateProgressDisplay(progressBar, progressText, remainingText)
        
        // Quick amount buttons for common spending
        val quickAmounts = arrayOf(5.0, 10.0, 20.0, 50.0)
        quickAmounts.forEach { amount ->
            val button = Button(context)
            button.text = currencyFormatter.format(amount)
            button.layoutParams = LinearLayout.LayoutParams(
                0, 
                LinearLayout.LayoutParams.WRAP_CONTENT, 
                1f
            ).apply { setMargins(4, 0, 4, 0) }
            button.setOnClickListener {
                // Add actual transaction instead of just updating spent amount
                onSpendingAdded(budgetCategory.name, amount, "Quick spending: ${currencyFormatter.format(amount)}")
                
                // Close the dialog after adding transaction
                mainDialog.dismiss()
            }
            quickAmountButtons.addView(button)
        }
        
        // Custom spending button
        customSpendingButton.setOnClickListener {
            showCustomSpendingDialog(mainDialog)
        }
        
        // Update progress when amounts change
        budgetAmountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateProgressDisplay(progressBar, progressText, remainingText)
            }
        })
        
        spentAmountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateProgressDisplay(progressBar, progressText, remainingText)
            }
        })
        
        // Set up button click listeners after dialog is created
        mainDialog.setOnShowListener {
            mainDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newBudgetAmount = budgetAmountInput.text.toString().toDoubleOrNull()
                val newSpentAmount = spentAmountInput.text.toString().toDoubleOrNull()
                
                if (newBudgetAmount != null && newBudgetAmount > 0 && newSpentAmount != null && newSpentAmount >= 0) {
                    // Always update both values to ensure synchronization
                    onBudgetUpdated(budgetCategory.id, newBudgetAmount)
                    onSpentUpdated(budgetCategory.id, newSpentAmount)
                    Toast.makeText(context, "Budget updated successfully!", Toast.LENGTH_SHORT).show()
                    mainDialog.dismiss()
                } else {
                    Toast.makeText(context, "Please enter valid amounts", Toast.LENGTH_SHORT).show()
                }
            }
            
            mainDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                showDeleteConfirmation()
            }
        }
            
        mainDialog.show()
    }
    
    private fun updateProgressDisplay(progressBar: ProgressBar, progressText: TextView, remainingText: TextView) {
        val budgetAmount = progressBar.rootView.findViewById<EditText>(R.id.edit_budget_amount)
            .text.toString().toDoubleOrNull() ?: budgetCategory.budgetAmount
        val spentAmount = progressBar.rootView.findViewById<EditText>(R.id.edit_spent_amount)
            .text.toString().toDoubleOrNull() ?: budgetCategory.spentAmount
        
        val progress = if (budgetAmount > 0) ((spentAmount / budgetAmount) * 100).toInt() else 0
        val remaining = budgetAmount - spentAmount
        
        progressBar.progress = progress.coerceAtMost(100)
        progressText.text = "$progress%"
        remainingText.text = "Remaining: ${currencyFormatter.format(remaining)}"
        
        // Change color based on progress
        val color = when {
            progress > 100 -> android.R.color.holo_red_dark
            progress > 80 -> android.R.color.holo_orange_dark
            else -> android.R.color.holo_green_dark
        }
        remainingText.setTextColor(context.getColor(color))
    }
    
    private fun showCustomSpendingDialog(parentDialog: AlertDialog) {
        val descriptionInput = EditText(context)
        descriptionInput.hint = "Description (e.g., Lunch at restaurant)"
        descriptionInput.inputType = android.text.InputType.TYPE_CLASS_TEXT
        
        val amountInput = EditText(context)
        amountInput.hint = "Amount"
        amountInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)
        layout.addView(descriptionInput)
        layout.addView(amountInput)
        
        val customDialog = AlertDialog.Builder(context)
            .setTitle("Add Spending to ${budgetCategory.name}")
            .setView(layout)
            .setPositiveButton("Add Transaction", null) // Set to null initially
            .setNegativeButton("Cancel", null)
            .create()
            
        customDialog.setOnShowListener {
            // Set the positive button color to green
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                context.getColor(android.R.color.holo_green_dark)
            )
            
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val description = descriptionInput.text.toString().trim()
                val amount = amountInput.text.toString().toDoubleOrNull()
                
                if (description.isNotEmpty() && amount != null && amount > 0) {
                    onSpendingAdded(budgetCategory.name, amount, description)
                    Toast.makeText(context, "Added ${currencyFormatter.format(amount)} to ${budgetCategory.name}", Toast.LENGTH_SHORT).show()
                    // Close both dialogs
                    customDialog.dismiss()
                    parentDialog.dismiss()
                } else {
                    Toast.makeText(context, "Please enter valid description and amount", Toast.LENGTH_SHORT).show()
                }
            }
        }
            
        customDialog.show()
    }
    
    private fun showDeleteConfirmation() {
        AlertDialog.Builder(context)
            .setTitle("Delete Budget Category")
            .setMessage("Are you sure you want to delete the '${budgetCategory.name}' budget category? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                onBudgetDeleted(budgetCategory.id)
                Toast.makeText(context, "Budget category deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}