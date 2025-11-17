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
import app.neurothrive.safehaven.data.database.entities.LegalResource

/**
 * Healthcare Journey Planner Screen
 *
 * COMPLETE JOURNEY COORDINATION:
 * Step-by-step wizard to help survivors plan their entire healthcare journey:
 * 1. Healthcare appointment (clinic, date, services)
 * 2. Outbound travel arrangements
 * 3. Childcare during appointment and recovery
 * 4. Recovery housing (if needed)
 * 5. Return travel
 * 6. Financial assistance
 * 7. Accompaniment for safety
 *
 * PRIVACY PROTECTIONS:
 * - All dates/locations/notes are AES-256 encrypted before storage
 * - No location tracking to/from medical facilities
 * - Optional auto-delete after journey completion
 * - Stealth mode available (hide from main screen)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthcareJourneyPlannerScreen(
    selectedClinic: LegalResource?,
    onBack: () -> Unit,
    onSaveJourney: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()

    // Journey data
    var appointmentDate by remember { mutableStateOf("") }
    var appointmentTime by remember { mutableStateOf("") }
    var servicesNeeded by remember { mutableStateOf(setOf<String>()) }
    var notes by remember { mutableStateOf("") }

    // Childcare
    var needsChildcare by remember { mutableStateOf(false) }
    var numberOfChildren by remember { mutableStateOf(0) }
    var childcareType by remember { mutableStateOf("during_appointment") }

    // Travel
    var departureLocation by remember { mutableStateOf("") }
    var outboundTransportType by remember { mutableStateOf("") }
    var returnTransportType by remember { mutableStateOf("") }

    // Recovery
    var needsRecoveryHousing by remember { mutableStateOf(false) }
    var recoveryDuration by remember { mutableStateOf("") }

    // Additional support
    var needsFinancialAssistance by remember { mutableStateOf(false) }
    var needsAccompaniment by remember { mutableStateOf(false) }

    // Privacy settings
    var autoDeleteAfterCompletion by remember { mutableStateOf(true) }
    var useStealthMode by remember { mutableStateOf(false) }

    val totalSteps = 7

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan Healthcare Journey") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Quick exit button
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, "Quick Exit", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { currentStep-- }
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (currentStep < totalSteps - 1) {
                                currentStep++
                            } else {
                                onSaveJourney()
                            }
                        }
                    ) {
                        Text(if (currentStep < totalSteps - 1) "Next" else "Save Journey")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            if (currentStep < totalSteps - 1) Icons.Default.ArrowForward else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = (currentStep + 1) / totalSteps.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step indicator
            Text(
                text = "Step ${currentStep + 1} of $totalSteps",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step content
            when (currentStep) {
                0 -> AppointmentStep(
                    selectedClinic = selectedClinic,
                    appointmentDate = appointmentDate,
                    onDateChange = { appointmentDate = it },
                    appointmentTime = appointmentTime,
                    onTimeChange = { appointmentTime = it },
                    servicesNeeded = servicesNeeded,
                    onServicesChange = { servicesNeeded = it },
                    notes = notes,
                    onNotesChange = { notes = it }
                )

                1 -> ChildcareStep(
                    needsChildcare = needsChildcare,
                    onNeedsChildcareChange = { needsChildcare = it },
                    numberOfChildren = numberOfChildren,
                    onNumberOfChildrenChange = { numberOfChildren = it },
                    childcareType = childcareType,
                    onChildcareTypeChange = { childcareType = it }
                )

                2 -> OutboundTravelStep(
                    departureLocation = departureLocation,
                    onDepartureLocationChange = { departureLocation = it },
                    transportType = outboundTransportType,
                    onTransportTypeChange = { outboundTransportType = it }
                )

                3 -> RecoveryHousingStep(
                    needsRecoveryHousing = needsRecoveryHousing,
                    onNeedsHousingChange = { needsRecoveryHousing = it },
                    recoveryDuration = recoveryDuration,
                    onDurationChange = { recoveryDuration = it }
                )

                4 -> ReturnTravelStep(
                    transportType = returnTransportType,
                    onTransportTypeChange = { returnTransportType = it }
                )

                5 -> FinancialSupportStep(
                    needsFinancialAssistance = needsFinancialAssistance,
                    onNeedsAssistanceChange = { needsFinancialAssistance = it },
                    needsAccompaniment = needsAccompaniment,
                    onNeedsAccompanimentChange = { needsAccompaniment = it }
                )

                6 -> PrivacySettingsStep(
                    autoDeleteAfterCompletion = autoDeleteAfterCompletion,
                    onAutoDeleteChange = { autoDeleteAfterCompletion = it },
                    useStealthMode = useStealthMode,
                    onStealthModeChange = { useStealthMode = it }
                )
            }
        }
    }
}

@Composable
fun AppointmentStep(
    selectedClinic: LegalResource?,
    appointmentDate: String,
    onDateChange: (String) -> Unit,
    appointmentTime: String,
    onTimeChange: (String) -> Unit,
    servicesNeeded: Set<String>,
    onServicesChange: (Set<String>) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Healthcare Appointment",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Selected clinic info
        if (selectedClinic != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            selectedClinic.organizationName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${selectedClinic.city}, ${selectedClinic.state}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Appointment date
        OutlinedTextField(
            value = appointmentDate,
            onValueChange = onDateChange,
            label = { Text("Appointment Date") },
            placeholder = { Text("MM/DD/YYYY") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
        )

        // Appointment time
        OutlinedTextField(
            value = appointmentTime,
            onValueChange = onTimeChange,
            label = { Text("Appointment Time") },
            placeholder = { Text("10:00 AM") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
        )

        // Services needed
        Text(
            "Services Needed",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        val serviceOptions = listOf("Consultation", "Medication", "Procedure", "Follow-up Care")
        serviceOptions.forEach { service ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = servicesNeeded.contains(service),
                    onCheckedChange = { checked ->
                        val updated = if (checked) {
                            servicesNeeded + service
                        } else {
                            servicesNeeded - service
                        }
                        onServicesChange(updated)
                    }
                )
                Text(service)
            }
        }

        // Private notes
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Private Notes (Encrypted)") },
            placeholder = { Text("Any additional details...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    "All appointment details are encrypted before storage",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun ChildcareStep(
    needsChildcare: Boolean,
    onNeedsChildcareChange: (Boolean) -> Unit,
    numberOfChildren: Int,
    onNumberOfChildrenChange: (Int) -> Unit,
    childcareType: String,
    onChildcareTypeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Childcare Arrangements",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = needsChildcare,
                onCheckedChange = onNeedsChildcareChange
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("I need childcare during this journey")
        }

        if (needsChildcare) {
            Text(
                "How many children?",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (numberOfChildren > 0) onNumberOfChildrenChange(numberOfChildren - 1) }
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }

                Text(
                    numberOfChildren.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                OutlinedButton(
                    onClick = { onNumberOfChildrenChange(numberOfChildren + 1) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }

            Text(
                "When do you need childcare?",
                style = MaterialTheme.typography.titleMedium
            )

            val childcareOptions = listOf(
                "during_appointment" to "During appointment only",
                "during_recovery" to "During recovery period",
                "full_journey" to "Entire journey"
            )

            childcareOptions.forEach { (value, label) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = childcareType == value,
                        onClick = { onChildcareTypeChange(value) }
                    )
                    Text(label)
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "SafeHaven will help you find trusted childcare providers who specialize in supporting survivors.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun OutboundTravelStep(
    departureLocation: String,
    onDepartureLocationChange: (String) -> Unit,
    transportType: String,
    onTransportTypeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Outbound Travel",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = departureLocation,
            onValueChange = onDepartureLocationChange,
            label = { Text("Departure City/State (Encrypted)") },
            placeholder = { Text("City, State") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
        )

        Text(
            "Transportation Type",
            style = MaterialTheme.typography.titleMedium
        )

        val transportOptions = listOf(
            "flight" to "Flight",
            "bus" to "Bus (Greyhound Home Free partner available)",
            "car" to "Personal vehicle",
            "volunteer_driver" to "Volunteer driver",
            "other" to "Other"
        )

        transportOptions.forEach { (value, label) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = transportType == value,
                    onClick = { onTransportTypeChange(value) }
                )
                Text(label)
            }
        }
    }
}

@Composable
fun RecoveryHousingStep(
    needsRecoveryHousing: Boolean,
    onNeedsHousingChange: (Boolean) -> Unit,
    recoveryDuration: String,
    onDurationChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Recovery Housing",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = needsRecoveryHousing,
                onCheckedChange = onNeedsHousingChange
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("I need recovery housing")
                Text(
                    "Safe, confidential housing during recovery",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (needsRecoveryHousing) {
            Text(
                "Expected recovery duration",
                style = MaterialTheme.typography.titleMedium
            )

            val durationOptions = listOf(
                "1-2 days",
                "3-5 days",
                "1 week",
                "Flexible"
            )

            durationOptions.forEach { duration ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = recoveryDuration == duration,
                        onClick = { onDurationChange(duration) }
                    )
                    Text(duration)
                }
            }
        }
    }
}

@Composable
fun ReturnTravelStep(
    transportType: String,
    onTransportTypeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Return Travel",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text("How will you return home?")

        val transportOptions = listOf(
            "flight" to "Flight",
            "bus" to "Bus",
            "car" to "Personal vehicle",
            "volunteer_driver" to "Volunteer driver",
            "same_as_outbound" to "Same as outbound travel",
            "other" to "Other"
        )

        transportOptions.forEach { (value, label) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = transportType == value,
                    onClick = { onTransportTypeChange(value) }
                )
                Text(label)
            }
        }
    }
}

@Composable
fun FinancialSupportStep(
    needsFinancialAssistance: Boolean,
    onNeedsAssistanceChange: (Boolean) -> Unit,
    needsAccompaniment: Boolean,
    onNeedsAccompanimentChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Additional Support",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = needsFinancialAssistance,
                        onCheckedChange = onNeedsAssistanceChange
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "I need financial assistance",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "For procedure, travel, lodging, or childcare costs",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        if (needsFinancialAssistance) {
            Card {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(
                        "SafeHaven will connect you with abortion funds and assistance programs.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = needsAccompaniment,
                        onCheckedChange = onNeedsAccompanimentChange
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "I want accompaniment services",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Volunteer support for safety during travel and appointment",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacySettingsStep(
    autoDeleteAfterCompletion: Boolean,
    onAutoDeleteChange: (Boolean) -> Unit,
    useStealthMode: Boolean,
    onStealthModeChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Privacy Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = autoDeleteAfterCompletion,
                        onCheckedChange = onAutoDeleteChange
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Auto-delete after completion",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Journey will be automatically deleted 30 days after completion",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = useStealthMode,
                        onCheckedChange = onStealthModeChange
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Stealth mode",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Hide journey from main screen (access via special code)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Card(
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
                        "Privacy Protections",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• All sensitive data is AES-256 encrypted\n" +
                                "• No location tracking to/from facilities\n" +
                                "• Journey can be quickly deleted\n" +
                                "• No data shared with third parties",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
