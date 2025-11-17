# SafeHaven Database Schema (Room)

**Database**: SQLite via Android Room  
**Encryption**: AES-256-GCM via SafeHavenCrypto  
**Version**: 1.0.0

---

## Schema Overview
```
AppDatabase
├── safehaven_profiles (user settings + identity)
├── incident_reports (abuse documentation)
├── verified_documents (SHA-256 hashes)
├── evidence_items (encrypted photos/videos/audio)
├── legal_resources (shelters, legal aid, hotlines)
└── survivor_profiles (detailed intersectional identity)
```

---

## Table 1: safehaven_profiles

**Purpose**: User authentication, settings, basic intersectional identity

### Schema
```sql
CREATE TABLE safehaven_profiles (
    userId TEXT PRIMARY KEY NOT NULL,
    
    -- Authentication
    safeHavenPasswordHash TEXT NOT NULL,
    duressPasswordHash TEXT NOT NULL,
    encryptionKey TEXT NOT NULL,
    
    -- Settings (CRITICAL DEFAULTS)
    gpsEnabled INTEGER NOT NULL DEFAULT 0,  -- 0 = OFF (safety)
    silentModeEnabled INTEGER NOT NULL DEFAULT 1,  -- 1 = ON
    panicGPSOverride INTEGER NOT NULL DEFAULT 0,
    autoCloudBackup INTEGER NOT NULL DEFAULT 1,
    
    -- Intersectional Identity (for resource matching)
    isLGBTQIA INTEGER NOT NULL DEFAULT 0,
    isTrans INTEGER NOT NULL DEFAULT 0,
    isNonBinary INTEGER NOT NULL DEFAULT 0,
    isBIPOC INTEGER NOT NULL DEFAULT 0,
    culturalIdentity TEXT,
    isMaleIdentifying INTEGER NOT NULL DEFAULT 0,
    isUndocumented INTEGER NOT NULL DEFAULT 0,
    isDisabled INTEGER NOT NULL DEFAULT 0,
    isDeaf INTEGER NOT NULL DEFAULT 0,
    primaryLanguage TEXT NOT NULL DEFAULT 'English',
    
    -- Sync
    lastSyncDate INTEGER,
    salesforceId TEXT,
    isActive INTEGER NOT NULL DEFAULT 1
);
```

### Kotlin Entity
```kotlin
@Entity(tableName = "safehaven_profiles")
data class SafeHavenProfile(
    @PrimaryKey
    val userId: String = UUID.randomUUID().toString(),
    
    val safeHavenPasswordHash: String,
    val duressPasswordHash: String,
    val encryptionKey: String,
    
    val gpsEnabled: Boolean = false,
    val silentModeEnabled: Boolean = true,
    val panicGPSOverride: Boolean = false,
    val autoCloudBackup: Boolean = true,
    
    val isLGBTQIA: Boolean = false,
    val isTrans: Boolean = false,
    val isNonBinary: Boolean = false,
    val isBIPOC: Boolean = false,
    val culturalIdentity: String? = null,
    val isMaleIdentifying: Boolean = false,
    val isUndocumented: Boolean = false,
    val isDisabled: Boolean = false,
    val isDeaf: Boolean = false,
    val primaryLanguage: String = "English",
    
    val lastSyncDate: Long? = null,
    val salesforceId: String? = null,
    val isActive: Boolean = true
)
```

### DAO
```kotlin
@Dao
interface SafeHavenProfileDao {
    @Query("SELECT * FROM safehaven_profiles WHERE userId = :userId")
    suspend fun getProfile(userId: String): SafeHavenProfile?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: SafeHavenProfile)
    
    @Update
    suspend fun update(profile: SafeHavenProfile)
    
    @Query("UPDATE safehaven_profiles SET lastSyncDate = :timestamp WHERE userId = :userId")
    suspend fun updateSyncDate(userId: String, timestamp: Long)
}
```

---

## Table 2: incident_reports

**Purpose**: Legal-formatted documentation of abuse incidents

### Schema
```sql
CREATE TABLE incident_reports (
    id TEXT PRIMARY KEY NOT NULL,
    userId TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    
    -- Incident details (ENCRYPTED)
    incidentType TEXT NOT NULL,  -- physical, verbal, emotional, financial, sexual, stalking
    descriptionEncrypted TEXT NOT NULL,
    locationText TEXT,
    
    -- GPS (only if enabled)
    latitude REAL,
    longitude REAL,
    
    -- Evidence
    witnessesEncrypted TEXT,
    injuriesEncrypted TEXT,
    photoEvidenceIds TEXT,  -- Comma-separated
    
    -- Legal
    policeInvolved INTEGER NOT NULL DEFAULT 0,
    policeReportNumber TEXT,
    medicalAttention INTEGER NOT NULL DEFAULT 0,
    
    -- Sync
    syncedToSalesforce INTEGER NOT NULL DEFAULT 0,
    salesforceId TEXT,
    exportedToPDF INTEGER NOT NULL DEFAULT 0,
    
    FOREIGN KEY (userId) REFERENCES safehaven_profiles(userId)
);
```

### Kotlin Entity
```kotlin
@Entity(
    tableName = "incident_reports",
    foreignKeys = [ForeignKey(
        entity = SafeHavenProfile::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class IncidentReport(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val userId: String,
    val timestamp: Long,
    
    val incidentType: String,
    val descriptionEncrypted: String,
    val locationText: String? = null,
    
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    val witnessesEncrypted: String? = null,
    val injuriesEncrypted: String? = null,
    val photoEvidenceIds: String? = null,
    
    val policeInvolved: Boolean = false,
    val policeReportNumber: String? = null,
    val medicalAttention: Boolean = false,
    
    val syncedToSalesforce: Boolean = false,
    val salesforceId: String? = null,
    val exportedToPDF: Boolean = false
)
```

### DAO
```kotlin
@Dao
interface IncidentReportDao {
    @Query("SELECT * FROM incident_reports WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAll(userId: String): List<IncidentReport>
    
    @Query("SELECT * FROM incident_reports WHERE id = :id")
    suspend fun getById(id: String): IncidentReport?
    
    @Insert
    suspend fun insert(report: IncidentReport)
    
    @Update
    suspend fun update(report: IncidentReport)
    
    @Delete
    suspend fun delete(report: IncidentReport)
    
    @Query("DELETE FROM incident_reports WHERE userId = :userId")
    suspend fun deleteAll(userId: String)
    
    @Query("SELECT COUNT(*) FROM incident_reports WHERE userId = :userId")
    suspend fun getCount(userId: String): Int
}
```

---

## Table 3: verified_documents

**Purpose**: Cryptographic verification of legal documents

### Schema
```sql
CREATE TABLE verified_documents (
    id TEXT PRIMARY KEY NOT NULL,
    userId TEXT NOT NULL,
    documentType TEXT NOT NULL,
    
    -- Cryptographic verification
    cryptographicHash TEXT NOT NULL UNIQUE,  -- SHA-256 (64 chars)
    blockchainTxHash TEXT,
    verificationMethod TEXT NOT NULL DEFAULT 'SHA256_Blockchain',
    notarizationDate INTEGER NOT NULL,
    
    -- File paths (ENCRYPTED)
    originalPhotoPathEncrypted TEXT NOT NULL,
    verifiedPDFPathEncrypted TEXT NOT NULL,
    
    -- Cloud
    cloudBackupUrl TEXT,
    
    -- Sync
    syncedToSalesforce INTEGER NOT NULL DEFAULT 0,
    salesforceId TEXT,
    
    FOREIGN KEY (userId) REFERENCES safehaven_profiles(userId)
);

CREATE INDEX idx_verified_documents_hash ON verified_documents(cryptographicHash);
```

### Kotlin Entity
```kotlin
@Entity(
    tableName = "verified_documents",
    foreignKeys = [ForeignKey(
        entity = SafeHavenProfile::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["cryptographicHash"], unique = true)]
)
data class VerifiedDocument(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val userId: String,
    val documentType: String,
    
    val cryptographicHash: String,
    val blockchainTxHash: String? = null,
    val verificationMethod: String = "SHA256_Blockchain",
    val notarizationDate: Long,
    
    val originalPhotoPathEncrypted: String,
    val verifiedPDFPathEncrypted: String,
    
    val cloudBackupUrl: String? = null,
    
    val syncedToSalesforce: Boolean = false,
    val salesforceId: String? = null
)
```

### DAO
```kotlin
@Dao
interface VerifiedDocumentDao {
    @Query("SELECT * FROM verified_documents WHERE userId = :userId")
    suspend fun getAll(userId: String): List<VerifiedDocument>
    
    @Query("SELECT * FROM verified_documents WHERE cryptographicHash = :hash")
    suspend fun getByHash(hash: String): VerifiedDocument?
    
    @Insert
    suspend fun insert(document: VerifiedDocument)
    
    @Delete
    suspend fun delete(document: VerifiedDocument)
    
    @Query("DELETE FROM verified_documents WHERE userId = :userId")
    suspend fun deleteAll(userId: String)
}
```

---

## Table 4: evidence_items

**Purpose**: Encrypted photos, videos, audio recordings

### Schema
```sql
CREATE TABLE evidence_items (
    id TEXT PRIMARY KEY NOT NULL,
    userId TEXT NOT NULL,
    evidenceType TEXT NOT NULL,  -- photo, video, audio, screenshot
    timestamp INTEGER NOT NULL,
    
    -- File (ENCRYPTED)
    encryptedFilePath TEXT NOT NULL,
    originalFileName TEXT NOT NULL,
    fileSize INTEGER NOT NULL,
    mimeType TEXT NOT NULL,
    
    -- Metadata
    captionEncrypted TEXT,
    latitude REAL,
    longitude REAL,
    
    -- Relationships
    incidentReportId TEXT,
    
    -- Cloud
    cloudBackupUrl TEXT,
    cloudBackupDate INTEGER,
    
    -- Sync
    syncedToSalesforce INTEGER NOT NULL DEFAULT 0,
    salesforceId TEXT,
    
    -- Deletion
    isDeleted INTEGER NOT NULL DEFAULT 0,
    deletedDate INTEGER,
    
    FOREIGN KEY (userId) REFERENCES safehaven_profiles(userId),
    FOREIGN KEY (incidentReportId) REFERENCES incident_reports(id)
);

CREATE INDEX idx_evidence_items_user ON evidence_items(userId);
CREATE INDEX idx_evidence_items_incident ON evidence_items(incidentReportId);
```

### Kotlin Entity
```kotlin
@Entity(
    tableName = "evidence_items",
    foreignKeys = [
        ForeignKey(
            entity = SafeHavenProfile::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IncidentReport::class,
            parentColumns = ["id"],
            childColumns = ["incidentReportId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("userId"),
        Index("incidentReportId")
    ]
)
data class EvidenceItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val userId: String,
    val evidenceType: String,
    val timestamp: Long,
    
    val encryptedFilePath: String,
    val originalFileName: String,
    val fileSize: Long,
    val mimeType: String,
    
    val captionEncrypted: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    val incidentReportId: String? = null,
    
    val cloudBackupUrl: String? = null,
    val cloudBackupDate: Long? = null,
    
    val syncedToSalesforce: Boolean = false,
    val salesforceId: String? = null,
    
    val isDeleted: Boolean = false,
    val deletedDate: Long? = null
)
```

### DAO
```kotlin
@Dao
interface EvidenceItemDao {
    @Query("SELECT * FROM evidence_items WHERE userId = :userId AND isDeleted = 0 ORDER BY timestamp DESC")
    suspend fun getAll(userId: String): List<EvidenceItem>
    
    @Query("SELECT * FROM evidence_items WHERE incidentReportId = :reportId AND isDeleted = 0")
    suspend fun getByIncident(reportId: String): List<EvidenceItem>
    
    @Insert
    suspend fun insert(item: EvidenceItem)
    
    @Update
    suspend fun update(item: EvidenceItem)
    
    @Query("UPDATE evidence_items SET isDeleted = 1, deletedDate = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long)
    
    @Query("DELETE FROM evidence_items WHERE userId = :userId")
    suspend fun deleteAll(userId: String)
}
```

---

## Table 5: legal_resources

**Purpose**: Intersectional resource database (shelters, legal aid, etc.)

### Schema
```sql
CREATE TABLE legal_resources (
    id TEXT PRIMARY KEY NOT NULL,
    
    -- Basic info
    resourceType TEXT NOT NULL,
    organizationName TEXT NOT NULL,
    phone TEXT,
    website TEXT,
    email TEXT,
    
    -- Location
    address TEXT,
    city TEXT NOT NULL,
    state TEXT NOT NULL,
    zipCode TEXT,
    latitude REAL,
    longitude REAL,
    
    -- Services
    servicesJson TEXT NOT NULL,  -- JSON array
    hours TEXT,
    is24_7 INTEGER NOT NULL DEFAULT 0,
    
    -- INTERSECTIONAL FILTERS (CRITICAL)
    servesLGBTQIA INTEGER NOT NULL DEFAULT 0,
    lgbtqSpecialized INTEGER NOT NULL DEFAULT 0,
    transInclusive INTEGER NOT NULL DEFAULT 0,
    nonBinaryInclusive INTEGER NOT NULL DEFAULT 0,
    
    servesBIPOC INTEGER NOT NULL DEFAULT 0,
    bipocLed INTEGER NOT NULL DEFAULT 0,
    culturallySpecificJson TEXT,  -- JSON array
    
    servesMaleIdentifying INTEGER NOT NULL DEFAULT 0,
    
    servesUndocumented INTEGER NOT NULL DEFAULT 0,
    uVisaSupport INTEGER NOT NULL DEFAULT 0,
    vawaSupport INTEGER NOT NULL DEFAULT 0,
    noICEContact INTEGER NOT NULL DEFAULT 0,
    
    servesDisabled INTEGER NOT NULL DEFAULT 0,
    wheelchairAccessible INTEGER NOT NULL DEFAULT 0,
    servesDeaf INTEGER NOT NULL DEFAULT 0,
    aslInterpreter INTEGER NOT NULL DEFAULT 0,
    
    -- Language
    languagesJson TEXT NOT NULL,  -- JSON array
    
    -- Cost
    isFree INTEGER NOT NULL DEFAULT 1,
    slidingScale INTEGER NOT NULL DEFAULT 0,
    
    -- Verification
    lastVerified INTEGER NOT NULL,
    verifiedBy TEXT,
    
    -- User feedback
    rating REAL,
    reviewCount INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_legal_resources_type ON legal_resources(resourceType);
CREATE INDEX idx_legal_resources_location ON legal_resources(state, city);
CREATE INDEX idx_legal_resources_lgbtq ON legal_resources(servesLGBTQIA);
CREATE INDEX idx_legal_resources_trans ON legal_resources(transInclusive);
CREATE INDEX idx_legal_resources_bipoc ON legal_resources(servesBIPOC);
CREATE INDEX idx_legal_resources_male ON legal_resources(servesMaleIdentifying);
CREATE INDEX idx_legal_resources_undoc ON legal_resources(servesUndocumented);
```

### Kotlin Entity
```kotlin
@Entity(
    tableName = "legal_resources",
    indices = [
        Index("resourceType"),
        Index("state", "city"),
        Index("servesLGBTQIA"),
        Index("transInclusive"),
        Index("servesBIPOC"),
        Index("servesMaleIdentifying"),
        Index("servesUndocumented")
    ]
)
data class LegalResource(
    @PrimaryKey
    val id: String,
    
    val resourceType: String,
    val organizationName: String,
    val phone: String? = null,
    val website: String? = null,
    val email: String? = null,
    
    val address: String? = null,
    val city: String,
    val state: String,
    val zipCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    val servicesJson: String,
    val hours: String? = null,
    val is24_7: Boolean = false,
    
    val servesLGBTQIA: Boolean = false,
    val lgbtqSpecialized: Boolean = false,
    val transInclusive: Boolean = false,
    val nonBinaryInclusive: Boolean = false,
    
    val servesBIPOC: Boolean = false,
    val bipocLed: Boolean = false,
    val culturallySpecificJson: String? = null,
    
    val servesMaleIdentifying: Boolean = false,
    
    val servesUndocumented: Boolean = false,
    val uVisaSupport: Boolean = false,
    val vawaSupport: Boolean = false,
    val noICEContact: Boolean = false,
    
    val servesDisabled: Boolean = false,
    val wheelchairAccessible: Boolean = false,
    val servesDeaf: Boolean = false,
    val aslInterpreter: Boolean = false,
    
    val languagesJson: String,
    
    val isFree: Boolean = true,
    val slidingScale: Boolean = false,
    
    val lastVerified: Long,
    val verifiedBy: String? = null,
    
    val rating: Float? = null,
    val reviewCount: Int = 0
)
```

### DAO
```kotlin
@Dao
interface LegalResourceDao {
    @Query("""
        SELECT * FROM legal_resources 
        WHERE resourceType = :type 
        AND latitude IS NOT NULL 
        AND longitude IS NOT NULL
        LIMIT 100
    """)
    suspend fun getByType(type: String): List<LegalResource>
    
    @Query("""
        SELECT * FROM legal_resources
        WHERE resourceType = :type
        AND servesLGBTQIA = :lgbtq
        AND servesBIPOC = :bipoc
        AND servesMaleIdentifying = :male
        AND servesUndocumented = :undoc
        LIMIT 100
    """)
    suspend fun getFiltered(
        type: String,
        lgbtq: Boolean,
        bipoc: Boolean,
        male: Boolean,
        undoc: Boolean
    ): List<LegalResource>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(resources: List<LegalResource>)
    
    @Query("DELETE FROM legal_resources")
    suspend fun deleteAll()
}
```

---

## Database Class
```kotlin
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
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

---

**This completes the 3 critical documentation files. Total: ~15,000 words.**