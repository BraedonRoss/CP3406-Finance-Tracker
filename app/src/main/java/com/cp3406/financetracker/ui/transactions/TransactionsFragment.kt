package com.cp3406.financetracker.ui.transactions

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cp3406.financetracker.databinding.FragmentTransactionsBinding
import com.cp3406.financetracker.ui.dialogs.AddTransactionDialog

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var viewModel: TransactionsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))[TransactionsViewModel::class.java]
        
        setupRecyclerView()
        setupFab()
        observeTransactions()
        
        return binding.root
    }
    
    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            // Handle transaction click - could show details, edit, etc.
            Log.d("TransactionsFragment", "Clicked transaction: ${transaction.description}")
        }
        
        binding.recyclerViewTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }
    }
    
    private fun setupFab() {
        binding.fabAddTransaction.setOnClickListener {
            val dialog = AddTransactionDialog(requireContext()) { description, amount, category, type, notes ->
                viewModel.addTransaction(description, amount, category, type, notes)
                Toast.makeText(context, "Transaction added successfully", Toast.LENGTH_SHORT).show()
            }
            dialog.show()
        }
    }
    
    private fun observeTransactions() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isEmpty()) {
                binding.emptyStateText.visibility = View.VISIBLE
                binding.recyclerViewTransactions.visibility = View.GONE
                binding.emptyStateText.text = "No transactions yet\nTap + to add your first transaction"
            } else {
                binding.emptyStateText.visibility = View.GONE
                binding.recyclerViewTransactions.visibility = View.VISIBLE
                transactionAdapter.submitList(transactions)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}