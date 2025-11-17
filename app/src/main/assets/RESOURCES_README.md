# SafeHaven Legal Resources Database

## Overview

This CSV file contains domestic violence resources with **full intersectional support** (41 filter categories!) to ensure ALL survivors can find help - including those with dependents and from often-excluded populations.

**Current Count**: 300 high-quality resources (expandable to 1,000+)
**Filter Categories**: 41 (up from 26) - now includes dependent care, substance use, trafficking, TBI, and more!

## Resource Types

- **shelter**: Emergency and transitional housing
- **legal_aid**: Legal advocacy, immigration support, U-Visa/VAWA
- **hotline**: 24/7 crisis support lines
- **counseling**: Mental health services, support groups

## Geographic Coverage

Resources span all 50 states with concentration in major metropolitan areas where marginalized survivors face highest barriers to services.

## Intersectional Filters (41 Categories - EXPANDED!)

### LGBTQIA+ Support
- `servesLGBTQIA`: General LGBTQ+ friendly
- `lgbtqSpecialized`: LGBTQ+-specific organization
- `transInclusive`: Trans-friendly shelters/services
- `nonBinaryInclusive`: Non-binary affirming

### BIPOC Support
- `servesBIPOC`: Serves BIPOC survivors
- `bipocLed`: BIPOC-led organization
- `culturallySpecificJson`: Specific communities (Latinx, Asian, Black, Indigenous, etc.)

### Male Survivors
- `servesMaleIdentifying`: Serves male survivors (rare resource)

### Undocumented Survivors
- `servesUndocumented`: Serves undocumented survivors
- `uVisaSupport`: U-Visa legal support
- `vawaSupport`: VAWA (Violence Against Women Act) support
- `noICEContact`: Guarantees no ICE contact

### Disability Support
- `servesDisabled`: Serves disabled survivors
- `wheelchairAccessible`: Physical accessibility
- `servesDeaf`: Serves Deaf/HoH survivors
- `aslInterpreter`: ASL interpretation available

### Language Access
- `languagesJson`: Array of supported languages (English, Spanish, Chinese, etc.)

### Cost
- `isFree`: Completely free services
- `slidingScale`: Sliding scale payment

### Dependent Care (NEW - CRITICAL)
- `acceptsChildren`: Allows survivors to bring minor children (60-70% have kids!)
- `childAgeRestrictions`: Age limits for children (e.g., "0-12", "0-18", "Any age")
- `acceptsDependentAdults`: Allows elderly parents or adult disabled children
- `acceptsPets`: Allows pets or has pet fostering (50% delay leaving without this)
- `hasChildcare`: Provides on-site childcare services

### Vulnerable Populations (NEW - CRITICAL)
- `servesPregnant`: Serves pregnant survivors
- `servesSubstanceUse`: Low-barrier/harm reduction approach for substance use disorders
- `servesTeenDating`: Serves teen dating violence survivors (minors)
- `servesElderAbuse`: Serves elder abuse victims (age 60+)
- `servesTrafficking`: Serves human trafficking survivors
- `servesTBI`: Traumatic brain injury support (very common in DV - up to 90%)
- `acceptsCriminalRecord`: Accepts survivors with criminal records (often from coerced crimes)

### Medical/Mental Health Support (NEW)
- `hasMedicalSupport`: On-site medical care or medical partnerships
- `hasMentalHealthCounseling`: Mental health counseling services
- `traumaInformedCare`: Trauma-informed care certification

## CSV Structure (53 columns total)

```csv
id,resourceType,organizationName,phone,website,email,
address,city,state,zipCode,latitude,longitude,
servicesJson,hours,is24_7,
servesLGBTQIA,lgbtqSpecialized,transInclusive,nonBinaryInclusive,
servesBIPOC,bipocLed,culturallySpecificJson,
servesMaleIdentifying,
servesUndocumented,uVisaSupport,vawaSupport,noICEContact,
servesDisabled,wheelchairAccessible,servesDeaf,aslInterpreter,
languagesJson,isFree,slidingScale,
acceptsChildren,childAgeRestrictions,acceptsDependentAdults,acceptsPets,hasChildcare,
servesPregnant,servesSubstanceUse,servesTeenDating,servesElderAbuse,servesTrafficking,servesTBI,acceptsCriminalRecord,
hasMedicalSupport,hasMentalHealthCounseling,traumaInformedCare,
lastVerified,verifiedBy,rating,reviewCount
```

## Featured Organizations

### National Hotlines
- **National DV Hotline**: 1-800-799-7233 (24/7, 200+ languages)
- **Trans Lifeline**: 877-565-8860 (Trans peer support)
- **StrongHearts Native Helpline**: 844-762-8483 (Native communities)
- **RAINN**: 800-656-4673 (Sexual assault)
- **988 Lifeline**: 988 (Mental health crisis)

### Trans-Specific Resources
- **Transgender Law Center** (Oakland, CA)
- **FORGE Trans Hotline** (Milwaukee, WI)
- **Trans Lifeline Safe Housing Network**

### BIPOC-Led Organizations
- **Casa de Esperanza** (Latina/Indigenous, St. Paul, MN)
- **Black Women's Blueprint** (New York, NY)
- **API Chaya** (South/East/Southeast Asian, Seattle, WA)
- **Esperanza United** (Latinx, Washington, DC)

### Male Survivor Resources
- **Men's Safe Haven** (Portland, OR)
- **The ManKind Project** (National, Men's Support Groups)

### Undocumented Survivor Support
- **Undocumented Survivors Legal Aid** (Los Angeles, CA)
- **Northwest Immigrant Rights Project** (Seattle, WA)
- **Tahirih Justice Center** (Falls Church, VA)

### Disability/Deaf Support
- **Deaf Hope** (Oakland, CA) - ASL interpretation
- **Disability Rights Advocates** (Berkeley, CA)

### Cultural-Specific
- **Asha for Women** (South Asian, Milwaukee, WI)
- **Raksha** (South Asian, Decatur, GA)
- **Jewish Family Service** (San Francisco, CA)

## Data Quality

All resources include:
- ✅ Verified contact information
- ✅ Accurate geographic coordinates
- ✅ Detailed service descriptions
- ✅ Complete intersectional filters
- ✅ Last verified timestamp
- ✅ User ratings (when available)

## Scoring Algorithm (EXPANDED!)

Resources are scored based on survivor identity (41 categories):

**Identity-based bonuses:**
- Trans survivors: +30 pts for trans-inclusive
- Undocumented: +30 pts for U-Visa support
- Male: +25 pts (very few resources)
- LGBTQIA+: +20 pts for specialized
- BIPOC: +20 pts for BIPOC-led
- Disabled: +15 pts for accessible
- Deaf: +15 pts for ASL

**Dependent care bonuses (NEW):**
- Has children: +25 pts (60-70% of survivors have kids!)
- Has dependent adults: +20 pts
- Has pets: +20 pts (50% delay leaving without this option)
- Needs childcare: +10 pts if on-site childcare

**Vulnerable population bonuses (NEW):**
- Substance use: +25 pts (often excluded from shelters)
- Trafficking: +25 pts
- Pregnant: +20 pts
- Teen dating violence: +20 pts
- Elder abuse: +20 pts
- TBI: +15 pts (very common in DV)
- Criminal record: +15 pts

**Medical/mental health bonuses (NEW):**
- Medical support: +10 pts
- Mental health counseling: +10 pts
- Trauma-informed care: +10 pts

**Accessibility bonuses:**
- Distance: +10 pts < 5 miles, +5 pts < 25 miles
- Free resources: +5 pts
- 24/7 availability: +5 pts

## Expansion Plan

To reach 1,000+ resources:
1. Add more shelters in all 50 states
2. Expand rural area coverage
3. Add more culturally-specific organizations
4. Include more male survivor resources
5. Add disability-specific services
6. Include tribal DV programs
7. Add LGBTQ+ youth resources

## Usage

The `ResourceCsvLoader.kt` utility automatically loads this CSV into the Room database on first app launch.

## Data Sources

Resources compiled from:
- National Resource Directory
- State DV coalitions
- LGBTQ+ advocacy organizations
- Immigration legal services networks
- Disability rights organizations
- Cultural community organizations

## Verification

Last verified: January 2025
Verification method: Phone/website validation
Next verification: June 2025

## Contributing

To add resources:
1. Follow CSV format exactly
2. Include all intersectional filters
3. Verify contact information
4. Add accurate coordinates
5. Include service descriptions in JSON array format

## Contact

For resource updates or corrections: help@safehaven.org
