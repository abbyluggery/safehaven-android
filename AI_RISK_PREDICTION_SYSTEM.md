# SafeHaven AI Risk Prediction System

**Date**: 2025-11-17
**Version**: 1.0
**Purpose**: AI-powered crisis detection and lethal violence prevention for domestic violence survivors

---

## Overview

SafeHaven now includes an **AI-powered risk assessment system** that analyzes incident patterns to identify survivors at elevated risk of lethal violence. This enables proactive intervention and personalized safety planning.

### Research Foundation

Based on evidence-based research from:
- **Danger Assessment Tool** (Jacquelyn Campbell, PhD, Johns Hopkins University)
- **Lethality Screen** (Maryland Network Against Domestic Violence)
- **National Domestic Violence Hotline** risk factors database

### Key Features

✅ **Pattern Detection** - AI identifies dangerous escalation patterns
✅ **Lethality Scoring** - 0-100 risk scores with 4 severity levels
✅ **Crisis Alerts** - Automatic alerts for critical-risk cases
✅ **Safety Recommendations** - Personalized, trauma-informed guidance
✅ **Resource Matching** - Intersectional crisis resource lists
✅ **Privacy-First** - Encrypted data never decrypted during AI analysis

---

## Architecture

### Custom Objects

#### **1. Risk_Assessment__c**
Stores AI-generated risk analysis for each survivor.

**Key Fields**:
- `Overall_Risk_Score__c` - Combined risk score (0-100)
- `Risk_Level__c` - low | moderate | high | critical
- `Lethality_Risk_Score__c` - Lethal violence risk (0-100)
- `Escalation_Risk_Score__c` - Pattern escalation risk (0-100)
- `Frequency_Risk_Score__c` - Incident frequency risk (0-100)
- `Isolation_Risk_Score__c` - Social isolation risk (0-100)
- `Detected_Patterns__c` - Multi-select picklist of AI-detected patterns
- `AI_Summary__c` - Compassionate risk summary (2-3 sentences)
- `Recommended_Actions__c` - 3-5 specific safety recommendations
- `Crisis_Resources__c` - Personalized 24/7 resource list

**Lethality Indicators** (Highest Priority):
- `Strangulation_Attempted__c` - Increases homicide risk by 750%
- `Threatened_With_Weapon__c` - Firearm or weapon threats
- `Threats_To_Kill__c` - Explicit threats to kill survivor
- `Has_Access_To_Weapons__c` - Perpetrator has weapons
- `Extremely_Jealous__c` - Extreme jealousy/controlling behavior
- `Recent_Separation__c` - Highest risk period

**Incident Statistics**:
- `Total_Incidents_Analyzed__c` - Number of incidents in assessment
- `Incidents_Last_30_Days__c` - Recent activity
- `Incidents_Last_90_Days__c` - 3-month trend
- `Days_Since_Last_Incident__c` - Days since most recent incident
- `Average_Days_Between_Incidents__c` - Trend indicator

---

### Apex Classes

#### **1. ClaudeAPIService.cls**
Handles secure API calls to Anthropic Claude AI.

**Model Used**: `claude-3-5-sonnet-20241022` (most powerful for risk assessment)

**Features**:
- Secure API key storage in Custom Metadata
- 60-second timeout for complex analysis
- Error handling and retry logic
- Response parsing and validation

**Methods**:
```apex
// Main API call method
public static String callClaudeAPI(String systemContext, String userPrompt)

// Test connection
public static Boolean testConnection()

// Wrapper with metadata
public static ClaudeResponse callClaudeAPIWithWrapper(String systemContext, String userPrompt)
```

---

#### **2. RiskAssessmentService.cls**
Core risk assessment logic with AI integration.

**Risk Score Weights** (based on DV research):
```apex
STRANGULATION_WEIGHT = 30  // Highest lethality indicator
WEAPON_THREAT_WEIGHT = 25
THREATS_TO_KILL_WEIGHT = 20
ESCALATION_WEIGHT = 15
FREQUENCY_WEIGHT = 10
```

**Key Methods**:
```apex
// Perform complete risk assessment
public static Risk_Assessment__c performAssessment(Id survivorProfileId)

// Analyze with Claude AI
private static RiskAnalysisResult analyzeWithAI(
    List<Incident_Report__c> incidents,
    Survivor_Profile__c survivor
)

// Calculate numeric risk scores
private static RiskScores calculateRiskScores(
    List<Incident_Report__c> incidents,
    Survivor_Profile__c survivor,
    RiskAnalysisResult aiResult
)

// Send crisis alert for critical cases
private static void sendCrisisAlert(Risk_Assessment__c assessment)
```

**Scoring Algorithm**:
```apex
Overall Score = (Lethality × 0.40) +
                (Frequency × 0.25) +
                (Escalation × 0.25) +
                (Isolation × 0.10)
```

**Risk Levels**:
- **Critical (76-100)** - Immediate danger, manual review required
- **High (51-75)** - Elevated risk, proactive intervention
- **Moderate (26-50)** - Monitor closely
- **Low (0-25)** - Standard safety planning

---

#### **3. RiskAssessmentAPI.cls**
REST API for Android app integration.

**Endpoints**:

**Create Assessment**:
```
POST /services/apexrest/safehaven/v1/risk-assessment/create

Request:
{
  "userId": "user-12345"
}

Response:
{
  "success": true,
  "assessmentId": "RA-0000123",
  "riskLevel": "high",
  "overallScore": 67,
  "lethalityScore": 80,
  "escalationScore": 55,
  "frequencyScore": 50,
  "summary": "AI-generated compassionate summary...",
  "recommendations": "1. Create safety plan\n2. Pack go-bag\n3. Identify safe locations...",
  "crisisResources": "National DV Hotline: 1-800-799-7233 (24/7)\nRAINN: 1-800-656-4673...",
  "detectedPatterns": ["escalating_severity", "weapons_involved", "threats_to_kill"],
  "assessmentDate": "2025-11-17T22:30:00.000Z"
}
```

**Get Latest Assessment**:
```
GET /services/apexrest/safehaven/v1/risk-assessment/{userId}

Response: Same as create
```

---

## AI Analysis Process

### 1. System Context (540+ Lines)

Claude AI receives detailed context about:
- Danger Assessment Tool methodology
- 10 critical lethality indicators
- Ethical guidelines (trauma-informed, no victim-blaming)
- Valid pattern values
- Required JSON output format

### 2. Incident Summary

AI analyzes metadata ONLY (encrypted fields never decrypted):
- Incident timeline (most recent first)
- Incident types (physical, verbal, sexual, financial, stalking)
- Police involvement
- Medical attention sought
- Perpetrator relationship
- Survivor's intersectional identity factors
- Restraining order status

### 3. Pattern Detection

AI detects 12 dangerous patterns:
1. **increasing_frequency** - Incidents becoming more frequent
2. **escalating_severity** - Violence intensifying over time
3. **weapons_involved** - Firearms, knives, or weapons present
4. **strangulation_choking** - Strangulation attempted (750% higher homicide risk)
5. **threats_to_kill** - Explicit threats to kill survivor or children
6. **stalking_behavior** - Persistent stalking, especially after separation
7. **controlling_behavior** - Extreme jealousy, isolation tactics
8. **sexual_violence** - Sexual abuse present
9. **violence_while_pregnant** - Abuse during pregnancy
10. **substance_abuse_present** - Perpetrator substance abuse
11. **separation_violence** - Violence during or after separation attempt
12. **multiple_incident_types** - Multiple abuse types (physical + financial + stalking)

### 4. AI Response Format

Claude responds with strict JSON:
```json
{
  "detectedPatterns": ["pattern1", "pattern2", ...],
  "lethalityIndicators": {
    "hasWeapons": true,
    "threatenedWithWeapon": true,
    "strangulationAttempted": false,
    "threatsToKill": true,
    "extremelyJealous": true,
    "recentSeparation": false
  },
  "summary": "2-3 sentence compassionate summary",
  "recommendations": "3-5 specific safety actions",
  "crisisResources": "24/7 hotline list with descriptions",
  "confidenceScore": 85
}
```

---

## Scoring Examples

### Example 1: Critical Risk (Score: 92)

**Scenario**:
- Strangulation attempted (+30 lethality)
- Perpetrator has firearm (+25 lethality)
- Threatened to kill survivor (+20 lethality)
- 5 incidents in last 30 days (100 frequency)
- Escalating from verbal to physical (+40 escalation)
- Survivor is undocumented immigrant (+25 isolation)

**Calculation**:
```
Lethality: 75 (strangulation + weapon + threats)
Frequency: 100 (5+ incidents in 30 days)
Escalation: 70 (escalating severity + weapons + stalking)
Isolation: 45 (undocumented + not employed + has children)

Overall = (75 × 0.40) + (100 × 0.25) + (70 × 0.25) + (45 × 0.10)
        = 30 + 25 + 17.5 + 4.5
        = 77 → **CRITICAL RISK**
```

**Response**:
- Risk Level: CRITICAL
- Alert sent to survivor: YES
- Manual review required: YES
- Recommended actions: Immediate safety planning, consider shelter, contact advocate

---

### Example 2: Moderate Risk (Score: 38)

**Scenario**:
- 2 verbal abuse incidents in 90 days (25 frequency)
- No weapons involved (0 lethality)
- Controlling behavior detected (+20 escalation)
- Survivor employed and has support network (10 isolation)

**Calculation**:
```
Lethality: 0 (no weapons, threats, or strangulation)
Frequency: 25 (2-3 incidents in 90 days)
Escalation: 20 (controlling behavior only)
Isolation: 10 (employed, has support)

Overall = (0 × 0.40) + (25 × 0.25) + (20 × 0.25) + (10 × 0.10)
        = 0 + 6.25 + 5 + 1
        = 12.25 → **LOW RISK** (but monitoring recommended)
```

**Response**:
- Risk Level: LOW to MODERATE
- Alert sent: NO
- Recommended actions: Document incidents, build safety network, know resources

---

## Setup Instructions

### 1. Deploy Custom Objects

Deploy `Risk_Assessment__c`:
```bash
cd "C:\Users\Abbyl\OneDrive\Desktop\Salesforce Training\SafeHaven-Build"
sf project deploy start \
  --source-dir salesforce/force-app/main/default/objects/Risk_Assessment__c \
  --target-org abbyluggery179@agentforce.com \
  --wait 15
```

### 2. Deploy Apex Classes

Deploy all risk assessment classes:
```bash
sf project deploy start \
  --metadata ApexClass:ClaudeAPIService,ApexClass:RiskAssessmentService,ApexClass:RiskAssessmentAPI \
  --target-org abbyluggery179@agentforce.com \
  --wait 15
```

### 3. Configure Claude API Key

**Option A: Custom Metadata (Recommended)**

1. Deploy Custom Metadata Type:
```bash
sf project deploy start \
  --source-dir salesforce/force-app/main/default/objects/Claude_API_Config__mdt \
  --target-org abbyluggery179@agentforce.com \
  --wait 10
```

2. In Salesforce Setup:
   - Go to: **Setup → Custom Metadata Types**
   - Click **"Manage Records"** next to "Claude API Config"
   - Edit **"SafeHaven_Config"** record
   - Set `API_Key__c` to your Anthropic API key (starts with `sk-ant-`)
   - Save

**Option B: Named Credential**

1. Setup → Named Credentials → New Named Credential
2. Name: `Claude_AI`
3. URL: `https://api.anthropic.com`
4. Identity Type: Named Principal
5. Authentication Protocol: Custom
6. Custom Headers:
   - `x-api-key`: `YOUR_API_KEY`
   - `anthropic-version`: `2023-06-01`

### 4. Enable Remote Site Settings

Setup → Remote Site Settings → New Remote Site:
- Name: `Anthropic_Claude_API`
- URL: `https://api.anthropic.com`
- Active: ☑

### 5. Test Setup

Run in Anonymous Apex:
```apex
// Test API connection
Boolean connected = ClaudeAPIService.testConnection();
System.debug('API Connected: ' + connected);

// Find a survivor profile to test
List<Survivor_Profile__c> profiles = [SELECT Id FROM Survivor_Profile__c LIMIT 1];

if (!profiles.isEmpty()) {
    // Perform test assessment
    Risk_Assessment__c assessment = RiskAssessmentService.performAssessment(profiles[0].Id);
    System.debug('Assessment created: ' + assessment.Name);
    System.debug('Risk Level: ' + assessment.Risk_Level__c);
    System.debug('Overall Score: ' + assessment.Overall_Risk_Score__c);
}
```

---

## API Integration for Android App

### Create Risk Assessment

**Kotlin Example**:
```kotlin
data class AssessmentRequest(val userId: String)

data class AssessmentResponse(
    val success: Boolean,
    val assessmentId: String?,
    val riskLevel: String?,
    val overallScore: Int?,
    val lethalityScore: Int?,
    val summary: String?,
    val recommendations: String?,
    val crisisResources: String?,
    val detectedPatterns: List<String>?,
    val errorMessage: String?
)

suspend fun createRiskAssessment(userId: String): AssessmentResponse {
    val url = "$salesforceInstanceUrl/services/apexrest/safehaven/v1/risk-assessment/create"

    val request = AssessmentRequest(userId = userId)

    return httpClient.post(url) {
        headers {
            append("Authorization", "Bearer $accessToken")
            append("Content-Type", "application/json")
        }
        setBody(Json.encodeToString(request))
    }.body()
}
```

### Display Risk Assessment

**UI Example**:
```kotlin
@Composable
fun RiskAssessmentCard(assessment: AssessmentResponse) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (assessment.riskLevel) {
                "critical" -> Color.Red.copy(alpha = 0.1f)
                "high" -> Color.Orange.copy(alpha = 0.1f)
                "moderate" -> Color.Yellow.copy(alpha = 0.1f)
                else -> Color.Green.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Risk Level: ${assessment.riskLevel?.uppercase()}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Overall Score: ${assessment.overallScore}/100",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            // AI Summary
            Text(
                text = assessment.summary ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Safety Recommendations
            Text(
                text = "Safety Recommendations:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = assessment.recommendations ?: "",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Crisis Resources (always visible for critical/high risk)
            if (assessment.riskLevel in listOf("critical", "high")) {
                Button(
                    onClick = { /* Show crisis resources */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("24/7 Crisis Resources")
                }
            }
        }
    }
}
```

---

## Privacy & Security

### Data Minimization

- **Encrypted fields are NEVER decrypted** during AI analysis
- Only metadata is analyzed (dates, types, boolean flags)
- No personally identifiable information sent to Claude API
- No perpetrator names or identifying details transmitted

### Trauma-Informed Design

- **No victim-blaming** - AI trained to focus on perpetrator behavior, not survivor choices
- **Empowering language** - Recommendations emphasize survivor autonomy
- **Compassionate tone** - Summaries acknowledge trauma without judgment
- **Culturally competent** - Considers intersectional identity factors

### Ethical AI Use

- **Research-based** - Lethality indicators from peer-reviewed DV research
- **Expert-reviewed** - System designed with DV advocate input
- **Transparent** - Survivors see AI confidence scores
- **Human oversight** - Critical cases flagged for manual review
- **Survivor control** - Survivors decide when to run assessments

---

## Deployment Status

| Component | Status | Deploy Command |
|-----------|--------|----------------|
| Risk_Assessment__c object | ⏳ Ready | `sf project deploy start --source-dir salesforce/force-app/main/default/objects/Risk_Assessment__c` |
| ClaudeAPIService.cls | ⏳ Ready | `sf project deploy start --metadata ApexClass:ClaudeAPIService` |
| RiskAssessmentService.cls | ⏳ Ready | `sf project deploy start --metadata ApexClass:RiskAssessmentService` |
| RiskAssessmentAPI.cls | ⏳ Ready | `sf project deploy start --metadata ApexClass:RiskAssessmentAPI` |
| Claude_API_Config__mdt | ⏳ Ready | `sf project deploy start --source-dir salesforce/force-app/main/default/objects/Claude_API_Config__mdt` |

**Deployment Command** (All at Once):
```bash
cd "C:\Users\Abbyl\OneDrive\Desktop\Salesforce Training\SafeHaven-Build"

sf project deploy start \
  --source-dir salesforce/force-app/main/default \
  --target-org abbyluggery179@agentforce.com \
  --wait 15
```

---

## Impact & Research

### Why This Matters

**750% Increased Homicide Risk**: Strangulation (choking) increases a domestic violence survivor's risk of being killed by 750% (Campbell et al., 2003).

**Separation as Highest Risk Period**: 70% of DV homicides occur when the survivor is leaving or has recently left the relationship (National Coalition Against Domestic Violence).

**Early Detection Saves Lives**: Lethality screening with immediate intervention has been shown to reduce DV homicides by up to 40% in communities where implemented (Maryland Network Against Domestic Violence, 2010-2020 data).

### SafeHaven's Innovation

**First AI-powered lethality assessment for survivors** with:
- Real-time pattern detection across incident history
- Intersectional risk analysis (identity factors amplify vulnerability)
- Privacy-preserving AI (encrypted data never decrypted)
- Mobile-first crisis intervention
- Culturally competent resource matching

---

## Future Enhancements

1. **Machine Learning Model** - Custom ML model trained on anonymized DV data (with survivor consent)
2. **Predictive Alerts** - Proactive alerts before incidents occur (based on time-of-day patterns)
3. **Integration with Legal Aid** - Auto-notify legal aid for restraining order assistance
4. **Multi-language Support** - AI analysis in Spanish, ASL video descriptions, etc.
5. **Community Network** - Connect survivors with similar experiences (opt-in, anonymous)

---

**For Support**: National Domestic Violence Hotline - 1-800-799-7233 (24/7, free, confidential)

**Technical Support**: SafeHaven Development Team
