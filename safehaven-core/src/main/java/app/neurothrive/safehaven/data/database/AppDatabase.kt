package app.neurothrive.safehaven.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import app.neurothrive.safehaven.data.database.dao.*
import app.neurothrive.safehaven.data.database.entities.*

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
