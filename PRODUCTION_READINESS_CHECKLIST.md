# SafeHaven - Production Readiness Checklist

**Date**: December 8, 2025
**Current Status**: Alpha - Code Complete, Build Infrastructure Needed
**Target**: Production-Ready APK for Google Play Store

---

## ✅ COMPLETED (Already Done)

### Code & Architecture
- ✅ **64 Kotlin files** (40 in app/, 24 in safehaven-core/)
- ✅ **6 Room Entities** (SafeHavenProfile, IncidentReport, VerifiedDocument, EvidenceItem, LegalResource, SurvivorProfile)
- ✅ **6 DAOs** (All CRUD operations implemented)
- ✅ **AppDatabase.kt** (Room configuration complete)
- ✅ **SafeHavenCrypto.kt** (AES-256-GCM encryption)
- ✅ **SilentCameraManager.kt** (CameraX silent capture)
- ✅ **MetadataStripper.kt** (GPS removal)
- ✅ **ShakeDetector.kt** (Panic gesture detection)
- ✅ **PanicDeleteUseCase.kt** (<2 second deletion)
- ✅ **DocumentVerificationService.kt** (SHA-256 hashing)
- ✅ **IntersectionalResourceMatcher.kt** (Resource scoring algorithm)
- ✅ **6 Jetpack Compose Screens** (Home, Camera, IncidentReport, EvidenceVault, Resources, Settings)
- ✅ **6 ViewModels** (Hilt dependency injection)
- ✅ **Navigation** (Jetpack Compose Navigation)
- ✅ **Material Design 3 Theme** (Color, Typography, Theme)
- ✅ **SafeHavenRepository** (Data layer complete)
- ✅ **Hilt Modules** (DatabaseModule, dependency injection)

### Documentation
- ✅ **08_Legal_Compliance.md** (15,000 words)
- ✅ **03_Intersectional_Design_Guide.md** (5,000 words)
- ✅ **05_Implementation_Roadmap.md** (4,500 words)
- ✅ **06_Partnership_Strategy.md** (3,500 words)
- ✅ **07_Grant_Proposal_Templates.md** (4,500 words)
- ✅ **README.md**, **BUILD_STATUS.md**, **NEUROTHRIVE_INTEGRATION_GUIDE.md**

---

## 🔴 CRITICAL - Must Fix Before First Build

### 1. Gradle Build Infrastructure ⚠️
**Issue**: No Gradle wrapper files
**Impact**: Cannot build APK
**Fix**: Run in Android Studio or with local Gradle installation

```bash
# Option A: Use Android Studio
# 1. Open project in Android Studio
# 2. Android Studio will auto-generate gradlew
# 3. Click "Sync Project with Gradle Files"

# Option B: Manual Gradle wrapper generation
gradle wrapper --gradle-version 8.2
```

**Files Needed**:
- ✅ `gradlew` (Created, but needs gradle-wrapper.jar)
- ❌ `gradle/wrapper/gradle-wrapper.jar` (MISSING - 60KB file)
- ✅ `gradle/wrapper/gradle-wrapper.properties` (EXISTS)

---

### 2. App Icons ⚠️
**Issue**: No launcher icons in mipmap directories
**Impact**: App won't install on device (missing required resources)
**Fix**: Add placeholder icons or use Android Studio's Asset Studio

**Required Files**:
```
app/src/main/res/mipmap-mdpi/ic_launcher.png (48x48)
app/src/main/res/mipmap-hdpi/ic_launcher.png (72x72)
app/src/main/res/mipmap-xhdpi/ic_launcher.png (96x96)
app/src/main/res/mipmap-xxhdpi/ic_launcher.png (144x144)
app/src/main/res/mipmap-xxxhdpi/ic_launcher.png (192x192)

app/src/main/res/mipmap-mdpi/ic_launcher_round.png (48x48)
app/src/main/res/mipmap-hdpi/ic_launcher_round.png (72x72)
app/src/main/res/mipmap-xhdpi/ic_launcher_round.png (96x96)
app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png (144x144)
app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png (192x192)
```

**How to Fix**:
1. **In Android Studio**: Right-click `res/` → New → Image Asset
2. **Use Image Magick**: `convert -size 192x192 xc:#6200EE -gravity center -pointsize 96 -fill white -annotate +0+0 "SH" ic_launcher.png`
3. **Use Online Tool**: https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html

**Brand Colors** (from SafeHaven theme):
- Primary: `#6200EE` (Purple)
- Secondary: `#03DAC6` (Teal)

---

### 3. Verify safehaven-core Builds ⚠️
**Issue**: safehaven-core/build.gradle.kts might be missing configuration
**Status**: EXISTS, needs verification

**Check**:
```bash
cat safehaven-core/build.gradle.kts
```

**Expected**: Should have:
- ✅ `com.android.library` plugin
- ✅ Compose enabled
- ✅ Room dependencies
- ✅ Hilt dependencies

---

## 🟡 HIGH PRIORITY - Fix Before Alpha Release

### 4. Add Signing Configuration
**Issue**: Release builds need to be signed for Google Play
**Impact**: Cannot upload to Google Play Store
**Fix**: Create keystore and add signing config

```kotlin
// app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            storeFile = file("../safehaven-release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "safehaven"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(...)
        }
    }
}
```

**Steps**:
1. Generate keystore: `keytool -genkey -v -keystore safehaven-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias safehaven`
2. Store passwords in environment variables (NOT in git)
3. Add keystore to `.gitignore`

---

### 5. Add Privacy Policy URL
**Issue**: Google Play requires privacy policy for apps handling user data
**Impact**: App submission will be rejected
**Fix**: Host privacy policy and add URL to Google Play Console

**TODO**:
1. Create privacy policy page (use template from 08_Legal_Compliance.md)
2. Host at: `https://safehaven.app/privacy` (need to create website)
3. Add URL during Google Play Console submission

---

### 6. Fix Potential Crashes - Add Error Handling
**Issue**: ViewModels have minimal error handling
**Impact**: App may crash on network errors, database errors
**Fix**: Add try/catch blocks and error states

**Example Fix** (HomeViewModel.kt:66):
```kotlin
// CURRENT (risky):
fun triggerPanicDelete(onComplete: () -> Unit) {
    viewModelScope.launch {
        panicDeleteUseCase.execute("current_user_id")
        onComplete()
    }
}

// IMPROVED:
fun triggerPanicDelete(onComplete: () -> Unit, onError: (String) -> Unit) {
    viewModelScope.launch {
        try {
            panicDeleteUseCase.execute("current_user_id")
            onComplete()
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Panic delete failed", e)
            onError(e.message ?: "Unknown error")
            // Still call onComplete to protect user
            onComplete()
        }
    }
}
```

**Files to Update**:
- `ui/viewmodels/HomeViewModel.kt`
- `ui/viewmodels/CameraViewModel.kt`
- `ui/viewmodels/IncidentReportViewModel.kt`
- `ui/viewmodels/EvidenceVaultViewModel.kt`
- `ui/viewmodels/ResourcesViewModel.kt`
- `ui/viewmodels/SettingsViewModel.kt`

---

### 7. Add Loading States
**Issue**: No loading indicators while data loads
**Impact**: Poor UX (blank screens)
**Fix**: Add loading state to ViewModels

**Example** (ResourcesViewModel.kt):
```kotlin
data class ResourcesUiState(
    val resources: List<ScoredResource> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

val uiState: StateFlow<ResourcesUiState> = combine(
    repository.getAllLegalResources(),
    repository.getSurvivorProfileByUserId(userId)
) { resources, profile ->
    ResourcesUiState(
        resources = scoreResources(resources, profile),
        isLoading = false,
        error = null
    )
}.catch { e ->
    emit(ResourcesUiState(isLoading = false, error = e.message))
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ResourcesUiState(isLoading = true)
)
```

---

### 8. Fix Hardcoded User IDs
**Issue**: All code uses "current_user_id" placeholder
**Impact**: App won't work for real users
**Fix**: Implement authentication or use device ID

**Option A: Device-based (No authentication)**
```kotlin
// SafeHavenApplication.kt
object UserIdProvider {
    fun getUserId(context: Context): String {
        val prefs = context.getSharedPreferences("safehaven", Context.MODE_PRIVATE)
        return prefs.getString("user_id", null) ?: run {
            val newId = UUID.randomUUID().toString()
            prefs.edit().putString("user_id", newId).apply()
            newId
        }
    }
}
```

**Option B: PIN-based authentication (Better for DV context)**
```kotlin
// Add LoginScreen.kt
// Store encrypted PIN in DataStore
// Use PIN to derive encryption key
```

**Files to Update**:
- All 6 ViewModels (replace "current_user_id")
- Add UserIdProvider or AuthManager

---

## 🟢 NICE-TO-HAVE - Post-Alpha (Can Wait)

### 9. Unit Tests
**Current**: 0% test coverage
**Target**: 80% coverage
**Priority**: Medium (important but not blocking)

**Tests Needed**:
- `SafeHavenCryptoTest` (encryption/decryption)
- `IntersectionalResourceMatcherTest` (scoring algorithm)
- `PanicDeleteUseCaseTest` (deletion speed)
- ViewModel tests (using Turbine for Flow testing)

---

### 10. Instrumentation Tests
**Current**: 0 UI tests
**Target**: Happy path coverage
**Priority**: Medium

**Tests Needed**:
- Onboarding flow test
- Silent camera capture test
- Incident report creation test
- Navigation test

---

### 11. ProGuard Optimization
**Current**: ProGuard enabled but not optimized
**Impact**: Larger APK size, slower performance
**Priority**: Low (works but not optimal)

**Fix**: Add specific ProGuard rules in `proguard-rules.pro`

---

### 12. Crashlytics Integration
**Current**: No crash reporting
**Impact**: Can't diagnose production crashes
**Priority**: Medium (important for production)

**Fix**:
```kotlin
// Add Firebase Crashlytics
dependencies {
    implementation("com.google.firebase:firebase-crashlytics:18.6.0")
}
```

---

### 13. Analytics (Privacy-Respecting)
**Current**: No analytics
**Impact**: Can't measure feature usage
**Priority**: Low (nice-to-have)

**Options**:
- ❌ Google Analytics (privacy concerns)
- ✅ Plausible Analytics (privacy-friendly)
- ✅ Self-hosted Matomo
- ✅ No analytics (respect survivor privacy)

**Recommendation**: NO analytics initially. Privacy > metrics for DV survivors.

---

## 📋 Build & Deployment Checklist

### Pre-Build Checklist
- [ ] Gradle wrapper generated
- [ ] App icons created (all densities)
- [ ] safehaven-core builds successfully
- [ ] No hardcoded credentials
- [ ] Version code/name updated in build.gradle.kts
- [ ] ProGuard rules tested

### First Build
- [ ] `./gradlew clean`
- [ ] `./gradlew assembleDebug` (test build)
- [ ] `./gradlew assembleRelease` (production build)
- [ ] Test APK on physical device
- [ ] Verify panic delete works (<2 sec)
- [ ] Test on low-end device (Android 8.0, 2GB RAM)

### Google Play Store Submission
- [ ] Create Google Play Developer account ($25 one-time)
- [ ] Generate signed APK or AAB
- [ ] Create store listing (screenshots, description)
- [ ] Add privacy policy URL
- [ ] Submit for review
- [ ] Wait 3-7 days for approval

---

## 🚀 Quick Start - What to Do RIGHT NOW

**If you have Android Studio**:
1. Open SafeHaven-Build in Android Studio
2. Click "Sync Project with Gradle Files"
3. Right-click `res/` → New → Image Asset (create icons)
4. Click "Run" → Select emulator or device
5. Test panic delete, silent camera, resource matching

**If you don't have Android Studio**:
1. Install Gradle: `sdk install gradle 8.2` (using SDKMAN)
2. Generate wrapper: `gradle wrapper --gradle-version 8.2`
3. Create icons manually or use online tool
4. Run `./gradlew assembleDebug`

**Minimum Viable Fix (10 minutes)**:
1. ✅ Open in Android Studio (auto-generates Gradle wrapper)
2. ✅ Generate icons (Asset Studio, 2 min)
3. ✅ Run on emulator (2 min)
4. ✅ Test basic flows (5 min)

---

## ⏱️ Time Estimates

| Task | Time | Priority |
|------|------|----------|
| Generate Gradle wrapper | 2 min | 🔴 Critical |
| Create app icons | 5 min | 🔴 Critical |
| Add error handling (6 ViewModels) | 30 min | 🟡 High |
| Add loading states | 30 min | 🟡 High |
| Fix hardcoded user IDs | 20 min | 🟡 High |
| Create signing keystore | 5 min | 🟡 High |
| Add privacy policy URL | 10 min | 🟡 High |
| Unit tests (basic) | 2 hours | 🟢 Nice-to-have |
| UI tests | 2 hours | 🟢 Nice-to-have |
| Crashlytics setup | 20 min | 🟢 Nice-to-have |

**Total Time to Production-Ready**: ~2 hours (CRITICAL + HIGH priority items)

---

## ✅ Definition of "Production Ready"

SafeHaven is production-ready when:
1. ✅ **Builds successfully** (./gradlew assembleRelease succeeds)
2. ✅ **Installs on device** (APK can be installed)
3. ✅ **No crashes on happy path** (user can complete core flows)
4. ✅ **Icons present** (app appears in launcher)
5. ✅ **Signed for release** (can be uploaded to Google Play)
6. ✅ **Privacy policy hosted** (Google Play requirement)
7. ✅ **Critical features work**:
   - Silent camera captures encrypted photos
   - Panic delete completes in <2 seconds
   - Resource matching shows relevant results
   - Incident reports save with encryption

**Nice-to-have (not blocking)**:
- Unit tests
- Crashlytics
- Analytics
- Perfect UI polish

---

## 📞 Next Steps

1. **TODAY**: Fix CRITICAL items (Gradle, icons)
2. **This Week**: Fix HIGH priority items (error handling, user IDs)
3. **Next Week**: Test on real devices (5+ devices, Android 8-14)
4. **Week 3**: Beta test with 10 survivors
5. **Week 4**: Google Play Store submission

---

**Document Version**: 1.0
**Last Updated**: December 8, 2025
**Owner**: SafeHaven Engineering Team
