package com.cp3406.financetracker.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cp3406.financetracker.databinding.FragmentBudgetBinding
import java.text.NumberFormat
import java.util.Locale

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var budgetAdapter: BudgetCategoryAdapter
    private lateinit var viewModel: BudgetViewModel
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        
        viewModel = ViewModelProvider(this)[BudgetViewModel::class.java]
        
        setupRecyclerView()
        observeViewModel()
        
        return binding.root
    }
    
    private fun setupRecyclerView() {
        budgetAdapter = BudgetCategoryAdapter()
        binding.budgetCategoriesRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = budgetAdapter
        }
    }
    
    private fun observeViewModel() {
        viewModel.budgetCategories.observe(viewLifecycleOwner) { categories ->
            budgetAdapter.submitList(categories)
        }
        
        viewModel.totalBudget.observe(viewLifecycleOwner) { total ->
            binding.totalBudgetAmount.text = currencyFormatter.format(total)
        }
        
        viewModel.totalSpent.observe(viewLifecycleOwner) { spent ->
            binding.totalSpentAmount.text = currencyFormatter.format(spent)
            
            // Calculate and display remaining amount
            viewModel.totalBudget.value?.let { budget ->
                val remaining = budget - spent
                binding.remainingAmount.text = currencyFormatter.format(remaining)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}