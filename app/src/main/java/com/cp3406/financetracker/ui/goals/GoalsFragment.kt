package com.cp3406.financetracker.ui.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cp3406.financetracker.databinding.FragmentGoalsBinding

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val goalsViewModel = ViewModelProvider(this)[GoalsViewModel::class.java]

        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        goalsViewModel.text.observe(viewLifecycleOwner) {
            binding.textGoals.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}