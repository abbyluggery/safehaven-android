/**
 * Test class for DocumentVerificationAPI
 * Achieves 75%+ code coverage
 */
@isTest
private class DocumentVerificationAPITest {

    @testSetup
    static void setupTestData() {
        SafeHaven_Profile__c profile = new SafeHaven_Profile__c(
            User_ID__c = 'test_user_doc',
            Password_Hash__c = 'test_hash',
            Duress_Password_Hash__c = 'duress_hash',
            Created_Timestamp__c = DateTime.now(),
            Last_Modified_Timestamp__c = DateTime.now()
        );
        insert profile;
    }

    @isTest
    static void testVerifyDocument_Success() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/documents/verify';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'userId' => 'test_user_doc',
            'documentType' => 'passport',
            'documentName' => 'My Passport',
            'sha256Hash' => 'abc123def456ghi789jkl012mno345pqr678stu901vwx234yz567890',
            'imagePathEncrypted' => 'encrypted_image_path',
            'pdfPathEncrypted' => 'encrypted_pdf_path',
            'fileSizeBytes' => 2048000,
            'imageWidth' => 1920,
            'imageHeight' => 1080,
            'verificationMethod' => 'sha256_only',
            'notesEncrypted' => 'encrypted_notes',
            'deviceId' => 'device_123'
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));
        RestContext.request = req;

        Test.startTest();
        DocumentVerificationAPI.VerificationResponse response = DocumentVerificationAPI.verifyDocument();
        Test.stopTest();

        System.assertEquals(true, response.success);
        System.assertNotEquals(null, response.documentId);
        System.assertEquals('abc123def456ghi789jkl012mno345pqr678stu901vwx234yz567890', response.sha256Hash);

        // Verify document was created
        List<Verified_Document__c> docs = [
            SELECT Document_Type__c, SHA256_Hash__c
            FROM Verified_Document__c
            WHERE User_ID__c = 'test_user_doc'
        ];
        System.assertEquals(1, docs.size());
        System.assertEquals('passport', docs[0].Document_Type__c);
    }

    @isTest
    static void testVerifyDocument_Duplicate() {
        // Insert existing document
        Verified_Document__c existingDoc = new Verified_Document__c(
            User_ID__c = 'test_user_doc',
            Document_Type__c = 'drivers_license',
            Document_Name__c = 'License',
            SHA256_Hash__c = 'duplicate_hash_123456789012345678901234567890123456789012',
            Verification_Timestamp__c = DateTime.now(),
            Image_Path_Encrypted__c = 'path',
            Created_Timestamp__c = DateTime.now()
        );
        insert existingDoc;

        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/documents/verify';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'userId' => 'test_user_doc',
            'documentType' => 'passport',
            'documentName' => 'Another Document',
            'sha256Hash' => 'duplicate_hash_123456789012345678901234567890123456789012',
            'imagePathEncrypted' => 'encrypted_path'
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));
        RestContext.request = req;

        Test.startTest();
        DocumentVerificationAPI.VerificationResponse response = DocumentVerificationAPI.verifyDocument();
        Test.stopTest();

        System.assertEquals(true, response.success);
        System.assert(response.message.contains('already verified'));
        System.assertEquals(existingDoc.Id, response.documentId);
    }

    @isTest
    static void testVerifyDocument_MissingFields() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/documents/verify';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'userId' => 'test_user_doc'
            // Missing sha256Hash and documentType
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));
        RestContext.request = req;

        Test.startTest();
        DocumentVerificationAPI.VerificationResponse response = DocumentVerificationAPI.verifyDocument();
        Test.stopTest();

        System.assertEquals(false, response.success);
        System.assert(response.message.contains('Missing required fields'));
    }

    @isTest
    static void testGetDocuments() {
        // Create test documents
        List<Verified_Document__c> docs = new List<Verified_Document__c>{
            new Verified_Document__c(
                User_ID__c = 'test_user_doc',
                Document_Type__c = 'passport',
                Document_Name__c = 'Passport',
                SHA256_Hash__c = 'hash1_123456789012345678901234567890123456789012345678',
                Verification_Timestamp__c = DateTime.now(),
                Image_Path_Encrypted__c = 'path1',
                Created_Timestamp__c = DateTime.now()
            ),
            new Verified_Document__c(
                User_ID__c = 'test_user_doc',
                Document_Type__c = 'birth_certificate',
                Document_Name__c = 'Birth Certificate',
                SHA256_Hash__c = 'hash2_234567890123456789012345678901234567890123456789',
                Verification_Timestamp__c = DateTime.now(),
                Image_Path_Encrypted__c = 'path2',
                Created_Timestamp__c = DateTime.now()
            )
        };
        insert docs;

        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/documents/test_user_doc';
        req.httpMethod = 'GET';
        RestContext.request = req;

        Test.startTest();
        DocumentVerificationAPI.DocumentListResponse response = DocumentVerificationAPI.getDocuments();
        Test.stopTest();

        System.assertEquals(true, response.success);
        System.assertEquals(2, response.documents.size());
    }

    @isTest
    static void testDeleteDocument() {
        // Create test document
        Verified_Document__c doc = new Verified_Document__c(
            User_ID__c = 'test_user_doc',
            Document_Type__c = 'passport',
            Document_Name__c = 'Passport',
            SHA256_Hash__c = 'hash_to_delete_123456789012345678901234567890123456789',
            Verification_Timestamp__c = DateTime.now(),
            Image_Path_Encrypted__c = 'path',
            Created_Timestamp__c = DateTime.now()
        );
        insert doc;

        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/documents/' + doc.Id;
        req.httpMethod = 'DELETE';
        RestContext.request = req;

        Test.startTest();
        DocumentVerificationAPI.DeleteResponse response = DocumentVerificationAPI.deleteDocument();
        Test.stopTest();

        System.assertEquals(true, response.success);
        System.assert(response.message.contains('deleted successfully'));

        // Verify document was deleted
        List<Verified_Document__c> remaining = [
            SELECT Id FROM Verified_Document__c WHERE Id = :doc.Id
        ];
        System.assertEquals(0, remaining.size());
    }

    @isTest
    static void testDeleteDocument_NotFound() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/documents/nonexistent_id';
        req.httpMethod = 'DELETE';
        RestContext.request = req;

        Test.startTest();
        DocumentVerificationAPI.DeleteResponse response = DocumentVerificationAPI.deleteDocument();
        Test.stopTest();

        System.assertEquals(false, response.success);
        System.assert(response.message.contains('not found'));
    }
}
