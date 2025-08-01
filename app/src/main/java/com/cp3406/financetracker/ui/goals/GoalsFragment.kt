package com.cp3406.financetracker.ui.goals

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cp3406.financetracker.databinding.FragmentGoalsBinding
import java.text.NumberFormat
import java.util.Locale

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var goalsAdapter: GoalsAdapter
    private lateinit var viewModel: GoalsViewModel
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        
        viewModel = ViewModelProvider(this)[GoalsViewModel::class.java]
        
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        
        return binding.root
    }
    
    private fun setupRecyclerView() {
        goalsAdapter = GoalsAdapter { goal ->
            // Handle goal click - could show details, edit, etc.
            Log.d("GoalsFragment", "Clicked goal: ${goal.title}")
        }
        
        binding.goalsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = goalsAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.addGoalButton.setOnClickListener {
            // TODO: Open add goal dialog/screen
            Log.d("GoalsFragment", "Add goal button clicked")
        }
    }
    
    private fun observeViewModel() {
        viewModel.goals.observe(viewLifecycleOwner) { goals ->
            if (goals.isEmpty()) {
                binding.emptyStateText.visibility = View.VISIBLE
                binding.goalsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyStateText.visibility = View.GONE
                binding.goalsRecyclerView.visibility = View.VISIBLE
                goalsAdapter.submitList(goals)
                
                // Update goal count
                val activeGoals = goals.filter { !it.isCompleted }
                binding.totalGoalsCount.text = activeGoals.size.toString()
            }
        }
        
        viewModel.totalSaved.observe(viewLifecycleOwner) { totalSaved ->
            binding.totalSavedAmount.text = currencyFormatter.format(totalSaved)
        }
        
        viewModel.averageProgress.observe(viewLifecycleOwner) { avgProgress ->
            binding.completionPercentage.text = "${avgProgress.toInt()}%"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}