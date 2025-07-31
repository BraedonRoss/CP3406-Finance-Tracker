package com.cp3406.financetracker.ui.budget

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cp3406.financetracker.databinding.ItemBudgetCategoryBinding
import java.text.NumberFormat
import java.util.Locale

class BudgetCategoryAdapter : ListAdapter<BudgetCategory, BudgetCategoryAdapter.ViewHolder>(BudgetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBudgetCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemBudgetCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        
        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

        fun bind(category: BudgetCategory) {
            binding.apply {
                categoryName.text = category.name
                categoryPercentage.text = "${category.progressPercentage.toInt()}%"
                
                spentAmount.text = "${currencyFormatter.format(category.spentAmount)} spent"
                budgetAmount.text = "of ${currencyFormatter.format(category.budgetAmount)}"
                
                budgetProgress.progress = category.progressPercentage.toInt()
                
                // Color coding based on budget status
                try {
                    categoryColorIndicator.setBackgroundColor(Color.parseColor(category.color))
                } catch (e: IllegalArgumentException) {
                    // Fallback to default color if parsing fails
                    categoryColorIndicator.setBackgroundColor(Color.parseColor("#4CAF50"))
                }
                
                // Update remaining amount text and color
                val remaining = category.remainingAmount
                if (remaining >= 0) {
                    remainingAmountText.text = "${currencyFormatter.format(remaining)} remaining" 
                    remainingAmountText.setTextColor(Color.parseColor("#4CAF50")) // Green
                } else {
                    remainingAmountText.text = "${currencyFormatter.format(-remaining)} over budget"
                    remainingAmountText.setTextColor(Color.parseColor("#F44336")) // Red
                }
                
                // Update progress bar color based on status
                if (category.isOverBudget) {
                    budgetProgress.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))
                } else if (category.progressPercentage > 80) {
                    budgetProgress.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800"))
                } else {
                    budgetProgress.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                }
            }
        }
    }

    private class BudgetDiffCallback : DiffUtil.ItemCallback<BudgetCategory>() {
        override fun areItemsTheSame(oldItem: BudgetCategory, newItem: BudgetCategory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BudgetCategory, newItem: BudgetCategory): Boolean {
            return oldItem == newItem
        }
    }
}