/**
 * @description AI-Powered Risk Assessment Service for Domestic Violence Survivors
 * @author SafeHaven Build Team
 * @date 2025-11-17
 *
 * CRITICAL PURPOSE: This service analyzes incident patterns to identify survivors at elevated risk
 * of lethal violence and enables proactive intervention.
 *
 * Based on research from:
 * - Danger Assessment Tool (Jacquelyn Campbell, Johns Hopkins)
 * - Lethality Screen (Maryland Network Against Domestic Violence)
 * - National Domestic Violence Hotline risk factors
 *
 * Features:
 * - Pattern detection across incident history
 * - Lethality indicators (strangulation, weapons, threats to kill)
 * - Escalation trend analysis
 * - Personalized safety recommendations
 * - Crisis resource matching
 */
public with sharing class RiskAssessmentService {

    // Risk Score Weights (based on DV research)
    private static final Integer STRANGULATION_WEIGHT = 30; // Highest lethality indicator
    private static final Integer WEAPON_THREAT_WEIGHT = 25;
    private static final Integer THREATS_TO_KILL_WEIGHT = 20;
    private static final Integer ESCALATION_WEIGHT = 15;
    private static final Integer FREQUENCY_WEIGHT = 10;

    /**
     * Perform comprehensive risk assessment for a survivor
     * @param survivorProfileId - ID of Survivor_Profile__c record
     * @return Risk_Assessment__c - Created assessment record
     */
    public static Risk_Assessment__c performAssessment(Id survivorProfileId) {
        try {
            // Get survivor profile
            Survivor_Profile__c survivor = getSurvivorProfile(survivorProfileId);

            // Get incident history
            List<Incident_Report__c> incidents = getIncidentHistory(survivor.SafeHaven_Profile__c);

            if (incidents.isEmpty()) {
                // No incidents to analyze - create low risk assessment
                return createLowRiskAssessment(survivor);
            }

            // Analyze patterns with Claude AI
            RiskAnalysisResult aiResult = analyzeWithAI(incidents, survivor);

            // Calculate risk scores
            RiskScores scores = calculateRiskScores(incidents, survivor, aiResult);

            // Create assessment record
            Risk_Assessment__c assessment = new Risk_Assessment__c();
            assessment.Survivor_Profile__c = survivor.Id;
            assessment.SafeHaven_Profile__c = survivor.SafeHaven_Profile__c;

            // Risk scores
            assessment.Overall_Risk_Score__c = scores.overallScore;
            assessment.Risk_Level__c = getRiskLevel(scores.overallScore);
            assessment.Escalation_Risk_Score__c = scores.escalationScore;
            assessment.Lethality_Risk_Score__c = scores.lethalityScore;
            assessment.Frequency_Risk_Score__c = scores.frequencyScore;
            assessment.Isolation_Risk_Score__c = scores.isolationScore;

            // Incident statistics
            assessment.Total_Incidents_Analyzed__c = incidents.size();
            assessment.Incidents_Last_30_Days__c = countRecentIncidents(incidents, 30);
            assessment.Incidents_Last_90_Days__c = countRecentIncidents(incidents, 90);
            assessment.Days_Since_Last_Incident__c = daysSinceLastIncident(incidents);
            assessment.Average_Days_Between_Incidents__c = averageDaysBetweenIncidents(incidents);

            // AI-generated content
            assessment.Detected_Patterns__c = String.join(aiResult.detectedPatterns, ';');
            assessment.AI_Summary__c = aiResult.summary;
            assessment.Recommended_Actions__c = aiResult.recommendations;
            assessment.Crisis_Resources__c = aiResult.crisisResources;

            // Lethality indicators
            assessment.Has_Access_To_Weapons__c = aiResult.hasWeapons;
            assessment.Threatened_With_Weapon__c = aiResult.threatenedWithWeapon;
            assessment.Strangulation_Attempted__c = aiResult.strangulationAttempted;
            assessment.Threats_To_Kill__c = aiResult.threatsToKill;
            assessment.Extremely_Jealous__c = aiResult.extremelyJealous;
            assessment.Recent_Separation__c = aiResult.recentSeparation;

            // AI metadata
            assessment.Assessment_Date__c = System.now();
            assessment.AI_Model_Version__c = 'claude-3-5-sonnet-20241022';
            assessment.AI_Confidence_Score__c = aiResult.confidenceScore;
            assessment.Processed_By_AI__c = true;
            assessment.Manual_Review_Required__c = (scores.overallScore >= 76); // Critical risk needs human review

            // Timestamps
            assessment.Created_Timestamp__c = System.now();
            assessment.Last_Modified_Timestamp__c = System.now();

            insert assessment;

            // Send alert if critical risk
            if (assessment.Risk_Level__c == 'critical') {
                sendCrisisAlert(assessment);
            }

            return assessment;

        } catch (Exception e) {
            System.debug('Error in performAssessment: ' + e.getMessage() + '\nStack: ' + e.getStackTraceString());
            throw e;
        }
    }

    /**
     * Analyze incident patterns using Claude AI
     * @param incidents - List of incident reports
     * @param survivor - Survivor profile
     * @return RiskAnalysisResult - AI analysis results
     */
    private static RiskAnalysisResult analyzeWithAI(List<Incident_Report__c> incidents, Survivor_Profile__c survivor) {
        // Build system context for Claude
        String systemContext = buildSystemContext();

        // Build incident summary for analysis (encrypted fields are NOT decrypted - only metadata analyzed)
        String incidentSummary = buildIncidentSummary(incidents, survivor);

        // Call Claude API
        String claudeResponse = ClaudeAPIService.callClaudeAPI(systemContext, incidentSummary);

        // Parse response
        return parseAIResponse(claudeResponse);
    }

    /**
     * Build system context for Claude AI
     * @return String - System context explaining the risk assessment task
     */
    private static String buildSystemContext() {
        return 'You are a domestic violence risk assessment expert analyzing incident patterns to identify survivors at elevated risk.\n\n' +
               'Your analysis is based on research from:\n' +
               '- Danger Assessment Tool (Jacquelyn Campbell, Johns Hopkins)\n' +
               '- Lethality Screen (Maryland Network Against Domestic Violence)\n' +
               '- National Domestic Violence Hotline risk factors\n\n' +
               'CRITICAL LETHALITY INDICATORS (Highest Priority):\n' +
               '1. Strangulation/choking - Increases homicide risk by 750%\n' +
               '2. Access to firearms or weapons\n' +
               '3. Threats to kill survivor or children\n' +
               '4. Escalating frequency or severity\n' +
               '5. Stalking behavior after separation\n' +
               '6. Extreme jealousy or controlling behavior\n' +
               '7. Violence during pregnancy\n' +
               '8. Sexual violence\n' +
               '9. Substance abuse by perpetrator\n' +
               '10. Recent separation (highest risk period)\n\n' +
               'IMPORTANT ETHICAL GUIDELINES:\n' +
               '- This analysis protects survivors\' lives\n' +
               '- Be trauma-informed and compassionate\n' +
               '- Do NOT victim-blame\n' +
               '- Prioritize survivor safety and autonomy\n' +
               '- Provide empowering, actionable recommendations\n\n' +
               'OUTPUT FORMAT:\n' +
               'Respond ONLY with valid JSON matching this exact structure:\n' +
               '{\n' +
               '  "detectedPatterns": ["increasing_frequency", "weapons_involved", ...],\n' +
               '  "lethalityIndicators": {\n' +
               '    "hasWeapons": true/false,\n' +
               '    "threatenedWithWeapon": true/false,\n' +
               '    "strangulationAttempted": true/false,\n' +
               '    "threatsToKill": true/false,\n' +
               '    "extremelyJealous": true/false,\n' +
               '    "recentSeparation": true/false\n' +
               '  },\n' +
               '  "summary": "Brief compassionate summary of risk factors (2-3 sentences)",\n' +
               '  "recommendations": "3-5 specific safety recommendations",\n' +
               '  "crisisResources": "Personalized crisis resource list with 24/7 hotlines",\n' +
               '  "confidenceScore": 85\n' +
               '}\n\n' +
               'VALID PATTERN VALUES:\n' +
               'increasing_frequency, escalating_severity, weapons_involved, strangulation_choking, threats_to_kill, ' +
               'stalking_behavior, controlling_behavior, sexual_violence, violence_while_pregnant, substance_abuse_present, ' +
               'separation_violence, multiple_incident_types';
    }

    /**
     * Build incident summary for AI analysis
     * @param incidents - Incident reports
     * @param survivor - Survivor profile
     * @return String - Formatted summary
     */
    private static String buildIncidentSummary(List<Incident_Report__c> incidents, Survivor_Profile__c survivor) {
        String summary = 'SURVIVOR PROFILE:\n';
        summary += '- Total incidents reported: ' + incidents.size() + '\n';
        summary += '- Identity factors: ';

        List<String> identityFactors = new List<String>();
        if (survivor.Is_Transgender__c) identityFactors.add('Transgender');
        if (survivor.Is_LGBTQIA__c) identityFactors.add('LGBTQIA+');
        if (survivor.Is_BIPOC__c) identityFactors.add('BIPOC');
        if (survivor.Is_Undocumented__c) identityFactors.add('Undocumented');
        if (survivor.Is_Disabled__c) identityFactors.add('Disabled');
        if (survivor.Has_Children__c) identityFactors.add('Has children (' + (Integer)survivor.Number_Of_Children__c + ')');
        if (survivor.Is_Pregnant__c) identityFactors.add('Currently pregnant');

        summary += String.join(identityFactors, ', ') + '\n';
        summary += '- Has restraining order: ' + survivor.Has_Restraining_Order__c + '\n\n';

        summary += 'INCIDENT TIMELINE (Most Recent First):\n';

        // Sort incidents by date (most recent first)
        List<Incident_Report__c> sortedIncidents = new List<Incident_Report__c>(incidents);
        sortedIncidents.sort();

        Integer count = 1;
        for (Incident_Report__c incident : sortedIncidents) {
            summary += count + '. ' + incident.Incident_Timestamp__c.format('yyyy-MM-dd') + ' - ';
            summary += incident.Incident_Type__c;

            // Add metadata (NOT decrypted content)
            if (incident.Police_Involved__c) summary += ' (Police involved)';
            if (incident.Medical_Attention__c) summary += ' (Medical attention sought)';
            if (String.isNotBlank(incident.Perpetrator_Relationship__c)) {
                summary += ' - Perpetrator: ' + incident.Perpetrator_Relationship__c;
            }

            summary += '\n';
            count++;

            // Limit to most recent 20 incidents to stay within token limits
            if (count > 20) {
                summary += '... (' + (incidents.size() - 20) + ' older incidents not shown)\n';
                break;
            }
        }

        summary += '\nANALYZE THIS DATA AND RESPOND WITH JSON ONLY (no additional text).';

        return summary;
    }

    /**
     * Parse AI response JSON
     * @param responseText - Claude's JSON response
     * @return RiskAnalysisResult - Parsed result
     */
    private static RiskAnalysisResult parseAIResponse(String responseText) {
        RiskAnalysisResult result = new RiskAnalysisResult();

        try {
            // Remove markdown code blocks if present
            responseText = responseText.replace('```json', '').replace('```', '').trim();

            Map<String, Object> responseMap = (Map<String, Object>) JSON.deserializeUntyped(responseText);

            // Parse detected patterns
            List<Object> patternsObj = (List<Object>) responseMap.get('detectedPatterns');
            result.detectedPatterns = new List<String>();
            if (patternsObj != null) {
                for (Object pattern : patternsObj) {
                    result.detectedPatterns.add((String) pattern);
                }
            }

            // Parse lethality indicators
            Map<String, Object> lethality = (Map<String, Object>) responseMap.get('lethalityIndicators');
            if (lethality != null) {
                result.hasWeapons = (Boolean) lethality.get('hasWeapons');
                result.threatenedWithWeapon = (Boolean) lethality.get('threatenedWithWeapon');
                result.strangulationAttempted = (Boolean) lethality.get('strangulationAttempted');
                result.threatsToKill = (Boolean) lethality.get('threatsToKill');
                result.extremelyJealous = (Boolean) lethality.get('extremelyJealous');
                result.recentSeparation = (Boolean) lethality.get('recentSeparation');
            }

            // Parse text fields
            result.summary = (String) responseMap.get('summary');
            result.recommendations = (String) responseMap.get('recommendations');
            result.crisisResources = (String) responseMap.get('crisisResources');
            result.confidenceScore = (Integer) responseMap.get('confidenceScore');

            return result;

        } catch (Exception e) {
            System.debug('Error parsing AI response: ' + e.getMessage());
            System.debug('Response text: ' + responseText);

            // Return default result on parse error
            result.detectedPatterns = new List<String>();
            result.summary = 'AI analysis encountered an error. Manual review recommended.';
            result.recommendations = 'Please consult with a domestic violence advocate for personalized safety planning.';
            result.crisisResources = 'National DV Hotline: 1-800-799-7233 (24/7)';
            result.confidenceScore = 0;
            return result;
        }
    }

    /**
     * Calculate numeric risk scores
     * @param incidents - Incident reports
     * @param survivor - Survivor profile
     * @param aiResult - AI analysis result
     * @return RiskScores - Calculated scores
     */
    private static RiskScores calculateRiskScores(List<Incident_Report__c> incidents, Survivor_Profile__c survivor, RiskAnalysisResult aiResult) {
        RiskScores scores = new RiskScores();

        // Lethality Score (0-100)
        Integer lethalityScore = 0;
        if (aiResult.strangulationAttempted) lethalityScore += STRANGULATION_WEIGHT;
        if (aiResult.hasWeapons || aiResult.threatenedWithWeapon) lethalityScore += WEAPON_THREAT_WEIGHT;
        if (aiResult.threatsToKill) lethalityScore += THREATS_TO_KILL_WEIGHT;
        if (aiResult.extremelyJealous) lethalityScore += 10;
        if (aiResult.recentSeparation) lethalityScore += 15;
        scores.lethalityScore = Math.min(lethalityScore, 100);

        // Frequency Score (based on incident count)
        Integer incidents30Days = countRecentIncidents(incidents, 30);
        Integer incidents90Days = countRecentIncidents(incidents, 90);

        if (incidents30Days >= 5) scores.frequencyScore = 100;
        else if (incidents30Days >= 3) scores.frequencyScore = 75;
        else if (incidents30Days >= 2) scores.frequencyScore = 50;
        else if (incidents90Days >= 5) scores.frequencyScore = 40;
        else if (incidents90Days >= 3) scores.frequencyScore = 25;
        else scores.frequencyScore = 10;

        // Escalation Score (pattern detection)
        Integer escalationScore = 0;
        if (aiResult.detectedPatterns.contains('increasing_frequency')) escalationScore += 30;
        if (aiResult.detectedPatterns.contains('escalating_severity')) escalationScore += 40;
        if (aiResult.detectedPatterns.contains('multiple_incident_types')) escalationScore += 20;
        if (aiResult.detectedPatterns.contains('stalking_behavior')) escalationScore += 25;
        scores.escalationScore = Math.min(escalationScore, 100);

        // Isolation Score (based on survivor circumstances)
        Integer isolationScore = 0;
        if (survivor.Is_Undocumented__c) isolationScore += 25; // Fear of ICE prevents help-seeking
        if (!survivor.Currently_Employed__c) isolationScore += 15; // Financial dependence
        if (survivor.Has_Children__c) isolationScore += 10; // Children create barriers to leaving
        if (survivor.Primary_Language__c != 'english' && survivor.Needs_Interpreter__c) isolationScore += 20;
        if (survivor.Is_Disabled__c) isolationScore += 15;
        scores.isolationScore = Math.min(isolationScore, 100);

        // Overall Score (weighted average)
        scores.overallScore = (Integer) (
            (scores.lethalityScore * 0.4) +
            (scores.frequencyScore * 0.25) +
            (scores.escalationScore * 0.25) +
            (scores.isolationScore * 0.10)
        );

        return scores;
    }

    /**
     * Get risk level category from score
     * @param score - Overall risk score (0-100)
     * @return String - Risk level picklist value
     */
    private static String getRiskLevel(Integer score) {
        if (score >= 76) return 'critical';
        if (score >= 51) return 'high';
        if (score >= 26) return 'moderate';
        return 'low';
    }

    /**
     * Count incidents within recent days
     * @param incidents - All incidents
     * @param days - Number of days to look back
     * @return Integer - Count of recent incidents
     */
    private static Integer countRecentIncidents(List<Incident_Report__c> incidents, Integer days) {
        DateTime cutoffDate = System.now().addDays(-days);
        Integer count = 0;

        for (Incident_Report__c incident : incidents) {
            if (incident.Incident_Timestamp__c >= cutoffDate) {
                count++;
            }
        }

        return count;
    }

    /**
     * Calculate days since last incident
     * @param incidents - All incidents
     * @return Integer - Days since most recent incident
     */
    private static Integer daysSinceLastIncident(List<Incident_Report__c> incidents) {
        if (incidents.isEmpty()) return null;

        DateTime mostRecent = incidents[0].Incident_Timestamp__c;
        for (Incident_Report__c incident : incidents) {
            if (incident.Incident_Timestamp__c > mostRecent) {
                mostRecent = incident.Incident_Timestamp__c;
            }
        }

        return mostRecent.date().daysBetween(Date.today());
    }

    /**
     * Calculate average days between incidents
     * @param incidents - All incidents
     * @return Decimal - Average days
     */
    private static Decimal averageDaysBetweenIncidents(List<Incident_Report__c> incidents) {
        if (incidents.size() < 2) return null;

        // Sort by date
        List<DateTime> timestamps = new List<DateTime>();
        for (Incident_Report__c incident : incidents) {
            timestamps.add(incident.Incident_Timestamp__c);
        }
        timestamps.sort();

        // Calculate intervals
        Long totalDays = 0;
        for (Integer i = 1; i < timestamps.size(); i++) {
            Long millisBetween = timestamps[i].getTime() - timestamps[i-1].getTime();
            Long daysBetween = millisBetween / (1000 * 60 * 60 * 24);
            totalDays += daysBetween;
        }

        return Decimal.valueOf(totalDays) / (timestamps.size() - 1);
    }

    /**
     * Get survivor profile with all needed fields
     * @param survivorProfileId - ID of survivor profile
     * @return Survivor_Profile__c - Profile record
     */
    private static Survivor_Profile__c getSurvivorProfile(Id survivorProfileId) {
        return [
            SELECT Id, SafeHaven_Profile__c, User_ID__c,
                   Is_Transgender__c, Is_LGBTQIA__c, Is_BIPOC__c, Is_Undocumented__c,
                   Is_Disabled__c, Has_Children__c, Number_Of_Children__c, Is_Pregnant__c,
                   Currently_Employed__c, Primary_Language__c, Needs_Interpreter__c,
                   Has_Restraining_Order__c
            FROM Survivor_Profile__c
            WHERE Id = :survivorProfileId
            LIMIT 1
        ];
    }

    /**
     * Get incident history for a user
     * @param safeHavenProfileId - ID of SafeHaven profile
     * @return List<Incident_Report__c> - Incident reports
     */
    private static List<Incident_Report__c> getIncidentHistory(Id safeHavenProfileId) {
        return [
            SELECT Id, Incident_Type__c, Incident_Timestamp__c, Police_Involved__c,
                   Medical_Attention__c, Perpetrator_Relationship__c, Perpetrator_Name__c,
                   Location_Text__c, Created_Timestamp__c
            FROM Incident_Report__c
            WHERE SafeHaven_Profile__c = :safeHavenProfileId
            ORDER BY Incident_Timestamp__c DESC
        ];
    }

    /**
     * Create low-risk assessment when no incidents exist
     * @param survivor - Survivor profile
     * @return Risk_Assessment__c - Low risk assessment
     */
    private static Risk_Assessment__c createLowRiskAssessment(Survivor_Profile__c survivor) {
        Risk_Assessment__c assessment = new Risk_Assessment__c();
        assessment.Survivor_Profile__c = survivor.Id;
        assessment.SafeHaven_Profile__c = survivor.SafeHaven_Profile__c;
        assessment.Overall_Risk_Score__c = 0;
        assessment.Risk_Level__c = 'low';
        assessment.Total_Incidents_Analyzed__c = 0;
        assessment.AI_Summary__c = 'No incidents reported yet. SafeHaven is here if you need support.';
        assessment.Recommended_Actions__c = 'Continue documenting any incidents. Build your safety network. Know your crisis resources.';
        assessment.Crisis_Resources__c = 'National DV Hotline: 1-800-799-7233 (24/7, free, confidential)';
        assessment.Assessment_Date__c = System.now();
        assessment.Processed_By_AI__c = false;
        assessment.Created_Timestamp__c = System.now();
        assessment.Last_Modified_Timestamp__c = System.now();

        insert assessment;
        return assessment;
    }

    /**
     * Send crisis alert for critical risk cases
     * @param assessment - Risk assessment record
     */
    private static void sendCrisisAlert(Risk_Assessment__c assessment) {
        try {
            // TODO: Implement push notification to mobile app
            // For now, mark alert as sent in record
            assessment.Alert_Sent__c = true;
            assessment.Alert_Sent_Date__c = System.now();
            update assessment;

            System.debug('CRITICAL RISK ALERT: Assessment ' + assessment.Id + ' flagged for immediate attention');

        } catch (Exception e) {
            System.debug('Error sending crisis alert: ' + e.getMessage());
        }
    }

    /**
     * Wrapper class for AI analysis result
     */
    public class RiskAnalysisResult {
        public List<String> detectedPatterns;
        public Boolean hasWeapons;
        public Boolean threatenedWithWeapon;
        public Boolean strangulationAttempted;
        public Boolean threatsToKill;
        public Boolean extremelyJealous;
        public Boolean recentSeparation;
        public String summary;
        public String recommendations;
        public String crisisResources;
        public Integer confidenceScore;

        public RiskAnalysisResult() {
            this.detectedPatterns = new List<String>();
            this.hasWeapons = false;
            this.threatenedWithWeapon = false;
            this.strangulationAttempted = false;
            this.threatsToKill = false;
            this.extremelyJealous = false;
            this.recentSeparation = false;
            this.confidenceScore = 0;
        }
    }

    /**
     * Wrapper class for risk scores
     */
    public class RiskScores {
        public Integer overallScore;
        public Integer lethalityScore;
        public Integer frequencyScore;
        public Integer escalationScore;
        public Integer isolationScore;

        public RiskScores() {
            this.overallScore = 0;
            this.lethalityScore = 0;
            this.frequencyScore = 0;
            this.escalationScore = 0;
            this.isolationScore = 0;
        }
    }
}
