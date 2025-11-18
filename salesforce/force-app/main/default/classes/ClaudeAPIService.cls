/**
 * @description Service class for Anthropic Claude API integration in SafeHaven
 * @author SafeHaven Build Team
 * @date 2025-11-17
 *
 * IMPORTANT: This service analyzes domestic violence patterns to protect survivors.
 * All data transmitted is encrypted and handled with extreme care.
 *
 * Features:
 * - Secure API calls to Claude AI (claude-3-5-sonnet-20241022 for critical risk assessment)
 * - Pattern detection in incident reports
 * - Risk scoring and lethality assessment
 * - Crisis resource recommendations
 */
public with sharing class ClaudeAPIService {

    // API Configuration
    private static final String CLAUDE_ENDPOINT = 'https://api.anthropic.com/v1/messages';
    private static final String CLAUDE_MODEL = 'claude-3-5-sonnet-20241022'; // More powerful for risk assessment
    private static final String API_VERSION = '2023-06-01';
    private static final Integer MAX_TOKENS = 2000;
    private static final Decimal TEMPERATURE = 0.3; // Lower temperature for consistent risk assessment

    /**
     * Main method to call Claude API with system context and user prompt
     * @param systemContext - Context about the DV risk assessment task
     * @param userPrompt - Specific data to analyze (incident history, patterns)
     * @return String - Claude's response (JSON with risk scores and patterns)
     */
    public static String callClaudeAPI(String systemContext, String userPrompt) {
        try {
            // Get API key from Custom Metadata (secure storage)
            String apiKey = getAPIKey();

            if (String.isBlank(apiKey)) {
                throw new CalloutException('Claude API key not configured in Custom Metadata');
            }

            // Build request body
            Map<String, Object> requestBody = new Map<String, Object>{
                'model' => CLAUDE_MODEL,
                'max_tokens' => MAX_TOKENS,
                'temperature' => TEMPERATURE,
                'system' => systemContext,
                'messages' => new List<Map<String, String>>{
                    new Map<String, String>{
                        'role' => 'user',
                        'content' => userPrompt
                    }
                }
            };

            // Make HTTP callout
            HttpRequest req = new HttpRequest();
            req.setEndpoint(CLAUDE_ENDPOINT);
            req.setMethod('POST');
            req.setHeader('Content-Type', 'application/json');
            req.setHeader('x-api-key', apiKey);
            req.setHeader('anthropic-version', API_VERSION);
            req.setTimeout(60000); // 60 second timeout for complex analysis
            req.setBody(JSON.serialize(requestBody));

            Http http = new Http();
            HttpResponse res = http.send(req);

            // Handle response
            if (res.getStatusCode() == 200) {
                Map<String, Object> responseMap = (Map<String, Object>) JSON.deserializeUntyped(res.getBody());

                // Extract Claude's response text from content array
                List<Object> content = (List<Object>) responseMap.get('content');
                if (content != null && !content.isEmpty()) {
                    Map<String, Object> firstContent = (Map<String, Object>) content[0];
                    return (String) firstContent.get('text');
                }

                return 'No response content from Claude';
            } else {
                String errorBody = res.getBody();
                System.debug('Claude API Error: ' + res.getStatusCode() + ' - ' + errorBody);
                throw new CalloutException('Claude API returned status ' + res.getStatusCode() + ': ' + errorBody);
            }

        } catch (Exception e) {
            System.debug('Exception in callClaudeAPI: ' + e.getMessage() + '\nStack: ' + e.getStackTraceString());
            throw e;
        }
    }

    /**
     * Get Claude API key from Custom Metadata Type
     * @return String - API key
     */
    private static String getAPIKey() {
        try {
            // Query Custom Metadata Type: Claude_API_Config__mdt
            List<Claude_API_Config__mdt> configs = [
                SELECT API_Key__c
                FROM Claude_API_Config__mdt
                WHERE DeveloperName = 'SafeHaven_Config'
                LIMIT 1
            ];

            if (!configs.isEmpty() && String.isNotBlank(configs[0].API_Key__c)) {
                return configs[0].API_Key__c;
            }

            // Fallback: Try to get from Named Credential (if configured)
            return getAPIKeyFromNamedCredential();

        } catch (Exception e) {
            System.debug('Error retrieving API key: ' + e.getMessage());
            return null;
        }
    }

    /**
     * Alternative: Get API key from Named Credential
     * @return String - API key
     */
    private static String getAPIKeyFromNamedCredential() {
        // This would be configured in Setup -> Named Credentials
        // For now, return null - user must set up Custom Metadata
        return null;
    }

    /**
     * Test method for basic API connectivity
     * @return Boolean - True if API is reachable
     */
    public static Boolean testConnection() {
        try {
            String systemContext = 'You are a helpful AI assistant.';
            String userPrompt = 'Respond with: API connection successful';

            String response = callClaudeAPI(systemContext, userPrompt);
            return response.contains('successful');

        } catch (Exception e) {
            System.debug('Connection test failed: ' + e.getMessage());
            return false;
        }
    }

    /**
     * Wrapper class for API responses
     */
    public class ClaudeResponse {
        public String responseText;
        public Boolean success;
        public String errorMessage;
        public Integer tokenCount;

        public ClaudeResponse() {
            this.success = false;
        }
    }

    /**
     * Enhanced method with response wrapper
     * @param systemContext - System context
     * @param userPrompt - User prompt
     * @return ClaudeResponse - Wrapped response with metadata
     */
    public static ClaudeResponse callClaudeAPIWithWrapper(String systemContext, String userPrompt) {
        ClaudeResponse response = new ClaudeResponse();

        try {
            response.responseText = callClaudeAPI(systemContext, userPrompt);
            response.success = true;
            return response;

        } catch (Exception e) {
            response.success = false;
            response.errorMessage = e.getMessage();
            return response;
        }
    }
}
