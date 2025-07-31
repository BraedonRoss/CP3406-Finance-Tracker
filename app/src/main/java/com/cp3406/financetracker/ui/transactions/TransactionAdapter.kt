package com.cp3406.financetracker.ui.transactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cp3406.financetracker.R
import com.cp3406.financetracker.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit = {}
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class TransactionViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        
        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

        fun bind(transaction: Transaction, onItemClick: (Transaction) -> Unit) {
            binding.apply {
                descriptionText.text = transaction.description
                categoryText.text = transaction.category
                
                // Show merchant if available
                if (!transaction.merchantName.isNullOrBlank() && transaction.merchantName != transaction.description) {
                    merchantText.text = transaction.merchantName
                    merchantText.visibility = View.VISIBLE
                } else {
                    merchantText.visibility = View.GONE
                }
                
                // Format date
                dateText.text = formatDate(transaction.date)
                
                // Format amount with proper color
                val formattedAmount = currencyFormatter.format(Math.abs(transaction.amount))
                if (transaction.isExpense) {
                    amountText.text = "-$formattedAmount"
                    amountText.setTextColor(ContextCompat.getColor(root.context, R.color.expense_red))
                } else {
                    amountText.text = "+$formattedAmount"
                    amountText.setTextColor(ContextCompat.getColor(root.context, R.color.income_green))
                }
                
                root.setOnClickListener { onItemClick(transaction) }
            }
        }
        
        private fun formatDate(date: Date): String {
            val now = Calendar.getInstance()
            val transactionCal = Calendar.getInstance().apply { time = date }
            
            val daysDiff = TimeUnit.MILLISECONDS.toDays(now.timeInMillis - date.time)
            
            return when {
                daysDiff == 0L -> "Today"
                daysDiff == 1L -> "Yesterday"
                daysDiff < 7 -> SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
                else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
            }
        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}