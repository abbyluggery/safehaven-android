# Security Notice

## Open Source Security Philosophy

SafeHaven is designed for survivors of domestic violence operating under adversarial conditions. This public repository demonstrates our security architecture while intentionally limiting certain implementation details.

---

## What's in This Repository

### Included (Educational/Demonstrable)
- **Architecture Documentation** - Security layer design, threat models
- **Generic Implementation Patterns** - Encryption wrappers, secure storage, camera handling
- **Database Schema** - Encrypted Room database structure
- **UI Components** - Compose screens and navigation
- **Resource Matching** - Intersectional algorithm design

### Security Considerations

While this repository contains functional security code, some mechanisms rely on **security through obscurity** as an additional (not primary) defense layer. The following features are documented at a conceptual level in the README, with implementation details intentionally generic:

| Feature | Status | Notes |
|---------|--------|-------|
| AES-256-GCM Encryption | Full implementation | Standard cryptographic patterns |
| SQLCipher Integration | Full implementation | Well-documented library |
| Hardware KeyStore | Full implementation | Android standard APIs |
| Silent Camera | Full implementation | CameraX standard APIs |
| Panic Delete | Reference implementation | Generic secure delete pattern |
| Shake Detection | Reference implementation | Standard sensor handling |
| Document Verification | Full implementation | SHA-256 + blockchain hashing |

---

## Security Best Practices

If you're forking this project for a DV organization:

1. **Conduct a security audit** before deployment
2. **Customize anti-coercion mechanisms** - generic implementations should be adapted
3. **Review local legal requirements** for evidence handling
4. **Train staff** on security features and limitations
5. **Establish incident response** procedures

---

## Responsible Disclosure

If you discover a security vulnerability:

1. **Do NOT** open a public GitHub issue
2. Contact the maintainer directly via LinkedIn
3. Allow 90 days for remediation before public disclosure
4. Vulnerabilities affecting user safety are prioritized

---

## Not Included in Public Repository

Certain implementation details are maintained separately:
- Specific anti-coercion trigger thresholds
- Stealth mode activation sequences
- Decoy content generation logic
- Emergency contact escalation protocols

Organizations deploying SafeHaven can request these materials for security review.

---

## Disclaimer

This software is provided "as-is" for educational and humanitarian purposes. While designed with security in mind, no system is impenetrable. Users in danger should work with professional domestic violence organizations who can provide comprehensive safety planning.

**If you are in immediate danger, please contact:**
- Emergency: 911 (US)
- National DV Hotline: 1-800-799-7233
- Crisis Text Line: Text START to 88788

---

*Security is a process, not a product. This repository represents our commitment to transparent, ethical security design for vulnerable populations.*
