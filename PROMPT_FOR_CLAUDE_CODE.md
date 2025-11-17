# Prompt for Claude Code: Build SafeHaven Android App

**Copy and paste this entire prompt to Claude Code to start the 24-hour sprint:**

---

# BUILD SAFEHAVEN ANDROID APP - 24-HOUR SPRINT

## Mission
Build a **domestic violence safety planning Android app** that centers marginalized survivors (trans, BIPOC, male, undocumented, disabled) with silent documentation, encrypted storage, and intersectional resource matching.

## Repository
https://github.com/abbyluggery/SafeHaven-Build

## Your Task
Follow the instructions in **CLAUDE_CODE_COMPLETE_INSTRUCTIONS.md** to build a production-ready Android app in 24 hours.

## Critical Features (Build in This Order)

### PHASE 1: CRITICAL (Hours 0-12)

**1. Database Schema (Hours 0-2)**
- Create 6 Room entities: SafeHavenProfile, IncidentReport, VerifiedDocument, EvidenceItem, LegalResource, SurvivorProfile
- Create 6 DAOs with Flow-based queries
- Setup AppDatabase with version 1
- Complete code templates in: `# SafeHaven Database Schema (Room).md`

**2. Encryption System (Hours 2-4)**
- Implement SafeHavenCrypto.kt with AES-256-GCM
- Android KeyStore integration
- Methods: encrypt(), decrypt(), encryptFile(), decryptFile(), generateSHA256(), secureDelete()
- Complete code in: `SafeHaven Technical Specification.MD` (search "SafeHavenCrypto")

**3. Silent Camera (Hours 4-8)** ← MOST CRITICAL
- SilentCameraManager.kt: Mute system volume during capture
- MetadataStripper.kt: Remove GPS from photos
- Immediate file encryption after capture
- No flash, no gallery thumbnails, no shutter sound

**4. Panic Delete (Hours 8-10)**
- ShakeDetector.kt: 3 rapid shakes triggers delete
- PanicDeleteUseCase.kt: Secure delete all evidence <2 seconds
- Overwrite files with random data before deletion

**5. Document Verification (Hours 10-12)**
- DocumentVerificationService.kt: SHA-256 hashing
- Generate verified PDF with embedded hash
- (Optional) Polygon blockchain timestamping

### PHASE 2: IMPORTANT (Hours 12-18)

**6. Intersectional Resource Matching (Hours 12-14)**
- IntersectionalResourceMatcher.kt with scoring algorithm
- Trans survivors +30 pts for trans resources
- Undocumented +30 pts for U-Visa support
- Male survivors +25 pts (few resources exist)
- LGBTQIA+ +20 pts, BIPOC +20 pts

**7. Incident Report Form (Hours 14-16)**
- Legal-formatted abuse documentation
- Encrypted description and witnesses fields
- Police involvement, medical attention tracking
- Export to PDF

**8. Evidence Vault (Hours 16-18)**
- Grid view of encrypted photos
- Decrypt on-demand for viewing
- Link to incident reports

### PHASE 3: NICE-TO-HAVE (Hours 18-24)
- Onboarding flow
- Settings screen
- Salesforce sync

## Technology Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose (Material Design 3)
- **Database**: Room 2.6.1 (SQLite)
- **Encryption**: AES-256-GCM + Android KeyStore
- **Camera**: CameraX 1.3.1
- **DI**: Hilt 2.48.1
- **PDF**: iText7 7.2.5

## Key Security Requirements
- ✅ GPS OFF by default (user opt-in only)
- ✅ All sensitive fields AES-256 encrypted
- ✅ Silent camera (no sound, flash, or gallery thumbnails)
- ✅ Secure file deletion (overwrite before delete)
- ✅ Panic delete <2 seconds execution time
- ✅ Dual password system (real vs. decoy data)

## Success Criteria
By end of 24 hours:
- [x] 6 Room entities + DAOs compiling
- [x] Encryption working (encrypt → decrypt = original)
- [x] Silent camera captures without sound/flash
- [x] Photos encrypted immediately
- [x] GPS metadata stripped from all photos
- [x] Panic delete removes all data <2 seconds
- [x] SHA-256 document hashing functional
- [x] Intersectional resource matching algorithm working

## Complete Documentation
- **Build Instructions**: CLAUDE_CODE_COMPLETE_INSTRUCTIONS.md (hour-by-hour guide)
- **Technical Specs**: SafeHaven Technical Specification.MD (complete code examples)
- **Database Schema**: # SafeHaven Database Schema (Room).md (all entities)
- **Quick Start**: SafeHaven Quick Start Guide - 24.md (24-hour timeline)

## Start Here
1. Read: CLAUDE_CODE_COMPLETE_INSTRUCTIONS.md
2. Setup: Android Studio project with Kotlin + Jetpack Compose
3. Build: Follow PHASE 1 (Hours 0-12) first - everything else depends on database + encryption

## Why This Matters
- **70% of survivors** can't leave due to economic dependence
- **Trans BIPOC women** have highest IPV rates, fewest resources
- **Male survivors** face stigma, no shelters
- **Undocumented survivors** fear deportation, avoid police

You're building an app that could save lives. Follow the instructions carefully, prioritize security, and center marginalized survivors.

**Ready? Start with CLAUDE_CODE_COMPLETE_INSTRUCTIONS.md → Hours 0-2: Database Schema**

---

**Repository**: https://github.com/abbyluggery/SafeHaven-Build
**Documentation**: All files in this repository
**Questions**: Check SafeHaven Technical Specification.MD for complete code examples