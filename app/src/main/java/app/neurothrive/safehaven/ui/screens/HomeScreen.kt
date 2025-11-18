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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.neurothrive.safehaven.data.session.UserSession
import app.neurothrive.safehaven.ui.components.FeatureCard
import app.neurothrive.safehaven.ui.components.SOSPanicButton
import app.neurothrive.safehaven.ui.viewmodels.HomeViewModel
import app.neurothrive.safehaven.ui.viewmodels.SOSViewModel
import app.neurothrive.safehaven.ui.viewmodels.RiskAssessmentViewModel

/**
 * Home Screen - Main Dashboard
 * Central hub for all SafeHaven features
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    sosViewModel: SOSViewModel = hiltViewModel(),
    riskViewModel: RiskAssessmentViewModel = hiltViewModel(),
    userSession: UserSession = hiltViewModel(),
    onNavigateToCamera: () -> Unit,
    onNavigateToIncidents: () -> Unit,
    onNavigateToEvidence: () -> Unit,
    onNavigateToVerification: () -> Unit,
    onNavigateToResources: () -> Unit,
    onNavigateToSafetyPlan: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHealthcare: () -> Unit = {},
    onNavigateToAbuseResources: () -> Unit = {},
    onNavigateToEmergencyContacts: () -> Unit = {},
    onNavigateToRiskAssessment: () -> Unit = {}
) {
    // Collect state from ViewModel
    val stats by viewModel.stats.collectAsState()
    val currentUserId by userSession.currentUserId.collectAsState(initial = null)

    // Collect SOS state
    val isSOSActive by sosViewModel.isSOSActive.collectAsState()

    // Collect risk assessment state
    val latestRiskAssessment by riskViewModel.latestAssessment.collectAsState()
    val hasCriticalRisk by riskViewModel.hasCriticalRisk.collectAsState()

    // Load dashboard when screen launches
    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            viewModel.loadDashboard(userId)
            sosViewModel.loadActiveSession(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SafeHaven") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
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
            // Welcome Card with Stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Welcome to SafeHaven",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your data is encrypted and stays on your device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    // Quick Stats
                    if (stats.incidentCount > 0 || stats.evidenceCount > 0 || stats.documentCount > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("Incidents", stats.incidentCount)
                            StatItem("Evidence", stats.evidenceCount)
                            StatItem("Documents", stats.documentCount)
                        }

                        if (stats.activeHealthcareJourneys > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "🏥 ${stats.activeHealthcareJourneys} active healthcare journey(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Critical Risk Alert (if present)
            if (hasCriticalRisk) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "⚠️ CRITICAL RISK DETECTED",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = latestRiskAssessment?.aiSummary ?: "Your latest risk assessment indicates elevated danger. Please review safety recommendations.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToRiskAssessment,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("View Risk Assessment")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // SOS Panic Button Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Emergency SOS",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    currentUserId?.let { userId ->
                        SOSPanicButton(
                            isActive = isSOSActive,
                            onActivate = {
                                sosViewModel.activateSOS(userId, includeLocation = true)
                            },
                            onDeactivate = {
                                sosViewModel.deactivateSOS(userId, sendAllClear = true)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onNavigateToEmergencyContacts,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Contacts, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage Emergency Contacts")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Document Evidence",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            FeatureCard(
                title = "Silent Camera",
                description = "Capture photos silently (no sound, flash, or GPS)",
                icon = {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                },
                onClick = onNavigateToCamera
            )

            Spacer(modifier = Modifier.height(8.dp))

            FeatureCard(
                title = "Evidence Vault",
                description = "View encrypted photos and videos",
                icon = {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                },
                onClick = onNavigateToEvidence
            )

            Spacer(modifier = Modifier.height(8.dp))

            FeatureCard(
                title = "Document Verification",
                description = "Verify IDs, passports, birth certificates",
                icon = {
                    Icon(
                        Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                },
                onClick = onNavigateToVerification
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Track & Report",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            FeatureCard(
                title = "Incident Reports",
                description = "Legal-formatted documentation of abuse",
                icon = {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                },
                onClick = onNavigateToIncidents
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Find Help",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            FeatureCard(
                title = "Find Resources",
                description = "Shelters, legal aid, hotlines near you",
                icon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                },
                onClick = onNavigateToResources
            )

            Spacer(modifier = Modifier.height(8.dp))

            FeatureCard(
                title = "Safety Plan",
                description = "Create your personalized safety plan",
                icon = {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                },
                onClick = onNavigateToSafetyPlan
            )

            Spacer(modifier = Modifier.height(8.dp))

            FeatureCard(
                title = "Healthcare Journey",
                description = "Plan reproductive healthcare appointments",
                icon = {
                    Icon(
                        Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                },
                onClick = onNavigateToHealthcare
            )

            Spacer(modifier = Modifier.height(8.dp))

            FeatureCard(
                title = "Learn About Abuse",
                description = "Educational resources on recognizing and documenting abuse",
                icon = {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                },
                onClick = onNavigateToAbuseResources
            )

            Spacer(modifier = Modifier.height(8.dp))

            FeatureCard(
                title = "AI Risk Assessment",
                description = "AI-powered pattern detection and safety recommendations",
                icon = {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                },
                onClick = onNavigateToRiskAssessment
            )
        }
    }
}

@Composable
private fun StatItem(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
