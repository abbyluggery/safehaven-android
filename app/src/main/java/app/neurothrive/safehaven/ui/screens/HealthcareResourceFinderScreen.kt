package app.neurothrive.safehaven.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.neurothrive.safehaven.data.database.entities.LegalResource

/**
 * Healthcare Resource Finder Screen
 *
 * CRITICAL POST-ROE SAFETY FEATURE:
 * Helps survivors find comprehensive reproductive healthcare resources including:
 * - Clinics accepting out-of-state patients
 * - Recovery housing facilities
 * - Childcare during appointments and recovery
 * - Financial assistance and travel funding
 * - Accompaniment services for safety
 *
 * PRIVACY FEATURES:
 * - No location tracking while searching
 * - Search history not stored
 * - Can be quickly hidden via back button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthcareResourceFinderScreen(
    onBack: () -> Unit,
    onResourceClick: (LegalResource) -> Unit,
    onStartJourney: (LegalResource) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedState by remember { mutableStateOf("") }
    var showClinicsOnly by remember { mutableStateOf(false) }
    var showRecoveryHousing by remember { mutableStateOf(false) }
    var showChildcare by remember { mutableStateOf(false) }
    var showFinancialAssistance by remember { mutableStateOf(false) }
    var showAccompaniment by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Mock data - replace with actual ViewModel data
    val resources by remember { mutableStateOf(emptyList<LegalResource>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Healthcare Resources") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Privacy Notice
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Privacy",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Column {
                        Text(
                            "Private Search",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Your search is not tracked or stored. No location data is collected.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search resources...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showClinicsOnly,
                    onClick = { showClinicsOnly = !showClinicsOnly },
                    label = { Text("Clinics") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocalHospital,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                FilterChip(
                    selected = showRecoveryHousing,
                    onClick = { showRecoveryHousing = !showRecoveryHousing },
                    label = { Text("Housing") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                FilterChip(
                    selected = showChildcare,
                    onClick = { showChildcare = !showChildcare },
                    label = { Text("Childcare") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ChildCare,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resource list
            if (resources.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Select filters to find resources",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "440+ resources available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(resources) { resource ->
                        HealthcareResourceCard(
                            resource = resource,
                            onClick = { onResourceClick(resource) },
                            onStartJourney = { onStartJourney(resource) }
                        )
                    }
                }
            }
        }

        // Filter dialog
        if (showFilterDialog) {
            HealthcareFilterDialog(
                selectedState = selectedState,
                onStateSelected = { selectedState = it },
                showFinancialAssistance = showFinancialAssistance,
                onFinancialAssistanceToggle = { showFinancialAssistance = it },
                showAccompaniment = showAccompaniment,
                onAccompanimentToggle = { showAccompaniment = it },
                onDismiss = { showFilterDialog = false },
                onApply = { showFilterDialog = false }
            )
        }
    }
}

@Composable
fun HealthcareResourceCard(
    resource: LegalResource,
    onClick: () -> Unit,
    onStartJourney: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resource.organizationName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${resource.city}, ${resource.state}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                </Column>

                // Resource type badge
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            when (resource.resourceType) {
                                "reproductive_healthcare" -> "Clinic"
                                "recovery_housing" -> "Housing"
                                "childcare" -> "Childcare"
                                "financial_assistance" -> "Funding"
                                "accompaniment" -> "Support"
                                else -> resource.resourceType
                            }
                        )
                    }
                )
            }

            // Key features
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (resource.acceptsOutOfStatePatients) {
                    FeatureChip(
                        icon = Icons.Default.Public,
                        text = "Out-of-State OK"
                    )
                }
                if (resource.providesRecoveryHousing) {
                    FeatureChip(
                        icon = Icons.Default.Hotel,
                        text = "Recovery Housing"
                    )
                }
                if (resource.childcareDuringAppointment) {
                    FeatureChip(
                        icon = Icons.Default.ChildCare,
                        text = "Childcare"
                    )
                }
            }

            if (resource.financialAssistanceAvailable || resource.travelFundingAvailable) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (resource.financialAssistanceAvailable) {
                        FeatureChip(
                            icon = Icons.Default.AttachMoney,
                            text = "Financial Aid"
                        )
                    }
                    if (resource.accompanimentServices) {
                        FeatureChip(
                            icon = Icons.Default.People,
                            text = "Accompaniment"
                        )
                    }
                }
            }

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Details")
                }

                Button(
                    onClick = onStartJourney,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.FlightTakeoff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Plan Journey")
                }
            }
        }
    }
}

@Composable
fun FeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun HealthcareFilterDialog(
    selectedState: String,
    onStateSelected: (String) -> Unit,
    showFinancialAssistance: Boolean,
    onFinancialAssistanceToggle: (Boolean) -> Unit,
    showAccompaniment: Boolean,
    onAccompanimentToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Resources") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Select filters to find resources that match your needs",
                    style = MaterialTheme.typography.bodyMedium
                )

                // State filter
                Text(
                    "State (for clinics)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                // TODO: Add state dropdown

                Divider()

                // Additional filters
                Text(
                    "Additional Services",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showFinancialAssistance,
                        onCheckedChange = onFinancialAssistanceToggle
                    )
                    Text("Financial assistance available")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showAccompaniment,
                        onCheckedChange = onAccompanimentToggle
                    )
                    Text("Accompaniment services")
                }
            }
        },
        confirmButton = {
            Button(onClick = onApply) {
                Text("Apply Filters")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
