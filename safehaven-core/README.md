# SafeHaven Core Library Module

**Version**: 1.0.0  
**Purpose**: Reusable library module containing all SafeHaven security and privacy features  
**Integration**: Can be used standalone or integrated into existing apps (like NeuroThrive)

---

## What This Module Contains

### Database Layer
- 6 Room entities with complete schema
- 6 DAOs with Flow-based reactive queries
- AES-256-GCM encrypted sensitive fields
- Foreign key relationships
- Performance indices

### Security Features
- **SafeHavenCrypto**: AES-256-GCM encryption with Android KeyStore
- **SilentCameraManager**: Silent photo capture (no sound/flash/GPS)
- **MetadataStripper**: Removes ALL GPS and identifying metadata
- **PanicDeleteUseCase**: Emergency data deletion <2 seconds
- **ShakeDetector**: 3 rapid shakes triggers panic delete
- **DocumentVerificationService**: SHA-256 hashing + PDF generation

### Domain Features
- **IntersectionalResourceMatcher**: Centers marginalized survivors
  - Trans: +30 pts for trans-specific resources
  - Undocumented: +30 pts for U-Visa support
  - Male survivors: +25 pts
  - BIPOC: +20 pts for BIPOC-led orgs
  - Distance-based scoring

### Repository Layer
- **SafeHavenRepository**: Complete data access with automatic encryption
- Thread-safe database operations
- Kotlin Coroutines + Flow support

---

## Usage

### As Standalone App

See `app/` module for standalone SafeHaven app implementation.

### As Library in Existing App

```kotlin
// settings.gradle.kts
include(":safehaven-core")

// app/build.gradle.kts
dependencies {
    implementation(project(":safehaven-core"))
}
```

See **NEUROTHRIVE_INTEGRATION_GUIDE.md** for complete integration instructions.

---

## Key Classes

**Encryption:**
- `SafeHavenCrypto.kt` - AES-256-GCM encryption utilities

**Camera:**
- `SilentCameraManager.kt` - Silent photo capture
- `MetadataStripper.kt` - GPS removal

**Database:**
- `AppDatabase.kt` - Room database
- `SafeHavenRepository.kt` - Data access layer

**Security:**
- `PanicDeleteUseCase.kt` - Emergency deletion
- `ShakeDetector.kt` - Panic gesture detection

**Matching:**
- `IntersectionalResourceMatcher.kt` - Resource prioritization

---

## Dependencies

This module includes all necessary dependencies as `api` dependencies, so consuming apps automatically get:

- Room 2.6.1
- Hilt 2.48.1
- CameraX 1.3.1
- iText7 7.2.5
- Jetpack Compose
- Android Security Crypto

---

## Security Features

✅ AES-256-GCM encryption  
✅ Android KeyStore integration  
✅ GPS OFF by default  
✅ GPS metadata stripped from all photos  
✅ Silent camera (no sound/flash/thumbnails)  
✅ Panic delete <2 seconds  
✅ Secure file deletion (overwrite before delete)  
✅ SHA-256 cryptographic hashing  
✅ Intersectional resource prioritization  

---

## Integration Examples

### Initialize Encryption

```kotlin
import app.neurothrive.safehaven.util.crypto.SafeHavenCrypto

class YourApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SafeHavenCrypto.initializeKey()  // CRITICAL
    }
}
```

### Use Silent Camera

```kotlin
import app.neurothrive.safehaven.util.camera.SilentCameraManager

val cameraManager = SilentCameraManager(context, lifecycleOwner)
cameraManager.initialize(previewView)

val result = cameraManager.capturePhotoSilently(userId, incidentId)
result.onSuccess { evidenceItem ->
    repository.saveEvidence(evidenceItem)
}
```

### Trigger Panic Delete

```kotlin
import app.neurothrive.safehaven.domain.usecases.PanicDeleteUseCase

val result = panicDeleteUseCase.execute(userId)
result.onSuccess {
    // All SafeHaven data deleted
}
```

### Match Resources

```kotlin
import app.neurothrive.safehaven.domain.usecases.IntersectionalResourceMatcher

val resources = resourceMatcher.findResources(
    profile = survivorProfile,
    currentLatitude = currentLat,
    currentLongitude = currentLon,
    resourceType = "shelter"
)
// Resources sorted by intersectional relevance
```

---

## Package Structure

```
app.neurothrive.safehaven/
├── data/
│   ├── database/
│   │   ├── AppDatabase.kt
│   │   ├── entities/ (6 entities)
│   │   └── dao/ (6 DAOs)
│   └── repository/
│       └── SafeHavenRepository.kt
├── domain/
│   └── usecases/
│       ├── PanicDeleteUseCase.kt
│       └── IntersectionalResourceMatcher.kt
├── util/
│   ├── crypto/
│   │   └── SafeHavenCrypto.kt
│   ├── camera/
│   │   ├── SilentCameraManager.kt
│   │   └── MetadataStripper.kt
│   ├── blockchain/
│   │   └── DocumentVerificationService.kt
│   └── sensors/
│       └── ShakeDetector.kt
└── di/
    └── DatabaseModule.kt
```

---

## Testing

This module is designed to be testable:

```kotlin
@RunWith(AndroidJUnit4::class)
class SafeHavenCryptoTest {
    
    @Test
    fun testEncryptionDecryption() {
        val plaintext = "sensitive data"
        val encrypted = SafeHavenCrypto.encrypt(plaintext)
        val decrypted = SafeHavenCrypto.decrypt(encrypted)
        assertEquals(plaintext, decrypted)
    }
    
    @Test
    fun testSHA256Hashing() {
        val file = createTestFile()
        val hash = SafeHavenCrypto.generateSHA256(file)
        assertEquals(64, hash.length)
    }
}
```

---

## Why This Module Exists

**Reusability**: SafeHaven features can be used in:
1. Standalone SafeHaven app
2. NeuroThrive app (hidden integration)
3. Other survivor safety apps
4. White-label deployments

**Separation of Concerns**:
- Core security logic isolated
- Easy to audit
- Single source of truth
- Consistent behavior across apps

---

## Version History

**1.0.0** (Nov 17, 2025)
- Initial release
- 6 Room entities + DAOs
- AES-256-GCM encryption
- Silent camera system
- Panic delete
- Document verification
- Intersectional resource matching

---

## License

See main repository LICENSE file.

---

## Support

For integration help, see **NEUROTHRIVE_INTEGRATION_GUIDE.md**
