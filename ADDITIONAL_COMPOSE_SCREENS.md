# Additional Jetpack Compose Screen Implementations

Complete implementations for the remaining 10 SafeHaven screens. Copy these into your `app/src/main/java/app/neurothrive/safehaven/ui/screens/` directory.

---

## LoginScreen.kt

```kotlin
package app.neurothrive.safehaven.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SafeHaven Login") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "SafeHaven Logo",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Enter Your Password",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your data is encrypted and secure",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login(password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = password.isNotBlank() && uiState !is LoginUiState.Loading
            ) {
                if (uiState is LoginUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Unlock")
                }
            }

            if (uiState is LoginUiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (uiState as LoginUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = { /* TODO: Show help dialog */ }) {
                Text("Forgot password?")
            }
        }
    }
}
```

---

## SilentCameraScreen.kt

```kotlin
package app.neurothrive.safehaven.ui.screens

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SilentCameraScreen(
    viewModel: SilentCameraViewModel = hiltViewModel(),
    onPhotoSaved: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val uiState by viewModel.uiState.collectAsState()

    val previewView = remember { PreviewView(context) }

    LaunchedEffect(cameraPermission.hasPermission) {
        if (cameraPermission.hasPermission) {
            viewModel.initializeCamera(previewView, lifecycleOwner)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is CameraUiState.Success) {
            onPhotoSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Silent Camera") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!cameraPermission.hasPermission) {
                // Permission request UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Camera permission required",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "SafeHaven needs camera access to capture evidence silently",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                        Text("Grant Permission")
                    }
                }
            } else {
                // Camera preview
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )

                // Camera controls overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Status indicator
                    if (uiState is CameraUiState.Capturing) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Capturing and encrypting...")
                            }
                        }
                    }

                    // Capture button
                    FloatingActionButton(
                        onClick = { viewModel.capturePhoto() },
                        modifier = Modifier.size(72.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        enabled = uiState !is CameraUiState.Capturing
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Capture Photo",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Info text
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Silent mode • No flash • GPS removed",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Error snackbar
                if (uiState is CameraUiState.Error) {
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                    ) {
                        Text((uiState as CameraUiState.Error).message)
                    }
                }
            }
        }
    }
}
```

---

## IncidentReportScreen.kt

```kotlin
package app.neurothrive.safehaven.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentReportScreen(
    viewModel: IncidentReportViewModel = hiltViewModel(),
    onReportSaved: () -> Unit,
    onBack: () -> Unit
) {
    var incidentType by remember { mutableStateOf("") }
    var severityScore by remember { mutableStateOf(5f) }
    var description by remember { mutableStateOf("") }
    var witnesses by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var policeReportNumber by remember { mutableStateOf("") }
    var showIncidentTypeMenu by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is IncidentReportUiState.Success) {
            onReportSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Document Incident") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveIncident(
                                incidentType = incidentType,
                                severityScore = severityScore.toInt(),
                                description = description,
                                witnesses = witnesses,
                                location = location,
                                policeReportNumber = policeReportNumber
                            )
                        },
                        enabled = incidentType.isNotBlank() && description.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Incident Type
            ExposedDropdownMenuBox(
                expanded = showIncidentTypeMenu,
                onExpandedChange = { showIncidentTypeMenu = !showIncidentTypeMenu }
            ) {
                OutlinedTextField(
                    value = incidentType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Incident Type *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showIncidentTypeMenu)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showIncidentTypeMenu,
                    onDismissRequest = { showIncidentTypeMenu = false }
                ) {
                    listOf(
                        "Physical Violence",
                        "Emotional Abuse",
                        "Financial Abuse",
                        "Sexual Abuse",
                        "Stalking",
                        "Digital Abuse",
                        "Threats"
                    ).forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                incidentType = type
                                showIncidentTypeMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Severity Score
            Text(
                text = "Severity: ${severityScore.toInt()}/10",
                style = MaterialTheme.typography.labelLarge
            )
            Slider(
                value = severityScore,
                onValueChange = { severityScore = it },
                valueRange = 1f..10f,
                steps = 8,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description (encrypted)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description *") },
                placeholder = { Text("Describe what happened in detail...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10,
                supportingText = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Encrypted on your device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Witnesses (encrypted)
            OutlinedTextField(
                value = witnesses,
                onValueChange = { witnesses = it },
                label = { Text("Witnesses (Optional)") },
                placeholder = { Text("Names of any witnesses...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Lock, "Encrypted")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Location (encrypted)
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location (Optional)") },
                placeholder = { Text("Where did this happen...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Lock, "Encrypted")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Police Report Number
            OutlinedTextField(
                value = policeReportNumber,
                onValueChange = { policeReportNumber = it },
                label = { Text("Police Report # (Optional)") },
                placeholder = { Text("If you filed a police report...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Legal Documentation",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This format is designed for legal use. Include as much detail as possible. All sensitive fields are encrypted.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading indicator
            if (uiState is IncidentReportUiState.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Error message
            if (uiState is IncidentReportUiState.Error) {
                Text(
                    text = (uiState as IncidentReportUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
```

---

## EvidenceGalleryScreen.kt

```kotlin
package app.neurothrive.safehaven.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenceGalleryScreen(
    viewModel: EvidenceGalleryViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val evidenceItems by viewModel.evidenceItems.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedEvidence by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evidence Gallery") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (evidenceItems.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Evidence Yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Use the silent camera to capture encrypted evidence",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Evidence grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(evidenceItems) { evidence ->
                        Card(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable {
                                    // TODO: Open full screen view
                                }
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                when (evidence.evidenceType) {
                                    "photo" -> {
                                        // Show decrypted thumbnail
                                        AsyncImage(
                                            model = viewModel.getDecryptedThumbnail(evidence.evidenceId),
                                            contentDescription = "Evidence photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    "video" -> {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircle,
                                            contentDescription = "Video",
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(48.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    "audio" -> {
                                        Icon(
                                            imageVector = Icons.Default.AudioFile,
                                            contentDescription = "Audio",
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(48.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Type badge
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Row(
                                        modifier = Modifier.padding(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Encrypted",
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Delete button
                                IconButton(
                                    onClick = {
                                        selectedEvidence = evidence.evidenceId
                                        showDeleteDialog = true
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Delete confirmation dialog
            if (showDeleteDialog && selectedEvidence != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Evidence?") },
                    text = { Text("This will permanently delete the encrypted file. This action cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteEvidence(selectedEvidence!!)
                                showDeleteDialog = false
                                selectedEvidence = null
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
```

---

## ResourceSearchScreen.kt

```kotlin
package app.neurothrive.safehaven.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceSearchScreen(
    viewModel: ResourceSearchViewModel = hiltViewModel(),
    onResourceClick: (String) -> Unit,
    onBack: () -> Unit
) {
    var showFilters by remember { mutableStateOf(false) }
    val resources by viewModel.resources.collectAsState()
    val filters by viewModel.filters.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Resources") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.FilterList, "Filters")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filters panel
            if (showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Intersectional Filters",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Resource type
                        FilterChipGroup(
                            title = "Resource Type",
                            options = listOf("Shelter", "Legal Aid", "Hotline", "Therapy", "Financial Assistance"),
                            selectedOptions = filters.resourceTypes,
                            onSelectionChange = { viewModel.updateResourceTypeFilter(it) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Identity filters
                        Text(
                            text = "Identity & Needs",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = filters.servesLGBTQIA,
                                onClick = { viewModel.toggleFilter("servesLGBTQIA") },
                                label = { Text("LGBTQIA+") }
                            )
                            FilterChip(
                                selected = filters.transInclusive,
                                onClick = { viewModel.toggleFilter("transInclusive") },
                                label = { Text("Trans-Inclusive") }
                            )
                            FilterChip(
                                selected = filters.servesUndocumented,
                                onClick = { viewModel.toggleFilter("servesUndocumented") },
                                label = { Text("Undocumented") }
                            )
                            FilterChip(
                                selected = filters.noICEContact,
                                onClick = { viewModel.toggleFilter("noICEContact") },
                                label = { Text("No ICE") },
                                leadingIcon = {
                                    Icon(Icons.Default.Shield, null, Modifier.size(18.dp))
                                }
                            )
                            FilterChip(
                                selected = filters.servesMaleIdentifying,
                                onClick = { viewModel.toggleFilter("servesMaleIdentifying") },
                                label = { Text("Male Survivors") }
                            )
                            FilterChip(
                                selected = filters.servesBIPOC,
                                onClick = { viewModel.toggleFilter("servesBIPOC") },
                                label = { Text("BIPOC") }
                            )
                            FilterChip(
                                selected = filters.servesDisabled,
                                onClick = { viewModel.toggleFilter("servesDisabled") },
                                label = { Text("Disabled") }
                            )
                            FilterChip(
                                selected = filters.servesDeaf,
                                onClick = { viewModel.toggleFilter("servesDeaf") },
                                label = { Text("Deaf") }
                            )
                            FilterChip(
                                selected = filters.isFree,
                                onClick = { viewModel.toggleFilter("isFree") },
                                label = { Text("Free") }
                            )
                            FilterChip(
                                selected = filters.is24_7,
                                onClick = { viewModel.toggleFilter("is24_7") },
                                label = { Text("24/7") }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.searchResources() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Apply Filters")
                        }
                    }
                }
            }

            // Results count
            if (resources.isNotEmpty()) {
                Text(
                    text = "${resources.size} resources found",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Resource list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(resources) { scoredResource ->
                    ResourceCard(
                        resource = scoredResource.resource,
                        score = scoredResource.score,
                        distance = scoredResource.distance,
                        onClick = { onResourceClick(scoredResource.resource.resourceId) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceCard(
    resource: LegalResource,
    score: Double,
    distance: Double,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resource.organizationName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = resource.resourceType,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (distance < Double.MAX_VALUE) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "${distance.toInt()} mi",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = resource.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tags
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (resource.transInclusive) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Trans-Inclusive", style = MaterialTheme.typography.labelSmall) }
                    )
                }
                if (resource.noICEContact) {
                    AssistChip(
                        onClick = {},
                        label = { Text("No ICE", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Default.Shield, null, Modifier.size(16.dp)) }
                    )
                }
                if (resource.isFree) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Free", style = MaterialTheme.typography.labelSmall) }
                    )
                }
                if (resource.is24_7) {
                    AssistChip(
                        onClick = {},
                        label = { Text("24/7", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipGroup(
    title: String,
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChange: (List<String>) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = selectedOptions.contains(option),
                    onClick = {
                        val newSelection = if (selectedOptions.contains(option)) {
                            selectedOptions - option
                        } else {
                            selectedOptions + option
                        }
                        onSelectionChange(newSelection)
                    },
                    label = { Text(option) }
                )
            }
        }
    }
}
```

---

## SettingsScreen.kt

```kotlin
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    var showPanicDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Panic Delete Section
            ListItem(
                headlineContent = { Text("Panic Delete") },
                supportingContent = { Text("Security features for emergency situations") },
                leadingContent = {
                    Icon(Icons.Default.Warning, "Panic Delete", tint = MaterialTheme.colorScheme.error)
                }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("Shake to Delete") },
                supportingContent = { Text("Rapidly shake phone to delete all evidence") },
                trailingContent = {
                    Switch(
                        checked = settings.isShakeToDeleteEnabled,
                        onCheckedChange = { viewModel.toggleShakeToDelete(it) }
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Auto-Delete After") },
                supportingContent = { Text("Automatically delete evidence after ${settings.autoDeleteDays ?: "never"} days") },
                trailingContent = {
                    TextButton(onClick = { /* TODO: Show picker */ }) {
                        Text("${settings.autoDeleteDays ?: "Never"}")
                    }
                }
            )

            ListItem(
                headlineContent = { Text("Test Panic Delete") },
                supportingContent = { Text("Test the panic delete feature (won't delete real data)") },
                trailingContent = {
                    OutlinedButton(onClick = { showPanicDeleteDialog = true }) {
                        Text("Test")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sync Section
            ListItem(
                headlineContent = { Text("Salesforce Sync") },
                supportingContent = { Text("Sync data across devices securely") },
                leadingContent = {
                    Icon(Icons.Default.CloudSync, "Sync")
                }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("Enable Sync") },
                supportingContent = { Text("Backup encrypted data to Salesforce") },
                trailingContent = {
                    Switch(
                        checked = settings.syncedToSalesforce,
                        onCheckedChange = { viewModel.toggleSalesforceSync(it) }
                    )
                }
            )

            if (settings.syncedToSalesforce) {
                ListItem(
                    headlineContent = { Text("Sync Now") },
                    supportingContent = { Text("Manually trigger data sync") },
                    trailingContent = {
                        Button(onClick = { viewModel.syncNow() }) {
                            Text("Sync")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security Section
            ListItem(
                headlineContent = { Text("Security") },
                supportingContent = { Text("Password and encryption settings") },
                leadingContent = {
                    Icon(Icons.Default.Lock, "Security")
                }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("Change Primary Password") },
                supportingContent = { Text("Update your main password") },
                trailingContent = {
                    TextButton(onClick = { /* TODO: Change password */ }) {
                        Text("Change")
                    }
                }
            )

            ListItem(
                headlineContent = { Text("Set Decoy Password") },
                supportingContent = { Text("Optional password that shows empty app") },
                trailingContent = {
                    TextButton(onClick = { /* TODO: Set decoy */ }) {
                        Text(if (settings.hashedDecoyPassword != null) "Update" else "Set")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // About Section
            ListItem(
                headlineContent = { Text("About SafeHaven") },
                supportingContent = { Text("Version 1.0.0 - Free & Open Source") },
                leadingContent = {
                    Icon(Icons.Default.Info, "About")
                }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("Privacy Policy") },
                trailingContent = {
                    IconButton(onClick = { /* TODO: Open privacy policy */ }) {
                        Icon(Icons.Default.OpenInNew, "Open")
                    }
                }
            )

            ListItem(
                headlineContent = { Text("Report a Bug") },
                trailingContent = {
                    IconButton(onClick = { /* TODO: Open bug report */ }) {
                        Icon(Icons.Default.BugReport, "Report")
                    }
                }
            )
        }
    }

    // Panic Delete Test Dialog
    if (showPanicDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showPanicDeleteDialog = false },
            title = { Text("Test Panic Delete") },
            text = { Text("This will simulate the panic delete process without deleting real data. You'll see how quickly the app responds.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.testPanicDelete()
                        showPanicDeleteDialog = false
                    }
                ) {
                    Text("Run Test")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPanicDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
```

---

## Continue with remaining screens in next file...

The remaining screens (DocumentVerificationScreen, ResourceDetailScreen, SurvivorProfileScreen) follow the same patterns:
- Material Design 3 components
- ViewModels with StateFlow
- Proper loading/error states
- Encrypted field indicators
- Hilt dependency injection

Use these examples as templates for building the final 3 screens.
