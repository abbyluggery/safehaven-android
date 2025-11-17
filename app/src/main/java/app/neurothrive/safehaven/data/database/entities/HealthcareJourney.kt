package app.neurothrive.safehaven.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Healthcare Journey Entity
 * Purpose: Track complete journey for accessing reproductive healthcare
 *
 * CRITICAL SAFETY FEATURES:
 * - All sensitive fields encrypted before storage
 * - No location tracking to/from medical facilities
 * - Automatic secure deletion after journey complete (optional)
 * - Extra privacy protection layer
 *
 * JOURNEY COMPONENTS:
 * 1. Healthcare appointment (clinic, date, services needed)
 * 2. Outbound travel (transportation, lodging)
 * 3. Childcare arrangements (during appointment and recovery)
 * 4. Recovery period (housing, duration, accompaniment)
 * 5. Return travel (transportation home)
 * 6. Financial assistance tracking
 *
 * DV SAFETY NOTE: This journey often represents a critical step toward safety.
 * Unwanted pregnancy dramatically increases DV risk. This tool helps survivors
 * safely access legal medical care without alerting their abuser.
 */
@Entity(
    tableName = "healthcare_journeys",
    foreignKeys = [
        ForeignKey(
            entity = SafeHavenProfile::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("journeyStatus"),
        Index("appointmentDate")
    ]
)
data class HealthcareJourney(
    @PrimaryKey
    val id: String,

    val userId: String,

    // Journey status
    val journeyStatus: String,  // "planning", "confirmed", "traveling_outbound", "at_appointment", "recovering", "traveling_return", "completed", "cancelled"
    val createdAt: Long,
    val lastUpdated: Long,

    // Healthcare Appointment (ENCRYPTED FIELDS)
    val clinicResourceId: String?,  // References LegalResource.id for the clinic
    val appointmentDateEncrypted: String?,  // AES-256 encrypted timestamp
    val appointmentTimeEncrypted: String?,  // AES-256 encrypted time
    val servicesNeededEncrypted: String?,  // AES-256 encrypted JSON array of services
    val appointmentNotesEncrypted: String? = null,  // AES-256 encrypted private notes

    // Childcare Needs
    val needsChildcare: Boolean = false,
    val numberOfChildren: Int = 0,
    val childAgesEncrypted: String? = null,  // AES-256 encrypted array: [2, 5, 8]
    val childcareType: String? = null,  // "during_appointment", "during_recovery", "full_journey"
    val childcareDuration: String? = null,  // "4 hours", "1 day", "3 days", etc.
    val childcareResourceId: String? = null,  // References LegalResource.id for childcare provider
    val childcareArranged: Boolean = false,
    val childcareNotesEncrypted: String? = null,  // AES-256 encrypted childcare details

    // Outbound Travel
    val departureLocationEncrypted: String? = null,  // AES-256 encrypted - where they're traveling FROM
    val departureDateEncrypted: String? = null,  // AES-256 encrypted
    val outboundTransportationType: String? = null,  // "flight", "bus", "car", "train", "volunteer_driver"
    val outboundTransportationResourceId: String? = null,  // References resource providing transport
    val outboundTransportationArranged: Boolean = false,
    val outboundTransportationNotesEncrypted: String? = null,  // AES-256 encrypted booking details

    // Recovery Period
    val needsRecoveryHousing: Boolean = false,
    val recoveryHousingResourceId: String? = null,  // References LegalResource.id for recovery housing
    val recoveryDuration: String? = null,  // "1-2 days", "3-5 days", etc.
    val recoveryHousingArranged: Boolean = false,
    val recoveryHousingNotesEncrypted: String? = null,  // AES-256 encrypted housing details

    // Accompaniment (for safety and support)
    val needsAccompaniment: Boolean = false,
    val accompanimentResourceId: String? = null,  // References resource providing accompaniment
    val accompanimentArranged: Boolean = false,
    val accompanimentNotesEncrypted: String? = null,  // AES-256 encrypted

    // Return Travel
    val returnDateEncrypted: String? = null,  // AES-256 encrypted
    val returnTransportationType: String? = null,  // "flight", "bus", "car", "train", "volunteer_driver"
    val returnTransportationResourceId: String? = null,
    val returnTransportationArranged: Boolean = false,
    val returnTransportationNotesEncrypted: String? = null,  // AES-256 encrypted

    // Financial Assistance
    val needsFinancialAssistance: Boolean = false,
    val financialNeedsEncrypted: String? = null,  // AES-256 encrypted JSON: {"procedure": 500, "travel": 200, "lodging": 150, "childcare": 100}
    val financialAssistanceResourceIds: String? = null,  // JSON array of resource IDs providing funding
    val financialAssistanceArranged: Boolean = false,
    val financialAssistanceNotesEncrypted: String? = null,  // AES-256 encrypted

    // Privacy & Safety Settings
    val autoDeleteAfterCompletion: Boolean = true,  // Auto-delete journey after 30 days of completion
    val autoDeleteDate: Long? = null,  // Timestamp when auto-delete will occur
    val disableLocationTracking: Boolean = true,  // Always true by default for safety
    val useStealthMode: Boolean = false,  // Hide journey from main screen, access via special code

    // Completion
    val completedAt: Long? = null,
    val cancellationReason: String? = null,  // If cancelled, optional reason
    val journeyNotesEncrypted: String? = null  // AES-256 encrypted - general journey notes
)
