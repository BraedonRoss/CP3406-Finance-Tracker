package com.cp3406.financetracker.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeProfileScreen(
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.observeAsState()
    val preferences by viewModel.preferences.observeAsState()
    
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header
        item {
            userProfile?.let { profile ->
                ProfileHeaderCard(
                    profile = profile,
                    onMemberSinceFormat = { viewModel.formatMemberSince(it) }
                )
            }
        }
        
        // App Settings Section
        item {
            preferences?.let { prefs ->
                SettingsSection(
                    title = "App Settings",
                    settings = listOf(
                        SettingItem.Switch(
                            title = "Dark Mode",
                            subtitle = "Switch between light and dark theme",
                            icon = Icons.Default.Settings,
                            checked = prefs.darkModeEnabled,
                            onCheckedChange = { viewModel.updateDarkModeSetting(it) }
                        )
                    )
                )
            }
        }
        
        // Data Management Section
        item {
            SettingsSection(
                title = "Data Management",
                settings = listOf(
                    SettingItem.Action(
                        title = "Clear All Data",
                        subtitle = "Reset app to initial state",
                        icon = Icons.Default.Delete,
                        isDestructive = true,
                        onClick = { showClearDataDialog = true }
                    )
                )
            )
        }
        
        // Account Section
        item {
            SettingsSection(
                title = "Account",
                settings = listOf(
                    SettingItem.Action(
                        title = "Sign Out",
                        subtitle = "Log out of your account",
                        icon = Icons.Default.ExitToApp,
                        isDestructive = true,
                        onClick = { showSignOutDialog = true }
                    )
                )
            )
        }
    }
    
    // Dialogs
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to clear all your financial data? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to sign out?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.signOut()
                        onSignOut()
                        showSignOutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileHeaderCard(
    profile: UserProfile,
    onMemberSinceFormat: (java.util.Date) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.avatarInitials,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Name
            Text(
                text = profile.fullName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            // Email
            Text(
                text = profile.email,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Member Since
            Text(
                text = onMemberSinceFormat(profile.memberSince),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    settings: List<SettingItem>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            settings.forEachIndexed { index, setting ->
                when (setting) {
                    is SettingItem.Switch -> {
                        SwitchSettingRow(setting)
                    }
                    is SettingItem.Action -> {
                        ActionSettingRow(setting)
                    }
                    is SettingItem.Dropdown -> {
                        DropdownSettingRow(setting)
                    }
                }
                
                if (index < settings.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchSettingRow(setting: SettingItem.Switch) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = setting.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = setting.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = setting.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        Switch(
            checked = setting.checked,
            onCheckedChange = setting.onCheckedChange
        )
    }
}

@Composable
private fun ActionSettingRow(setting: SettingItem.Action) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = setting.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (setting.isDestructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            }
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = setting.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (setting.isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = setting.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        IconButton(onClick = setting.onClick) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Execute action",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun DropdownSettingRow(setting: SettingItem.Dropdown) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = setting.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = setting.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = setting.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select option"
                )
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                setting.options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(setting.optionDisplayName(option)) },
                        onClick = {
                            setting.onOptionSelected(option)
                            expanded = false
                        },
                        leadingIcon = if (option == setting.selectedOption) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected"
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}

sealed class SettingItem {
    data class Switch(
        val title: String,
        val subtitle: String,
        val icon: ImageVector,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : SettingItem()
    
    data class Action(
        val title: String,
        val subtitle: String,
        val icon: ImageVector,
        val isDestructive: Boolean = false,
        val onClick: () -> Unit
    ) : SettingItem()
    
    data class Dropdown(
        val title: String,
        val subtitle: String,
        val icon: ImageVector,
        val options: List<String>,
        val selectedOption: String,
        val onOptionSelected: (String) -> Unit,
        val optionDisplayName: (String) -> String
    ) : SettingItem()
}