package com.cp3406.financetracker.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.*
import com.cp3406.financetracker.R
import com.cp3406.financetracker.data.entity.TransactionType

class AddTransactionDialog(
    private val context: Context,
    private val onTransactionAdded: (String, Double, String, TransactionType, String?) -> Unit
) {
    
    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_transaction, null)
        
        val descriptionInput = dialogView.findViewById<EditText>(R.id.edit_description)
        val amountInput = dialogView.findViewById<EditText>(R.id.edit_amount)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinner_category)
        val typeRadioGroup = dialogView.findViewById<RadioGroup>(R.id.radio_group_type)
        val notesInput = dialogView.findViewById<EditText>(R.id.edit_notes)
        
        // Setup category spinner
        val categories = arrayOf(
            "Food & Dining", "Transportation", "Shopping", "Entertainment", 
            "Bills & Utilities", "Health & Fitness", "Income", "Other"
        )
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
        
        val dialog = AlertDialog.Builder(context)
            .setTitle("Add Transaction")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val description = descriptionInput.text.toString().trim()
                val amountText = amountInput.text.toString().trim()
                val category = categorySpinner.selectedItem.toString()
                val notes = notesInput.text.toString().trim().takeIf { it.isNotEmpty() }
                
                if (description.isNotEmpty() && amountText.isNotEmpty()) {
                    try {
                        val amount = amountText.toDouble()
                        val type = when (typeRadioGroup.checkedRadioButtonId) {
                            R.id.radio_income -> TransactionType.INCOME
                            else -> TransactionType.EXPENSE
                        }
                        
                        onTransactionAdded(description, amount, category, type, notes)
                    } catch (e: NumberFormatException) {
                        Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            
        dialog.show()
    }
}