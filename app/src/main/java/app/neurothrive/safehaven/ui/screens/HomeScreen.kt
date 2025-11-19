package app.neurothrive.safehaven.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.neurothrive.safehaven.ui.navigation.Screen
import app.neurothrive.safehaven.ui.theme.SOSRed
import app.neurothrive.safehaven.ui.viewmodels.HomeViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val incidentCount by viewModel.incidentCount.collectAsState()
    val evidenceCount by viewModel.evidenceCount.collectAsState()

    var showPanicDialog by remember { mutableStateOf(false) }
    var panicDeleteInProgress by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SafeHaven") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // SOS Panic Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = SOSRed)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚠️ EMERGENCY",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showPanicDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "PANIC DELETE",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Hold for 3 seconds to delete all evidence",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    title = "Incidents",
                    count = incidentCount,
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )

                StatCard(
                    title = "Evidence",
                    count = evidenceCount,
                    icon = Icons.Default.PhotoLibrary,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Cards
            NavigationCard(
                title = "Document Incident",
                subtitle = "Create legal-formatted report",
                icon = Icons.Default.Description,
                onClick = { navController.navigate(Screen.IncidentReport.route) }
            )

            NavigationCard(
                title = "Silent Camera",
                subtitle = "Capture encrypted evidence",
                icon = Icons.Default.CameraAlt,
                onClick = { navController.navigate(Screen.SilentCamera.route) }
            )

            NavigationCard(
                title = "Evidence Vault",
                subtitle = "View encrypted photos",
                icon = Icons.Default.Lock,
                onClick = { navController.navigate(Screen.EvidenceVault.route) }
            )

            NavigationCard(
                title = "Find Resources",
                subtitle = "Intersectional support matching",
                icon = Icons.Default.Explore,
                onClick = { navController.navigate(Screen.Resources.route) }
            )

            NavigationCard(
                title = "Settings",
                subtitle = "Profile & security settings",
                icon = Icons.Default.Settings,
                onClick = { navController.navigate(Screen.Settings.route) }
            )
        }
    }

    // Panic Delete Confirmation Dialog
    if (showPanicDialog) {
        AlertDialog(
            onDismissRequest = { showPanicDialog = false },
            title = { Text("⚠️ Panic Delete") },
            text = { Text("This will PERMANENTLY delete ALL evidence, incidents, and data. This cannot be undone. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        panicDeleteInProgress = true
                        showPanicDialog = false
                        viewModel.triggerPanicDelete {
                            panicDeleteInProgress = false
                            // App could close or show confirmation
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("DELETE ALL")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPanicDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Panic Delete Progress
    if (panicDeleteInProgress) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Deleting Evidence...") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Securely deleting all data...")
                }
            },
            confirmButton = { }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    count: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun NavigationCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
