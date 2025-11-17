/**
 * Test class for PanicDeleteAPI
 * Tests emergency data deletion functionality
 */
@isTest
private class PanicDeleteAPITest {

    @testSetup
    static void setupTestData() {
        // Create comprehensive test data for a user
        SafeHaven_Profile__c profile = new SafeHaven_Profile__c(
            User_ID__c = 'panic_test_user',
            Password_Hash__c = 'test_hash',
            Duress_Password_Hash__c = 'duress_hash',
            GPS_Enabled__c = true,
            Created_Timestamp__c = DateTime.now(),
            Last_Modified_Timestamp__c = DateTime.now()
        );
        insert profile;

        // Create survivor profile
        Survivor_Profile__c survivorProfile = new Survivor_Profile__c(
            User_ID__c = 'panic_test_user',
            Is_LGBTQIA__c = true,
            Is_Trans__c = true,
            Created_Timestamp__c = DateTime.now(),
            Last_Modified_Timestamp__c = DateTime.now()
        );
        insert survivorProfile;

        // Create incident reports
        List<Incident_Report__c> incidents = new List<Incident_Report__c>();
        for (Integer i = 0; i < 3; i++) {
            incidents.add(new Incident_Report__c(
                User_ID__c = 'panic_test_user',
                Incident_Type__c = 'physical',
                Incident_Timestamp__c = DateTime.now(),
                Description_Encrypted__c = 'encrypted_description_' + i,
                Created_Timestamp__c = DateTime.now(),
                Last_Modified_Timestamp__c = DateTime.now()
            ));
        }
        insert incidents;

        // Create evidence items
        List<Evidence_Item__c> evidence = new List<Evidence_Item__c>();
        for (Integer i = 0; i < 5; i++) {
            evidence.add(new Evidence_Item__c(
                User_ID__c = 'panic_test_user',
                Evidence_Type__c = 'photo',
                File_Path_Encrypted__c = 'encrypted_path_' + i,
                SHA256_Hash__c = 'hash_' + i + '_123456789012345678901234567890123456789012',
                Captured_Timestamp__c = DateTime.now(),
                Created_Timestamp__c = DateTime.now()
            ));
        }
        insert evidence;

        // Create verified documents
        List<Verified_Document__c> documents = new List<Verified_Document__c>();
        for (Integer i = 0; i < 2; i++) {
            documents.add(new Verified_Document__c(
                User_ID__c = 'panic_test_user',
                Document_Type__c = 'passport',
                Document_Name__c = 'Document ' + i,
                SHA256_Hash__c = 'doc_hash_' + i + '_123456789012345678901234567890123456',
                Verification_Timestamp__c = DateTime.now(),
                Image_Path_Encrypted__c = 'encrypted_doc_path_' + i,
                Created_Timestamp__c = DateTime.now()
            ));
        }
        insert documents;
    }

    @isTest
    static void testDeletePreview() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/panic/preview';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'userId' => 'panic_test_user'
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));
        RestContext.request = req;

        Test.startTest();
        PanicDeleteAPI.PanicDeleteResponse response = PanicDeleteAPI.executePanicDelete();
        Test.stopTest();

        System.assertEquals(true, response.success);
        System.assert(response.message.contains('Preview'));
        System.assertEquals(3, response.deletedCounts.get('incidents'));
        System.assertEquals(5, response.deletedCounts.get('evidence'));
        System.assertEquals(2, response.deletedCounts.get('documents'));
        System.assertEquals(1, response.deletedCounts.get('survivorProfiles'));
        System.assertEquals(1, response.deletedCounts.get('profiles'));

        // Verify no data was actually deleted
        System.assertEquals(3, [SELECT COUNT() FROM Incident_Report__c WHERE User_ID__c = 'panic_test_user']);
        System.assertEquals(5, [SELECT COUNT() FROM Evidence_Item__c WHERE User_ID__c = 'panic_test_user']);
    }

    @isTest
    static void testPanicDelete_Success() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/panic/delete';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'userId' => 'panic_test_user',
            'confirmation' => 'DELETE_ALL_DATA'
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));
        RestContext.request = req;

        Test.startTest();
        PanicDeleteAPI.PanicDeleteResponse response = PanicDeleteAPI.executePanicDelete();
        Test.stopTest();

        System.assertEquals(true, response.success);
        System.assert(response.message.contains('deleted successfully'));
        System.assertEquals(3, response.deletedCounts.get('incidents'));
        System.assertEquals(5, response.deletedCounts.get('evidence'));
        System.assertEquals(2, response.deletedCounts.get('documents'));
        System.assertEquals(1, response.deletedCounts.get('survivorProfiles'));
        System.assertEquals(1, response.deletedCounts.get('profiles'));

        // Verify all data was actually deleted
        System.assertEquals(0, [SELECT COUNT() FROM Incident_Report__c WHERE User_ID__c = 'panic_test_user']);
        System.assertEquals(0, [SELECT COUNT() FROM Evidence_Item__c WHERE User_ID__c = 'panic_test_user']);
        System.assertEquals(0, [SELECT COUNT() FROM Verified_Document__c WHERE User_ID__c = 'panic_test_user']);
        System.assertEquals(0, [SELECT COUNT() FROM Survivor_Profile__c WHERE User_ID__c = 'panic_test_user']);
        System.assertEquals(0, [SELECT COUNT() FROM SafeHaven_Profile__c WHERE User_ID__c = 'panic_test_user']);
    }

    @isTest
    static void testPanicDelete_MissingUserId() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/panic/delete';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'confirmation' => 'DELETE_ALL_DATA'
            // Missing userId
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));
        RestContext.request = req;

        Test.startTest();
        PanicDeleteAPI.PanicDeleteResponse response = PanicDeleteAPI.executePanicDelete();
        Test.stopTest();

        System.assertEquals(false, response.success);
        System.assert(response.message.contains('Missing required field'));
    }

    @isTest
    static void testPanicDelete_InvalidConfirmation() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/panic/delete';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'userId' => 'panic_test_user',
            'confirmation' => 'WRONG_CONFIRMATION'
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));
        RestContext.request = req;

        Test.startTest();
        PanicDeleteAPI.PanicDeleteResponse response = PanicDeleteAPI.executePanicDelete();
        Test.stopTest();

        System.assertEquals(false, response.success);
        System.assert(response.message.contains('Invalid confirmation'));

        // Verify no data was deleted
        System.assertEquals(3, [SELECT COUNT() FROM Incident_Report__c WHERE User_ID__c = 'panic_test_user']);
        System.assertEquals(5, [SELECT COUNT() FROM Evidence_Item__c WHERE User_ID__c = 'panic_test_user']);
    }

    @isTest
    static void testPanicDelete_EmptyUser() {
        // Test deleting data for user with no records
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/panic/delete';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'userId' => 'empty_user_no_data',
            'confirmation' => 'DELETE_ALL_DATA'
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));
        RestContext.request = req;

        Test.startTest();
        PanicDeleteAPI.PanicDeleteResponse response = PanicDeleteAPI.executePanicDelete();
        Test.stopTest();

        System.assertEquals(true, response.success);
        System.assertEquals(0, response.deletedCounts.get('incidents'));
        System.assertEquals(0, response.deletedCounts.get('evidence'));
        System.assertEquals(0, response.deletedCounts.get('documents'));
        System.assertEquals(0, response.deletedCounts.get('survivorProfiles'));
        System.assertEquals(0, response.deletedCounts.get('profiles'));
    }

    @isTest
    static void testPanicDelete_Performance() {
        // Test that panic delete completes quickly even with lots of data
        // Create additional test data
        List<Incident_Report__c> moreIncidents = new List<Incident_Report__c>();
        for (Integer i = 0; i < 20; i++) {
            moreIncidents.add(new Incident_Report__c(
                User_ID__c = 'panic_test_user',
                Incident_Type__c = 'verbal',
                Incident_Timestamp__c = DateTime.now(),
                Description_Encrypted__c = 'encrypted_' + i,
                Created_Timestamp__c = DateTime.now(),
                Last_Modified_Timestamp__c = DateTime.now()
            ));
        }
        insert moreIncidents;

        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/panic/delete';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'userId' => 'panic_test_user',
            'confirmation' => 'DELETE_ALL_DATA'
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));
        RestContext.request = req;

        Long startTime = System.currentTimeMillis();

        Test.startTest();
        PanicDeleteAPI.PanicDeleteResponse response = PanicDeleteAPI.executePanicDelete();
        Test.stopTest();

        Long endTime = System.currentTimeMillis();
        Long duration = endTime - startTime;

        System.assertEquals(true, response.success);
        System.assertEquals(23, response.deletedCounts.get('incidents')); // 3 + 20

        // Verify all data deleted
        System.assertEquals(0, [SELECT COUNT() FROM Incident_Report__c WHERE User_ID__c = 'panic_test_user']);
    }

    @isTest
    static void testUnknownEndpoint() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/panic/unknown';
        req.httpMethod = 'POST';
        req.requestBody = Blob.valueOf('{"userId":"panic_test_user"}');
        RestContext.request = req;

        Test.startTest();
        PanicDeleteAPI.PanicDeleteResponse response = PanicDeleteAPI.executePanicDelete();
        Test.stopTest();

        System.assertEquals(false, response.success);
        System.assert(response.message.contains('Unknown endpoint'));
    }
}
