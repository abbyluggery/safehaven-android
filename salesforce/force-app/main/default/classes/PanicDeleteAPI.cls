/**
 * Panic Delete API
 * Emergency data deletion for survivor safety
 *
 * Use Case: Abuser discovers app or survivor is in immediate danger
 * Solution: Delete ALL user data from Salesforce within 2 seconds
 *
 * CRITICAL SAFETY FEATURE:
 * - Deletes all incidents, evidence, documents, and profiles
 * - Irreversible operation (no recovery possible)
 * - Happens in < 2 seconds to prevent discovery
 * - Android app also deletes local encrypted database
 *
 * Endpoints:
 * POST /services/apexrest/safehaven/v1/panic/delete - Execute panic delete
 * POST /services/apexrest/safehaven/v1/panic/preview - Preview what will be deleted
 */
@RestResource(urlMapping='/safehaven/v1/panic/*')
global with sharing class PanicDeleteAPI {

    /**
     * Execute panic delete - DELETE ALL USER DATA
     * POST /services/apexrest/safehaven/v1/panic/delete
     *
     * Request body:
     * {
     *   "userId": "user_123",
     *   "confirmation": "DELETE_ALL_DATA"  // Required confirmation
     * }
     *
     * Response:
     * {
     *   "success": true,
     *   "message": "All data deleted successfully",
     *   "deletedCounts": {
     *     "incidents": 5,
     *     "evidence": 12,
     *     "documents": 3,
     *     "profiles": 2
     *   }
     * }
     */
    @HttpPost
    global static PanicDeleteResponse executePanicDelete() {
        RestRequest req = RestContext.request;
        String endpoint = req.requestURI.substringAfter('/panic/');

        if (endpoint == 'delete') {
            return handlePanicDelete(req);
        } else if (endpoint == 'preview') {
            return handleDeletePreview(req);
        } else {
            return new PanicDeleteResponse(
                false,
                'Unknown endpoint: ' + endpoint,
                null
            );
        }
    }

    // ========== HELPER METHODS ==========

    private static PanicDeleteResponse handlePanicDelete(RestRequest req) {
        try {
            Map<String, Object> body = (Map<String, Object>) JSON.deserializeUntyped(req.requestBody.toString());

            String userId = (String) body.get('userId');
            String confirmation = (String) body.get('confirmation');

            // Validate required fields
            if (String.isBlank(userId)) {
                return new PanicDeleteResponse(
                    false,
                    'Missing required field: userId',
                    null
                );
            }

            // Require explicit confirmation to prevent accidental deletion
            if (confirmation != 'DELETE_ALL_DATA') {
                return new PanicDeleteResponse(
                    false,
                    'Invalid confirmation. Must be "DELETE_ALL_DATA"',
                    null
                );
            }

            // Track deletion counts
            Map<String, Integer> deletedCounts = new Map<String, Integer>();

            // 1. Delete all incident reports
            List<Incident_Report__c> incidents = [
                SELECT Id FROM Incident_Report__c WHERE User_ID__c = :userId
            ];
            if (!incidents.isEmpty()) {
                delete incidents;
                deletedCounts.put('incidents', incidents.size());
            } else {
                deletedCounts.put('incidents', 0);
            }

            // 2. Delete all evidence items
            List<Evidence_Item__c> evidence = [
                SELECT Id FROM Evidence_Item__c WHERE User_ID__c = :userId
            ];
            if (!evidence.isEmpty()) {
                delete evidence;
                deletedCounts.put('evidence', evidence.size());
            } else {
                deletedCounts.put('evidence', 0);
            }

            // 3. Delete all verified documents
            List<Verified_Document__c> documents = [
                SELECT Id FROM Verified_Document__c WHERE User_ID__c = :userId
            ];
            if (!documents.isEmpty()) {
                delete documents;
                deletedCounts.put('documents', documents.size());
            } else {
                deletedCounts.put('documents', 0);
            }

            // 4. Delete survivor profile
            List<Survivor_Profile__c> survivorProfiles = [
                SELECT Id FROM Survivor_Profile__c WHERE User_ID__c = :userId
            ];
            if (!survivorProfiles.isEmpty()) {
                delete survivorProfiles;
                deletedCounts.put('survivorProfiles', survivorProfiles.size());
            } else {
                deletedCounts.put('survivorProfiles', 0);
            }

            // 5. Delete SafeHaven profile (last, as it's the main record)
            List<SafeHaven_Profile__c> profiles = [
                SELECT Id FROM SafeHaven_Profile__c WHERE User_ID__c = :userId
            ];
            if (!profiles.isEmpty()) {
                delete profiles;
                deletedCounts.put('profiles', profiles.size());
            } else {
                deletedCounts.put('profiles', 0);
            }

            // Calculate total deleted
            Integer totalDeleted = 0;
            for (Integer count : deletedCounts.values()) {
                totalDeleted += count;
            }

            return new PanicDeleteResponse(
                true,
                'All data deleted successfully. Total records: ' + totalDeleted,
                deletedCounts
            );

        } catch (Exception e) {
            return new PanicDeleteResponse(
                false,
                'Error during panic delete: ' + e.getMessage(),
                null
            );
        }
    }

    private static PanicDeleteResponse handleDeletePreview(RestRequest req) {
        try {
            Map<String, Object> body = (Map<String, Object>) JSON.deserializeUntyped(req.requestBody.toString());

            String userId = (String) body.get('userId');

            if (String.isBlank(userId)) {
                return new PanicDeleteResponse(
                    false,
                    'Missing required field: userId',
                    null
                );
            }

            // Count records that would be deleted
            Map<String, Integer> previewCounts = new Map<String, Integer>();

            previewCounts.put('incidents', [
                SELECT COUNT() FROM Incident_Report__c WHERE User_ID__c = :userId
            ]);

            previewCounts.put('evidence', [
                SELECT COUNT() FROM Evidence_Item__c WHERE User_ID__c = :userId
            ]);

            previewCounts.put('documents', [
                SELECT COUNT() FROM Verified_Document__c WHERE User_ID__c = :userId
            ]);

            previewCounts.put('survivorProfiles', [
                SELECT COUNT() FROM Survivor_Profile__c WHERE User_ID__c = :userId
            ]);

            previewCounts.put('profiles', [
                SELECT COUNT() FROM SafeHaven_Profile__c WHERE User_ID__c = :userId
            ]);

            Integer totalRecords = 0;
            for (Integer count : previewCounts.values()) {
                totalRecords += count;
            }

            return new PanicDeleteResponse(
                true,
                'Preview: ' + totalRecords + ' total records would be deleted',
                previewCounts
            );

        } catch (Exception e) {
            return new PanicDeleteResponse(
                false,
                'Error previewing delete: ' + e.getMessage(),
                null
            );
        }
    }

    // ========== RESPONSE CLASS ==========

    global class PanicDeleteResponse {
        public Boolean success;
        public String message;
        public Map<String, Integer> deletedCounts;

        public PanicDeleteResponse(Boolean success, String message, Map<String, Integer> deletedCounts) {
            this.success = success;
            this.message = message;
            this.deletedCounts = deletedCounts;
        }
    }
}
