package app.neurothrive.safehaven.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.neurothrive.safehaven.data.session.UserSession
import app.neurothrive.safehaven.domain.usecases.IntersectionalResourceMatcher
import app.neurothrive.safehaven.ui.components.EmptyState
import app.neurothrive.safehaven.ui.components.SafeHavenTopBar
import app.neurothrive.safehaven.ui.viewmodels.ResourceFinderViewModel

/**
 * Resource Finder Screen
 * Search and filter legal resources (shelters, legal aid, hotlines)
 *
 * CRITICAL: Uses intersectional matching algorithm
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceFinderScreen(
    viewModel: ResourceFinderViewModel = hiltViewModel(),
    userSession: UserSession = hiltViewModel(),
    onBack: () -> Unit,
    onResourceClick: (String) -> Unit
) {
    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val resources by viewModel.resources.collectAsState()
    val currentUserId by userSession.currentUserId.collectAsState(initial = null)
    val selectedType by viewModel.selectedType.collectAsState()

    // Load survivor profile
    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            viewModel.loadSurvivorProfile(userId)
        }
    }

    // Search resources when type changes
    LaunchedEffect(selectedType) {
        viewModel.searchResources(selectedType)
    }

    Scaffold(
        topBar = {
            SafeHavenTopBar(
                title = "Find Resources (${resources.size})",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Resource Type Tabs
            ScrollableTabRow(
                selectedTabIndex = viewModel.getResourceTypes().indexOfFirst { it.id == selectedType }
            ) {
                viewModel.getResourceTypes().forEach { type ->
                    Tab(
                        selected = selectedType == type.id,
                        onClick = { viewModel.searchResources(type.id) },
                        text = { Text(type.name) }
                    )
                }
            }

            // Profile status indicator
            if (!uiState.hasProfile) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "💡 Complete your profile to see personalized resource recommendations",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Loading/Error/Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    // Loading state
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Error state
                    uiState.error != null -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error: ${uiState.error}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.searchResources(selectedType) }) {
                                Text("Retry")
                            }
                        }
                    }

                    // Empty state
                    resources.isEmpty() -> {
                        EmptyState(
                            message = "No ${selectedType.replace("_", " ")} resources found.\nTry a different category."
                        )
                    }

                    // Success - show resources
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(resources) { scoredResource ->
                                ResourceCard(
                                    resource = scoredResource,
                                    onClick = { onResourceClick(scoredResource.resource.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourceCard(
    resource: IntersectionalResourceMatcher.ScoredResource,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Resource name
                Text(
                    text = resource.resource.organizationName,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Location
                resource.resource.city?.let { city ->
                    Text(
                        text = "$city, ${resource.resource.state}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Distance if available
                if (resource.distance < Double.MAX_VALUE) {
                    Text(
                        text = "${String.format("%.1f", resource.distance)} miles away",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Match score indicator
                if (resource.score > 20.0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "⭐ Recommended for you",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Key features
                Spacer(modifier = Modifier.height(4.dp))
                val features = mutableListOf<String>()
                if (resource.resource.isFree) features.add("Free")
                if (resource.resource.is24_7) features.add("24/7")
                if (resource.resource.lgbtqSpecialized) features.add("LGBTQ+ Specialized")
                if (resource.resource.bipocLed) features.add("BIPOC-Led")
                if (features.isNotEmpty()) {
                    Text(
                        text = features.joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Contact icon
            Icon(
                Icons.Default.Phone,
                "View Details",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
