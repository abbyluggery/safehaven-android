/**
 * Test class for SafeHavenSyncAPI
 * Achieves 75%+ code coverage for Salesforce deployment
 */
@isTest
private class SafeHavenSyncAPITest {

    @testSetup
    static void setupTestData() {
        // Create test profile
        SafeHaven_Profile__c profile = new SafeHaven_Profile__c(
            User_ID__c = 'test_user_123',
            Password_Hash__c = 'test_hash_abc',
            Duress_Password_Hash__c = 'test_duress_hash_xyz',
            GPS_Enabled__c = false,
            Stealth_Mode_Enabled__c = false,
            Auto_Delete_Enabled__c = false,
            Auto_Delete_Days__c = 30,
            Created_Timestamp__c = DateTime.now(),
            Last_Modified_Timestamp__c = DateTime.now()
        );
        insert profile;
    }

    @isTest
    static void testProfileSync_Create() {
        // Prepare request
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/sync/profile';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'userId' => 'new_user_456',
            'passwordHash' => 'new_hash_123',
            'duressPasswordHash' => 'duress_hash_456',
            'gpsEnabled' => true,
            'stealthModeEnabled' => false,
            'autoDeleteEnabled' => true,
            'autoDeleteDays' => 60,
            'deviceId' => 'device_abc'
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));

        RestContext.request = req;

        Test.startTest();
        SafeHavenSyncAPI.SyncResponse response = SafeHavenSyncAPI.syncProfile();
        Test.stopTest();

        System.assertEquals(true, response.success, 'Profile sync should succeed');
        System.assertNotEquals(null, response.recordId, 'Record ID should be returned');

        // Verify profile was created
        SafeHaven_Profile__c profile = [
            SELECT User_ID__c, GPS_Enabled__c, Auto_Delete_Days__c
            FROM SafeHaven_Profile__c
            WHERE User_ID__c = 'new_user_456'
        ];
        System.assertEquals(true, profile.GPS_Enabled__c);
        System.assertEquals(60, profile.Auto_Delete_Days__c);
    }

    @isTest
    static void testProfileSync_Update() {
        // Update existing profile
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/sync/profile';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'userId' => 'test_user_123',
            'passwordHash' => 'updated_hash',
            'duressPasswordHash' => 'updated_duress',
            'gpsEnabled' => true,
            'stealthModeEnabled' => true,
            'autoDeleteEnabled' => true,
            'autoDeleteDays' => 90,
            'deviceId' => 'device_updated'
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));
        RestContext.request = req;

        Test.startTest();
        SafeHavenSyncAPI.SyncResponse response = SafeHavenSyncAPI.syncProfile();
        Test.stopTest();

        System.assertEquals(true, response.success);

        // Verify profile was updated
        SafeHaven_Profile__c profile = [
            SELECT GPS_Enabled__c, Stealth_Mode_Enabled__c
            FROM SafeHaven_Profile__c
            WHERE User_ID__c = 'test_user_123'
        ];
        System.assertEquals(true, profile.GPS_Enabled__c);
        System.assertEquals(true, profile.Stealth_Mode_Enabled__c);
    }

    @isTest
    static void testIncidentSync() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/sync/incident';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'userId' => 'test_user_123',
            'incidentType' => 'physical',
            'incidentTimestamp' => DateTime.now().getTime(),
            'descriptionEncrypted' => 'encrypted_description_123',
            'witnessesEncrypted' => 'encrypted_witnesses_456',
            'injuriesEncrypted' => 'encrypted_injuries_789',
            'policeInvolved' => true,
            'policeReportNumber' => 'PR-2024-001',
            'medicalAttention' => false,
            'location' => 'San Francisco, CA',
            'latitude' => 37.7749,
            'longitude' => -122.4194,
            'perpetratorName' => 'John Doe',
            'perpetratorRelationship' => 'spouse'
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));
        RestContext.request = req;

        Test.startTest();
        SafeHavenSyncAPI.SyncResponse response = SafeHavenSyncAPI.syncProfile();
        Test.stopTest();

        System.assertEquals(true, response.success);

        // Verify incident was created
        List<Incident_Report__c> incidents = [
            SELECT User_ID__c, Incident_Type__c, Police_Involved__c
            FROM Incident_Report__c
            WHERE User_ID__c = 'test_user_123'
        ];
        System.assertEquals(1, incidents.size());
        System.assertEquals('physical', incidents[0].Incident_Type__c);
        System.assertEquals(true, incidents[0].Police_Involved__c);
    }

    @isTest
    static void testEvidenceSync() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/sync/evidence';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'userId' => 'test_user_123',
            'evidenceType' => 'photo',
            'filePathEncrypted' => 'encrypted_path_abc',
            'originalFilenameEncrypted' => 'encrypted_filename_xyz',
            'fileSizeBytes' => 2048000,
            'durationSeconds' => null,
            'mimeType' => 'image/jpeg',
            'sha256Hash' => 'abc123def456ghi789jkl012mno345pqr678stu901vwx234yz567890',
            'capturedTimestamp' => DateTime.now().getTime(),
            'latitude' => 37.7749,
            'longitude' => -122.4194,
            'notesEncrypted' => 'encrypted_notes',
            'deviceId' => 'device_abc'
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));
        RestContext.request = req;

        Test.startTest();
        SafeHavenSyncAPI.SyncResponse response = SafeHavenSyncAPI.syncProfile();
        Test.stopTest();

        System.assertEquals(true, response.success);

        // Verify evidence was created
        List<Evidence_Item__c> evidence = [
            SELECT User_ID__c, Evidence_Type__c, SHA256_Hash__c
            FROM Evidence_Item__c
            WHERE User_ID__c = 'test_user_123'
        ];
        System.assertEquals(1, evidence.size());
        System.assertEquals('photo', evidence[0].Evidence_Type__c);
    }

    @isTest
    static void testSurvivorProfileSync() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/sync/survivorprofile';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'userId' => 'test_user_123',
            'isLGBTQIA' => true,
            'isTrans' => false,
            'isNonBinary' => false,
            'isBIPOC' => true,
            'isMaleIdentifying' => false,
            'isUndocumented' => false,
            'isDisabled' => false,
            'isDeaf' => false,
            'hasChildren' => true,
            'hasPets' => false,
            'needsLegalAid' => true,
            'needsHousing' => true
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));
        RestContext.request = req;

        Test.startTest();
        SafeHavenSyncAPI.SyncResponse response = SafeHavenSyncAPI.syncProfile();
        Test.stopTest();

        System.assertEquals(true, response.success);

        // Verify survivor profile was created
        List<Survivor_Profile__c> profiles = [
            SELECT User_ID__c, Is_LGBTQIA__c, Is_BIPOC__c, Has_Children__c
            FROM Survivor_Profile__c
            WHERE User_ID__c = 'test_user_123'
        ];
        System.assertEquals(1, profiles.size());
        System.assertEquals(true, profiles[0].Is_LGBTQIA__c);
        System.assertEquals(true, profiles[0].Is_BIPOC__c);
    }

    @isTest
    static void testGetAllData() {
        // Create test data
        Incident_Report__c incident = new Incident_Report__c(
            User_ID__c = 'test_user_123',
            Incident_Type__c = 'verbal',
            Incident_Timestamp__c = DateTime.now(),
            Description_Encrypted__c = 'encrypted',
            Created_Timestamp__c = DateTime.now(),
            Last_Modified_Timestamp__c = DateTime.now()
        );
        insert incident;

        Evidence_Item__c evidence = new Evidence_Item__c(
            User_ID__c = 'test_user_123',
            Evidence_Type__c = 'audio',
            File_Path_Encrypted__c = 'encrypted_path',
            SHA256_Hash__c = 'unique_hash_123456789012345678901234567890123456789012345678',
            Captured_Timestamp__c = DateTime.now(),
            Created_Timestamp__c = DateTime.now()
        );
        insert evidence;

        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/sync/all/test_user_123';
        req.httpMethod = 'GET';
        RestContext.request = req;

        Test.startTest();
        SafeHavenSyncAPI.AllDataResponse response = SafeHavenSyncAPI.getAllData();
        Test.stopTest();

        System.assertEquals(true, response.success);
        System.assertNotEquals(null, response.profile);
        System.assertEquals(1, response.incidents.size());
        System.assertEquals(1, response.evidence.size());
    }

    @isTest
    static void testGetAllData_UserNotFound() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/sync/all/nonexistent_user';
        req.httpMethod = 'GET';
        RestContext.request = req;

        Test.startTest();
        SafeHavenSyncAPI.AllDataResponse response = SafeHavenSyncAPI.getAllData();
        Test.stopTest();

        System.assertEquals(false, response.success);
        System.assert(response.message.contains('User not found'));
    }

    @isTest
    static void testUnknownEndpoint() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/sync/unknown';
        req.httpMethod = 'POST';
        req.requestBody = Blob.valueOf('{}');
        RestContext.request = req;

        Test.startTest();
        SafeHavenSyncAPI.SyncResponse response = SafeHavenSyncAPI.syncProfile();
        Test.stopTest();

        System.assertEquals(false, response.success);
        System.assert(response.message.contains('Unknown endpoint'));
    }
}
