package app.neurothrive.safehaven.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.neurothrive.safehaven.data.session.UserSession
import app.neurothrive.safehaven.ui.components.SafeHavenTopBar
import app.neurothrive.safehaven.ui.viewmodels.SettingsViewModel

/**
 * Settings Screen
 * App configuration and security settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    userSession: UserSession = hiltViewModel(),
    onBack: () -> Unit
) {
    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val currentUserId by userSession.currentUserId.collectAsState(initial = null)

    // Load profile when screen launches
    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            viewModel.loadProfile(userId)
        }
    }

    // Panic delete confirmation dialog
    var showPanicDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SafeHavenTopBar(
                title = "Settings",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Security Settings
            Text(
                text = "Security",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            profile?.let { currentProfile ->
                SettingRow(
                    title = "GPS Tracking",
                    description = "Include location in incident reports (OFF by default for safety)",
                    checked = currentProfile.gpsEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.toggleGPS(enabled)
                    }
                )

                SettingRow(
                    title = "Stealth Mode",
                    description = "Hide SafeHaven from recent apps list",
                    checked = currentProfile.stealthModeEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.toggleStealthMode(enabled)
                    }
                )

                SettingRow(
                    title = "Auto-Delete",
                    description = "Automatically delete data after ${currentProfile.autoDeleteDays} days",
                    checked = currentProfile.autoDeleteEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.toggleAutoDelete(enabled, currentProfile.autoDeleteDays)
                    }
                )
            }

            // Save success indicator
            if (uiState.saveSuccess) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "✓ Settings saved",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    viewModel.resetSaveSuccess()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account Actions
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { /* TODO: Export data */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export All Data (PDF)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Danger Zone
            Text(
                text = "Danger Zone",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ Panic Delete",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Permanently delete ALL SafeHaven data. This cannot be undone. Completes in <2 seconds.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showPanicDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isPanicDeleting
                    ) {
                        if (uiState.isPanicDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onError
                            )
                        } else {
                            Text("Delete All Data")
                        }
                    }
                }
            }

            // Error message
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Info
            Text(
                text = "SafeHaven v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "All data encrypted with AES-256",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Panic Delete Confirmation Dialog
    if (showPanicDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showPanicDeleteDialog = false },
            title = { Text("⚠️ PANIC DELETE") },
            text = {
                Column {
                    Text("This will PERMANENTLY delete:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• All incident reports")
                    Text("• All evidence items (photos/videos)")
                    Text("• All verified documents")
                    Text("• All healthcare journeys")
                    Text("• Your profile and settings")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "This action CANNOT be undone.",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPanicDeleteDialog = false
                        currentUserId?.let { userId ->
                            viewModel.executePanicDelete(userId) {
                                // Navigate away after delete completes
                                onBack()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("DELETE EVERYTHING")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPanicDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout? Your data will remain on this device.") },
            confirmButton = {
                Button(onClick = {
                    showLogoutDialog = false
                    kotlinx.coroutines.GlobalScope.launch {
                        userSession.clearCurrentUser()
                    }
                    onBack()
                }) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
