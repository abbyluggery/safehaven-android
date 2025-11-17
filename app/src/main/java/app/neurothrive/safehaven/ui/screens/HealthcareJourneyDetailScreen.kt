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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.neurothrive.safehaven.data.database.entities.HealthcareJourney

/**
 * Healthcare Journey Detail Screen
 *
 * VIEW AND MANAGE EXISTING JOURNEY:
 * - Track journey status and progress
 * - Mark arrangements as completed
 * - View what still needs to be arranged
 * - Edit journey details
 * - Update journey status
 * - Complete or cancel journey
 *
 * PRIVACY:
 * - Quick delete option
 * - All sensitive data encrypted
 * - No external sharing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthcareJourneyDetailScreen(
    journey: HealthcareJourney?,
    onBack: () -> Unit,
    onEditJourney: () -> Unit,
    onDeleteJourney: () -> Unit,
    onCompleteJourney: () -> Unit,
    onUpdateStatus: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    if (journey == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Journey not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Healthcare Journey") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditJourney) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Journey status
            JourneyStatusCard(
                journeyStatus = journey.journeyStatus,
                createdAt = journey.createdAt,
                lastUpdated = journey.lastUpdated,
                onUpdateStatus = onUpdateStatus
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Arrangements checklist
            ArrangementsChecklistCard(journey)

            Spacer(modifier = Modifier.height(16.dp))

            // Journey details sections
            if (journey.needsChildcare) {
                ChildcareDetailsCard(
                    numberOfChildren = journey.numberOfChildren,
                    childcareType = journey.childcareType,
                    arranged = journey.childcareArranged
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (journey.needsRecoveryHousing) {
                RecoveryHousingDetailsCard(
                    recoveryDuration = journey.recoveryHousingDuration,
                    arranged = journey.recoveryHousingArranged
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (journey.needsFinancialAssistance) {
                FinancialAssistanceCard(arranged = journey.financialAssistanceArranged)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (journey.needsAccompaniment) {
                AccompanimentCard(arranged = journey.accompanimentArranged)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Actions
            if (journey.journeyStatus != "completed" && journey.journeyStatus != "cancelled") {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showCompleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark Journey as Complete")
                    }

                    OutlinedButton(
                        onClick = { /* Cancel journey */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel Journey")
                    }
                }
            }

            // Privacy reminder
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
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Column {
                        Text(
                            "Your Privacy",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "All journey details are encrypted. You can delete this journey at any time.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Delete Journey?") },
            text = { Text("This will permanently delete all journey information. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteJourney()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Complete confirmation dialog
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
            title = { Text("Mark Journey as Complete?") },
            text = {
                Column {
                    Text("Mark this journey as successfully completed?")
                    if (journey.autoDeleteAfterCompletion) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Note: This journey will be automatically deleted in 30 days.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCompleteDialog = false
                        onCompleteJourney()
                    }
                ) {
                    Text("Complete Journey")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun JourneyStatusCard(
    journeyStatus: String,
    createdAt: Long,
    lastUpdated: Long,
    onUpdateStatus: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Journey Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            when (journeyStatus) {
                                "planning" -> "Planning"
                                "confirmed" -> "Confirmed"
                                "traveling_outbound" -> "Traveling"
                                "at_appointment" -> "At Appointment"
                                "recovering" -> "Recovering"
                                "traveling_return" -> "Returning"
                                "completed" -> "Completed"
                                "cancelled" -> "Cancelled"
                                else -> journeyStatus
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            when (journeyStatus) {
                                "completed" -> Icons.Default.CheckCircle
                                "cancelled" -> Icons.Default.Cancel
                                else -> Icons.Default.FlightTakeoff
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            // Status timeline options
            if (journeyStatus != "completed" && journeyStatus != "cancelled") {
                Divider()

                Text(
                    "Update Status:",
                    style = MaterialTheme.typography.labelMedium
                )

                val statusOptions = listOf(
                    "planning" to "Planning",
                    "confirmed" to "Confirmed",
                    "traveling_outbound" to "Traveling Outbound",
                    "at_appointment" to "At Appointment",
                    "recovering" to "Recovering",
                    "traveling_return" to "Returning Home"
                )

                Column {
                    statusOptions.forEach { (value, label) ->
                        TextButton(
                            onClick = { onUpdateStatus(value) },
                            enabled = value != journeyStatus
                        ) {
                            Icon(
                                if (value == journeyStatus) Icons.Default.CheckCircle else Icons.Default.Circle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArrangementsChecklistCard(journey: HealthcareJourney) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Arrangements Checklist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            ArrangementItem(
                label = "Outbound Transportation",
                arranged = journey.outboundTransportationArranged
            )

            if (journey.needsChildcare) {
                ArrangementItem(
                    label = "Childcare",
                    arranged = journey.childcareArranged
                )
            }

            if (journey.needsRecoveryHousing) {
                ArrangementItem(
                    label = "Recovery Housing",
                    arranged = journey.recoveryHousingArranged
                )
            }

            ArrangementItem(
                label = "Return Transportation",
                arranged = journey.returnTransportationArranged
            )

            if (journey.needsFinancialAssistance) {
                ArrangementItem(
                    label = "Financial Assistance",
                    arranged = journey.financialAssistanceArranged
                )
            }

            if (journey.needsAccompaniment) {
                ArrangementItem(
                    label = "Accompaniment",
                    arranged = journey.accompanimentArranged
                )
            }
        }
    }
}

@Composable
fun ArrangementItem(label: String, arranged: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                if (arranged) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (arranged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(label)
        }

        if (!arranged) {
            TextButton(onClick = { /* Mark as arranged */ }) {
                Text("Mark Done")
            }
        }
    }
}

@Composable
fun ChildcareDetailsCard(
    numberOfChildren: Int,
    childcareType: String?,
    arranged: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.ChildCare, contentDescription = null)
            Column {
                Text(
                    "Childcare",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text("$numberOfChildren ${if (numberOfChildren == 1) "child" else "children"}")
                Text(
                    when (childcareType) {
                        "during_appointment" -> "During appointment only"
                        "during_recovery" -> "During recovery period"
                        "full_journey" -> "Entire journey"
                        else -> "Type not specified"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun RecoveryHousingDetailsCard(
    recoveryDuration: String?,
    arranged: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Hotel, contentDescription = null)
            Column {
                Text(
                    "Recovery Housing",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(recoveryDuration ?: "Duration not specified")
            }
        }
    }
}

@Composable
fun FinancialAssistanceCard(arranged: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.AttachMoney, contentDescription = null)
            Column {
                Text(
                    "Financial Assistance",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (arranged) "Assistance secured" else "Needs to be arranged",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun AccompanimentCard(arranged: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.People, contentDescription = null)
            Column {
                Text(
                    "Accompaniment Services",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (arranged) "Volunteer confirmed" else "Needs to be arranged",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
