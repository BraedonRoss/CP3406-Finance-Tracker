package com.cp3406.financetracker.ui.goals

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cp3406.financetracker.R
import com.cp3406.financetracker.databinding.ItemGoalBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class GoalsAdapter(
    private val onGoalClick: (FinancialGoal) -> Unit = {}
) : ListAdapter<FinancialGoal, GoalsAdapter.GoalViewHolder>(GoalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = ItemGoalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(getItem(position), onGoalClick)
    }

    class GoalViewHolder(private val binding: ItemGoalBinding) : RecyclerView.ViewHolder(binding.root) {
        
        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
        private val dateFormatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())

        fun bind(goal: FinancialGoal, onGoalClick: (FinancialGoal) -> Unit) {
            binding.apply {
                goalEmoji.text = goal.category.emoji
                goalTitle.text = goal.title
                goalDescription.text = goal.description
                
                // Amount formatting
                currentAmount.text = currencyFormatter.format(goal.currentAmount)
                targetAmount.text = "of ${currencyFormatter.format(goal.targetAmount)}"
                progressPercentage.text = "${goal.progressPercentage.toInt()}%"
                
                // Progress bar
                progressBar.progress = goal.progressPercentage.toInt()
                
                // Remaining amount
                if (goal.remainingAmount > 0) {
                    remainingAmount.text = "${currencyFormatter.format(goal.remainingAmount)} remaining"
                } else {
                    remainingAmount.text = "Goal completed! 🎉"
                    remainingAmount.setTextColor(ContextCompat.getColor(root.context, R.color.success_green))
                }
                
                // Target date
                targetDate.text = "Target: ${dateFormatter.format(goal.targetDate)}"
                
                // Priority badge
                priorityBadge.text = goal.priority.displayName.uppercase()
                when (goal.priority) {
                    GoalPriority.HIGH -> {
                        priorityBadge.background.setTint(ContextCompat.getColor(root.context, R.color.error_red))
                    }
                    GoalPriority.MEDIUM -> {
                        priorityBadge.background.setTint(ContextCompat.getColor(root.context, R.color.warning_orange))
                    }
                    GoalPriority.LOW -> {
                        priorityBadge.background.setTint(ContextCompat.getColor(root.context, R.color.success_green))
                    }
                }
                
                // Overdue warning
                if (goal.isOverdue) {
                    overdueWarning.visibility = View.VISIBLE
                    overdueWarning.text = "⚠️ Goal was due ${Math.abs(goal.daysTillTarget)} days ago"
                } else {
                    overdueWarning.visibility = View.GONE
                }
                
                // Progress bar color based on completion
                when {
                    goal.isCompleted -> {
                        progressBar.progressTintList = android.content.res.ColorStateList.valueOf(
                            ContextCompat.getColor(root.context, R.color.success_green)
                        )
                    }
                    goal.progressPercentage >= 80 -> {
                        progressBar.progressTintList = android.content.res.ColorStateList.valueOf(
                            ContextCompat.getColor(root.context, R.color.primary_green)
                        )
                    }
                    goal.progressPercentage >= 50 -> {
                        progressBar.progressTintList = android.content.res.ColorStateList.valueOf(
                            ContextCompat.getColor(root.context, R.color.warning_orange)
                        )
                    }
                    else -> {
                        progressBar.progressTintList = android.content.res.ColorStateList.valueOf(
                            ContextCompat.getColor(root.context, R.color.accent_blue)
                        )
                    }
                }
                
                root.setOnClickListener { onGoalClick(goal) }
            }
        }
    }

    private class GoalDiffCallback : DiffUtil.ItemCallback<FinancialGoal>() {
        override fun areItemsTheSame(oldItem: FinancialGoal, newItem: FinancialGoal): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FinancialGoal, newItem: FinancialGoal): Boolean {
            return oldItem == newItem
        }
    }
}