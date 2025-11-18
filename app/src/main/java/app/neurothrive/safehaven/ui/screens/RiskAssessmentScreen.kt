package app.neurothrive.safehaven.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.neurothrive.safehaven.data.database.entities.DetectedPattern
import app.neurothrive.safehaven.data.database.entities.RiskAssessment
import app.neurothrive.safehaven.data.database.entities.RiskLevel
import app.neurothrive.safehaven.ui.viewmodels.RiskAssessmentViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Risk Assessment Screen
 *
 * Displays AI-powered risk analysis and crisis detection
 *
 * Features:
 * - Latest risk assessment with AI summary
 * - Risk level visualization (low/moderate/high/critical)
 * - Detected dangerous patterns
 * - Personalized safety recommendations
 * - Crisis resources (24/7 hotlines)
 * - Lethality indicators
 * - Assessment history
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskAssessmentScreen(
    viewModel: RiskAssessmentViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToSafetyPlan: () -> Unit = {},
    onNavigateToEmergencyContacts: () -> Unit = {}
) {
    val latestAssessment by viewModel.latestAssessment.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Risk Assessment") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.createAssessment() }) {
                        Icon(Icons.Default.Refresh, "New Assessment")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Error Message
                    errorMessage?.let { error ->
                        item {
                            ErrorBanner(
                                message = error,
                                onDismiss = { viewModel.clearError() }
                            )
                        }
                    }

                    // Latest Assessment or Empty State
                    if (latestAssessment != null) {
                        val assessment = latestAssessment!!

                        // Risk Level Card
                        item {
                            RiskLevelCard(assessment)
                        }

                        // AI Summary
                        item {
                            AISummaryCard(assessment)
                        }

                        // Lethality Indicators (if present)
                        if (assessment.hasAnyLethalityIndicators()) {
                            item {
                                LethalityIndicatorsCard(assessment)
                            }
                        }

                        // Detected Patterns
                        if (assessment.detectedPatterns.isNotBlank()) {
                            item {
                                DetectedPatternsCard(assessment)
                            }
                        }

                        // Safety Recommendations
                        item {
                            SafetyRecommendationsCard(
                                assessment = assessment,
                                onNavigateToSafetyPlan = onNavigateToSafetyPlan,
                                onNavigateToEmergencyContacts = onNavigateToEmergencyContacts
                            )
                        }

                        // Crisis Resources
                        item {
                            CrisisResourcesCard(assessment)
                        }

                        // Risk Scores Breakdown
                        item {
                            RiskScoresCard(assessment)
                        }

                        // Assessment Metadata
                        item {
                            AssessmentMetadataCard(assessment, viewModel)
                        }

                    } else {
                        item {
                            EmptyStateCard(onCreateAssessment = { viewModel.createAssessment() })
                        }
                    }

                    // Info Footer
                    item {
                        InfoFooter()
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, "Dismiss")
            }
        }
    }
}

@Composable
private fun RiskLevelCard(assessment: RiskAssessment) {
    val riskColor = Color(android.graphics.Color.parseColor(assessment.riskLevel.toColorHex()))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = riskColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Current Risk Level",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(riskColor, shape = RoundedCornerShape(60.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${assessment.overallRiskScore}",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "/ 100",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = assessment.riskLevel.toDisplayString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = riskColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when (assessment.riskLevel) {
                    RiskLevel.CRITICAL -> "⚠️ IMMEDIATE DANGER - Seek help now"
                    RiskLevel.HIGH -> "📈 Elevated risk - Take action"
                    RiskLevel.MODERATE -> "⚠️ Monitor closely"
                    RiskLevel.LOW -> "✅ Standard safety planning"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AISummaryCard(assessment: RiskAssessment) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "AI Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = assessment.aiSummary,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun LethalityIndicatorsCard(assessment: RiskAssessment) {
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
                    text = "High-Risk Indicators Present",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (assessment.strangulationAttempted) {
                LethalityIndicatorItem(
                    color = Color(0xFFD32F2F),
                    text = "Strangulation Attempted",
                    description = "Research shows this significantly increases risk"
                )
            }

            if (assessment.threatsToKill) {
                LethalityIndicatorItem(
                    color = Color(0xFFC62828),
                    text = "Threats Made",
                    description = "Explicit threats to harm you or children"
                )
            }

            if (assessment.threatenedWithWeapon || assessment.hasWeapons) {
                LethalityIndicatorItem(
                    color = Color(0xFFB71C1C),
                    text = "Weapons Present",
                    description = "Access to or use of weapons documented"
                )
            }

            if (assessment.extremelyJealous) {
                LethalityIndicatorItem(
                    color = Color(0xFFE64A19),
                    text = "Controlling Behavior",
                    description = "Extreme jealousy and isolation tactics"
                )
            }

            if (assessment.recentSeparation) {
                LethalityIndicatorItem(
                    color = Color(0xFFD84315),
                    text = "Recent Separation",
                    description = "Separation attempts increase risk"
                )
            }
        }
    }
}

@Composable
private fun LethalityIndicatorItem(color: Color, text: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, shape = androidx.compose.foundation.shape.CircleShape)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun DetectedPatternsCard(assessment: RiskAssessment) {
    val patterns = DetectedPattern.parsePatterns(assessment.detectedPatterns)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Detected Patterns",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            patterns.forEach { pattern ->
                PatternItem(pattern)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PatternItem(pattern: DetectedPattern) {
    val patternColor = Color(android.graphics.Color.parseColor(pattern.getColorHex()))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(patternColor, shape = androidx.compose.foundation.shape.CircleShape)
                .padding(top = 6.dp)
        )
        Column {
            Text(
                text = pattern.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = pattern.getDescription(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SafetyRecommendationsCard(
    assessment: RiskAssessment,
    onNavigateToSafetyPlan: () -> Unit,
    onNavigateToEmergencyContacts: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Safety Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = assessment.recommendedActions,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onNavigateToSafetyPlan,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Safety Plan", style = MaterialTheme.typography.bodySmall)
                }

                OutlinedButton(
                    onClick = onNavigateToEmergencyContacts,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Contacts", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun CrisisResourcesCard(assessment: RiskAssessment) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "24/7 Crisis Resources",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = assessment.crisisResources,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun RiskScoresCard(assessment: RiskAssessment) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Risk Score Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            RiskScoreItem("Lethality Risk", assessment.lethalityRiskScore)
            RiskScoreItem("Escalation Risk", assessment.escalationRiskScore)
            RiskScoreItem("Frequency Risk", assessment.frequencyRiskScore)
            if (assessment.isolationRiskScore > 0) {
                RiskScoreItem("Isolation Risk", assessment.isolationRiskScore)
            }
        }
    }
}

@Composable
private fun RiskScoreItem(label: String, score: Int) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$score / 100",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        LinearProgressIndicator(
            progress = score / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = when {
                score >= 76 -> Color(android.graphics.Color.parseColor(RiskLevel.CRITICAL.toColorHex()))
                score >= 51 -> Color(android.graphics.Color.parseColor(RiskLevel.HIGH.toColorHex()))
                score >= 26 -> Color(android.graphics.Color.parseColor(RiskLevel.MODERATE.toColorHex()))
                else -> Color(android.graphics.Color.parseColor(RiskLevel.LOW.toColorHex()))
            }
        )
    }
}

@Composable
private fun AssessmentMetadataCard(assessment: RiskAssessment, viewModel: RiskAssessmentViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Assessment Information",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            MetadataRow("Assessment ID", assessment.assessmentId)
            MetadataRow("Date", formatDate(assessment.assessmentDate))
            MetadataRow("Incidents Analyzed", assessment.totalIncidentsAnalyzed.toString())

            if (!assessment.userAcknowledged) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.acknowledgeAssessment(assessment.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mark as Reviewed")
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyStateCard(onCreateAssessment: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Assessment,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Risk Assessment Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create your first AI-powered risk assessment to identify danger patterns and get personalized safety recommendations.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onCreateAssessment) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Assessment")
            }
        }
    }
}

@Composable
private fun InfoFooter() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "About Risk Assessments",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Powered by AI trained on the Danger Assessment Tool (Jacquelyn Campbell, Johns Hopkins) and Lethality Screen research. Assessments are encrypted and never shared without your consent.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                    )
                }
            }
        }
    }
}

private fun RiskAssessment.hasAnyLethalityIndicators(): Boolean {
    return strangulationAttempted || threatsToKill || threatenedWithWeapon ||
            hasWeapons || extremelyJealous || recentSeparation
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.US)
    return sdf.format(Date(timestamp))
}
