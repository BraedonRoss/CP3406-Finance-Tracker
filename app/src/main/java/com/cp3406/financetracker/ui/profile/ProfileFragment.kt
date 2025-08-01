package com.cp3406.financetracker.ui.profile

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cp3406.financetracker.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        
        setupClickListeners()
        observeUserProfile()
        observePreferences()

        return binding.root
    }
    
    private fun setupClickListeners() {
        binding.apply {
            // Profile edit button
            editProfileButton.setOnClickListener {
                Toast.makeText(context, "Edit profile functionality coming soon!", Toast.LENGTH_SHORT).show()
            }
            
            // Settings click listeners
            currencySetting.setOnClickListener {
                showCurrencySelector()
            }
            
            changePasswordSetting.setOnClickListener {
                Toast.makeText(context, "Change password functionality coming soon!", Toast.LENGTH_SHORT).show()
            }
            
            helpCenterSetting.setOnClickListener {
                Toast.makeText(context, "Help center coming soon!", Toast.LENGTH_SHORT).show()
            }
            
            privacyPolicySetting.setOnClickListener {
                Toast.makeText(context, "Privacy policy coming soon!", Toast.LENGTH_SHORT).show()
            }
            
            aboutSetting.setOnClickListener {
                showAboutDialog()
            }
            
            signOutButton.setOnClickListener {
                showSignOutDialog()
            }
            
            // Switch listeners
            notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateNotificationSetting(isChecked)
                val message = if (isChecked) "Notifications enabled" else "Notifications disabled"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            
            darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateDarkModeSetting(isChecked)
                val message = if (isChecked) "Dark mode enabled" else "Dark mode disabled"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            
            biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateBiometricSetting(isChecked)
                val message = if (isChecked) "Biometric lock enabled" else "Biometric lock disabled"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun observeUserProfile() {
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            binding.apply {
                profileAvatar.text = profile.avatarInitials
                userName.text = profile.fullName
                userEmail.text = profile.email
                memberSince.text = viewModel.formatMemberSince(profile.memberSince)
            }
        }
    }
    
    private fun observePreferences() {
        viewModel.preferences.observe(viewLifecycleOwner) { preferences ->
            binding.apply {
                // Update switches without triggering listeners
                notificationsSwitch.setOnCheckedChangeListener(null)
                darkModeSwitch.setOnCheckedChangeListener(null)
                biometricSwitch.setOnCheckedChangeListener(null)
                
                notificationsSwitch.isChecked = preferences.notificationsEnabled
                darkModeSwitch.isChecked = preferences.darkModeEnabled
                biometricSwitch.isChecked = preferences.biometricLockEnabled
                selectedCurrency.text = viewModel.getCurrencyDisplayName(preferences.selectedCurrency)
                
                // Re-attach listeners
                setupSwitchListeners()
            }
        }
    }
    
    private fun setupSwitchListeners() {
        binding.apply {
            notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateNotificationSetting(isChecked)
                val message = if (isChecked) "Notifications enabled" else "Notifications disabled"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            
            darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateDarkModeSetting(isChecked)
                val message = if (isChecked) "Dark mode enabled" else "Dark mode disabled"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            
            biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateBiometricSetting(isChecked)
                val message = if (isChecked) "Biometric lock enabled" else "Biometric lock disabled"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showCurrencySelector() {
        val currencies = arrayOf("USD ($)", "EUR (€)", "GBP (£)", "JPY (¥)", "AUD (A$)", "CAD (C$)")
        val currencyCodes = arrayOf("USD", "EUR", "GBP", "JPY", "AUD", "CAD")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Select Currency")
            .setItems(currencies) { _, which ->
                viewModel.updateCurrency(currencyCodes[which])
                Toast.makeText(context, "Currency updated to ${currencies[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
    
    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("About Finance Tracker")
            .setMessage("Finance Tracker v1.0.0\n\nA comprehensive personal finance management app to help you track expenses, manage budgets, and achieve your financial goals.\n\nDeveloped for CP3406 Mobile Computing.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    private fun showSignOutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { _, _ ->
                Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                // In real app, this would handle authentication logout
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}