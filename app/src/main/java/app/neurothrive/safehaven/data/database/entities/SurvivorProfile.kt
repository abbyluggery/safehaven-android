package app.neurothrive.safehaven.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Survivor Profile Entity
 * Purpose: Extended intersectional identity and preferences for resource matching
 *
 * EXPANDED FIELDS (NEW):
 * - Dependent care needs: dependent adults, pets, childcare
 * - Vulnerable populations: pregnant, substance use, teen dating, elder abuse, trafficking, TBI, criminal record
 *
 * Note: This extends SafeHavenProfile with additional demographic details
 * used for more refined resource matching and survivor support. The new fields
 * ensure we can match survivors with resources that accept their dependents and
 * serve often-excluded populations.
 */
@Entity(
    tableName = "survivor_profiles",
    foreignKeys = [ForeignKey(
        entity = SafeHavenProfile::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class SurvivorProfile(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val userId: String,

    // Additional demographic details
    val ageRange: String? = null,  // 18-24, 25-34, etc. for privacy
    val hasChildren: Boolean = false,
    val numberOfChildren: Int = 0,
    val isPrimaryCaregiver: Boolean = false,

    // Dependent care needs (NEW - CRITICAL)
    val hasDependentAdults: Boolean = false,  // Elderly parents, adult disabled children
    val hasPets: Boolean = false,  // 50% of survivors delay leaving without pet option
    val needsChildcare: Boolean = false,

    // Vulnerable population identifiers (NEW - CRITICAL)
    val isPregnant: Boolean = false,
    val hasSubstanceUse: Boolean = false,  // Substance use disorder
    val isTeenDating: Boolean = false,  // Teen dating violence (minor)
    val isElderAbuse: Boolean = false,  // Elder abuse victim (60+)
    val isTraffickingSurvivor: Boolean = false,  // Human trafficking survivor
    val hasTBI: Boolean = false,  // Traumatic brain injury
    val hasCriminalRecord: Boolean = false,  // Criminal record (often from coerced crimes)

    // Economic status
    val employmentStatus: String? = null,
    val hasTransportation: Boolean = false,

    // Support network
    val hasSupportNetwork: Boolean = false,
    val canRelocate: Boolean = false,

    // Preferences
    val preferredContactMethod: String = "app",  // app, email, phone
    val shareDataForResearch: Boolean = false,
    val allowAnonymousStorySharing: Boolean = false,

    // Safety
    val currentlySafe: Boolean = false,
    val needsImmediateShelter: Boolean = false,
    val hasActivePPO: Boolean = false,  // Protection order

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
