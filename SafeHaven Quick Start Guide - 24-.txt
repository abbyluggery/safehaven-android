# SafeHaven Quick Start Guide - 24-Hour Sprint Edition

**Target Audience**: Developers building the app  
**Time to Read**: 15 minutes  
**Goal**: Get from zero to working prototype in 24 hours

---

## What You're Building

### The Mission (60 seconds)
Build a **free, encrypted safety planning app** for domestic violence survivors that:
1. **Documents abuse silently** (no camera sounds, GPS off)
2. **Verifies legal documents** (SHA-256 + blockchain)
3. **Enables economic independence** (integrated job search)
4. **Centers marginalized survivors** (BIPOC, LGBTQIA+, male, disabled)

### Why This Matters
- 70% of survivors can't leave due to **economic dependence**
- Trans BIPOC women have **highest IPV rates, fewest resources**
- Male survivors face **stigma, no shelters**
- Existing DV apps ignore intersectionality

---

## 24-Hour Sprint Priorities

### CRITICAL (Hours 0-12) - Build These First

#### 1. Database Schema (Hour 0-2)
✅ **SafeHavenProfile** - User settings, intersectional identity  
✅ **IncidentReport** - Legal-formatted abuse documentation  
✅ **VerifiedDocument** - SHA-256 hashes, blockchain timestamps  
✅ **EvidenceItem** - Encrypted photos/videos/audio  
✅ **LegalResource** - 1,000+ shelters with intersectional filters  

**Why**: Everything else depends on data structure

#### 2. Encryption System (Hour 2-4)
✅ **SafeHavenCrypto.kt** - AES-256-GCM encryption  
✅ **Android KeyStore** integration  
✅ **File encryption** for photos/videos  
✅ **SHA-256 hashing** for document verification  

**Why**: Security is non-negotiable. Build it right from the start.

#### 3. Silent Documentation (Hour 4-8)
✅ **SilentCameraManager.kt**:
- Mute system volume during photo capture
- Disable flash by default
- Remove GPS metadata from photos
- No preview thumbnails in gallery
- Immediate encryption of captured media

**Why**: This is THE core feature. Survivors need stealth documentation.

#### 4. Panic Features (Hour 8-10)
✅ **ShakeDetector.kt** - Detect 3 rapid shakes  
✅ **PanicModeManager.kt**:
- Quick delete all SafeHaven data
- Dual password (real vs. decoy data)
- SOS mode (GPS to emergency contacts)

**Why**: Survivor safety > everything else. They need instant escape.

#### 5. Document Verification (Hour 10-12)
✅ **DocumentVerificationService.kt**:
- Generate SHA-256 hash of document photo
- Create verified PDF with embedded hash
- Optional: Timestamp on Polygon blockchain
- Verification portal (web)

**Why**: "I can't take original documents" is a huge barrier. This solves it.

---

### IMPORTANT (Hours 12-18) - Build These Second

#### 6. Intersectional Resource Matching (Hour 12-14)
✅ **IntersectionalResourceMatcher.kt**:
- Scoring algorithm (trans +30 pts, undocumented +30 pts, LGBTQ +20 pts)
- Distance calculation (Haversine formula)
- Filter by identity + location
- Prioritize marginalized survivors

**Why**: Generic resources don't work. Trans survivor needs trans-inclusive shelter.

#### 7. Incident Report Form (Hour 14-16)
✅ **IncidentReportScreen.kt**:
- Date/time picker
- Incident type (physical, verbal, emotional, financial, sexual)
- Description (encrypted)
- Witness names (encrypted)
- Photo evidence (link to EvidenceItem)
- Export to PDF

**Why**: Legal documentation format increases restraining order success rate.

#### 8. Evidence Vault (Hour 16-18)
✅ **EvidenceVaultScreen.kt**:
- Gallery view of encrypted photos/videos
- Decrypt and display on-demand
- Add captions (encrypted)
- Link to incident reports
- Cloud backup indicator

**Why**: Organized evidence = stronger legal case.

---

### NICE-TO-HAVE (Hours 18-24) - Build If Time Permits

#### 9. Onboarding Flow (Hour 18-20)
✅ **IntersectionalOnboardingScreen.kt**:
- Collect identity info (optional)
- Set passwords (main + duress)
- Configure GPS settings
- Explain features

#### 10. Settings Screen (Hour 20-22)
✅ **SettingsScreen.kt**:
- GPS toggle (default OFF)
- Silent mode toggle (default ON)
- Panic settings
- Cloud backup settings

#### 11. Salesforce Sync (Hour 22-24)
✅ **SalesforceApiService.kt**:
- Sync incidents to cloud
- Download resources
- OAuth authentication

---

## Tech Stack (Copy-Paste Ready)

### build.gradle.kts (Module: app)
```kotlin