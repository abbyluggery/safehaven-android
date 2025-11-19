package app.neurothrive.safehaven.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.neurothrive.safehaven.ui.viewmodels.IncidentReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentReportScreen(
    navController: NavController,
    viewModel: IncidentReportViewModel = hiltViewModel()
) {
    var description by remember { mutableStateOf("") }
    var witnesses by remember { mutableStateOf("") }
    var policeInvolved by remember { mutableStateOf(false) }
    var policeReportNumber by remember { mutableStateOf("") }
    var medicalAttention by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf("") }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Document Incident") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Save incident report
                            if (description.isBlank()) {
                                errorMessage = "Description is required"
                                showErrorDialog = true
                                return@IconButton
                            }

                            viewModel.saveIncidentReport(
                                userId = "current_user_id", // Replace with actual user ID
                                description = description,
                                witnesses = witnesses.takeIf { it.isNotBlank() },
                                policeInvolved = policeInvolved,
                                policeReportNumber = policeReportNumber.takeIf { it.isNotBlank() },
                                medicalAttention = medicalAttention,
                                location = location.takeIf { it.isNotBlank() },
                                linkedEvidenceIds = null,
                                onSuccess = {
                                    showSuccessDialog = true
                                },
                                onError = { error ->
                                    errorMessage = error.message ?: "Unknown error"
                                    showErrorDialog = true
                                }
                            )
                        }
                    ) {
                        Icon(Icons.Default.Save, "Save")
                    }
                }
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
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Legal-Formatted Report",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "All information is encrypted before saving. This report can be exported as a PDF for legal proceedings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description Field (Required)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description of Incident *") },
                placeholder = { Text("What happened? Include date, time, and details...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp),
                maxLines = 10,
                supportingText = { Text("Required - Will be encrypted") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Witnesses Field
            OutlinedTextField(
                value = witnesses,
                onValueChange = { witnesses = it },
                label = { Text("Witnesses (Optional)") },
                placeholder = { Text("Names and contact information of any witnesses...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Police Involved Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Police Involved?", modifier = Modifier.alignByBaseline())
                Switch(
                    checked = policeInvolved,
                    onCheckedChange = { policeInvolved = it }
                )
            }

            if (policeInvolved) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = policeReportNumber,
                    onValueChange = { policeReportNumber = it },
                    label = { Text("Police Report Number") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Medical Attention Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Medical Attention Received?", modifier = Modifier.alignByBaseline())
                Switch(
                    checked = medicalAttention,
                    onCheckedChange = { medicalAttention = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location Field
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location (Optional)") },
                placeholder = { Text("Where did this occur?") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("GPS is disabled by default for safety") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    if (description.isBlank()) {
                        errorMessage = "Description is required"
                        showErrorDialog = true
                        return@Button
                    }

                    viewModel.saveIncidentReport(
                        userId = "current_user_id",
                        description = description,
                        witnesses = witnesses.takeIf { it.isNotBlank() },
                        policeInvolved = policeInvolved,
                        policeReportNumber = policeReportNumber.takeIf { it.isNotBlank() },
                        medicalAttention = medicalAttention,
                        location = location.takeIf { it.isNotBlank() },
                        linkedEvidenceIds = null,
                        onSuccess = { showSuccessDialog = true },
                        onError = { error ->
                            errorMessage = error.message ?: "Unknown error"
                            showErrorDialog = true
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = description.isNotBlank()
            ) {
                Icon(Icons.Default.Save, "Save", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Encrypted Report")
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("✓ Report Saved") },
            text = { Text("Incident report has been encrypted and saved securely.") },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    navController.navigateUp()
                }) {
                    Text("OK")
                }
            }
        )
    }

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
