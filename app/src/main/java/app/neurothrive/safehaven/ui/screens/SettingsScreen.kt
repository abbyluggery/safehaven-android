package app.neurothrive.safehaven.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.neurothrive.safehaven.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val profile by viewModel.getProfileState("current_user_id").collectAsState()

    var gpsEnabled by remember { mutableStateOf(profile?.gpsEnabled ?: false) }
    var shakeSensitivity by remember { mutableStateOf(profile?.shakeSensitivity ?: 2.5f) }

    // Intersectional Identity
    var isLGBTQIA by remember { mutableStateOf(false) }
    var isTrans by remember { mutableStateOf(false) }
    var isBIPOC by remember { mutableStateOf(false) }
    var isMaleIdentifying by remember { mutableStateOf(false) }
    var isUndocumented by remember { mutableStateOf(false) }
    var hasDisability by remember { mutableStateOf(false) }
    var primaryLanguage by remember { mutableStateOf("English") }

    var showSaveSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
            // Security Settings Section
            Text(
                text = "Security Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // GPS Setting
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable GPS",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Default: OFF for safety. Only enable if you explicitly want location data.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = gpsEnabled,
                            onCheckedChange = {
                                gpsEnabled = it
                                viewModel.updateGPSSetting(
                                    userId = "current_user_id",
                                    gpsEnabled = it,
                                    onSuccess = { showSaveSuccess = true },
                                    onError = { }
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Panic Delete Sensitivity
                    Text(
                        text = "Panic Delete Sensitivity",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Shake detection threshold (lower = more sensitive)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = shakeSensitivity,
                        onValueChange = { shakeSensitivity = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        onValueChangeFinished = {
                            viewModel.updatePanicDeleteSensitivity(
                                userId = "current_user_id",
                                shakeSensitivity = shakeSensitivity,
                                onSuccess = { showSaveSuccess = true },
                                onError = { }
                            )
                        }
                    )
                    Text(
                        text = when {
                            shakeSensitivity < 2f -> "Very Sensitive"
                            shakeSensitivity < 3f -> "Sensitive"
                            shakeSensitivity < 4f -> "Moderate"
                            else -> "Less Sensitive"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Intersectional Identity Profile
            Text(
                text = "Intersectional Identity Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This information helps match you with culturally competent resources. All data is encrypted and never shared.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    IdentityCheckbox(
                        label = "LGBTQIA+",
                        description = "Prioritizes LGBTQIA+-affirming resources",
                        checked = isLGBTQIA,
                        onCheckedChange = { isLGBTQIA = it }
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    IdentityCheckbox(
                        label = "Trans/Non-binary",
                        description = "Prioritizes trans-inclusive shelters and services",
                        checked = isTrans,
                        onCheckedChange = { isTrans = it }
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    IdentityCheckbox(
                        label = "BIPOC (Black, Indigenous, Person of Color)",
                        description = "Prioritizes BIPOC-led and culturally specific resources",
                        checked = isBIPOC,
                        onCheckedChange = { isBIPOC = it }
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    IdentityCheckbox(
                        label = "Male-Identifying",
                        description = "Few resources exist - prioritizes male-serving programs",
                        checked = isMaleIdentifying,
                        onCheckedChange = { isMaleIdentifying = it }
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    IdentityCheckbox(
                        label = "Undocumented",
                        description = "Prioritizes U-Visa support and immigration-safe resources",
                        checked = isUndocumented,
                        onCheckedChange = { isUndocumented = it }
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    IdentityCheckbox(
                        label = "Disability",
                        description = "Prioritizes accessible facilities and services",
                        checked = hasDisability,
                        onCheckedChange = { hasDisability = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Language
            OutlinedTextField(
                value = primaryLanguage,
                onValueChange = { primaryLanguage = it },
                label = { Text("Primary Language") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Identity Profile Button
            Button(
                onClick = {
                    viewModel.updateSurvivorProfile(
                        userId = "current_user_id",
                        isLGBTQIA = isLGBTQIA,
                        isTrans = isTrans,
                        isBIPOC = isBIPOC,
                        isMaleIdentifying = isMaleIdentifying,
                        isUndocumented = isUndocumented,
                        hasDisability = hasDisability,
                        primaryLanguage = primaryLanguage,
                        onSuccess = { showSaveSuccess = true },
                        onError = { }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Identity Profile")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "About SafeHaven",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A domestic violence safety planning app designed to center marginalized survivors.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    // Success Snackbar
    if (showSaveSuccess) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showSaveSuccess = false
        }
    }
}

@Composable
fun IdentityCheckbox(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
