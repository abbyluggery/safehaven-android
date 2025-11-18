# SafeHaven Integration Guide for NeuroThrive App

**Purpose**: Integrate SafeHaven as a hidden feature within the NeuroThrive app for survivor safety  
**Security Strategy**: NeuroThrive appears as an innocent wellness/mental health app, but secretly contains SafeHaven features  
**Date**: November 17, 2025

---

## Why This Integration Matters

**Dual App Strategy**:
1. **Standalone SafeHaven App**: For survivors who want direct access
2. **Hidden in NeuroThrive**: For survivors who need plausible deniability

**Safety Benefit**: If an abuser sees "NeuroThrive" on the phone, it looks like a harmless mental health/wellness app. They won't know it contains SafeHaven's domestic violence safety features.

---

## Integration Architecture

```
NeuroThrive App
├── neurothrive-app/ (your existing app module)
├── neurothrive-core/ (your existing core features)
└── safehaven-core/ (ADDED - hidden DV safety features)
    ├── Silent Camera
    ├── Panic Delete
    ├── Encrypted Evidence Vault
    ├── Document Verification
    └── Intersectional Resource Matcher
```

---

## Step 1: Add safehaven-core Module to NeuroThrive

### 1.1 Copy Module Files

Copy the entire `safehaven-core/` directory into your NeuroThrive project:

```bash
# From SafeHaven-Build repository
cp -r safehaven-core/ /path/to/NeuroThrive/
```

### 1.2 Update settings.gradle.kts

Add SafeHaven core to your NeuroThrive project:

```kotlin
// NeuroThrive/settings.gradle.kts
rootProject.name = "NeuroThrive"
include(":app")
include(":neurothrive-core")  // Your existing module
include(":safehaven-core")     // ADDED
```

### 1.3 Add Dependency to App Module

```kotlin
// NeuroThrive/app/build.gradle.kts
dependencies {
    // Existing dependencies...
    implementation(project(":neurothrive-core"))
    
    // ADD THIS:
    implementation(project(":safehaven-core"))
}
```

---

## Step 2: Initialize SafeHaven Silently

### 2.1 Update Application Class

**CRITICAL**: Initialize SafeHaven encryption on app startup, but do it silently:

```kotlin
// NeuroThriveApplication.kt
import app.neurothrive.safehaven.util.crypto.SafeHavenCrypto
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NeuroThriveApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Your existing NeuroThrive initialization...
        initializeNeuroThrive()
        
        // ADDED: Initialize SafeHaven encryption (silent, no UI)
        SafeHavenCrypto.initializeKey()
    }
    
    private fun initializeNeuroThrive() {
        // Your existing code...
    }
}
```

### 2.2 Update AndroidManifest.xml

Merge SafeHaven permissions into your existing manifest:

```xml
<!-- NeuroThrive/app/src/main/AndroidManifest.xml -->
<manifest>
    <!-- Your existing permissions... -->
    
    <!-- ADDED: SafeHaven permissions (look innocent) -->
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.VIBRATE" />
    
    <!-- Note: Location permission already likely in NeuroThrive -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    
    <application>
        <!-- Your existing activities... -->
    </application>
</manifest>
```

---

## Step 3: Create Hidden Entry Points

**CRITICAL SECURITY FEATURE**: Make SafeHaven accessible through innocent-looking gestures/screens.

### Option A: Hidden Gesture in Wellness Journal

Add a hidden gesture to your existing wellness/journal screen:

```kotlin
// In your NeuroThrive wellness journal screen
@Composable
fun WellnessJournalScreen() {
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastTapTime < 500) {
                            tapCount++
                            if (tapCount == 3) {
                                // 3 rapid double-taps = open SafeHaven
                                navController.navigate("safehaven_home")
                                tapCount = 0
                            }
                        } else {
                            tapCount = 1
                        }
                        lastTapTime = currentTime
                    }
                )
            }
    ) {
        // Your normal wellness journal UI...
    }
}
```

### Option B: Hidden Menu Item

Add a hidden menu item that only appears after a specific action:

```kotlin
// In your NeuroThrive settings screen
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val showSafeHaven by viewModel.showSafeHaven.collectAsState()
    
    Column {
        // Regular settings...
        SettingItem("Profile")
        SettingItem("Notifications")
        SettingItem("Privacy")
        
        // Hidden item - only shows after tapping "About" 5 times rapidly
        if (showSafeHaven) {
            SettingItem(
                title = "Personal Safety",
                onClick = { navController.navigate("safehaven_home") }
            )
        }
        
        SettingItem(
            title = "About",
            onClick = { viewModel.incrementAboutTaps() }
        )
    }
}
```

### Option C: Secret Code Entry

Add a hidden text field in your profile screen:

```kotlin
// In NeuroThrive profile or settings
@Composable
fun ProfileScreen() {
    var codeEntry by remember { mutableStateOf("") }
    
    // Hidden TextField (visually looks like empty space)
    TextField(
        value = codeEntry,
        onValueChange = { 
            codeEntry = it
            if (it == "safehaven2025") {
                navController.navigate("safehaven_home")
                codeEntry = ""
            }
        },
        modifier = Modifier
            .width(1.dp)
            .height(1.dp)
            .alpha(0f),  // Invisible
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
    
    // Your normal profile UI...
}
```

---

## Step 4: Setup Navigation

Add SafeHaven screens to your navigation graph:

```kotlin
// NeuroThrive/app/src/main/java/com/neurothrive/navigation/NavGraph.kt
import app.neurothrive.safehaven.ui.screens.SafeHavenHomeScreen
import app.neurothrive.safehaven.ui.screens.documentation.SilentCameraScreen
import app.neurothrive.safehaven.ui.screens.documentation.IncidentReportScreen
import app.neurothrive.safehaven.ui.screens.documentation.EvidenceVaultScreen

@Composable
fun NeuroThriveNavGraph(
    navController: NavHostController,
    startDestination: String = "home"
) {
    NavHost(navController, startDestination) {
        // Your existing NeuroThrive screens...
        composable("home") { NeuroThriveHomeScreen() }
        composable("journal") { WellnessJournalScreen() }
        composable("meditation") { MeditationScreen() }
        
        // ADDED: SafeHaven screens (hidden from main navigation)
        composable("safehaven_home") { 
            SafeHavenHomeScreen(
                onNavigateToCamera = { navController.navigate("safehaven_camera") },
                onNavigateToIncidents = { navController.navigate("safehaven_incidents") },
                onNavigateToEvidence = { navController.navigate("safehaven_evidence") },
                onNavigateToResources = { navController.navigate("safehaven_resources") }
            )
        }
        
        composable("safehaven_camera") { 
            SilentCameraScreen(
                onBack = { navController.popBackStack() }
            ) 
        }
        
        composable("safehaven_incidents") { 
            IncidentReportScreen(
                onBack = { navController.popBackStack() }
            ) 
        }
        
        composable("safehaven_evidence") { 
            EvidenceVaultScreen(
                onBack = { navController.popBackStack() }
            ) 
        }
        
        composable("safehaven_resources") { 
            ResourceFinderScreen(
                onBack = { navController.popBackStack() }
            ) 
        }
    }
}
```

---

## Step 5: Setup Hilt Dependency Injection

SafeHaven uses Hilt for DI. Merge with your existing Hilt setup:

```kotlin
// If NeuroThrive already uses Hilt, just add SafeHaven modules:

// NeuroThrive/app/src/main/java/com/neurothrive/di/AppModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    // Your existing providers...
    
    // ADDED: SafeHaven Database
    @Provides
    @Singleton
    fun provideSafeHavenDatabase(
        @ApplicationContext context: Context
    ): app.neurothrive.safehaven.data.database.AppDatabase {
        return Room.databaseBuilder(
            context,
            app.neurothrive.safehaven.data.database.AppDatabase::class.java,
            "safehaven_db"  // Separate database from NeuroThrive
        )
        .fallbackToDestructiveMigration()
        .build()
    }
}
```

**IMPORTANT**: SafeHaven uses a **separate database** (`safehaven_db`) from NeuroThrive. This ensures:
- Panic delete only wipes SafeHaven data, not NeuroThrive data
- Clear separation of concerns
- No data conflicts

---

## Step 6: Panic Delete Integration

**CRITICAL**: Ensure panic delete only affects SafeHaven data, not NeuroThrive.

### 6.1 Setup ShakeDetector

Add shake detection to MainActivity (only when SafeHaven is unlocked):

```kotlin
// NeuroThrive MainActivity
import app.neurothrive.safehaven.util.sensors.ShakeDetector
import app.neurothrive.safehaven.domain.usecases.PanicDeleteUseCase

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var panicDeleteUseCase: PanicDeleteUseCase
    
    private var shakeDetector: ShakeDetector? = null
    private var safeHavenUnlocked = false  // Only enable when SafeHaven active
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize shake detector (but don't start yet)
        shakeDetector = ShakeDetector(this) {
            showPanicConfirmationDialog()
        }
        
        setContent {
            // Your existing NeuroThrive UI...
        }
    }
    
    // Call this when user enters SafeHaven section
    fun enablePanicDelete() {
        safeHavenUnlocked = true
        shakeDetector?.start()
    }
    
    // Call this when user exits SafeHaven section
    fun disablePanicDelete() {
        safeHavenUnlocked = false
        shakeDetector?.stop()
    }
    
    private fun showPanicConfirmationDialog() {
        if (!safeHavenUnlocked) return
        
        AlertDialog.Builder(this)
            .setTitle("Emergency Delete")
            .setMessage("Delete all SafeHaven data? This cannot be undone. Your NeuroThrive data will not be affected.")
            .setPositiveButton("DELETE SAFEHAVEN DATA") { _, _ ->
                lifecycleScope.launch {
                    panicDeleteUseCase.execute(currentUserId)
                    // Return to NeuroThrive home (SafeHaven data gone)
                    navController.navigate("home") {
                        popUpTo("safehaven_home") { inclusive = true }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        shakeDetector?.stop()
    }
}
```

---

## Step 7: Stealth Features

### 7.1 App Switcher Disguise

Make SafeHaven screens appear as NeuroThrive in recent apps:

```kotlin
// When entering SafeHaven
ComponentActivity.setTaskDescription(
    ActivityManager.TaskDescription(
        "NeuroThrive - Wellness Journal",  // Fake title
        R.drawable.neurothrive_icon,       // NeuroThrive icon
        Color.parseColor("#6200EE")        // NeuroThrive brand color
    )
)
```

### 7.2 Notification Disguise

If SafeHaven needs to show notifications, disguise them:

```kotlin
val notification = NotificationCompat.Builder(context, CHANNEL_ID)
    .setContentTitle("NeuroThrive Reminder")  // Looks innocent
    .setContentText("Your wellness check-in is ready")  // Generic message
    .setSmallIcon(R.drawable.neurothrive_icon)  // NeuroThrive icon
    .build()
```

### 7.3 Lock Screen Hiding

Prevent SafeHaven screens from appearing on lock screen:

```kotlin
// In SafeHaven screens
ComponentActivity.setShowWhenLocked(false)
ComponentActivity.window.setFlags(
    WindowManager.LayoutParams.FLAG_SECURE,
    WindowManager.LayoutParams.FLAG_SECURE
)
```

---

## Step 8: Database Management

SafeHaven uses a **separate encrypted database**:

```
NeuroThrive Databases:
- neurothrive_db (your existing data - NOT affected by panic delete)
- safehaven_db (SafeHaven data - DELETED by panic delete)
```

This separation ensures:
- ✅ Panic delete only removes SafeHaven evidence
- ✅ NeuroThrive wellness data remains intact
- ✅ Clear data boundaries
- ✅ No accidental data loss

---

## Step 9: Testing Integration

### 9.1 Test Hidden Entry

1. Open NeuroThrive app
2. Navigate to wellness journal (or wherever you added hidden entry)
3. Perform hidden gesture (e.g., 3 rapid double-taps)
4. Verify SafeHaven home screen appears
5. Verify it looks distinct from NeuroThrive

### 9.2 Test Panic Delete

1. Enter SafeHaven section
2. Add test evidence (photo, incident report)
3. Shake device 3 times rapidly
4. Confirm panic delete dialog appears
5. Accept deletion
6. Verify SafeHaven data deleted
7. **CRITICAL**: Verify NeuroThrive data still intact

### 9.3 Test Stealth

1. Open SafeHaven section
2. Switch to recent apps (app switcher)
3. Verify it shows "NeuroThrive" (not "SafeHaven")
4. Verify icon is NeuroThrive icon
5. Lock device
6. Verify SafeHaven screens don't appear on lock screen

---

## Step 10: User Onboarding (Hidden)

When user first enters SafeHaven section, show brief introduction:

```kotlin
@Composable
fun SafeHavenFirstTimeDialog() {
    AlertDialog(
        onDismissRequest = { /* ... */ },
        title = { Text("Personal Safety Features") },
        text = { 
            Text(
                "You've unlocked SafeHaven's personal safety features.\n\n" +
                "• Silent photo documentation\n" +
                "• Encrypted evidence storage\n" +
                "• Emergency resource finder\n" +
                "• Panic delete (shake 3 times)\n\n" +
                "All data is encrypted and separate from your NeuroThrive wellness journal."
            )
        },
        confirmButton = {
            Button(onClick = { /* ... */ }) {
                Text("Continue")
            }
        }
    )
}
```

---

## Security Considerations

### ✅ Data Separation
- SafeHaven uses **separate database** from NeuroThrive
- Panic delete **only affects SafeHaven data**
- NeuroThrive wellness data **always safe**

### ✅ Stealth Mode
- App switcher shows "NeuroThrive"
- Notifications disguised as wellness reminders
- Lock screen prevents screenshots
- No obvious SafeHaven branding when hidden

### ✅ Plausible Deniability
- Camera permission: "For wellness progress photos"
- Location permission: "For finding local wellness resources"
- Storage permission: "For journal backups"

### ✅ Encryption
- All SafeHaven data encrypted with AES-256-GCM
- Android KeyStore (hardware-backed when available)
- GPS metadata stripped from all photos
- Secure file deletion on panic delete

---

## Recommended Hidden Entry Methods

**Best Options (Ranked by Safety)**:

1. **Hidden Gesture in Wellness Journal** (Best - most natural)
   - 3 rapid double-taps on wellness journal header
   - Looks like accidental taps if seen

2. **Secret Code in Settings** (Good - requires knowledge)
   - Type "safehaven" in invisible text field
   - Only survivor knows the code

3. **Multiple Taps on "About"** (Okay - might be discovered)
   - Tap "About" 5 times rapidly
   - Could be found by accident

4. **Long Press on Logo** (Not Recommended - too obvious)
   - Long press on NeuroThrive logo
   - Might be discovered easily

---

## FAQ

**Q: What if an abuser sees NeuroThrive on the phone?**  
A: NeuroThrive appears as a mental health/wellness app. SafeHaven features are completely hidden until unlocked with hidden gesture.

**Q: Will panic delete remove NeuroThrive data?**  
A: NO. Panic delete ONLY removes SafeHaven evidence data. All NeuroThrive wellness journals, meditations, etc. remain intact.

**Q: Can I use SafeHaven features without NeuroThrive?**  
A: Yes! The standalone SafeHaven app exists separately. This integration just adds a hidden option.

**Q: What if I forget the hidden entry gesture?**  
A: Document it in your onboarding flow and/or settings (disguised as "Advanced Features").

**Q: Should I show SafeHaven in NeuroThrive's main menu?**  
A: **NO**. For safety, always use hidden entry points. Visible menu items could expose survivors.

---

## Next Steps

1. ✅ Copy `safehaven-core` module to NeuroThrive project
2. ✅ Update `settings.gradle.kts` and `app/build.gradle.kts`
3. ✅ Initialize `SafeHavenCrypto` in `NeuroThriveApplication`
4. ✅ Add hidden entry point (choose one method above)
5. ✅ Setup navigation to SafeHaven screens
6. ✅ Configure panic delete (shake detector)
7. ✅ Test integration thoroughly
8. ✅ Deploy to Google Play as "NeuroThrive" (wellness app)

---

## Support

**Repository**: https://github.com/abbyluggery/SafeHaven-Build  
**Module**: `safehaven-core/`  
**Documentation**: See BUILD_STATUS.md for complete feature list

---

**This integration could save lives by providing hidden safety features within an innocent-looking wellness app.**
