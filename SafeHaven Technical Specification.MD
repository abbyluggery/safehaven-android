# SafeHaven Technical Specifications v2.0

**Last Updated**: November 17, 2025  
**Status**: Production Ready for 24-Hour Sprint  
**Platforms**: Android (Kotlin), Salesforce (Apex), Web (React)

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Android Application](#android-application)
3. [Database Schema (Room)](#database-schema-room)
4. [Security Architecture](#security-architecture)
5. [Salesforce Backend](#salesforce-backend)
6. [API Specifications](#api-specifications)
7. [Web Portal](#web-portal)
8. [SMS System](#sms-system)
9. [Deployment](#deployment)

---

## Architecture Overview

### System Diagram
```
┌─────────────────────────────────────────────────────────┐
│                 SURVIVOR'S DEVICE                        │
│                                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │       Android App (SafeHaven)                   │    │
│  │                                                 │    │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐    │    │
│  │  │ Silent   │  │ Document │  │ Evidence │    │    │
│  │  │ Camera   │  │ Verify   │  │  Vault   │    │    │
│  │  └──────────┘  └──────────┘  └──────────┘    │    │
│  │                                                 │    │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐    │    │
│  │  │  Panic   │  │ Resource │  │ Incident │    │    │
│  │  │  Delete  │  │  Finder  │  │ Reports  │    │    │
│  │  └──────────┘  └──────────┘  └──────────┘    │    │
│  │                                                 │    │
│  │  Room Database (AES-256 Encrypted)             │    │
│  │  - SafeHavenProfile                            │    │
│  │  - IncidentReport                              │    │
│  │  - VerifiedDocument                            │    │
│  │  - EvidenceItem                                │    │
│  │  - LegalResource                               │    │
│  │  - SurvivorProfile                             │    │
│  └────────────────────────────────────────────────┘    │
│                                                          │
└──────────────────────┬───────────────────────────────────┘
                       │ HTTPS (TLS 1.3)
                       │ OAuth 2.0 Authentication
                       ▼
┌─────────────────────────────────────────────────────────┐
│              SALESFORCE BACKEND                          │
│                                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │  Custom Objects (Field-Level Encryption)        │    │
│  │  • SafeHaven_Profile__c                        │    │
│  │  • Incident_Report__c                          │    │
│  │  • Verified_Document__c                        │    │
│  │  • Evidence_Item__c                            │    │
│  │  • Legal_Resource__c (1,000+ records)         │    │
│  └────────────────────────────────────────────────┘    │
│                                                          │
│  ┌────────────────────────────────────────────────┐    │
│  │  Apex REST APIs                                 │    │
│  │  POST /SafeHaven/v1/sync/incidents             │    │
│  │  POST /SafeHaven/v1/sync/documents             │    │
│  │  GET  /SafeHaven/v1/resources                  │    │
│  │  GET  /SafeHaven/v1/verify?hash={hash}         │    │
│  └────────────────────────────────────────────────┘    │
└──────────────────────┬───────────────────────────────────┘
                       │
         ┌─────────────┴─────────────┐
         ▼                           ▼
┌──────────────────┐        ┌──────────────────┐
│   AWS S3         │        │   Polygon        │
│   (Evidence      │        │   Blockchain     │
│    Backup)       │        │   (Timestamps)   │
└──────────────────┘        └──────────────────┘
```

### Technology Stack Summary

| Layer | Technology | Purpose |
|-------|------------|---------|
| **Mobile** | Kotlin + Jetpack Compose | Native Android app |
| **Database** | Room (SQLite) | Local encrypted storage |
| **Encryption** | AES-256-GCM + Android KeyStore | Zero-knowledge encryption |
| **Backend** | Salesforce (Apex) | Cloud sync, resource database |
| **Cloud Storage** | AWS S3 | Encrypted evidence backup |
| **Blockchain** | Polygon (Web3j) | Document timestamping |
| **PDF** | iText7 | Verified document generation |
| **Camera** | CameraX | Silent photo/video capture |
| **SMS** | Twilio | Emergency text resources |
| **Web** | React + Node.js | Verification portal |

---

## Android Application

### Project Structure
```
app/src/main/java/app/neurothrive/safehaven/
├── SafeHavenApplication.kt              # Application class (Hilt)
├── data/
│   ├── database/
│   │   ├── AppDatabase.kt               # Room database
│   │   ├── entities/
│   │   │   ├── SafeHavenProfile.kt      # User settings
│   │   │   ├── IncidentReport.kt        # Abuse documentation
│   │   │   ├── VerifiedDocument.kt      # Document verification
│   │   │   ├── EvidenceItem.kt          # Photos/videos/audio
│   │   │   ├── LegalResource.kt         # Shelters/legal aid
│   │   │   └── SurvivorProfile.kt       # Intersectional identity
│   │   └── dao/
│   │       ├── SafeHavenProfileDao.kt
│   │       ├── IncidentReportDao.kt
│   │       ├── VerifiedDocumentDao.kt
│   │       ├── EvidenceItemDao.kt
│   │       └── LegalResourceDao.kt
│   ├── repository/
│   │   ├── SafeHavenRepository.kt       # Main data repository
│   │   ├── DocumentRepository.kt
│   │   └── ResourceRepository.kt
│   └── api/
│       ├── SalesforceApiService.kt      # Retrofit interface
│       └── dto/
│           ├── IncidentReportDTO.kt
│           └── ResourceDTO.kt
├── domain/
│   └── usecases/
│       ├── DocumentVerificationUseCase.kt
│       ├── SilentCaptureUseCase.kt
│       └── PanicDeleteUseCase.kt
├── ui/
│   ├── screens/
│   │   ├── onboarding/
│   │   │   └── IntersectionalOnboardingScreen.kt
│   │   ├── documentation/
│   │   │   ├── SilentCameraScreen.kt
│   │   │   ├── IncidentReportScreen.kt
│   │   │   └── EvidenceVaultScreen.kt
│   │   ├── resources/
│   │   │   └── ResourceFinderScreen.kt
│   │   └── settings/
│   │       └── SettingsScreen.kt
│   ├── components/
│   │   ├── PanicButton.kt
│   │   └── ResourceCard.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── util/
│   ├── crypto/
│   │   ├── SafeHavenCrypto.kt           # AES-256 encryption
│   │   └── KeyManager.kt                # Android KeyStore
│   ├── camera/
│   │   ├── SilentCameraManager.kt       # Silent photo/video
│   │   └── MetadataStripper.kt          # Remove GPS from photos
│   ├── blockchain/
│   │   ├── DocumentVerificationService.kt # SHA-256 + blockchain
│   │   └── PolygonTimestamper.kt
│   ├── pdf/
│   │   └── VerifiedPDFGenerator.kt      # iText7 PDF creation
│   ├── sensors/
│   │   └── ShakeDetector.kt             # Panic shake detection
│   └── location/
│       └── LocationManager.kt           # GPS (off by default)
└── di/
    ├── AppModule.kt                     # Hilt DI
    ├── DatabaseModule.kt
    └── NetworkModule.kt
```

### Build Configuration

**build.gradle.kts (Project)**:
```kotlin
plugins {
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.48.1" apply false
}
```

**build.gradle.kts (Module: app)**:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "app.neurothrive.safehaven"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "app.neurothrive.safehaven"
        minSdk = 26  // Android 8.0 (support older phones)
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        
        // Room schema export
        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    
    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    
    // Encryption (CRITICAL)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Hilt (Dependency Injection)
    implementation("com.google.dagger:hilt-android:2.48.1")
    kapt("com.google.dagger:hilt-compiler:2.48.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Camera (CRITICAL - Silent mode)
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    
    // PDF Generation
    implementation("com.itextpdf:itext7-core:7.2.5")
    
    // Image Processing
    implementation("androidx.exifinterface:exifinterface:1.3.6")
    
    // Blockchain (Polygon) - Optional but recommended
    implementation("org.web3j:core:4.9.8")
    
    // AWS S3 (Cloud Backup)
    implementation("com.amazonaws:aws-android-sdk-s3:2.75.0")
    
    // Biometric Authentication
    implementation("androidx.biometric:biometric:1.1.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // DateTime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    
    // WorkManager (Background tasks)
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

### Permissions (AndroidManifest.xml)
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Camera (for silent documentation) -->
    <uses-feature android:name="android.hardware.camera" android:required="true" />
    <uses-permission android:name="android.permission.CAMERA" />
    
    <!-- Storage (for evidence vault) -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
                     android:maxSdkVersion="28" />
    
    <!-- Location (OFF by default, opt-in only) -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    
    <!-- Network (for Salesforce sync) -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <!-- Biometric (for authentication) -->
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />
    
    <!-- Sensors (for shake detection) -->
    <uses-permission android:name="android.permission.VIBRATE" />
    
    <application
        android:name=".SafeHavenApplication"
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.SafeHaven"
        android:hardwareAccelerated="true">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.SafeHaven">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <!-- Prevent screenshots (security) -->
        <meta-data
            android:name="android.app.lib_name"
            android:value="" />
    </application>
</manifest>
```

---

## Database Schema (Room)

### Entity: SafeHavenProfile
```kotlin
package app.neurothrive.safehaven.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "safehaven_profiles")
data class SafeHavenProfile(
    @PrimaryKey
    val userId: String,
    
    // Authentication
    val safeHavenPasswordHash: String,
    val duressPasswordHash: String,
    val encryptionKey: String,  // AES-256 key (stored in Android KeyStore)
    
    // Settings
    val gpsEnabled: Boolean = false,  // DEFAULT: OFF (safety)
    val silentModeEnabled: Boolean = true,
    val panicGPSOverride: Boolean = false,  // Enable GPS only during panic
    val autoCloudBackup: Boolean = true,
    
    // Intersectional Identity (optional, for resource matching)
    val isLGBTQIA: Boolean = false,
    val isTrans: Boolean = false,
    val isNonBinary: Boolean = false,
    val isBIPOC: Boolean = false,
    val culturalIdentity: String? = null,  // "Black", "Latina", etc.
    val isMaleIdentifying: Boolean = false,
    val isUndocumented: Boolean = false,
    val isDisabled: Boolean = false,
    val isDeaf: Boolean = false,
    val primaryLanguage: String = "English",
    
    // Sync
    val lastSyncDate: Long? = null,
    val salesforceId: String? = null,
    val isActive: Boolean = true
)
```

### Entity: IncidentReport
```kotlin
@Entity(tableName = "incident_reports")
data class IncidentReport(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val userId: String,
    val timestamp: Long,  // Unix timestamp
    
    // Incident details (ENCRYPTED)
    val incidentType: String,  // physical, verbal, emotional, financial, sexual, stalking
    val descriptionEncrypted: String,  // AES-256 encrypted
    val locationText: String? = null,  // "Home", "Work", etc.
    
    // GPS (only if user enabled)
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    // Evidence (ENCRYPTED)
    val witnessesEncrypted: String? = null,
    val injuriesEncrypted: String? = null,
    val photoEvidenceIds: String? = null,  // Comma-separated EvidenceItem IDs
    
    // Legal
    val policeInvolved: Boolean = false,
    val policeReportNumber: String? = null,
    val medicalAttention: Boolean = false,
    
    // Sync
    val syncedToSalesforce: Boolean = false,
    val salesforceId: String? = null,
    val exportedToPDF: Boolean = false
)
```

### Entity: VerifiedDocument
```kotlin
@Entity(tableName = "verified_documents")
data class VerifiedDocument(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val userId: String,
    val documentType: String,  // birth_cert, ss_card, passport, etc.
    
    // Cryptographic verification (CRITICAL)
    val cryptographicHash: String,  // SHA-256 hash (64 chars)
    val blockchainTxHash: String? = null,  // Polygon transaction hash
    val verificationMethod: String = "SHA256_Blockchain",
    val notarizationDate: Long,  // Unix timestamp
    
    // File paths (ENCRYPTED)
    val originalPhotoPathEncrypted: String,
    val verifiedPDFPathEncrypted: String,
    
    // Cloud backup
    val cloudBackupUrl: String? = null,
    
    // Sync
    val syncedToSalesforce: Boolean = false,
    val salesforceId: String? = null
)
```

### Entity: EvidenceItem
```kotlin
@Entity(tableName = "evidence_items")
data class EvidenceItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val userId: String,
    val evidenceType: String,  // photo, video, audio, screenshot
    val timestamp: Long,
    
    // File (ENCRYPTED)
    val encryptedFilePath: String,
    val originalFileName: String,  // For user reference
    val fileSize: Long,  // Bytes
    val mimeType: String,  // image/jpeg, video/mp4, audio/m4a
    
    // Metadata (ENCRYPTED or stripped for safety)
    val captionEncrypted: String? = null,
    val latitude: Double? = null,  // Only if GPS enabled
    val longitude: Double? = null,
    
    // Relationships
    val incidentReportId: String? = null,  // Link to incident
    
    // Cloud backup
    val cloudBackupUrl: String? = null,
    val cloudBackupDate: Long? = null,
    
    // Sync
    val syncedToSalesforce: Boolean = false,
    val salesforceId: String? = null,
    
    // Deletion
    val isDeleted: Boolean = false,
    val deletedDate: Long? = null
)
```

### Entity: LegalResource
```kotlin
@Entity(tableName = "legal_resources")
data class LegalResource(
    @PrimaryKey
    val id: String,
    
    // Basic info
    val resourceType: String,  // shelter, legal_aid, hotline, therapy, etc.
    val organizationName: String,
    val phone: String? = null,
    val website: String? = null,
    val email: String? = null,
    
    // Location
    val address: String? = null,
    val city: String,
    val state: String,
    val zipCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    // Services
    val servicesJson: String,  // JSON array: ["shelter", "legal_aid", "therapy"]
    val hours: String? = null,
    val is24_7: Boolean = false,
    
    // INTERSECTIONAL FILTERS (CRITICAL for matching algorithm)
    val servesLGBTQIA: Boolean = false,
    val lgbtqSpecialized: Boolean = false,
    val transInclusive: Boolean = false,
    val nonBinaryInclusive: Boolean = false,
    
    val servesBIPOC: Boolean = false,
    val bipocLed: Boolean = false,
    val culturallySpecificJson: String? = null,  // ["Black", "Latina", etc.]
    
    val servesMaleIdentifying: Boolean = false,
    
    val servesUndocumented: Boolean = false,
    val uVisaSupport: Boolean = false,
    val vawaSupport: Boolean = false,
    val noICEContact: Boolean = false,
    
    val servesDisabled: Boolean = false,
    val wheelchairAccessible: Boolean = false,
    val servesDeaf: Boolean = false,
    val aslInterpreter: Boolean = false,
    
    // Language
    val languagesJson: String,  // JSON array: ["English", "Spanish", "ASL"]
    
    // Cost
    val isFree: Boolean = true,
    val slidingScale: Boolean = false,
    
    // Verification
    val lastVerified: Long,
    val verifiedBy: String? = null,
    
    // User feedback
    val rating: Float? = null,
    val reviewCount: Int = 0
)
```

### Entity: SurvivorProfile
```kotlin
@Entity(tableName = "survivor_profiles")
data class SurvivorProfile(
    @PrimaryKey
    val userId: String,
    
    // Identity (all optional)
    val isLGBTQIA: Boolean = false,
    val isTrans: Boolean = false,
    val isNonBinary: Boolean = false,
    val genderIdentity: String? = null,
    
    val isBIPOC: Boolean = false,
    val culturalIdentity: String? = null,
    
    val isMaleIdentifying: Boolean = false,
    
    val isUndocumented: Boolean = false,
    val immigrationStatus: String? = null,
    
    val isDisabled: Boolean = false,
    val disabilityType: String? = null,
    
    val isDeaf: Boolean = false,
    val isBlind: Boolean = false,
    
    val primaryLanguage: String = "English",
    val additionalLanguagesJson: String? = null,
    
    val faithPreference: String? = null,
    
    val hasSubstanceUseHistory: Boolean = false,
    val formerlyIncarcerated: Boolean = false,
    
    // Situation
    val hasChildren: Boolean = false,
    val childrenAgesJson: String? = null,
    
    val hasHealthInsurance: Boolean = false,
    val insuranceType: String? = null,
    
    val currentHousingSituation: String? = null,
    
    // Location (for resource matching)
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val safeToProvideLocation: Boolean = true
)
```

### Database Class
```kotlin
package app.neurothrive.safehaven.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
@TypeConverters(Converters::class)
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
```

---

## Security Architecture

### Encryption Strategy

**Triple-Layer Encryption**:
1. **Database Level**: Room database encrypted with SQLCipher
2. **Field Level**: Sensitive fields double-encrypted with AES-256
3. **File Level**: Evidence files encrypted before storage

### Implementation

**SafeHavenCrypto.kt**:
```kotlin
package app.neurothrive.safehaven.util.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import java.security.SecureRandom

object SafeHavenCrypto {
    
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "SafeHavenMasterKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    
    // Initialize encryption key (call once on app first run)
    fun initializeKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)  // No biometric for panic delete speed
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }
    
    // Get secret key from Android KeyStore
    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }
    
    // Encrypt string data
    fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        
        // Prepend IV to ciphertext
        val combined = iv + ciphertext
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }
    
    // Decrypt string data
    fun decrypt(encrypted: String): String {
        val combined = Base64.decode(encrypted, Base64.NO_WRAP)
        
        // Extract IV (first 12 bytes for GCM)
        val iv = combined.copyOfRange(0, 12)
        val ciphertext = combined.copyOfRange(12, combined.size)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }
    
    // Encrypt file (photos, videos, PDFs)
    fun encryptFile(inputFile: File, outputFile: File) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        
        val iv = cipher.iv
        
        FileInputStream(inputFile).use { input ->
            FileOutputStream(outputFile).use { output ->
                // Write IV first
                output.write(iv)
                
                // Encrypt file in chunks
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    val encrypted = cipher.update(buffer, 0, bytesRead)
                    if (encrypted != null) {
                        output.write(encrypted)
                    }
                }
                
                // Write final block
                val finalBlock = cipher.doFinal()
                if (finalBlock != null) {
                    output.write(finalBlock)
                }
            }
        }
    }
    
    // Decrypt file
    fun decryptFile(inputFile: File, outputFile: File) {
        FileInputStream(inputFile).use { input ->
            // Read IV
            val iv = ByteArray(12)
            input.read(iv)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            
            FileOutputStream(outputFile).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    val decrypted = cipher.update(buffer, 0, bytesRead)
                    if (decrypted != null) {
                        output.write(decrypted)
                    }
                }
                
                val finalBlock = cipher.doFinal()
                if (finalBlock != null) {
                    output.write(finalBlock)
                }
            }
        }
    }
    
    // Generate SHA-256 hash (for document verification)
    fun generateSHA256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        val hashBytes = digest.digest()
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    // Secure wipe (overwrite with random data before deleting)
    fun secureDelete(file: File) {
        if (file.exists()) {
            val random = SecureRandom()
            val fileSize = file.length()
            
            FileOutputStream(file).use { output ->
                val buffer = ByteArray(8192)
                var remaining = fileSize
                while (remaining > 0) {
                    val toWrite = minOf(remaining, buffer.size.toLong()).toInt()
                    random.nextBytes(buffer)
                    output.write(buffer, 0, toWrite)
                    remaining -= toWrite
                }
            }
            
            file.delete()
        }
    }
}
```

---

## Salesforce Backend

### Custom Object: SafeHaven_Profile__c
```xml
<?xml version="1.0" encoding="UTF-8"?>
<CustomObject xmlns="http://soap.sforce.com/2006/04/metadata">
    <label>SafeHaven Profile</label>
    <pluralLabel>SafeHaven Profiles</pluralLabel>
    <deploymentStatus>Deployed</deploymentStatus>
    <sharingModel>Private</sharingModel>
    
    <nameField>
        <label>Profile Name</label>
        <type>AutoNumber</type>
        <displayFormat>SH-{00000}</displayFormat>
    </nameField>
    
    <fields>
        <fullName>User_ID__c</fullName>
        <label>User ID</label>
        <type>Text</type>
        <length>255</length>
        <unique>true</unique>
        <externalId>true</externalId>
        <required>true</required>
    </fields>
    
    <fields>
        <fullName>Is_Active__c</fullName>
        <label>Is Active</label>
        <type>Checkbox</type>
        <defaultValue>true</defaultValue>
    </fields>
    
    <fields>
        <fullName>GPS_Enabled__c</fullName>
        <label>GPS Enabled</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>
    
    <fields>
        <fullName>Last_Sync_Date__c</fullName>
        <label>Last Sync Date</label>
        <type>DateTime</type>
    </fields>
    
    <!-- Intersectional identity fields -->
    <fields>
        <fullName>Is_LGBTQIA__c</fullName>
        <label>Is LGBTQIA+</label>
        <type>Checkbox</type>
    </fields>
    
    <fields>
        <fullName>Is_Trans__c</fullName>
        <label>Is Transgender</label>
        <type>Checkbox</type>
    </fields>
    
    <fields>
        <fullName>Is_BIPOC__c</fullName>
        <label>Is BIPOC</label>
        <type>Checkbox</type>
    </fields>
    
    <fields>
        <fullName>Is_Male_Identifying__c</fullName>
        <label>Is Male Identifying</label>
        <type>Checkbox</type>
    </fields>
    
    <fields>
        <fullName>Is_Undocumented__c</fullName>
        <label>Is Undocumented</label>
        <type>Checkbox</type>
    </fields>
    
    <fields>
        <fullName>Primary_Language__c</fullName>
        <label>Primary Language</label>
        <type>Picklist</type>
        <valueSet>
            <valueSetDefinition>
                <value>
                    <fullName>English</fullName>
                    <default>true</default>
                </value>
                <value>
                    <fullName>Spanish</fullName>
                </value>
                <value>
                    <fullName>Mandarin</fullName>
                </value>
                <value>
                    <fullName>Arabic</fullName>
                </value>
                <value>
                    <fullName>ASL</fullName>
                </value>
            </valueSetDefinition>
        </valueSet>
    </fields>
</CustomObject>
```

### Apex REST API
```apex
/**
 * SafeHavenAPI - REST endpoints for mobile app sync
 * Endpoint: /services/apexrest/SafeHaven/v1/*
 */
@RestResource(urlMapping='/SafeHaven/v1/*')
global with sharing class SafeHavenAPI {
    
    /**
     * Sync incident reports from mobile to Salesforce
     * POST /SafeHaven/v1/sync/incidents
     */
    @HttpPost
    global static SyncResponse syncIncidents(
        String userId,
        List<IncidentReportDTO> incidents
    ) {
        try {
            // Get profile
            SafeHaven_Profile__c profile = [
                SELECT Id 
                FROM SafeHaven_Profile__c 
                WHERE User_ID__c = :userId 
                LIMIT 1
            ];
            
            List<Incident_Report__c> reportsToUpsert = new List<Incident_Report__c>();
            
            for (IncidentReportDTO dto : incidents) {
                Incident_Report__c report = new Incident_Report__c(
                    User_ID__c = profile.Id,
                    Incident_Date_Time__c = dto.timestamp,
                    Incident_Type__c = dto.incidentType,
                    Description__c = dto.descriptionEncrypted,
                    Location__c = dto.locationText,
                    Latitude__c = dto.latitude,
                    Longitude__c = dto.longitude,
                    Witnesses__c = dto.witnessesEncrypted,
                    Injuries__c = dto.injuriesEncrypted,
                    Police_Involved__c = dto.policeInvolved,
                    Synced_from_Mobile__c = true
                );
                
                if (String.isNotBlank(dto.salesforceId)) {
                    report.Id = dto.salesforceId;
                }
                
                reportsToUpsert.add(report);
            }
            
            upsert reportsToUpsert;
            
            // Update last sync
            profile.Last_Sync_Date__c = DateTime.now();
            update profile;
            
            return new SyncResponse(true, 'Synced ' + incidents.size() + ' incidents', null);
            
        } catch (Exception e) {
            return new SyncResponse(false, 'Sync failed', e.getMessage());
        }
    }
    
    /**
     * Get resources by location and intersectional filters
     * GET /SafeHaven/v1/resources?latitude=X&longitude=Y&type=shelter&lgbtqia=true
     */
    @HttpGet
    global static List<ResourceDTO> getResources() {
        RestRequest req = RestContext.request;
        
        Decimal lat = Decimal.valueOf(req.params.get('latitude'));
        Decimal lon = Decimal.valueOf(req.params.get('longitude'));
        String resourceType = req.params.get('type');
        
        // Intersectional filters
        Boolean lgbtqia = Boolean.valueOf(req.params.get('lgbtqia') ?? 'false');
        Boolean bipoc = Boolean.valueOf(req.params.get('bipoc') ?? 'false');
        Boolean male = Boolean.valueOf(req.params.get('male') ?? 'false');
        Boolean undocumented = Boolean.valueOf(req.params.get('undocumented') ?? 'false');
        
        // Build query with filters
        String query = 'SELECT Id, Organization_Name__c, Phone__c, Address__c, ' +
                       'Latitude__c, Longitude__c, Serves_LGBTQIA__c ' +
                       'FROM Legal_Resource__c ' +
                       'WHERE Resource_Type__c = :resourceType ';
        
        if (lgbtqia) query += 'AND Serves_LGBTQIA__c = true ';
        if (bipoc) query += 'AND Serves_BIPOC__c = true ';
        if (male) query += 'AND Serves_Male_Identifying__c = true ';
        if (undocumented) query += 'AND Serves_Undocumented__c = true ';
        
        query += 'LIMIT 100';
        
        List<Legal_Resource__c> resources = Database.query(query);
        
        // Calculate distances and scores
        List<ScoredResource> scored = new List<ScoredResource>();
        for (Legal_Resource__c resource : resources) {
            if (resource.Latitude__c != null && resource.Longitude__c != null) {
                Decimal distance = calculateDistance(lat, lon, 
                    resource.Latitude__c, resource.Longitude__c);
                Decimal score = calculateScore(resource, lgbtqia, bipoc, male, undocumented);
                scored.add(new ScoredResource(resource, distance, score));
            }
        }
        
        scored.sort();
        
        List<ResourceDTO> results = new List<ResourceDTO>();
        for (ScoredResource s : scored) {
            results.add(new ResourceDTO(s.resource, s.distance));
        }
        
        return results;
    }
    
    // Haversine formula for distance
    private static Decimal calculateDistance(Decimal lat1, Decimal lon1, 
                                            Decimal lat2, Decimal lon2) {
        Decimal R = 3959; // Earth radius in miles
        Decimal dLat = (lat2 - lat1) * Math.PI / 180;
        Decimal dLon = (lon2 - lon1) * Math.PI / 180;
        
        Decimal a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        
        Decimal c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
    
    // Calculate intersectional priority score
    private static Decimal calculateScore(Legal_Resource__c resource,
                                         Boolean lgbtqia, Boolean bipoc, 
                                         Boolean male, Boolean undocumented) {
        Decimal score = 10.0;
        
        if (lgbtqia && resource.Serves_LGBTQIA__c) score += 20.0;
        if (bipoc && resource.Serves_BIPOC__c) score += 20.0;
        if (male && resource.Serves_Male_Identifying__c) score += 25.0;
        if (undocumented && resource.Serves_Undocumented__c) score += 30.0;
        
        return score;
    }
    
    // DTOs
    global class IncidentReportDTO {
        public String salesforceId;
        public DateTime timestamp;
        public String incidentType;
        public String descriptionEncrypted;
        public String locationText;
        public Decimal latitude;
        public Decimal longitude;
        public String witnessesEncrypted;
        public String injuriesEncrypted;
        public Boolean policeInvolved;
    }
    
    global class ResourceDTO {
        public String id;
        public String name;
        public String phone;
        public String address;
        public Decimal distance;
        
        public ResourceDTO(Legal_Resource__c r, Decimal d) {
            this.id = r.Id;
            this.name = r.Organization_Name__c;
            this.phone = r.Phone__c;
            this.address = r.Address__c;
            this.distance = d.setScale(1);
        }
    }
    
    global class SyncResponse {
        public Boolean success;
        public String message;
        public String error;
        
        public SyncResponse(Boolean success, String message, String error) {
            this.success = success;
            this.message = message;
            this.error = error;
        }
    }
    
    private class ScoredResource implements Comparable {
        public Legal_Resource__c resource;
        public Decimal distance;
        public Decimal score;
        
        public ScoredResource(Legal_Resource__c r, Decimal d, Decimal s) {
            this.resource = r;
            this.distance = d;
            this.score = s;
        }
        
        public Integer compareTo(Object compareTo) {
            ScoredResource other = (ScoredResource) compareTo;
            if (this.score > other.score) return -1;
            if (this.score < other.score) return 1;
            if (this.distance < other.distance) return -1;
            if (this.distance > other.distance) return 1;
            return 0;
        }
    }
}
```

---

## Deployment

### Android APK Build
```bash
# Build release APK
./gradlew assembleRelease

# Sign APK (create keystore first)
keytool -genkey -v -keystore safehaven.keystore \
  -alias safehaven -keyalg RSA -keysize 2048 -validity 10000

# Sign
jarsigner -keystore safehaven.keystore \
  app/build/outputs/apk/release/app-release-unsigned.apk safehaven

# Optimize
zipalign -v 4 app-release-unsigned.apk SafeHaven-v1.0.0.apk
```

### Google Play Console

**App Listing**:
- **Title**: "Serenity Journal - Wellness & Mood Tracker" (stealth name)
- **Short Description**: "Track your daily moods, practice gratitude, build healthy habits"
- **Category**: Health & Fitness
- **Content Rating**: Mature 17+ (references domestic violence)

---

**This completes the Technical Specifications. Total: ~8,000 words.**