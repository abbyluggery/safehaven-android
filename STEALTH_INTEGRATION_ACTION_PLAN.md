# SafeHaven → NeuroThrive Stealth Integration - Action Plan

**Date**: 2025-11-18
**Current Status**: SafeHaven Android app fully built (package: `app.neurothrive.safehaven`)
**Goal**: Rebrand as "NeuroThrive" wellness app with hidden SafeHaven features

---

## 🎯 What We Have Now

### Current Android App Status ✅

**Location**: `C:/Users/Abbyl/OneDrive/Desktop/Salesforce Training/SafeHaven-Build/app/`

**Package**: `app.neurothrive.safehaven`

**Features Already Built**:
- ✅ Incident Report documentation
- ✅ Evidence Item storage (photos/videos)
- ✅ Verified Document upload
- ✅ Legal Resource database
- ✅ Healthcare Journey tracking
- ✅ Risk Assessment (AI-powered)
- ✅ Emergency Contacts
- ✅ SOS Sessions
- ✅ Survivor Profile
- ✅ Panic Delete system
- ✅ Shake detection
- ✅ Salesforce sync APIs
- ✅ Room database (encrypted)

**What's Missing for Stealth**:
- ❌ NeuroThrive wellness features (ADHD/Autism tracking)
- ❌ Secret unlock mechanism
- ❌ Rebranding as "NeuroThrive"
- ❌ Innocent app icon
- ❌ Play Store listing as wellness app

---

## 🔄 Stealth Integration Strategy

### Phase 1: Add NeuroThrive Wellness Features (2-3 days)

Add innocent features that make this look like a legitimate ADHD/Autism app:

#### **Features to Add**:

1. **Daily Routine Tracker** (Already in Salesforce, add to Android)
   ```kotlin
   // app/src/main/java/app/neurothrive/neurothrive/data/entities/DailyRoutine.kt
   @Entity(tableName = "daily_routines")
   data class DailyRoutine(
       @PrimaryKey(autoGenerate = true) val id: Long = 0,
       val date: LocalDate,
       val morningRoutine: String,
       val afternoonRoutine: String,
       val eveningRoutine: String,
       val medicationTaken: Boolean,
       val moodRating: Int, // 1-10
       val energyLevel: Int, // 1-10
       val notes: String
   )
   ```

2. **Mood Tracker**
   ```kotlin
   @Entity(tableName = "mood_entries")
   data class MoodEntry(
       @PrimaryKey(autoGenerate = true) val id: Long = 0,
       val timestamp: LocalDateTime,
       val mood: String, // "Happy", "Anxious", "Stressed", "Calm"
       val energyLevel: Int,
       val triggers: List<String>, // "Loud noise", "Crowds", "Deadlines"
       val copingStrategies: List<String>,
       val notes: String
   )
   ```

3. **Sensory Profile**
   ```kotlin
   @Entity(tableName = "sensory_preferences")
   data class SensoryProfile(
       @PrimaryKey(autoGenerate = true) val id: Long = 0,
       val soundSensitivity: Int, // 1-10
       val lightSensitivity: Int,
       val textureSensitivity: Int,
       val preferredEnvironment: String,
       val commonTriggers: List<String>,
       val safeStimming: List<String>
   )
   ```

4. **Box Breathing Exercise** (Already have in PWA, add to Android)
   ```kotlin
   @Composable
   fun BoxBreathingScreen() {
       // 4-4-4-4 breathing pattern
       // Visual circle animation
       // Calming colors
   }
   ```

5. **Imposter Syndrome Tracker** (Already in Salesforce)
   ```kotlin
   @Entity(tableName = "imposter_sessions")
   data class ImposterSession(
       @PrimaryKey(autoGenerate = true) val id: Long = 0,
       val timestamp: LocalDateTime,
       val severity: Int, // 1-10
       val pattern: String, // "Perfectionist", "Expert", "Soloist"
       val situation: String,
       val reframedThought: String
   )
   ```

6. **Medication Reminder**
   ```kotlin
   @Entity(tableName = "medications")
   data class Medication(
       @PrimaryKey(autoGenerate = true) val id: Long = 0,
       val name: String,
       val dosage: String,
       val times: List<LocalTime>, // Multiple times per day
       val reminderEnabled: Boolean
   )
   ```

---

### Phase 2: Create Two UI Modes (1-2 days)

#### **NeuroThrive Mode** (Default - What Abuser Sees)

```kotlin
// app/src/main/java/app/neurothrive/ui/navigation/NeuroThriveNavGraph.kt

sealed class NeuroThriveScreen(val route: String) {
    object Home : NeuroThriveScreen("home")
    object DailyRoutine : NeuroThriveScreen("routine")
    object MoodTracker : NeuroThriveScreen("mood")
    object BoxBreathing : NeuroThriveScreen("breathing")
    object ImposterSyndrome : NeuroThriveScreen("imposter")
    object Medications : NeuroThriveScreen("medications")
    object SensoryProfile : NeuroThriveScreen("sensory")
    object Settings : NeuroThriveScreen("settings")
}

@Composable
fun NeuroThriveNavGraph(
    navController: NavHostController,
    startDestination: String = NeuroThriveScreen.Home.route
) {
    NavHost(navController, startDestination) {
        composable(NeuroThriveScreen.Home.route) {
            NeuroThriveHomeScreen(navController)
        }
        composable(NeuroThriveScreen.DailyRoutine.route) {
            DailyRoutineScreen()
        }
        composable(NeuroThriveScreen.MoodTracker.route) {
            MoodTrackerScreen()
        }
        // ... other NeuroThrive screens
    }
}
```

#### **SafeHaven Mode** (Hidden - After Unlock)

```kotlin
// app/src/main/java/app/neurothrive/ui/navigation/SafeHavenNavGraph.kt
// This already exists, just needs to be hidden by default

sealed class SafeHavenScreen(val route: String) {
    object Dashboard : SafeHavenScreen("safehaven/dashboard")
    object IncidentReport : SafeHavenScreen("safehaven/incident")
    object Evidence : SafeHavenScreen("safehaven/evidence")
    object LegalResources : SafeHavenScreen("safehaven/legal")
    object RiskAssessment : SafeHavenScreen("safehaven/risk")
    object EmergencyContacts : SafeHavenScreen("safehaven/contacts")
    object Settings : SafeHavenScreen("safehaven/settings")
}
```

---

### Phase 3: Implement Secret Unlock (1 day)

#### **Method: Triple-Tap Logo + Hold**

```kotlin
// app/src/main/java/app/neurothrive/ui/unlock/SafeHavenUnlockDetector.kt

class SafeHavenUnlockDetector(
    private val context: Context,
    private val onUnlocked: () -> Unit
) {
    private var tapCount = 0
    private var lastTapTime = 0L
    private val prefs = context.getSharedPreferences("neurothrive_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAP_WINDOW = 500L // ms between taps
        private const val HOLD_DURATION = 2000L // ms to hold
        private const val REQUIRED_TAPS = 3
        private const val PREF_SAFEHAVEN_UNLOCKED = "safehaven_unlocked"
    }

    fun isSafeHavenUnlocked(): Boolean {
        return prefs.getBoolean(PREF_SAFEHAVEN_UNLOCKED, false)
    }

    fun onLogoInteraction(): Boolean {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastTapTime < TAP_WINDOW) {
            tapCount++
        } else {
            tapCount = 1
        }

        lastTapTime = currentTime

        return tapCount >= REQUIRED_TAPS
    }

    fun startHoldTimer(onHoldComplete: () -> Unit) {
        // Coroutine to detect 2-second hold
        GlobalScope.launch {
            delay(HOLD_DURATION)
            unlockSafeHaven()
            onHoldComplete()
        }
    }

    private fun unlockSafeHaven() {
        prefs.edit().putBoolean(PREF_SAFEHAVEN_UNLOCKED, true).apply()
        Timber.i("SafeHaven mode unlocked")
        onUnlocked()
    }

    fun lockSafeHaven() {
        prefs.edit().putBoolean(PREF_SAFEHAVEN_UNLOCKED, false).apply()
        Timber.i("SafeHaven mode locked")
    }
}
```

#### **Integration in HomeScreen**

```kotlin
// app/src/main/java/app/neurothrive/ui/screens/NeuroThriveHomeScreen.kt

@Composable
fun NeuroThriveHomeScreen(
    navController: NavHostController,
    unlockDetector: SafeHavenUnlockDetector
) {
    var showHoldPrompt by remember { mutableStateOf(false) }
    var showUnlockConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo with secret unlock
        Image(
            painter = painterResource(R.drawable.neurothrive_logo),
            contentDescription = "NeuroThrive",
            modifier = Modifier
                .size(120.dp)
                .padding(top = 32.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            if (unlockDetector.onLogoInteraction()) {
                                showHoldPrompt = true

                                // Start hold detection
                                val holdJob = launch {
                                    delay(2000L)
                                    if (awaitPointerEventScope { currentEvent.pressed }) {
                                        showUnlockConfirmation = true
                                    }
                                }

                                tryAwaitRelease()
                                holdJob.cancel()
                                showHoldPrompt = false
                            }
                        }
                    )
                }
        )

        // Subtle hold prompt (only shows after 3 taps)
        if (showHoldPrompt) {
            Text(
                "Hold...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.padding(8.dp)
            )
        }

        // Standard NeuroThrive UI
        Text(
            "Welcome to NeuroThrive",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            "Your neurodivergent wellness companion",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ... rest of home screen
    }

    // Unlock confirmation dialog
    if (showUnlockConfirmation) {
        SafeHavenUnlockDialog(
            onConfirm = {
                unlockDetector.unlockSafeHaven()
                showUnlockConfirmation = false
                navController.navigate(SafeHavenScreen.Dashboard.route)
            },
            onDismiss = {
                showUnlockConfirmation = false
            }
        )
    }
}

@Composable
fun SafeHavenUnlockDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Advanced Privacy Features") },
        text = {
            Column {
                Text(
                    "You've discovered the advanced privacy and safety features.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "These features include enhanced data encryption, " +
                    "secure evidence storage, and emergency safety tools.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "⚠️ Only enable if you understand these features.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Enable Advanced Features")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
```

---

### Phase 4: Modify Navigation (1 day)

#### **Unified MainActivity with Mode Switching**

```kotlin
// app/src/main/java/app/neurothrive/safehaven/MainActivity.kt (Updated)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var panicDeleteUseCase: PanicDeleteUseCase

    private lateinit var shakeDetector: ShakeDetector
    private lateinit var unlockDetector: SafeHavenUnlockDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize unlock detector
        unlockDetector = SafeHavenUnlockDetector(this) {
            // Reload UI when SafeHaven unlocked
            recreate()
        }

        // Initialize shake detector (only if SafeHaven unlocked)
        if (unlockDetector.isSafeHavenUnlocked()) {
            shakeDetector = ShakeDetector(this) {
                showPanicConfirmationDialog()
            }
        }

        setContent {
            SafeHavenTheme {
                val navController = rememberNavController()
                val safeHavenUnlocked = remember {
                    mutableStateOf(unlockDetector.isSafeHavenUnlocked())
                }

                Surface(color = MaterialTheme.colorScheme.background) {
                    // Show different nav graphs based on unlock status
                    if (safeHavenUnlocked.value) {
                        // SafeHaven mode - show both NeuroThrive AND SafeHaven features
                        CombinedNavGraph(
                            navController = navController,
                            startDestination = Screen.SafeHavenDashboard.route
                        )
                    } else {
                        // NeuroThrive mode - only wellness features
                        NeuroThriveNavGraph(
                            navController = navController,
                            unlockDetector = unlockDetector,
                            startDestination = NeuroThriveScreen.Home.route
                        )
                    }
                }
            }
        }

        Timber.d("MainActivity created - SafeHaven unlocked: ${unlockDetector.isSafeHavenUnlocked()}")
    }

    // ... rest of MainActivity
}
```

---

### Phase 5: Rebranding (1 day)

#### **App Name & Icon**

```xml
<!-- app/src/main/res/values/strings.xml -->
<resources>
    <string name="app_name">NeuroThrive</string>
    <string name="app_tagline">Your neurodivergent wellness companion</string>
    <string name="home_title">Welcome to NeuroThrive</string>
    <string name="daily_routine">Daily Routine</string>
    <string name="mood_tracker">Mood Tracker</string>
    <string name="box_breathing">Calm Breathing</string>
    <string name="imposter_syndrome">Confidence Tools</string>
    <string name="medications">Medications</string>
    <string name="sensory_profile">Sensory Preferences</string>

    <!-- SafeHaven strings (only used when unlocked) -->
    <string name="safehaven_title">Personal Safety</string>
    <string name="incident_report">Incident Documentation</string>
    <string name="evidence_storage">Evidence Storage</string>
    <string name="legal_resources">Legal Resources</string>
</resources>
```

#### **App Icon (Innocent)**

Create launcher icon:
- **Design**: Purple/teal brain with butterfly
- **Colors**: ADHD awareness colors (purple, teal)
- **Style**: Friendly, wellness-focused
- **NO**: Shields, locks, SOS symbols

#### **Package Name** (Keep as-is)

```
package app.neurothrive.safehaven
```

**Why keep "safehaven" in package**:
- Package names aren't visible to users
- Changing package name breaks everything
- Only developers see it
- Abusers won't see package name

---

### Phase 6: Play Store Listing (Stealth)

```
App Name: NeuroThrive

Short Description:
ADHD & Autism support for daily routines, mood tracking, and wellness

Full Description:
NeuroThrive is your personal wellness companion designed for neurodivergent
adults living with ADHD, Autism, or both (AuDHD).

FEATURES:
✨ Daily Routine Tracking - Build consistent habits
📊 Mood & Energy Logging - Understand your patterns
🌊 Box Breathing - Calm anxiety and sensory overload
💊 Medication Reminders - Never miss a dose
🧠 Imposter Syndrome Support - Reframe negative thoughts
👂 Sensory Profiles - Track what works for you

Perfect for:
• Adults with ADHD
• Autistic adults
• AuDHD individuals
• Anyone seeking neurodivergent-friendly wellness tools

Privacy-first design with optional enhanced encryption for sensitive notes.

Keywords:
ADHD, Autism, AuDHD, neurodivergent, mental health, wellness, routine,
mood tracker, anxiety, sensory processing, executive function
```

**NO MENTION OF**:
- ❌ Domestic violence
- ❌ Abuse
- ❌ Evidence collection
- ❌ Safety planning
- ❌ Emergency features

---

## 📁 File Structure After Integration

```
app/src/main/java/app/neurothrive/
├── safehaven/                          (Existing SafeHaven code - hidden)
│   ├── data/
│   │   ├── entities/
│   │   │   ├── IncidentReport.kt
│   │   │   ├── EvidenceItem.kt
│   │   │   ├── VerifiedDocument.kt
│   │   │   └── ... (all SafeHaven entities)
│   │   ├── dao/
│   │   └── repository/
│   ├── domain/
│   │   └── usecases/
│   │       ├── PanicDeleteUseCase.kt
│   │       ├── RiskAssessmentUseCase.kt
│   │       └── ... (all SafeHaven use cases)
│   ├── ui/
│   │   ├── screens/
│   │   │   ├── IncidentReportScreen.kt
│   │   │   ├── EvidenceScreen.kt
│   │   │   └── ... (all SafeHaven screens)
│   │   └── navigation/
│   │       └── SafeHavenNavGraph.kt
│   └── MainActivity.kt
│
├── neurothrive/                        (NEW - NeuroThrive wellness features)
│   ├── data/
│   │   ├── entities/
│   │   │   ├── DailyRoutine.kt
│   │   │   ├── MoodEntry.kt
│   │   │   ├── SensoryProfile.kt
│   │   │   ├── Medication.kt
│   │   │   └── ImposterSession.kt
│   │   ├── dao/
│   │   └── repository/
│   ├── ui/
│   │   ├── screens/
│   │   │   ├── NeuroThriveHomeScreen.kt
│   │   │   ├── DailyRoutineScreen.kt
│   │   │   ├── MoodTrackerScreen.kt
│   │   │   ├── BoxBreathingScreen.kt
│   │   │   ├── ImposterSyndromeScreen.kt
│   │   │   └── MedicationScreen.kt
│   │   └── navigation/
│   │       ├── NeuroThriveNavGraph.kt
│   │       └── CombinedNavGraph.kt
│   └── unlock/
│       └── SafeHavenUnlockDetector.kt
│
└── common/                             (Shared utilities)
    ├── theme/
    │   └── SafeHavenTheme.kt           (Rename to NeuroThriveTheme)
    └── util/
```

---

## ✅ Implementation Checklist

### Week 1: NeuroThrive Features

- [ ] Create DailyRoutine entity, DAO, repository
- [ ] Create MoodEntry entity, DAO, repository
- [ ] Create SensoryProfile entity, DAO, repository
- [ ] Create Medication entity, DAO, repository
- [ ] Create ImposterSession entity, DAO, repository
- [ ] Build DailyRoutineScreen
- [ ] Build MoodTrackerScreen
- [ ] Build BoxBreathingScreen
- [ ] Build ImposterSyndromeScreen
- [ ] Build MedicationScreen
- [ ] Build SensoryProfileScreen

### Week 2: Stealth Integration

- [ ] Create SafeHavenUnlockDetector
- [ ] Add triple-tap + hold to logo
- [ ] Create NeuroThriveNavGraph (innocent features only)
- [ ] Create CombinedNavGraph (both modes)
- [ ] Update MainActivity with mode switching
- [ ] Add auto-lock mechanism (5 min inactivity)
- [ ] Add quick-lock gesture (2 shakes)
- [ ] Test unlock/lock flows

### Week 3: Rebranding & Polish

- [ ] Rename app to "NeuroThrive"
- [ ] Design innocent app icon
- [ ] Update all strings.xml
- [ ] Update theme colors (purple/teal)
- [ ] Create Play Store screenshots (only NeuroThrive features)
- [ ] Write Play Store description
- [ ] Test with potential abuser simulation
- [ ] Final security audit

### Week 4: Testing & Deployment

- [ ] End-to-end testing
- [ ] Unlock mechanism testing
- [ ] Data encryption verification
- [ ] Panic delete testing (5-second hold)
- [ ] File obfuscation testing
- [ ] Network traffic analysis
- [ ] Build release APK
- [ ] Submit to Play Store

---

## 🚀 Quick Start (Today)

### Immediate Next Steps:

**1. Create NeuroThrive Package Structure**

```bash
cd "C:/Users/Abbyl/OneDrive/Desktop/Salesforce Training/SafeHaven-Build"

# Create neurothrive package
mkdir -p app/src/main/java/app/neurothrive/neurothrive
mkdir -p app/src/main/java/app/neurothrive/neurothrive/data/entities
mkdir -p app/src/main/java/app/neurothrive/neurothrive/ui/screens
mkdir -p app/src/main/java/app/neurothrive/neurothrive/unlock
```

**2. Copy Template Files**

I'll create template files for:
- DailyRoutine.kt
- MoodEntry.kt
- NeuroThriveHomeScreen.kt
- SafeHavenUnlockDetector.kt
- NeuroThriveNavGraph.kt

**3. Update MainActivity**

Integrate unlock detection and mode switching.

---

## 📊 Timeline

**Total Time**: 3-4 weeks for complete stealth integration

- Week 1: NeuroThrive wellness features (40 hours)
- Week 2: Stealth unlock mechanism (30 hours)
- Week 3: Rebranding & polish (20 hours)
- Week 4: Testing & deployment (20 hours)

**Minimum Viable Stealth Version**: 1 week (focus on unlock + rebrand only)

---

## ✅ Summary

### Current Status

**You have**: Complete SafeHaven Android app with all DV features built
- Location: `SafeHaven-Build/app/`
- Package: `app.neurothrive.safehaven`
- Features: Incident reports, evidence, panic delete, Salesforce sync

### What's Needed for Stealth

**Add**: NeuroThrive wellness features (ADHD/Autism tracking)
**Implement**: Secret unlock mechanism (triple-tap + hold)
**Rebrand**: Change name/icon to "NeuroThrive"
**Deploy**: Play Store as wellness app

### Integration Architecture

**NeuroThrive Mode** (Default):
- Only shows: Daily routines, mood tracking, breathing exercises
- Looks like: Innocent ADHD/Autism wellness app
- Accessible to: Everyone (including abusers)

**SafeHaven Mode** (After Unlock):
- Shows: All NeuroThrive features PLUS SafeHaven features
- Looks like: Same wellness app with "Advanced Privacy" section
- Accessible to: Only survivors who know the secret code

**Result**: One app that serves dual purpose, completely invisible to abusers.

Would you like me to start creating the template files for the NeuroThrive wellness features?
