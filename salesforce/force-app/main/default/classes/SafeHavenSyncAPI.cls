/**
 * SafeHaven Sync API
 * REST API for syncing user data between Android app and Salesforce backend
 *
 * Security: All sensitive data arrives ALREADY ENCRYPTED (AES-256-GCM)
 * Salesforce NEVER sees plaintext descriptions, witnesses, injuries, or file paths
 *
 * Endpoints:
 * POST /services/apexrest/safehaven/v1/sync/profile - Create/update user profile
 * POST /services/apexrest/safehaven/v1/sync/incident - Create incident report
 * POST /services/apexrest/safehaven/v1/sync/evidence - Upload evidence metadata
 * POST /services/apexrest/safehaven/v1/sync/survivorprofile - Create/update survivor profile
 * GET /services/apexrest/safehaven/v1/sync/all/{userId} - Get all user data
 */
@RestResource(urlMapping='/safehaven/v1/sync/*')
global with sharing class SafeHavenSyncAPI {

    /**
     * Sync user profile (passwords, settings)
     * POST /services/apexrest/safehaven/v1/sync/profile
     */
    @HttpPost
    global static SyncResponse syncProfile() {
        RestRequest req = RestContext.request;
        String endpoint = req.requestURI.substringAfter('/sync/');

        if (endpoint == 'profile') {
            return handleProfileSync(req);
        } else if (endpoint == 'incident') {
            return handleIncidentSync(req);
        } else if (endpoint == 'evidence') {
            return handleEvidenceSync(req);
        } else if (endpoint == 'survivorprofile') {
            return handleSurvivorProfileSync(req);
        } else {
            return new SyncResponse(false, 'Unknown endpoint: ' + endpoint, null);
        }
    }

    /**
     * Get all data for a user
     * GET /services/apexrest/safehaven/v1/sync/all/{userId}
     */
    @HttpGet
    global static AllDataResponse getAllData() {
        RestRequest req = RestContext.request;
        String userId = req.requestURI.substringAfterLast('/');

        try {
            // Query all user data
            List<SafeHaven_Profile__c> profiles = [
                SELECT Id, User_ID__c, Password_Hash__c, Duress_Password_Hash__c,
                       GPS_Enabled__c, Stealth_Mode_Enabled__c, Auto_Delete_Enabled__c,
                       Auto_Delete_Days__c, Last_Sync_Timestamp__c, Device_ID__c
                FROM SafeHaven_Profile__c
                WHERE User_ID__c = :userId
                LIMIT 1
            ];

            if (profiles.isEmpty()) {
                return new AllDataResponse(false, 'User not found', null, null, null, null);
            }

            SafeHaven_Profile__c profile = profiles[0];

            // Get incidents
            List<Incident_Report__c> incidents = [
                SELECT Id, User_ID__c, Incident_Type__c, Incident_Timestamp__c,
                       Description_Encrypted__c, Witnesses_Encrypted__c, Injuries_Encrypted__c,
                       Police_Involved__c, Police_Report_Number__c, Medical_Attention__c,
                       Location_Text__c, Latitude__c, Longitude__c,
                       Perpetrator_Name__c, Perpetrator_Relationship__c,
                       Created_Timestamp__c
                FROM Incident_Report__c
                WHERE User_ID__c = :userId
                ORDER BY Incident_Timestamp__c DESC
            ];

            // Get evidence
            List<Evidence_Item__c> evidence = [
                SELECT Id, User_ID__c, Evidence_Type__c, File_Path_Encrypted__c,
                       Original_Filename_Encrypted__c, File_Size_Bytes__c, Duration_Seconds__c,
                       SHA256_Hash__c, Captured_Timestamp__c, Notes_Encrypted__c,
                       Latitude__c, Longitude__c
                FROM Evidence_Item__c
                WHERE User_ID__c = :userId
                ORDER BY Captured_Timestamp__c DESC
            ];

            // Get verified documents
            List<Verified_Document__c> documents = [
                SELECT Id, User_ID__c, Document_Type__c, Document_Name__c,
                       SHA256_Hash__c, Verification_Timestamp__c, Image_Path_Encrypted__c,
                       Verification_Method__c, Notes_Encrypted__c
                FROM Verified_Document__c
                WHERE User_ID__c = :userId
                ORDER BY Verification_Timestamp__c DESC
            ];

            return new AllDataResponse(true, 'Success', profile, incidents, evidence, documents);

        } catch (Exception e) {
            return new AllDataResponse(false, 'Error: ' + e.getMessage(), null, null, null, null);
        }
    }

    // ========== HELPER METHODS ==========

    private static SyncResponse handleProfileSync(RestRequest req) {
        try {
            Map<String, Object> body = (Map<String, Object>) JSON.deserializeUntyped(req.requestBody.toString());

            String userId = (String) body.get('userId');
            String passwordHash = (String) body.get('passwordHash');
            String duressPasswordHash = (String) body.get('duressPasswordHash');

            // Upsert profile (create or update based on User_ID__c)
            SafeHaven_Profile__c profile = new SafeHaven_Profile__c(
                User_ID__c = userId,
                Password_Hash__c = passwordHash,
                Duress_Password_Hash__c = duressPasswordHash,
                GPS_Enabled__c = (Boolean) body.get('gpsEnabled'),
                Stealth_Mode_Enabled__c = (Boolean) body.get('stealthModeEnabled'),
                Auto_Delete_Enabled__c = (Boolean) body.get('autoDeleteEnabled'),
                Auto_Delete_Days__c = body.get('autoDeleteDays') != null ?
                    Decimal.valueOf(String.valueOf(body.get('autoDeleteDays'))) : 30,
                Created_Timestamp__c = DateTime.now(),
                Last_Modified_Timestamp__c = DateTime.now(),
                Last_Sync_Timestamp__c = DateTime.now(),
                Device_ID__c = (String) body.get('deviceId')
            );

            // Upsert using external ID
            Database.UpsertResult result = Database.upsert(profile, SafeHaven_Profile__c.User_ID__c, false);

            if (result.isSuccess()) {
                return new SyncResponse(true, 'Profile synced successfully', result.getId());
            } else {
                String errorMsg = result.getErrors()[0].getMessage();
                return new SyncResponse(false, 'Error syncing profile: ' + errorMsg, null);
            }

        } catch (Exception e) {
            return new SyncResponse(false, 'Exception: ' + e.getMessage(), null);
        }
    }

    private static SyncResponse handleIncidentSync(RestRequest req) {
        try {
            Map<String, Object> body = (Map<String, Object>) JSON.deserializeUntyped(req.requestBody.toString());

            String userId = (String) body.get('userId');

            // CRITICAL: These fields arrive ALREADY ENCRYPTED from Android app
            // Salesforce NEVER sees plaintext sensitive data
            Incident_Report__c incident = new Incident_Report__c(
                User_ID__c = userId,
                Incident_Type__c = (String) body.get('incidentType'),
                Incident_Timestamp__c = body.get('incidentTimestamp') != null ?
                    DateTime.newInstance((Long) body.get('incidentTimestamp')) : DateTime.now(),
                Description_Encrypted__c = (String) body.get('descriptionEncrypted'),
                Witnesses_Encrypted__c = (String) body.get('witnessesEncrypted'),
                Injuries_Encrypted__c = (String) body.get('injuriesEncrypted'),
                Police_Involved__c = (Boolean) body.get('policeInvolved'),
                Police_Report_Number__c = (String) body.get('policeReportNumber'),
                Medical_Attention__c = (Boolean) body.get('medicalAttention'),
                Location_Text__c = (String) body.get('location'),
                Latitude__c = body.get('latitude') != null ?
                    Decimal.valueOf(String.valueOf(body.get('latitude'))) : null,
                Longitude__c = body.get('longitude') != null ?
                    Decimal.valueOf(String.valueOf(body.get('longitude'))) : null,
                Perpetrator_Name__c = (String) body.get('perpetratorName'),
                Perpetrator_Relationship__c = (String) body.get('perpetratorRelationship'),
                Created_Timestamp__c = DateTime.now(),
                Last_Modified_Timestamp__c = DateTime.now(),
                Synced_From_Device__c = true
            );

            insert incident;
            return new SyncResponse(true, 'Incident report synced successfully', incident.Id);

        } catch (Exception e) {
            return new SyncResponse(false, 'Exception: ' + e.getMessage(), null);
        }
    }

    private static SyncResponse handleEvidenceSync(RestRequest req) {
        try {
            Map<String, Object> body = (Map<String, Object>) JSON.deserializeUntyped(req.requestBody.toString());

            String userId = (String) body.get('userId');
            String sha256Hash = (String) body.get('sha256Hash');

            // Check if evidence already exists (deduplication via SHA-256)
            List<Evidence_Item__c> existing = [
                SELECT Id FROM Evidence_Item__c WHERE SHA256_Hash__c = :sha256Hash LIMIT 1
            ];

            if (!existing.isEmpty()) {
                return new SyncResponse(true, 'Evidence already synced', existing[0].Id);
            }

            // CRITICAL: File path arrives ALREADY ENCRYPTED
            Evidence_Item__c evidence = new Evidence_Item__c(
                User_ID__c = userId,
                Evidence_Type__c = (String) body.get('evidenceType'),
                File_Path_Encrypted__c = (String) body.get('filePathEncrypted'),
                Original_Filename_Encrypted__c = (String) body.get('originalFilenameEncrypted'),
                File_Size_Bytes__c = body.get('fileSizeBytes') != null ?
                    Decimal.valueOf(String.valueOf(body.get('fileSizeBytes'))) : null,
                Duration_Seconds__c = body.get('durationSeconds') != null ?
                    Decimal.valueOf(String.valueOf(body.get('durationSeconds'))) : null,
                MIME_Type__c = (String) body.get('mimeType'),
                SHA256_Hash__c = sha256Hash,
                Captured_Timestamp__c = body.get('capturedTimestamp') != null ?
                    DateTime.newInstance((Long) body.get('capturedTimestamp')) : DateTime.now(),
                Created_Timestamp__c = DateTime.now(),
                Latitude__c = body.get('latitude') != null ?
                    Decimal.valueOf(String.valueOf(body.get('latitude'))) : null,
                Longitude__c = body.get('longitude') != null ?
                    Decimal.valueOf(String.valueOf(body.get('longitude'))) : null,
                Notes_Encrypted__c = (String) body.get('notesEncrypted'),
                Synced_From_Device__c = true,
                Device_ID__c = (String) body.get('deviceId')
            );

            insert evidence;
            return new SyncResponse(true, 'Evidence synced successfully', evidence.Id);

        } catch (Exception e) {
            return new SyncResponse(false, 'Exception: ' + e.getMessage(), null);
        }
    }

    private static SyncResponse handleSurvivorProfileSync(RestRequest req) {
        try {
            Map<String, Object> body = (Map<String, Object>) JSON.deserializeUntyped(req.requestBody.toString());

            String userId = (String) body.get('userId');

            // Create/update survivor profile with intersectional identity data
            Survivor_Profile__c survivorProfile = new Survivor_Profile__c(
                User_ID__c = userId,
                Is_LGBTQIA__c = (Boolean) body.get('isLGBTQIA'),
                Is_Transgender__c = (Boolean) body.get('isTrans'),
                Is_Nonbinary__c = (Boolean) body.get('isNonBinary'),
                Is_BIPOC__c = (Boolean) body.get('isBIPOC'),
                Is_Male_Identifying__c = (Boolean) body.get('isMaleIdentifying'),
                Is_Undocumented__c = (Boolean) body.get('isUndocumented'),
                Is_Disabled__c = (Boolean) body.get('isDisabled'),
                Is_Deaf_HOH__c = (Boolean) body.get('isDeaf'),
                Has_Children__c = (Boolean) body.get('hasChildren'),
                Has_Pets__c = (Boolean) body.get('hasPets'),
                Needs_Legal_Aid__c = (Boolean) body.get('needsLegalAid'),
                Needs_Housing__c = (Boolean) body.get('needsHousing'),
                Created_Timestamp__c = DateTime.now(),
                Last_Modified_Timestamp__c = DateTime.now(),
                Last_Sync_Timestamp__c = DateTime.now()
            );

            // Upsert using external ID
            Database.UpsertResult result = Database.upsert(survivorProfile, Survivor_Profile__c.User_ID__c, false);

            if (result.isSuccess()) {
                return new SyncResponse(true, 'Survivor profile synced successfully', result.getId());
            } else {
                String errorMsg = result.getErrors()[0].getMessage();
                return new SyncResponse(false, 'Error syncing survivor profile: ' + errorMsg, null);
            }

        } catch (Exception e) {
            return new SyncResponse(false, 'Exception: ' + e.getMessage(), null);
        }
    }

    // ========== RESPONSE CLASSES ==========

    global class SyncResponse {
        public Boolean success;
        public String message;
        public String recordId;

        public SyncResponse(Boolean success, String message, String recordId) {
            this.success = success;
            this.message = message;
            this.recordId = recordId;
        }
    }

    global class AllDataResponse {
        public Boolean success;
        public String message;
        public SafeHaven_Profile__c profile;
        public List<Incident_Report__c> incidents;
        public List<Evidence_Item__c> evidence;
        public List<Verified_Document__c> documents;

        public AllDataResponse(
            Boolean success,
            String message,
            SafeHaven_Profile__c profile,
            List<Incident_Report__c> incidents,
            List<Evidence_Item__c> evidence,
            List<Verified_Document__c> documents
        ) {
            this.success = success;
            this.message = message;
            this.profile = profile;
            this.incidents = incidents;
            this.evidence = evidence;
            this.documents = documents;
        }
    }
}
