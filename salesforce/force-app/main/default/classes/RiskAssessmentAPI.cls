/**
 * @description REST API for Risk Assessment - Android App Integration
 * @author SafeHaven Build Team
 * @date 2025-11-17
 *
 * Endpoints:
 * POST /services/apexrest/safehaven/v1/risk-assessment/create - Create risk assessment
 * GET  /services/apexrest/safehaven/v1/risk-assessment/{userId} - Get latest assessment
 */
@RestResource(urlMapping='/safehaven/v1/risk-assessment/*')
global with sharing class RiskAssessmentAPI {

    /**
     * Create a new risk assessment for a survivor
     * POST /services/apexrest/safehaven/v1/risk-assessment/create
     *
     * Request Body:
     * {
     *   "userId": "user-12345"
     * }
     *
     * Response:
     * {
     *   "success": true,
     *   "assessmentId": "RA-0000123",
     *   "riskLevel": "high",
     *   "overallScore": 67,
     *   "summary": "AI-generated summary...",
     *   "recommendations": "Safety recommendations...",
     *   "crisisResources": "24/7 hotlines..."
     * }
     */
    @HttpPost
    global static AssessmentResponse createAssessment() {
        AssessmentResponse response = new AssessmentResponse();

        try {
            // Parse request
            RestRequest req = RestContext.request;
            String requestBody = req.requestBody.toString();
            Map<String, Object> requestMap = (Map<String, Object>) JSON.deserializeUntyped(requestBody);

            String userId = (String) requestMap.get('userId');

            if (String.isBlank(userId)) {
                response.success = false;
                response.errorMessage = 'Missing required field: userId';
                return response;
            }

            // Get survivor profile
            List<Survivor_Profile__c> profiles = [
                SELECT Id
                FROM Survivor_Profile__c
                WHERE User_ID__c = :userId
                LIMIT 1
            ];

            if (profiles.isEmpty()) {
                response.success = false;
                response.errorMessage = 'Survivor profile not found for userId: ' + userId;
                return response;
            }

            // Perform assessment
            Risk_Assessment__c assessment = RiskAssessmentService.performAssessment(profiles[0].Id);

            // Build response
            response.success = true;
            response.assessmentId = assessment.Name;
            response.riskLevel = assessment.Risk_Level__c;
            response.overallScore = (Integer) assessment.Overall_Risk_Score__c;
            response.lethalityScore = (Integer) assessment.Lethality_Risk_Score__c;
            response.escalationScore = (Integer) assessment.Escalation_Risk_Score__c;
            response.frequencyScore = (Integer) assessment.Frequency_Risk_Score__c;
            response.summary = assessment.AI_Summary__c;
            response.recommendations = assessment.Recommended_Actions__c;
            response.crisisResources = assessment.Crisis_Resources__c;
            response.detectedPatterns = assessment.Detected_Patterns__c != null ?
                assessment.Detected_Patterns__c.split(';') : new List<String>();
            response.assessmentDate = assessment.Assessment_Date__c;

            return response;

        } catch (Exception e) {
            response.success = false;
            response.errorMessage = e.getMessage();
            System.debug('Error in createAssessment: ' + e.getMessage() + '\nStack: ' + e.getStackTraceString());
            return response;
        }
    }

    /**
     * Get latest risk assessment for a user
     * GET /services/apexrest/safehaven/v1/risk-assessment/{userId}
     *
     * Response:
     * {
     *   "success": true,
     *   "assessmentId": "RA-0000123",
     *   "riskLevel": "high",
     *   "overallScore": 67,
     *   ...
     * }
     */
    @HttpGet
    global static AssessmentResponse getLatestAssessment() {
        AssessmentResponse response = new AssessmentResponse();

        try {
            RestRequest req = RestContext.request;
            String requestUri = req.requestURI;

            // Extract userId from URL: /safehaven/v1/risk-assessment/{userId}
            String userId = requestUri.substringAfterLast('/');

            if (String.isBlank(userId)) {
                response.success = false;
                response.errorMessage = 'Missing userId in URL';
                return response;
            }

            // Get survivor profile
            List<Survivor_Profile__c> profiles = [
                SELECT Id
                FROM Survivor_Profile__c
                WHERE User_ID__c = :userId
                LIMIT 1
            ];

            if (profiles.isEmpty()) {
                response.success = false;
                response.errorMessage = 'Survivor profile not found for userId: ' + userId;
                return response;
            }

            // Get latest assessment
            List<Risk_Assessment__c> assessments = [
                SELECT Id, Name, Risk_Level__c, Overall_Risk_Score__c,
                       Lethality_Risk_Score__c, Escalation_Risk_Score__c,
                       Frequency_Risk_Score__c, AI_Summary__c,
                       Recommended_Actions__c, Crisis_Resources__c,
                       Detected_Patterns__c, Assessment_Date__c
                FROM Risk_Assessment__c
                WHERE Survivor_Profile__c = :profiles[0].Id
                ORDER BY Assessment_Date__c DESC
                LIMIT 1
            ];

            if (assessments.isEmpty()) {
                response.success = false;
                response.errorMessage = 'No risk assessments found for this user';
                return response;
            }

            Risk_Assessment__c assessment = assessments[0];

            // Build response
            response.success = true;
            response.assessmentId = assessment.Name;
            response.riskLevel = assessment.Risk_Level__c;
            response.overallScore = (Integer) assessment.Overall_Risk_Score__c;
            response.lethalityScore = (Integer) assessment.Lethality_Risk_Score__c;
            response.escalationScore = (Integer) assessment.Escalation_Risk_Score__c;
            response.frequencyScore = (Integer) assessment.Frequency_Risk_Score__c;
            response.summary = assessment.AI_Summary__c;
            response.recommendations = assessment.Recommended_Actions__c;
            response.crisisResources = assessment.Crisis_Resources__c;
            response.detectedPatterns = assessment.Detected_Patterns__c != null ?
                assessment.Detected_Patterns__c.split(';') : new List<String>();
            response.assessmentDate = assessment.Assessment_Date__c;

            return response;

        } catch (Exception e) {
            response.success = false;
            response.errorMessage = e.getMessage();
            System.debug('Error in getLatestAssessment: ' + e.getMessage());
            return response;
        }
    }

    /**
     * Response wrapper class
     */
    global class AssessmentResponse {
        public Boolean success;
        public String errorMessage;
        public String assessmentId;
        public String riskLevel;
        public Integer overallScore;
        public Integer lethalityScore;
        public Integer escalationScore;
        public Integer frequencyScore;
        public String summary;
        public String recommendations;
        public String crisisResources;
        public List<String> detectedPatterns;
        public DateTime assessmentDate;

        public AssessmentResponse() {
            this.success = false;
        }
    }
}
