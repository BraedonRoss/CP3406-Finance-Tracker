package com.cp3406.financetracker.ui.transactions

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cp3406.financetracker.databinding.FragmentTransactionsBinding

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var transactionsAdapter: TransactionAdapter
    private lateinit var vm: TransactionsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        
        setupViewModel()
        setupRecyclerView()
        observeTransactions()
        
        return binding.root
    }
    
    private fun setupViewModel() {
        vm = ViewModelProvider(this)[TransactionsViewModel::class.java]
    }
    
    private fun setupRecyclerView() {
        transactionsAdapter = TransactionAdapter { transaction ->
            Log.d("TransactionsFragment", "Clicked transaction: ${transaction.description}")
        }
        
        binding.recyclerViewTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionsAdapter
        }
    }
    
    private fun observeTransactions() {
        vm.transactions.observe(viewLifecycleOwner) { transactionList ->
            transactionsAdapter.submitList(transactionList)
            
            if (transactionList.isEmpty()) {
                binding.emptyStateText.visibility = View.VISIBLE
                binding.recyclerViewTransactions.visibility = View.GONE
            } else {
                binding.emptyStateText.visibility = View.GONE
                binding.recyclerViewTransactions.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}