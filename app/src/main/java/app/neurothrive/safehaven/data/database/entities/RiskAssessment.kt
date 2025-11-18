package app.neurothrive.safehaven.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Risk Assessment Entity
 *
 * Stores AI-generated risk assessments from Salesforce
 * Based on Danger Assessment Tool (Jacquelyn Campbell, Johns Hopkins)
 *
 * Features:
 * - AI-powered pattern detection (12 dangerous patterns)
 * - Lethality scoring (0-100 scale)
 * - Crisis alerts for critical risk cases
 * - Personalized safety recommendations
 * - Trauma-informed AI summaries
 */
@Entity(tableName = "risk_assessments")
data class RiskAssessment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // User identification
    val userId: String,

    // Salesforce IDs
    val assessmentId: String, // e.g., "RA-0000123"
    val survivorProfileId: String? = null,

    // Risk Scores (0-100)
    val overallRiskScore: Int, // Combined risk score
    val lethalityRiskScore: Int, // Risk of lethal violence
    val escalationRiskScore: Int, // Pattern escalation
    val frequencyRiskScore: Int, // Incident frequency
    val isolationRiskScore: Int = 0, // Social isolation

    // Risk Level (Categorical)
    val riskLevel: RiskLevel,

    // AI-Generated Content
    val aiSummary: String, // 2-3 sentence compassionate summary
    val recommendedActions: String, // 3-5 specific safety recommendations
    val crisisResources: String, // 24/7 hotline list

    // Pattern Detection (comma-separated)
    val detectedPatterns: String, // e.g., "escalating_severity,weapons_involved,threats_to_kill"

    // Lethality Indicators (Danger Assessment Tool)
    val hasWeapons: Boolean = false,
    val threatenedWithWeapon: Boolean = false,
    val strangulationAttempted: Boolean = false, // 750% higher homicide risk
    val threatsToKill: Boolean = false,
    val extremelyJealous: Boolean = false,
    val recentSeparation: Boolean = false,

    // Incident Statistics
    val totalIncidentsAnalyzed: Int = 0,
    val incidentsLast30Days: Int = 0,
    val incidentsLast90Days: Int = 0,
    val daysSinceLastIncident: Int? = null,
    val averageDaysBetweenIncidents: Double? = null,

    // Metadata
    val assessmentDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val syncedToSalesforce: Boolean = false,
    val lastSyncedAt: Long? = null,

    // Alerts
    val isCriticalRisk: Boolean = false, // Risk level = critical
    val alertSent: Boolean = false, // Crisis alert sent to contacts
    val alertSentAt: Long? = null,
    val userAcknowledged: Boolean = false, // User saw risk assessment
    val acknowledgedAt: Long? = null
)

/**
 * Risk Level Enum
 * Based on overall risk score
 */
enum class RiskLevel {
    LOW,        // 0-25: Standard safety planning
    MODERATE,   // 26-50: Monitor closely
    HIGH,       // 51-75: Elevated risk, proactive intervention
    CRITICAL;   // 76-100: Immediate danger, manual review required

    companion object {
        fun fromString(value: String): RiskLevel {
            return when (value.lowercase()) {
                "low" -> LOW
                "moderate" -> MODERATE
                "high" -> HIGH
                "critical" -> CRITICAL
                else -> LOW
            }
        }

        fun fromScore(score: Int): RiskLevel {
            return when {
                score >= 76 -> CRITICAL
                score >= 51 -> HIGH
                score >= 26 -> MODERATE
                else -> LOW
            }
        }
    }

    fun toDisplayString(): String {
        return when (this) {
            LOW -> "Low Risk"
            MODERATE -> "Moderate Risk"
            HIGH -> "High Risk"
            CRITICAL -> "Critical Risk"
        }
    }

    fun toColorHex(): String {
        return when (this) {
            LOW -> "#4CAF50"      // Green
            MODERATE -> "#FF9800" // Orange
            HIGH -> "#FF5722"     // Deep Orange
            CRITICAL -> "#F44336" // Red
        }
    }
}

/**
 * Detected Pattern Enum
 * 12 dangerous patterns identified by AI
 */
enum class DetectedPattern(val value: String, val displayName: String) {
    INCREASING_FREQUENCY("increasing_frequency", "Increasing Frequency"),
    ESCALATING_SEVERITY("escalating_severity", "Escalating Severity"),
    WEAPONS_INVOLVED("weapons_involved", "Weapons Involved"),
    STRANGULATION_CHOKING("strangulation_choking", "Strangulation/Choking"),
    THREATS_TO_KILL("threats_to_kill", "Threats to Kill"),
    STALKING_BEHAVIOR("stalking_behavior", "Stalking Behavior"),
    CONTROLLING_BEHAVIOR("controlling_behavior", "Controlling Behavior"),
    SEXUAL_VIOLENCE("sexual_violence", "Sexual Violence"),
    VIOLENCE_WHILE_PREGNANT("violence_while_pregnant", "Violence While Pregnant"),
    SUBSTANCE_ABUSE_PRESENT("substance_abuse_present", "Substance Abuse Present"),
    SEPARATION_VIOLENCE("separation_violence", "Separation Violence"),
    MULTIPLE_INCIDENT_TYPES("multiple_incident_types", "Multiple Incident Types");

    companion object {
        fun fromString(value: String): DetectedPattern? {
            return values().find { it.value == value }
        }

        fun parsePatterns(patternsString: String): List<DetectedPattern> {
            if (patternsString.isBlank()) return emptyList()
            return patternsString.split(",")
                .mapNotNull { fromString(it.trim()) }
        }

        fun formatPatterns(patterns: List<DetectedPattern>): String {
            return patterns.joinToString(",") { it.value }
        }
    }

    fun getDescription(): String {
        return when (this) {
            INCREASING_FREQUENCY -> "Incidents are becoming more frequent over time"
            ESCALATING_SEVERITY -> "Violence is intensifying and becoming more severe"
            WEAPONS_INVOLVED -> "Weapons (firearms, knives) are present or used"
            STRANGULATION_CHOKING -> "Strangulation attempted - 750% higher homicide risk"
            THREATS_TO_KILL -> "Explicit threats to kill you or children"
            STALKING_BEHAVIOR -> "Persistent stalking, especially after separation"
            CONTROLLING_BEHAVIOR -> "Extreme jealousy and isolation tactics"
            SEXUAL_VIOLENCE -> "Sexual abuse or reproductive coercion present"
            VIOLENCE_WHILE_PREGNANT -> "Abuse during pregnancy or postpartum"
            SUBSTANCE_ABUSE_PRESENT -> "Perpetrator has substance abuse issues"
            SEPARATION_VIOLENCE -> "Violence during or after separation attempts"
            MULTIPLE_INCIDENT_TYPES -> "Multiple types of abuse (physical + financial + stalking)"
        }
    }

    fun getColorHex(): String {
        return when (this) {
            INCREASING_FREQUENCY -> "#FF6B6B"
            ESCALATING_SEVERITY -> "#EE5A24"
            WEAPONS_INVOLVED -> "#D32F2F"
            STRANGULATION_CHOKING -> "#C62828"
            THREATS_TO_KILL -> "#B71C1C"
            STALKING_BEHAVIOR -> "#E64A19"
            CONTROLLING_BEHAVIOR -> "#D84315"
            SEXUAL_VIOLENCE -> "#BF360C"
            VIOLENCE_WHILE_PREGNANT -> "#FF5722"
            SUBSTANCE_ABUSE_PRESENT -> "#F4511E"
            SEPARATION_VIOLENCE -> "#E53935"
            MULTIPLE_INCIDENT_TYPES -> "#FF7043"
        }
    }
}
