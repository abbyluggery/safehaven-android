package app.neurothrive.safehaven.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import app.neurothrive.safehaven.data.database.dao.*
import app.neurothrive.safehaven.data.database.entities.*

/**
 * SafeHaven Room Database
 *
 * CRITICAL SECURITY:
 * - All sensitive fields encrypted before storage
 * - No cloud backup by default
 * - Supports secure deletion (cascade)
 *
 * Entities:
 * 1. SafeHavenProfile - User settings + identity
 * 2. IncidentReport - Abuse documentation
 * 3. VerifiedDocument - Cryptographic verification
 * 4. EvidenceItem - Encrypted photos/videos/audio
 * 5. LegalResource - Intersectional resource database
 * 6. SurvivorProfile - Extended demographic data
 */
@Database(
    entities = [
        SafeHavenProfile::class,
        IncidentReport::class,
        VerifiedDocument::class,
        EvidenceItem::class,
        LegalResource::class,
        SurvivorProfile::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    // DAOs
    abstract fun safeHavenProfileDao(): SafeHavenProfileDao
    abstract fun incidentReportDao(): IncidentReportDao
    abstract fun verifiedDocumentDao(): VerifiedDocumentDao
    abstract fun evidenceItemDao(): EvidenceItemDao
    abstract fun legalResourceDao(): LegalResourceDao
    abstract fun survivorProfileDao(): SurvivorProfileDao

    companion object {
        const val DATABASE_NAME = "safehaven_db"
    }
}
