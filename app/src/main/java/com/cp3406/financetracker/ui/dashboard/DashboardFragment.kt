package com.cp3406.financetracker.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cp3406.financetracker.databinding.FragmentDashboardBinding
import com.cp3406.financetracker.ui.transactions.TransactionAdapter

class DashboardFragment : Fragment() {

    private var binding: FragmentDashboardBinding? = null
    private lateinit var dashboardVM: DashboardViewModel
    private lateinit var recentTransactionsAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        
        dashboardVM = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))[DashboardViewModel::class.java]
        
        setupRecentTransactionsRecyclerView()
        setupClickListeners()
        setupObservers()
        
        return binding?.root
    }
    
    private fun setupRecentTransactionsRecyclerView() {
        recentTransactionsAdapter = TransactionAdapter { transaction ->
            // Handle click - could navigate to transaction details or edit
            android.util.Log.d("DashboardFragment", "Clicked transaction: ${transaction.description}")
        }
        
        binding?.recentTransactionsRecycler?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentTransactionsAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding?.viewAllTransactions?.setOnClickListener {
            // Navigate to transactions tab
            (activity as? androidx.fragment.app.FragmentActivity)?.let { fragmentActivity ->
                val bottomNav = fragmentActivity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    com.cp3406.financetracker.R.id.nav_view
                )
                bottomNav?.selectedItemId = com.cp3406.financetracker.R.id.navigation_transactions
            }
        }
    }
    
    private fun setupObservers() {
        dashboardVM.welcomeMessage.observe(viewLifecycleOwner) {
            binding?.welcomeText?.text = it
        }
        
        dashboardVM.currentBalance.observe(viewLifecycleOwner) { balanceText ->
            binding?.balanceAmount?.text = balanceText
        }
        
        dashboardVM.monthlyIncome.observe(viewLifecycleOwner) { income ->
            binding?.monthlyIncomeAmount?.text = income
        }
        
        dashboardVM.monthlyExpenses.observe(viewLifecycleOwner) { expenses ->
            binding?.monthlyExpensesAmount?.text = expenses
        }
        
        dashboardVM.budgetProgress.observe(viewLifecycleOwner) { progress ->
            binding?.budgetProgressBar?.progress = progress
            binding?.budgetProgressText?.text = "$progress%"
        }
        
        dashboardVM.activeGoalsCount.observe(viewLifecycleOwner) { count ->
            binding?.activeGoalsText?.text = count
        }
        
        dashboardVM.totalSavingsProgress.observe(viewLifecycleOwner) { savings ->
            binding?.totalSavingsAmount?.text = savings
        }
        
        dashboardVM.recentTransactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isEmpty()) {
                binding?.recentTransactionsRecycler?.visibility = View.GONE
                binding?.noTransactionsEmptyState?.visibility = View.VISIBLE
            } else {
                binding?.recentTransactionsRecycler?.visibility = View.VISIBLE
                binding?.noTransactionsEmptyState?.visibility = View.GONE
                recentTransactionsAdapter.submitList(transactions)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}