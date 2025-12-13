# SafeHaven - Privacy-Focused Mobile Security Application

A free, encrypted safety documentation platform designed for survivors of domestic violence, featuring zero-knowledge architecture, tamper-proof evidence verification, and intersectional resource matching.

---

## Overview

SafeHaven addresses a critical gap: survivors need secure documentation tools that work under adversarial conditions. Traditional apps fail because abusers often have access to victims' devices, accounts, and location data.

This application implements security patterns designed for threat scenarios where the attacker has physical device access, technical sophistication, and coercive control over the user.

---

## Architecture

### Security Design

```
┌────────────────────────────────────────────────────────────────────┐
│                    SAFEHAVEN SECURITY LAYERS                       │
├────────────────────────────────────────────────────────────────────┤
│                                                                    │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │                    APPLICATION LAYER                        │   │
│  │  • Stealth UI (disguised as utility app)                   │   │
│  │  • Anti-coercion authentication                            │   │
│  │  • Rapid data sanitization                                 │   │
│  └────────────────────────────────────────────────────────────┘   │
│                              │                                     │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │                    ENCRYPTION LAYER                         │   │
│  │  • AES-256-GCM (field-level)                               │   │
│  │  • SQLCipher (database-level)                              │   │
│  │  • Hardware-backed keystore                                │   │
│  └────────────────────────────────────────────────────────────┘   │
│                              │                                     │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │                   VERIFICATION LAYER                        │   │
│  │  • SHA-256 document hashing                                │   │
│  │  • Blockchain timestamping                                 │   │
│  │  • Court-admissible evidence chain                         │   │
│  └────────────────────────────────────────────────────────────┘   │
│                              │                                     │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │                    STORAGE LAYER                            │   │
│  │  • Local encrypted vault                                   │   │
│  │  • Optional cloud backup (zero-knowledge)                  │   │
│  │  • Geographic redundancy                                   │   │
│  └────────────────────────────────────────────────────────────┘   │
│                                                                    │
└────────────────────────────────────────────────────────────────────┘
```

### Design Decisions

**1. Zero-Knowledge Architecture**

*Challenge:* Cloud services can be subpoenaed or hacked. Server operators shouldn't be trusted with plaintext data.

*Approach:*
- All encryption occurs on-device before any data leaves
- Server only receives encrypted blobs
- Encryption keys stored in hardware-backed Android KeyStore
- No server-side decryption capability

*Why:* Even if servers are compromised, attacker gains only encrypted data without keys.

**2. Anti-Coercion Authentication**

*Challenge:* Abusers may force survivors to unlock their phone or reveal passwords under duress.

*Approach:*
- Multiple authentication pathways with different behaviors
- Plausible deniability mechanisms
- Time-delayed full access
- Decoy content layers

*Why:* System must protect user even when attacker is physically present and coercing compliance.

**3. Silent Documentation**

*Challenge:* Standard camera apps produce sounds, flash, save to gallery, and embed GPS—all dangerous for covert documentation.

*Approach:*
- System audio muted during capture
- Flash disabled by default
- No gallery integration
- GPS metadata stripped
- Immediate encryption

*Why:* Evidence collection must be undetectable to abuser who may be monitoring device.

**4. Tamper-Proof Verification**

*Challenge:* Abusers often claim evidence is fabricated. Courts need provable authenticity.

*Approach:*
- SHA-256 hash of original document
- Blockchain timestamping (immutable record)
- QR-code verification on exports
- Chain of custody documentation

*Why:* "This photo existed on [date]" is provable via public blockchain without revealing content.

**5. Intersectional Resource Matching**

*Challenge:* A trans woman of color needs different resources than a cisgender white woman. Generic hotlines often fail marginalized survivors.

*Approach:*
- 1,000+ verified organizations in database
- Identity-aware scoring algorithm
- Filters: LGBTQIA+, BIPOC, male survivors, undocumented, disabled
- Location-aware with distance weighting

*Why:* Resource recommendations must account for intersecting identities to be useful.

---

## Technical Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| Platform | Android (Kotlin) | Native mobile application |
| UI | Jetpack Compose | Modern declarative UI |
| Database | Room + SQLCipher | Encrypted local storage |
| Encryption | AES-256-GCM | Field and file encryption |
| Key Storage | Android KeyStore | Hardware-backed key protection |
| Camera | CameraX | Silent capture implementation |
| Blockchain | Polygon (Web3j) | Document timestamping |
| PDF | iText7 | Verified document generation |
| Backend | Salesforce (Apex) | Resource database, optional sync |

---

## Technical Statistics

| Metric | Count |
|--------|-------|
| Kotlin Files | 105 |
| Documentation Files | 50 |
| Room Entities | 8 |
| Security Layers | 4 |
| Supported Identity Filters | 12 |

---

## Key Features

### Evidence Documentation
- Silent photo/video capture
- Incident report logging with timestamps
- Secure evidence vault with encryption
- Metadata sanitization

### Document Verification
- Cryptographic hashing of documents
- Blockchain timestamping for court admissibility
- QR-code verification system
- PDF export with verification chain

### Safety Features
- Rapid data sanitization capabilities
- Anti-coercion authentication mechanisms
- Stealth operation modes
- Emergency contact alerting

### Resource Discovery
- Intersectional organization database
- Identity-aware matching algorithm
- Distance and availability filtering
- Verified contact information

### Economic Independence
- Integrated job search (via NeuroThrive platform)
- Resume generation
- Interview preparation
- Financial planning resources

---

## Security Philosophy

This project demonstrates threat modeling for adversarial conditions:

**Assume Compromise:** Device may be monitored, accounts may be accessed, location may be tracked.

**Defense in Depth:** Multiple independent security layers. Defeating one doesn't defeat all.

**Coercion Resistance:** System behavior under duress protects rather than exposes.

**Privacy by Default:** No data collection beyond minimum necessary. User controls what exists.

**Graceful Degradation:** Offline-first design. Network unavailability doesn't break core functions.

**Plausible Deniability:** Appearance and behavior can be innocuous when needed.

---

## Who This Serves

This platform centers marginalized survivors who face compounding barriers:

- **LGBTQIA+ survivors** - specialized resources, affirming services
- **BIPOC survivors** - culturally competent organizations
- **Male-identifying survivors** - rare dedicated services
- **Undocumented survivors** - U-Visa support, confidentiality
- **Disabled survivors** - accessibility, interpreter services
- **Deaf/DeafBlind survivors** - ASL and visual resources

**Why Intersectionality Matters:** Generic resources fail survivors with multiple marginalized identities. Our matching algorithm prioritizes identity-specific organizational expertise.

---

## What This Demonstrates

**For technical reviewers**, this project shows:

1. **Security Architecture** - Zero-knowledge design, anti-coercion mechanisms, defense in depth
2. **Threat Modeling** - Designing for adversarial conditions, not just accidental misuse
3. **Mobile Development** - Kotlin, Jetpack Compose, Room, CameraX
4. **Cryptography** - AES-256-GCM, hardware keystore integration, blockchain verification
5. **Algorithm Design** - Intersectional matching with weighted scoring
6. **Ethical Technology** - Building for vulnerable populations with appropriate safeguards

---

## Development Approach

This system was architected using AI-assisted development workflows. I designed the security architecture, made all decisions regarding threat models and protection mechanisms, and can explain every component:

- Why zero-knowledge over server-side encryption
- Why hardware keystore over software key storage
- Why blockchain timestamping over centralized verification
- How the anti-coercion mechanisms work conceptually
- How intersectional matching weights different factors

---

## Privacy & Legal

### Data Handling
- **On Device:** All user data (encrypted)
- **On Blockchain:** Document hashes only (no personal data)
- **On Cloud (opt-in):** Encrypted blobs, zero-knowledge

### Compliance
- GDPR/CCPA compliant
- No tracking or analytics
- Right to deletion via panic feature
- Data portability via PDF export

---

## Important Notice

**This is NOT a substitute for professional domestic violence services.**

If you are in immediate danger:
- **Emergency:** 911 (US)
- **National DV Hotline:** 1-800-799-7233
- **Crisis Text Line:** Text START to 88788

SafeHaven is a documentation and planning tool. Please work with local DV organizations for comprehensive support.

---

## License

MIT License - Free and open source.

We want this technology available to as many survivors as possible. Other organizations: please fork and adapt for your communities.

---

## Author

**Abby Luggery**
- GitHub: [@abbyluggery](https://github.com/abbyluggery)
- LinkedIn: [linkedin.com/in/abby-luggery-02a4b815a](https://www.linkedin.com/in/abby-luggery-02a4b815a/)

---

*Building safety tools for people who need them most.*
