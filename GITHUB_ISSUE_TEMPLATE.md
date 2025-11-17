# Complete SafeHaven Build - Salesforce Backend + Android UI

## Issue Type
- [x] Feature Implementation
- [x] Bug Fix (missing Salesforce integration)

## Priority
**CRITICAL** - User explicitly requested "Salesforce integration" which was not delivered in the first sprint.

---

## 📋 Overview

Complete the SafeHaven domestic violence safety app by building:
1. **Salesforce backend** (6 custom objects, 4 Apex REST APIs)
2. **Android UI** (12 Jetpack Compose screens)
3. **Legal resources data** (1,000+ resources CSV import)
4. **Tests** (unit + integration tests)

**Previous work**: Excellent Android backend completed (entities, DAOs, encryption, silent camera, panic delete, resource matching algorithm). See branch: `claude/safehaven-android-app-013udX3wnRYCxzmvCZP4mL97`

---

## 🎯 Acceptance Criteria

### 1. Salesforce Backend ✅

**Must Have**:
- [ ] All 6 custom objects deployed to Salesforce org:
  - [ ] SafeHaven_Profile__c
  - [ ] Incident_Report__c
  - [ ] Evidence_Item__c
  - [ ] Verified_Document__c
  - [ ] Legal_Resource__c
  - [ ] Survivor_Profile__c
- [ ] All 4 Apex REST API classes deployed:
  - [ ] SafeHavenSyncAPI.cls (profile sync)
  - [ ] IncidentReportSyncAPI.cls (incident sync)
  - [ ] LegalResourceAPI.cls (resource search)
  - [ ] DocumentVerificationAPI.cls (SHA-256 verification)
- [ ] Apex test classes with 75%+ coverage
- [ ] Connected App configured for OAuth 2.0
- [ ] Field-level encryption enabled for sensitive fields (requires Platform Shield)
- [ ] End-to-end sync test (Android → Salesforce → verify data)

**Files to Create**:
```
salesforce/
├── objects/
│   ├── SafeHaven_Profile__c.object
│   ├── Incident_Report__c.object
│   ├── Evidence_Item__c.object
│   ├── Verified_Document__c.object
│   ├── Legal_Resource__c.object
│   └── Survivor_Profile__c.object
├── classes/
│   ├── SafeHavenSyncAPI.cls
│   ├── IncidentReportSyncAPI.cls
│   ├── LegalResourceAPI.cls
│   ├── DocumentVerificationAPI.cls
│   ├── SafeHavenSyncAPITest.cls
│   └── LegalResourceAPITest.cls
├── package.xml
└── DEPLOYMENT_GUIDE.md
```

---

### 2. Android UI ✅

**Must Have**:
- [ ] All 12 Jetpack Compose screens implemented:
  - [ ] OnboardingScreen.kt (first-run setup)
  - [ ] LoginScreen.kt (dual password system)
  - [ ] HomeScreen.kt (dashboard)
  - [ ] SilentCameraScreen.kt (CameraX integration)
  - [ ] IncidentReportScreen.kt (legal-formatted form)
  - [ ] EvidenceGalleryScreen.kt (encrypted evidence grid)
  - [ ] DocumentVerificationScreen.kt (PDF generation, SHA-256)
  - [ ] ResourceSearchScreen.kt (intersectional filters)
  - [ ] ResourceDetailScreen.kt (organization info)
  - [ ] SurvivorProfileScreen.kt (intersectional identity)
  - [ ] SettingsScreen.kt (panic delete config)
  - [ ] PanicDeleteConfirmationScreen.kt (deletion confirmation)
- [ ] Navigation graph connecting all screens
- [ ] Material Design 3 theme applied (purple/teal color scheme)
- [ ] All ViewModels implemented with Hilt + StateFlow
- [ ] Proper loading/error states on all screens
- [ ] Encrypted field indicators (lock icons)

**Files to Create**:
```
app/src/main/java/app/neurothrive/safehaven/ui/
├── theme/
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
├── navigation/
│   └── SafeHavenNavGraph.kt
├── screens/
│   ├── OnboardingScreen.kt
│   ├── LoginScreen.kt
│   ├── HomeScreen.kt
│   ├── SilentCameraScreen.kt
│   ├── IncidentReportScreen.kt
│   ├── EvidenceGalleryScreen.kt
│   ├── DocumentVerificationScreen.kt
│   ├── ResourceSearchScreen.kt
│   ├── ResourceDetailScreen.kt
│   ├── SurvivorProfileScreen.kt
│   ├── SettingsScreen.kt
│   └── PanicDeleteConfirmationScreen.kt
└── viewmodels/
    ├── LoginViewModel.kt
    ├── SilentCameraViewModel.kt
    ├── IncidentReportViewModel.kt
    ├── EvidenceGalleryViewModel.kt
    ├── ResourceSearchViewModel.kt
    ├── DocumentVerificationViewModel.kt
    ├── SettingsViewModel.kt
    └── SurvivorProfileViewModel.kt
```

---

### 3. Legal Resources Data ✅

**Must Have**:
- [ ] CSV with 1,000+ legal resources
- [ ] All 26 intersectional filters populated:
  - servesLGBTQIA, lgbtqSpecialized, transInclusive, nonBinaryInclusive
  - servesBIPOC, bipocLed
  - servesUndocumented, uVisaSupport, vawaSupport, noICEContact
  - servesMaleIdentifying
  - servesDisabled, wheelchairAccessible
  - servesDeaf, aslInterpreter
  - isFree, slidingScale, is24_7
  - languagesSupported
- [ ] ResourceImporter.kt to load CSV on first run
- [ ] Verify IntersectionalResourceMatcher works with real data
- [ ] Test scoring algorithm (trans survivors get +30 pts, etc.)

**Files to Create**:
```
data/
└── legal_resources.csv (1,000+ rows)

app/src/main/java/app/neurothrive/safehaven/data/import/
└── ResourceImporter.kt
```

**Data Sources**:
- National Domestic Violence Hotline database
- LGBT National Help Center
- Immigrant Legal Resource Center
- National Coalition Against Domestic Violence
- Deaf Abused Women's Network

---

### 4. Tests ✅

**Must Have**:
- [ ] Unit tests for SafeHavenCrypto:
  - [ ] Encrypt/decrypt round-trip
  - [ ] Same plaintext produces different ciphertext (different IVs)
  - [ ] SHA-256 hash consistency
  - [ ] Secure file deletion
- [ ] Unit tests for IntersectionalResourceMatcher:
  - [ ] Trans survivors get +30 point bonus
  - [ ] Undocumented survivors prioritize no ICE resources
  - [ ] Male survivors get +25 point bonus
  - [ ] Distance calculations (Haversine formula)
- [ ] Integration tests:
  - [ ] SilentCamera: Capture → Encrypt → Save to DB
  - [ ] PanicDelete: Delete all evidence files + DB records
  - [ ] DocumentVerification: Generate PDF → SHA-256 → Save
- [ ] Code coverage: 50%+ minimum

**Files to Create**:
```
app/src/test/java/app/neurothrive/safehaven/
├── util/crypto/SafeHavenCryptoTest.kt
├── domain/usecases/IntersectionalResourceMatcherTest.kt
└── domain/usecases/PanicDeleteUseCaseTest.kt

app/src/androidTest/java/app/neurothrive/safehaven/
├── util/camera/SilentCameraIntegrationTest.kt
└── domain/usecases/DocumentVerificationIntegrationTest.kt
```

---

## 📂 Resources Provided

All code examples and instructions are in the `Safehaven-documentation` repository:

### **Master Instructions** (2,500+ lines)
📄 `CLAUDE_CODE_COMPLETION_INSTRUCTIONS.md`
- Complete Salesforce XML for all 6 custom objects
- All 4 Apex REST API implementations
- Apex test class examples
- Deployment guide
- Complete Compose screen guidance
- ViewModel patterns
- Resource importer implementation
- Test examples

### **Quick Start Guide**
📄 `START_HERE_CLAUDE_CODE.md`
- Summary of previous work
- Prioritized task breakdown
- Day-by-day workflow
- Common pitfalls to avoid
- Definition of done checklist

### **Build Review**
📄 `CLAUDE_CODE_BUILD_REVIEW.md`
- Detailed analysis of first sprint
- What was built (excellent backend)
- What's missing (Salesforce + UI)
- Specific corrections needed

### **Sample Data** (100 resources)
📄 `data/legal_resources_sample.csv`
- 100 real DV resources ready to import
- All intersectional filters populated
- Use for testing, then expand to 1,000+

### **Compose Screen Examples** (6 complete screens)
📄 `ADDITIONAL_COMPOSE_SCREENS.md`
- LoginScreen.kt
- SilentCameraScreen.kt
- IncidentReportScreen.kt
- EvidenceGalleryScreen.kt
- ResourceSearchScreen.kt
- SettingsScreen.kt

### **ViewModel Examples** (8 complete ViewModels)
📄 `VIEWMODEL_EXAMPLES.md`
- LoginViewModel
- SilentCameraViewModel
- IncidentReportViewModel
- EvidenceGalleryViewModel
- ResourceSearchViewModel
- DocumentVerificationViewModel
- SettingsViewModel
- SurvivorProfileViewModel

---

## ⏱️ Time Estimates

| Task | Estimated Time |
|------|----------------|
| Salesforce Backend | 8-12 hours |
| Android UI (12 screens + ViewModels) | 16-24 hours |
| Legal Resources Import | 4-8 hours |
| Tests | 8-12 hours |
| **TOTAL** | **36-56 hours** |

**With provided examples**: 24-36 hours

---

## 🚨 Critical Notes

### **1. Do NOT Modify Existing Backend Code**
The following files are **production-quality** and should NOT be modified:
- ✅ SafeHavenCrypto.kt (AES-256-GCM encryption)
- ✅ SilentCameraManager.kt (silent camera)
- ✅ MetadataStripper.kt (GPS removal)
- ✅ ShakeDetector.kt (accelerometer)
- ✅ PanicDeleteUseCase.kt (secure deletion)
- ✅ DocumentVerificationService.kt (SHA-256 hashing)
- ✅ IntersectionalResourceMatcher.kt (scoring algorithm)
- ✅ All 6 Room entities
- ✅ All 6 DAOs

**Only add the UI layer on top of existing backend.**

### **2. Salesforce Integration is Priority #1**
User explicitly requested: *"I asked that it build for a stand alone app and a **app integration for Salesforce**"*

This was a stated requirement that was not delivered in the first sprint. Complete Salesforce backend before UI work.

### **3. Survivor Safety is Critical**
This app protects domestic violence survivors. Every feature has a safety purpose:
- **Silent camera** → Capture evidence without alerting abuser
- **Panic delete** → Instant destruction if in danger
- **No ICE contact** → Critical for undocumented survivors
- **Dual password** → Plausible deniability if forced to unlock
- **Triple-layer encryption** → Protects from forensic recovery

**Build with care. Lives depend on this working correctly.**

### **4. Test on Physical Device**
Silent camera requires real hardware. Emulator will not work for camera testing.

### **5. Platform Shield Required**
Salesforce field-level encryption requires Platform Shield license. If user's org doesn't have it, use regular Text fields (not EncryptedText) for Salesforce deployment, but keep Android-side encryption.

---

## 📋 Definition of Done

### **Salesforce Backend Complete When**:
- [ ] All 6 custom objects visible in Salesforce org
- [ ] Can create SafeHaven_Profile__c record via Salesforce UI
- [ ] Can call SafeHavenSyncAPI via Workbench and get 200 response
- [ ] Apex tests pass with 75%+ coverage
- [ ] Android app successfully syncs incident to Salesforce

### **Android UI Complete When**:
- [ ] App builds without errors
- [ ] Can navigate between all 12 screens
- [ ] Silent camera captures photo → appears in Evidence Gallery
- [ ] Incident report saves → appears in incidents list
- [ ] Resource search with filters returns scored results
- [ ] Settings screen toggles work (shake to delete, sync)

### **Legal Resources Complete When**:
- [ ] 1,000+ resources imported from CSV
- [ ] Resource search returns results
- [ ] Trans-inclusive resources ranked higher for trans survivors
- [ ] No-ICE resources prioritized for undocumented survivors

### **Tests Complete When**:
- [ ] All tests pass (`./gradlew test`)
- [ ] Code coverage ≥50% (`./gradlew jacocoTestReport`)
- [ ] Integration tests pass on physical device

---

## 🔗 Repository Information

**Repository**: https://github.com/abbyluggery/SafeHaven-Build
**Previous Branch**: `claude/safehaven-android-app-013udX3wnRYCxzmvCZP4mL97`
**Documentation**: `Safehaven-documentation/` directory

---

## 📞 Questions?

If blocked:
1. Check `START_HERE_CLAUDE_CODE.md` for workflow guidance
2. Reference `CLAUDE_CODE_BUILD_REVIEW.md` for previous work analysis
3. Use provided screen/ViewModel examples as templates
4. Follow Android best practices (MVVM, Jetpack Compose, Hilt)

---

## ✅ Deliverables Checklist

Before closing this issue, verify:

- [ ] Salesforce deployment successful (all objects + APIs deployed)
- [ ] Salesforce tests pass (75%+ coverage)
- [ ] Connected App configured
- [ ] All 12 Android screens implemented
- [ ] Navigation working
- [ ] All ViewModels connecting to DAOs
- [ ] 1,000+ resources imported
- [ ] Resource matching tested with real data
- [ ] Unit tests written (encryption, resource matching)
- [ ] Integration tests written (camera, panic delete)
- [ ] Code coverage ≥50%
- [ ] README.md updated
- [ ] DEPLOYMENT_GUIDE.md created
- [ ] End-to-end test documented

---

**Assigned to**: Claude Code (GitHub Agent)
**Labels**: `critical`, `feature`, `salesforce`, `android-ui`, `testing`
**Milestone**: SafeHaven v1.0 MVP
**Estimated Completion**: 3-5 sprint cycles (24-40 hours)

---

**Generated**: 2025-11-17
**Created by**: Claude Assistant (Code Review)
**User**: @abbyluggery
