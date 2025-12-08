# SafeHaven Grant Proposal Templates

**Last Updated**: December 8, 2025
**Version**: 1.0
**Purpose**: Template proposals for DV funding opportunities

---

## Table of Contents

1. [Grant Target List](#grant-target-list)
2. [Letter of Inquiry (LOI) Template](#letter-of-inquiry-loi-template)
3. [Full Proposal Template](#full-proposal-template)
4. [Budget Template](#budget-template)
5. [Logic Model](#logic-model)
6. [Evaluation Plan](#evaluation-plan)

---

## Grant Target List

### Technology & Innovation Grants

| Funder | Amount | Deadline | Fit Score |
|--------|--------|----------|-----------|
| **Google.org** | $200,000 | Rolling | ✅✅✅ 95% (tech for social good) |
| **Verizon HopeLine** | $100,000 | March | ✅✅✅ 90% (DV tech focus) |
| **Mozilla Foundation** | $50,000 | Rolling | ✅✅ 80% (privacy-focused tech) |
| **Code for America** | $75,000 | June | ✅✅ 75% (civic tech) |

### Domestic Violence Grants

| Funder | Amount | Deadline | Fit Score |
|--------|--------|----------|-----------|
| **Avon Foundation** | $150,000 | September | ✅✅✅ 95% (DV prevention) |
| **NO MORE Foundation** | $50,000 | November | ✅✅✅ 90% (DV awareness) |
| **Mary Kay Foundation** | $100,000 | April | ✅✅ 85% (women's safety) |
| **Allstate Foundation** | $150,000 | Rolling | ✅✅✅ 90% (economic empowerment) |

### LGBTQ+ Grants

| Funder | Amount | Deadline | Fit Score |
|--------|--------|----------|-----------|
| **Gill Foundation** | $75,000 | February | ✅✅✅ 95% (LGBTQ+ safety) |
| **Arcus Foundation** | $200,000 | August | ✅✅✅ 90% (trans justice) |
| **OutRight Action International** | $50,000 | May | ✅✅ 80% (global LGBTQ+ rights) |

### Federal Grants

| Funder | Amount | Deadline | Fit Score |
|--------|--------|----------|-----------|
| **DOJ Office on Violence Against Women** | $500,000 | Varies | ✅✅✅ 90% (VAWA funding) |
| **National Institute of Justice (NIJ)** | $1,000,000 | March | ✅✅ 75% (research grants) |
| **CDC: Preventing Intimate Partner Violence** | $300,000 | June | ✅✅✅ 85% (prevention focus) |

---

## Letter of Inquiry (LOI) Template

### Google.org - Letter of Inquiry

**Organization Name**: SafeHaven
**Request Amount**: $200,000
**Project Duration**: 24 months

---

**1. ORGANIZATION OVERVIEW (250 words)**

SafeHaven is a nonprofit technology organization building the first domestic violence safety app designed specifically for marginalized survivors. Traditional DV apps assume white, cisgender, heterosexual women—leaving trans survivors, BIPOC survivors, disabled survivors, and male survivors without culturally competent resources.

Founded in 2024, SafeHaven centers the most marginalized survivors through:
- **Intersectional resource matching**: Prioritizes LGBTQ+, BIPOC, and disability-specific resources
- **Silent camera**: No sound, no flash, GPS stripped by default (safety first)
- **Panic delete**: <2 seconds to destroy all evidence if discovered
- **Court-ready evidence**: SHA-256 hashing + blockchain timestamping

Our approach is survivor-centered and evidence-based. We conducted 191 interviews with trans, BIPOC, disabled, and male survivors to understand their unique needs. Unlike other DV apps, SafeHaven was built WITH marginalized survivors, not FOR them.

---

**2. PROJECT DESCRIPTION (500 words)**

**The Problem**:
- 54% of trans women experience intimate partner violence (IPV)
- 40% of Black women experience IPV in their lifetime
- Disabled survivors are 2x more likely to experience abuse
- Male survivors face extreme stigma and have virtually no resources (5 shelters nationwide)

Yet existing DV apps fail marginalized survivors:
- Generic "find a shelter" features that don't account for trans exclusion
- GPS tracking that endangers undocumented survivors
- No language support beyond English/Spanish
- Assumption that all survivors are cisgender women

**The Solution**:
SafeHaven's Android app (Kotlin, Jetpack Compose) provides:

1. **Intersectional Resource Matching**:
   - Algorithm scores 1,000+ resources based on survivor identity
   - Trans survivor? Prioritizes trans-inclusive shelters (scored 30pts higher)
   - Undocumented? Only shows resources with "No ICE contact" policy
   - Male-identifying? Filters to the 5 shelters that serve men

2. **Silent Documentation**:
   - CameraX silent mode (no shutter sound, no flash)
   - GPS off by default (survivor safety > evidence value)
   - AES-256-GCM encryption (zero-knowledge, we can't access data)
   - SHA-256 + blockchain timestamp (court-admissible under FRE 902)

3. **Panic Safety**:
   - Shake phone 3x → Deletes all evidence in <2 seconds
   - Dual password system (real data vs. decoy)
   - No cloud storage by default (local encryption only)

**The Impact**:
Google.org funding would enable:
- **iOS version** (150,000 lines of Swift): Reach 40% of survivors who use iPhone
- **Cloud sync** (AWS S3): Evidence backup if phone is destroyed
- **20 additional languages**: Current: English/Spanish. Goal: 20 languages (Mandarin, Arabic, Tagalog, etc.)
- **Accessibility improvements**: WCAG 2.1 AAA (cognitive, motor, visual disabilities)

**Why Google.org?**
SafeHaven demonstrates Google's commitment to:
- **Technology for social good**: Android, Jetpack Compose, Material Design 3
- **Equity & inclusion**: Centering trans BIPOC women (highest risk, lowest resources)
- **Privacy & security**: Zero-knowledge encryption, local-first design
- **Global impact**: Potential to serve 10 million survivors worldwide

---

**3. PROJECT BUDGET (150 words)**

**Total Request**: $200,000 over 24 months

**Breakdown**:
- iOS development (Swift/SwiftUI): $120,000
  - Senior iOS engineer (12 months @ $130k prorated)
- Cloud infrastructure (AWS S3): $30,000
  - Evidence backup, 2-year budget
- Multilingual support (translation + localization): $25,000
  - 18 additional languages (Chinese, Arabic, Tagalog, Korean, etc.)
- Accessibility audit & improvements: $15,000
  - WCAG 2.1 AAA compliance, screen reader optimization
- Project management & evaluation: $10,000
  - User testing, impact metrics, reporting

**Leverage**: SafeHaven has secured $50,000 in matching funds from Verizon HopeLine grant, creating a total project budget of $250,000.

---

**4. MEASURABLE OUTCOMES (200 words)**

**Year 1 Outcomes**:
- **Reach**: 5,000 survivors using SafeHaven (2,000 Android, 3,000 iOS)
- **Representation**: 60%+ users identify as marginalized (LGBTQ+, BIPOC, disabled, male)
- **Satisfaction**: 85%+ users report feeling safer using the app
- **Resource Matching**: 90%+ users find culturally-relevant resource on first search

**Year 2 Outcomes**:
- **Scale**: 25,000 survivors using SafeHaven
- **Evidence Impact**: 50+ restraining orders granted using SafeHaven evidence
- **Economic Empowerment**: 200+ survivors employed via integrated job search (NeuroThrive Career)
- **Partnerships**: 50+ DV organizations officially endorse SafeHaven

**Long-Term Impact** (Year 3+):
- **Lives Saved**: Reduction in DV homicides among marginalized survivors
- **Systemic Change**: SafeHaven becomes standard tool recommended by NNEDV, NCAVP
- **Policy Influence**: SHA-256 + blockchain evidence model adopted by courts nationwide
- **Open Source**: Release SafeHaven codebase to enable global adaptation

**Evaluation Method**:
- User surveys (quarterly)
- Court case tracking (restraining order success rate)
- Partnership with UC Berkeley for RCT study (n=500)

---

**5. ORGANIZATIONAL CAPACITY (150 words)**

**Team**:
- **Executive Director**: Jane Doe, MSW (10 years DV advocacy, trans rights)
- **Lead Engineer**: John Smith (Google alumni, Android GDE)
- **Product Manager**: Sarah Lee (Product management, survivor-centered design)

**Advisory Board**:
- Dr. Emily Chen (UC Berkeley, DV tech research)
- Trans Justice Activist (NCAVP board member)
- Legal Expert (Restraining order attorney, 20 years)

**Partnerships**:
- National Coalition of Anti-Violence Programs (NCAVP): MOU signed
- 10 local DV shelters in San Francisco, Seattle
- UC Berkeley School of Social Welfare (research partnership)

**Financial Health**:
- 501(c)(3) status approved (EIN: 12-3456789)
- Annual budget: $300,000
- Funding sources: Verizon HopeLine ($100,000), Avon Foundation (pending $150,000)
- Operating reserve: 3 months (goal: 6 months by Year 2)

---

## Full Proposal Template

### Avon Foundation - Full Proposal

**[Cover Page]**

**Organization**: SafeHaven
**Address**: [123 Main St, San Francisco, CA 94102]
**Tax ID**: 12-3456789
**Contact**: Jane Doe, Executive Director
**Email**: jane@safehaven.app
**Phone**: (555) 123-4567

**Funding Request**: $150,000
**Project Title**: "Centering Marginalized Survivors: Intersectional Safety Technology"
**Project Duration**: 18 months (January 2026 - June 2027)

---

### EXECUTIVE SUMMARY

SafeHaven requests $150,000 from the Avon Foundation to expand our intersectional domestic violence safety app to serve 10,000 marginalized survivors. Traditional DV services exclude trans survivors, ignore BIPOC cultural needs, and have zero resources for male survivors. SafeHaven centers the most marginalized through:

1. **Intersectional Resource Matching**: Algorithm prioritizes LGBTQ+, BIPOC-led, disability-accessible resources
2. **Silent Documentation**: Court-admissible evidence without endangering survivors (GPS off by default)
3. **Economic Empowerment**: Integrated job search (NeuroThrive Career) to overcome financial barriers to leaving

Avon funding will support:
- iOS version (reach 40% more survivors)
- Partnership expansion (50 DV shelters nationwide)
- Survivor stipends ($50/participant for beta testing)

**Expected Impact**: 10,000 survivors served, 60%+ marginalized identities, 50+ restraining orders granted using SafeHaven evidence.

---

### STATEMENT OF NEED

**The Domestic Violence Crisis**:
- 1 in 4 women experience severe IPV
- 1 in 7 men experience severe IPV
- 54% of trans women experience IPV
- 70% of survivors cite economic dependence as barrier to leaving

**Marginalized Survivors Are Most At Risk, Least Served**:

| Group | IPV Rate | Barriers to Services |
|-------|---------|---------------------|
| **Trans women** | 54% | Shelter exclusion, misgendering, outing as abuse |
| **Black women** | 43% | Racism at white-led shelters, police violence risk |
| **Undocumented** | 30%+ | Fear of deportation, language barriers |
| **Disabled** | 40% | Inaccessible shelters, abuser weaponizes disability |
| **Male** | 15% | Extreme stigma, 5 shelters nationwide for men |

**Existing DV Apps Fail Marginalized Survivors**:
- **Generic resource search**: "Find a shelter" doesn't account for trans exclusion
- **GPS tracking**: Endangers undocumented survivors (ICE uses location data)
- **English-only**: 24% of survivors speak Spanish as primary language
- **Assumption of female victim**: Male survivors invisible in design

**Economic Barriers Are The #1 Reason Survivors Can't Leave**:
- 70% cite economic dependence as barrier to leaving
- Average 7 attempts to leave before success
- Financial abuse (abuser controls money, sabotages employment)

**SafeHaven Is Uniquely Positioned to Address This Crisis**:
Unlike existing DV apps, SafeHaven:
1. **Centers marginalized survivors** (trans, BIPOC, disabled, male)
2. **Prioritizes safety over evidence** (GPS off by default, panic delete)
3. **Integrates economic empowerment** (job search, secret savings tracker)
4. **Built WITH survivors** (191 survivor interviews informed design)

---

### PROJECT DESCRIPTION

[Continue with detailed methodology, timeline, staffing plan...]

---

## Budget Template

### 18-Month Budget: Avon Foundation Proposal

| Category | Description | Amount | % |
|----------|-------------|--------|---|
| **PERSONNEL** | | **$95,000** | **63%** |
| iOS Developer | 12 months @ $130k (prorated: $95k) | $95,000 | 63% |
| | | | |
| **TECHNOLOGY** | | **$30,000** | **20%** |
| Cloud Infrastructure | AWS S3 (evidence backup), 18 months | $15,000 | 10% |
| Blockchain Gas Fees | Polygon transaction fees | $5,000 | 3% |
| App Store Fees | Apple Developer Account ($99/yr x 2) | $200 | <1% |
| Server Costs | Backend API hosting | $10,000 | 7% |
| | | | |
| **PROGRAM** | | **$15,000** | **10%** |
| Survivor Stipends | 50 beta testers x $50 | $2,500 | 2% |
| Partnership Travel | NNEDV conference, shelter site visits | $5,000 | 3% |
| Marketing Materials | Posters, brochures for partner shelters | $2,500 | 2% |
| Accessibility Audit | WCAG 2.1 compliance testing | $5,000 | 3% |
| | | | |
| **EVALUATION** | | **$5,000** | **3%** |
| User Research | Surveys, focus groups (n=200) | $3,000 | 2% |
| Data Analysis | Partnership with UC Berkeley | $2,000 | 1% |
| | | | |
| **INDIRECT** | | **$5,000** | **3%** |
| Administrative Costs | Accounting, legal, insurance (prorated) | $5,000 | 3% |
| | | | |
| **TOTAL REQUEST** | | **$150,000** | **100%** |

**Match/Leverage**: $50,000 from Verizon HopeLine (confirmed)
**Total Project Budget**: $200,000

---

## Logic Model

### SafeHaven Logic Model (Avon Foundation Proposal)

| INPUTS | ACTIVITIES | OUTPUTS | SHORT-TERM OUTCOMES | LONG-TERM OUTCOMES | IMPACT |
|--------|-----------|---------|---------------------|-------------------|--------|
| **Resources** | **What We Do** | **What We Produce** | **Initial Changes** | **Sustained Changes** | **Ultimate Goal** |
| $150k Avon grant | Develop iOS app | 3,000 iOS users | 85% feel safer | 500 survivors leave abuser | **Lives saved** |
| $50k Verizon grant | Partner with 50 shelters | 50 partnership MOUs | 90% find relevant resource | 50 restraining orders granted | **Economic independence** |
| Engineering team | Integrate job search | 500 job applications | 200 survivors employed | Financial independence | **Break cycle of violence** |
| Survivor advisors | Conduct user research | 200 survey responses | App improvements | Sustained usage (6+ months) | **Systemic change** |
| Partner shelters | Train staff | 100 staff trained | Increased referrals | SafeHaven = standard tool | **Transformed DV services** |

---

## Evaluation Plan

### Evaluation Framework (Avon Foundation Proposal)

**Evaluation Questions**:
1. Does SafeHaven increase survivor safety?
2. Does intersectional matching improve resource relevance?
3. Does economic integration support leaving?
4. Is SafeHaven culturally competent for marginalized survivors?

**Methods**:

#### Quantitative Metrics

| Metric | Target | Measurement | Frequency |
|--------|--------|-------------|-----------|
| **Users** | 10,000 total | App analytics | Monthly |
| **Marginalized representation** | 60%+ LGBTQ+/BIPOC/disabled/male | Onboarding survey | Quarterly |
| **Resource relevance** | 90%+ find relevant match | In-app feedback | Per search |
| **Safety** | 85%+ feel safer | User survey | Quarterly |
| **Employment** | 200 survivors employed | Followup survey | Quarterly |
| **Legal outcomes** | 50 restraining orders | Court tracking | Ongoing |

#### Qualitative Metrics

**Survivor Interviews** (n=50):
- Semi-structured interviews
- Questions:
  1. How has SafeHaven impacted your safety?
  2. Did intersectional matching help? (trans/BIPOC/disabled/male survivors)
  3. What would you change?
- Compensation: $50 gift card per 1-hour interview

**Partner Feedback**:
- Quarterly calls with 50 shelter partners
- Questions:
  1. How many clients have you referred to SafeHaven?
  2. What feedback have they shared?
  3. How can we improve?

**Case Studies** (n=10):
- In-depth stories of impact
- Example: "Trans Latina survivor uses SafeHaven to document abuse, find trans-inclusive shelter, obtain restraining order, secure employment"

#### Reporting to Avon Foundation

**Interim Report** (Month 9):
- Progress on iOS development (milestone: beta testing)
- User metrics (target: 5,000 users)
- Preliminary survey results

**Final Report** (Month 18):
- Full user metrics (target: 10,000 users)
- Case studies (10 survivor stories)
- Lessons learned + recommendations
- Sustainability plan

---

## Conclusion

**Grant funding enables SafeHaven to:**
1. Reach more survivors (iOS version)
2. Deepen partnerships (50 shelters)
3. Prove impact (rigorous evaluation)
4. Center marginalized survivors (LGBTQ+, BIPOC, disabled, male)

**These templates are ready to customize for each funder's priorities.**

---

**Document Version**: 1.0
**Last Updated**: December 8, 2025
**Questions**: grants@safehaven.app (placeholder)
