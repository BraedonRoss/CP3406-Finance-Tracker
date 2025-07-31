package com.cp3406.financetracker.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cp3406.financetracker.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var binding: FragmentDashboardBinding? = null
    private lateinit var dashboardVM: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        
        dashboardVM = ViewModelProvider(this).get(DashboardViewModel::class.java)
        
        setupObservers()
        
        return binding?.root
    }
    
    private fun setupObservers() {
        dashboardVM.welcomeMessage.observe(viewLifecycleOwner) {
            binding?.welcomeText?.text = it
        }
        
        dashboardVM.balance.observe(viewLifecycleOwner) { balanceText ->
            binding?.balanceAmount?.text = balanceText
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}