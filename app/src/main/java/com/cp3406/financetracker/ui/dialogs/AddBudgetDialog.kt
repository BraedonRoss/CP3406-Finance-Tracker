package com.cp3406.financetracker.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.*
import com.cp3406.financetracker.R

class AddBudgetDialog(
    private val context: Context,
    private val onBudgetAdded: (String, Double, String, String) -> Unit
) {
    
    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_budget, null)
        
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinner_budget_category)
        val customCategoryInput = dialogView.findViewById<EditText>(R.id.edit_custom_category)
        val budgetAmountInput = dialogView.findViewById<EditText>(R.id.edit_budget_amount)
        val iconSpinner = dialogView.findViewById<Spinner>(R.id.spinner_budget_icon)
        val colorSpinner = dialogView.findViewById<Spinner>(R.id.spinner_budget_color)
        
        // Setup category spinner
        val categories = arrayOf(
            "Custom", "Food & Dining", "Transportation", "Shopping", 
            "Entertainment", "Bills & Utilities", "Health & Fitness", 
            "Education", "Travel", "Insurance", "Other"
        )
        val categoryAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter
        
        // Setup icon spinner
        val icons = arrayOf("🍽️", "🚗", "🛍️", "🎬", "💡", "💪", "📚", "✈️", "🛡️", "📱", "💰")
        val iconAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, icons)
        iconAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        iconSpinner.adapter = iconAdapter
        
        // Setup color spinner
        val colors = arrayOf("Green", "Blue", "Orange", "Purple", "Red", "Teal", "Pink")
        val colorValues = arrayOf("#4CAF50", "#2196F3", "#FF9800", "#9C27B0", "#F44336", "#009688", "#E91E63")
        val colorAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, colors)
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorSpinner.adapter = colorAdapter
        
        // Handle category selection
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (position == 0) { // Custom
                    customCategoryInput.visibility = android.view.View.VISIBLE
                } else {
                    customCategoryInput.visibility = android.view.View.GONE
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        val dialog = AlertDialog.Builder(context)
            .setTitle("Create Budget Category")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val selectedCategory = if (categorySpinner.selectedItemPosition == 0) {
                    customCategoryInput.text.toString().trim()
                } else {
                    categorySpinner.selectedItem.toString()
                }
                
                val budgetAmountText = budgetAmountInput.text.toString().trim()
                val selectedIcon = iconSpinner.selectedItem.toString()
                val selectedColor = colorValues[colorSpinner.selectedItemPosition]
                
                if (selectedCategory.isNotEmpty() && budgetAmountText.isNotEmpty()) {
                    try {
                        val budgetAmount = budgetAmountText.toDouble()
                        if (budgetAmount > 0) {
                            onBudgetAdded(selectedCategory, budgetAmount, selectedIcon, selectedColor)
                        } else {
                            Toast.makeText(context, "Please enter a positive amount", Toast.LENGTH_SHORT).show()
                        }
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