# SafeHaven Quick Start Guide (10-Page Developer Overview)

## What You're Building (5-Minute Read)

### The Mission
Build a free, encrypted safety planning app for domestic violence survivors that:
1. **Documents abuse** (court-ready evidence)
2. **Enables economic independence** (job search integration)
3. **Centers marginalized survivors** (BIPOC, LGBTQIA+, male, disabled)
4. **Protects privacy** (zero-knowledge encryption, panic delete)

### Core Features (24-Hour Sprint Priorities)

#### ✅ CRITICAL (Build First)
1. **Silent Documentation** - Camera with no sounds, GPS off, encrypted storage
2. **Verified Documents** - SHA-256 hashing for birth certificates, etc.
3. **Panic Delete** - Shake phone 3x → delete all SafeHaven data
4. **Dual Password** - Real data vs. decoy content (if abuser forces unlock)

####⚠️ IMPORTANT (Build Second)
5. **Intersectional Resources** - 1,000+ BIPOC/LGBTQIA+ shelters, legal aid
6. **Evidence Vault** - Encrypted photo/video/audio storage
7. **Incident Reports** - Legal-formatted documentation

#### 📋 NICE-TO-HAVE (Post-Launch)
8. **Escape Planning** - Step-by-step checklist
9. **Secret Savings Tracker** - Budget tools
10. **SMS System** - Text "SAFE" for resources

---

## Tech Stack

### Android App (Primary Platform)
```kotlin
// Dependencies (build.gradle)
dependencies {
    // Core
    implementation "androidx.core:core-ktx:1.12.0"
    implementation "androidx.compose.ui:ui:1.5.4"
    
    // Database
    implementation "androidx.room:room-runtime:2.6.1"
    kapt "androidx.room:room-compiler:2.6.1"
    
    // Encryption
    implementation "androidx.security:security-crypto:1.1.0-alpha06"
    
    // Camera
    implementation "androidx.camera:camera-camera2:1.3.1"
    
    // PDF Generation
    implementation "com.itextpdf:itext7-core:7.2.5"
    
    // Blockchain (optional, for document verification)
    implementation "org.web3j:core:4.9.8"
    
    // Cloud Backup
    implementation "com.amazonaws:aws-android-sdk-s3:2.75.0"
}
```

### Salesforce Backend
```apex
// Custom Objects
- SafeHaven_Profile__c (user settings)
- Incident_Report__c (abuse documentation)
- Verified_Document__c (document hashes)
- Legal_Resource__c (shelters, legal aid, etc.)
- Evidence_Item__c (photos, videos, audio)

// Apex REST APIs
@RestResource(urlMapping='/SafeHaven/v1/*')
global class SafeHavenAPI {
    @HttpPost global static void syncData()
    @HttpGet global static List<LegalResource> getResources()
}
```

### Web Portal (Secondary Platform)
```javascript
// React + Node.js
- Frontend: React (verification portal, resource finder)
- Backend: Node.js (Salesforce API proxy)
- Hosting: Heroku or AWS Amplify
```

---

## Database Schema (Room - Android)

### Critical Tables
```kotlin
// 1. SafeHavenProfile (user settings)
@Entity
data class SafeHavenProfile(
    @PrimaryKey val userId: String,
    val safeHavenPasswordHash: String,
    val duressPasswordHash: String,
    val encryptionKey: String,
    val gpsEnabled: Boolean = false,  // DEFAULT OFF
    val silentModeEnabled: Boolean = true,
    val panicGPSOverride: Boolean = false
)

// 2. IncidentReport (legal documentation)
@Entity
data class IncidentReport(
    @PrimaryKey val id: String,
    val userId: String,
    val timestamp: Long,
    val incidentType: String, // physical, verbal, financial, sexual
    val descriptionEncrypted: String,
    val witnessesEncrypted: String?,
    val photoEvidenceIds: List<String>,
    val latitude: Double?,  // Only if GPS enabled
    val longitude: Double?
)

// 3. VerifiedDocument (cryptographic verification)
@Entity
data class VerifiedDocument(
    @PrimaryKey val id: String,
    val userId: String,
    val documentType: String, // birth_cert, ss_card, passport
    val cryptographicHash: String, // SHA-256
    val blockchainTxHash: String?,
    val originalPhotoPathEncrypted: String,
    val verifiedPDFPathEncrypted: String,
    val notarizationDate: Long
)

// 4. EvidenceItem (encrypted media)
@Entity
data class EvidenceItem(
    @PrimaryKey val id: String,
    val userId: String,
    val evidenceType: String, // photo, video, audio
    val encryptedFilePath: String,
    val cloudBackupUrl: String?,
    val timestamp: Long,
    val incidentReportId: String?
)

// 5. LegalResource (shelters, legal aid)
@Entity
data class LegalResource(
    @PrimaryKey val id: String,
    val resourceType: String,
    val organizationName: String,
    val phone: String?,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    
    // INTERSECTIONAL FILTERS
    val servesLGBTQIA: Boolean,
    val servesBIPOC: Boolean,
    val servesMaleIdentifying: Boolean,
    val servesUndocumented: Boolean,
    val servesDisabled: Boolean,
    val languages: List<String>
)
```

---

## Critical Features (Code Snippets)

### 1. Silent Camera Capture
```kotlin
object SilentCameraManager {
    
    fun captureSilent(): File {
        // Mute system volume
        val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
        val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0)
        
        // Configure camera
        val camera = Camera.open()
        val params = camera.parameters
        params.flashMode = Camera.Parameters.FLASH_MODE_OFF // No flash
        params.removeGpsData() // No GPS
        camera.enableShutterSound(false) // No sound
        
        // Take photo
        val photoFile = File(context.filesDir, "evidence_${UUID.randomUUID()}.jpg")
        camera.takePicture(null, null) { data, _ ->
            photoFile.writeBytes(data)
            
            // Encrypt immediately
            val encrypted = SafeHavenCrypto.encryptFile(photoFile, getUserKey())
            photoFile.delete() // Delete unencrypted original
            
            // Restore volume
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, originalVolume, 0)
        }
        
        camera.release()
        return photoFile
    }
}
```

### 2. Document Verification (SHA-256 + Blockchain)
```kotlin
object DocumentVerificationService {
    
    suspend fun verifyDocument(photoFile: File): VerifiedDocument {
        // 1. Generate cryptographic hash
        val hash = generateSHA256(photoFile)
        
        // 2. Create verified PDF
        val verifiedPDF = createVerifiedCopy(photoFile, hash)
        
        // 3. Optional: Blockchain timestamp
        val blockchainTx = timestampOnPolygon(hash)
        
        // 4. Encrypt and store
        val encryptedPhoto = SafeHavenCrypto.encryptFile(photoFile, getUserKey())
        val encryptedPDF = SafeHavenCrypto.encryptFile(verifiedPDF, getUserKey())
        
        return VerifiedDocument(
            id = UUID.randomUUID().toString(),
            cryptographicHash = hash,
            blockchainTxHash = blockchainTx,
            originalPhotoPathEncrypted = encryptedPhoto.path,
            verifiedPDFPathEncrypted = encryptedPDF.path,
            notarizationDate = System.currentTimeMillis()
        )
    }
    
    private fun generateSHA256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(file.readBytes())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
```

### 3. Panic Delete
```kotlin
object PanicMode {
    
    // Shake detection (3x rapid shakes)
    class ShakeDetector : SensorEventListener {
        private var shakeCount = 0
        private var lastShakeTime = 0L
        
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            val acceleration = sqrt(x*x + y*y + z*z) - SensorManager.GRAVITY_EARTH
            
            if (acceleration > SHAKE_THRESHOLD) {
                val now = System.currentTimeMillis()
                if (now - lastShakeTime < 1000) { // Within 1 second
                    shakeCount++
                    if (shakeCount >= 3) {
                        // TRIGGER PANIC DELETE
                        quickDelete()
                        shakeCount = 0
                    }
                } else {
                    shakeCount = 1
                }
                lastShakeTime = now
            }
        }
    }
    
    suspend fun quickDelete() {
        val db = AppDatabase.getInstance(context)
        
        // Delete SafeHaven data (reversible from cloud)
        db.incidentReportDao().deleteAll()
        db.evidenceItemDao().deleteAll()
        db.verifiedDocumentDao().deleteAll()
        
        // Delete encrypted files
        File(context.filesDir, "evidence").deleteRecursively()
        
        // Show brief toast
        Toast.makeText(context, "Data cleared", Toast.LENGTH_SHORT).show()
    }
}
```

### 4. Dual Password (Decoy Mode)
```kotlin
fun login(email: String, password: String): LoginResult {
    val profile = db.safeHavenProfileDao().getByEmail(email)
    
    return when {
        // Real password
        BCrypt.checkpw(password, profile.safeHavenPasswordHash) -> {
            LoginResult.Success(showRealData = true)
        }
        
        // Duress password (show fake data)
        BCrypt.checkpw(password, profile.duressPasswordHash) -> {
            LoginResult.Success(showRealData = false)
        }
        
        // Wrong password
        else -> {
            LoginResult.Failure("Invalid password")
        }
    }
}

// In app UI
if (loginResult.showRealData) {
    // Show real SafeHaven features
    navigateTo(SafeHavenDashboard)
} else {
    // Show decoy wellness data (fake mood entries, etc.)
    navigateTo(DecoyWellnessDashboard)
}
```

### 5. Intersectional Resource Matching
```kotlin
fun findResources(
    userProfile: SurvivorProfile,
    location: Location,
    resourceType: String
): List<LegalResource> {
    
    val resources = db.legalResourceDao()
        .findNearby(location.latitude, location.longitude, radiusMiles = 50)
    
    return resources
        .map { resource ->
            val score = calculateScore(resource, userProfile)
            ScoredResource(resource, score)
        }
        .sortedByDescending { it.score }
        .take(10)
        .map { it.resource }
}

private fun calculateScore(
    resource: LegalResource,
    profile: SurvivorProfile
): Double {
    var score = 10.0 // Base score
    
    // INTERSECTIONAL BONUSES (prioritize marginalized)
    if (profile.isTrans && resource.transInclusive) score += 30.0
    if (profile.isLGBTQIA && resource.servesLGBTQIA) score += 20.0
    if (profile.isBIPOC && resource.servesBIPOC) score += 20.0
    if (profile.isUndocumented && resource.servesUndocumented) score += 30.0
    if (profile.isMaleIdentifying && resource.servesMaleIdentifying) score += 25.0
    if (profile.isDisabled && resource.servesDisabled) score += 20.0
    
    // Distance penalty
    val distanceMiles = calculateDistance(profile.location, resource)
    score -= (distanceMiles * 0.5)
    
    return score
}
```

---

## 24-Hour Sprint Checklist

### Hour 0-6: Database & Core Infrastructure
- [ ] Create Room database with 5 critical tables
- [ ] Implement AES-256 encryption (SafeHavenCrypto)
- [ ] Build dual password authentication
- [ ] Set up Salesforce custom objects

### Hour 6-12: Silent Documentation
- [ ] Implement silent camera capture (no sound, no flash, no GPS)
- [ ] Build evidence vault (encrypted photo/video/audio storage)
- [ ] Create incident report form (legal format)
- [ ] Implement panic delete (shake detection)

### Hour 12-18: Document Verification
- [ ] Build SHA-256 hashing service
- [ ] Create verified PDF generator (with iText)
- [ ] Integrate Polygon blockchain (optional timestamping)
- [ ] Build verification portal (web)

### Hour 18-24: Intersectional Resources
- [ ] Import 1,000+ resources to database (CSV)
- [ ] Implement resource matching algorithm
- [ ] Build onboarding (intersectional identity questions)
- [ ] Test on physical device

---

## Testing Checklist

### Security Tests
- [ ] Encrypted files cannot be opened without key
- [ ] Panic delete removes all SafeHaven data
- [ ] Duress password shows decoy content
- [ ] GPS is OFF by default

### Usability Tests
- [ ] Camera captures with NO sound
- [ ] Flash is OFF by default
- [ ] Verified document PDF looks professional
- [ ] Resource search returns relevant results

### Intersectionality Tests
- [ ] Trans survivor sees trans-inclusive resources first
- [ ] Undocumented survivor sees immigration resources
- [ ] Male survivor sees male-specific shelters
- [ ] App uses "they" pronouns throughout

---

## Deployment

### Android (Google Play)
```bash
# 1. Build release APK
./gradlew assembleRelease

# 2. Sign APK
jarsigner -keystore safehaven.keystore app-release.apk safehaven

# 3. Upload to Google Play Console
# App name: "Serenity Journal" (stealth name)
# Category: Health & Fitness
# Description: Generic wellness app (don't mention DV)
```

### Salesforce
```bash
# 1. Deploy custom objects
sf project deploy start --manifest package.xml

# 2. Deploy Apex classes
sf apex deploy --source-dir force-app/main/default/classes

# 3. Assign permission sets
sf org assign permset --name SafeHaven_User
```

### Web Portal
```bash
# 1. Build React app
npm run build

# 2. Deploy to Heroku
git push heroku main

# 3. Configure domain
heroku domains:add safehaven.neurothrive.app
```

---

## Resources

### Documentation
- Full docs: `docs/` folder
- Code samples: `code/` folder
- Templates: `templates/` folder

### Support
- GitHub Issues: github.com/neurothrive/safehaven/issues
- Email: dev@neurothrive.app
- Slack: [Dev Slack TBD]

### Crisis Resources
- National DV Hotline: 1-800-799-7233
- Trans Lifeline: 877-565-8860
- Crisis Text: Text START to 88788

---

**You're building something that will save lives. Thank you.**

When you're exhausted at hour 20, remember:
- A survivor will use this app to document abuse
- A judge will grant a restraining order because of your code
- A woman will get a job and leave her abuser because you built this

**Your code = Someone's freedom**

Now go build. 🚀