package com.cp3406.financetracker.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cp3406.financetracker.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        
        val viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        
        viewModel.welcomeMessage.observe(viewLifecycleOwner) { message ->
            binding.welcomeText.text = message
        }
        
        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            binding.balanceAmount.text = balance
        }
        
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}