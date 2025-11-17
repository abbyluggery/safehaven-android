/**
 * Document Verification API
 * Cryptographic verification for legal documents (IDs, passports, birth certificates)
 *
 * Use Case: Survivor can't access original documents (abuser controls them)
 * Solution: Take photo → Generate SHA-256 hash → Create verifiable PDF with embedded hash
 *
 * Endpoints:
 * POST /services/apexrest/safehaven/v1/documents/verify - Verify and store document
 * GET /services/apexrest/safehaven/v1/documents/{userId} - Get all verified documents
 * DELETE /services/apexrest/safehaven/v1/documents/{documentId} - Delete document
 * POST /services/apexrest/safehaven/v1/documents/blockchain - Record hash on blockchain (future)
 */
@RestResource(urlMapping='/safehaven/v1/documents/*')
global with sharing class DocumentVerificationAPI {

    /**
     * Verify and store document with cryptographic hash
     * POST /services/apexrest/safehaven/v1/documents/verify
     *
     * Request body:
     * {
     *   "userId": "user_123",
     *   "documentType": "passport",
     *   "documentName": "My Passport",
     *   "sha256Hash": "abc123...",  // Calculated on device
     *   "imagePathEncrypted": "encrypted_path",
     *   "fileSizeBytes": 2048000,
     *   "imageWidth": 1920,
     *   "imageHeight": 1080,
     *   "verificationMethod": "sha256_only",
     *   "notesEncrypted": "encrypted_notes"
     * }
     */
    @HttpPost
    global static VerificationResponse verifyDocument() {
        RestRequest req = RestContext.request;

        try {
            Map<String, Object> body = (Map<String, Object>) JSON.deserializeUntyped(req.requestBody.toString());

            String userId = (String) body.get('userId');
            String sha256Hash = (String) body.get('sha256Hash');
            String documentType = (String) body.get('documentType');

            // Validate required fields
            if (String.isBlank(userId) || String.isBlank(sha256Hash) || String.isBlank(documentType)) {
                return new VerificationResponse(
                    false,
                    'Missing required fields: userId, sha256Hash, documentType',
                    null,
                    null
                );
            }

            // Check if document with this hash already exists (deduplication)
            List<Verified_Document__c> existing = [
                SELECT Id, SHA256_Hash__c, Verification_Timestamp__c
                FROM Verified_Document__c
                WHERE SHA256_Hash__c = :sha256Hash
                LIMIT 1
            ];

            if (!existing.isEmpty()) {
                return new VerificationResponse(
                    true,
                    'Document already verified',
                    existing[0].Id,
                    existing[0].SHA256_Hash__c
                );
            }

            // Create new verified document record
            Verified_Document__c document = new Verified_Document__c(
                User_ID__c = userId,
                Document_Type__c = documentType,
                Document_Name__c = (String) body.get('documentName'),
                SHA256_Hash__c = sha256Hash,
                Verification_Timestamp__c = DateTime.now(),
                Image_Path_Encrypted__c = (String) body.get('imagePathEncrypted'),
                PDF_Path_Encrypted__c = (String) body.get('pdfPathEncrypted'),
                File_Size_Bytes__c = body.get('fileSizeBytes') != null ?
                    Decimal.valueOf(String.valueOf(body.get('fileSizeBytes'))) : null,
                Image_Width__c = body.get('imageWidth') != null ?
                    Decimal.valueOf(String.valueOf(body.get('imageWidth'))) : null,
                Image_Height__c = body.get('imageHeight') != null ?
                    Decimal.valueOf(String.valueOf(body.get('imageHeight'))) : null,
                Verification_Method__c = body.get('verificationMethod') != null ?
                    (String) body.get('verificationMethod') : 'sha256_only',
                Notes_Encrypted__c = (String) body.get('notesEncrypted'),
                Created_Timestamp__c = DateTime.now(),
                Synced_From_Device__c = true,
                Device_ID__c = (String) body.get('deviceId')
            );

            insert document;

            return new VerificationResponse(
                true,
                'Document verified successfully',
                document.Id,
                document.SHA256_Hash__c
            );

        } catch (Exception e) {
            return new VerificationResponse(
                false,
                'Error verifying document: ' + e.getMessage(),
                null,
                null
            );
        }
    }

    /**
     * Get all verified documents for a user
     * GET /services/apexrest/safehaven/v1/documents/{userId}
     */
    @HttpGet
    global static DocumentListResponse getDocuments() {
        RestRequest req = RestContext.request;
        String userId = req.requestURI.substringAfterLast('/');

        try {
            // Query all documents for user
            List<Verified_Document__c> documents = [
                SELECT Id, User_ID__c, Document_Type__c, Document_Name__c,
                       SHA256_Hash__c, Verification_Timestamp__c, Image_Path_Encrypted__c,
                       PDF_Path_Encrypted__c, File_Size_Bytes__c, Verification_Method__c,
                       Notes_Encrypted__c, Created_Timestamp__c
                FROM Verified_Document__c
                WHERE User_ID__c = :userId
                ORDER BY Verification_Timestamp__c DESC
            ];

            return new DocumentListResponse(
                true,
                'Success',
                documents
            );

        } catch (Exception e) {
            return new DocumentListResponse(
                false,
                'Error retrieving documents: ' + e.getMessage(),
                null
            );
        }
    }

    /**
     * Delete a verified document
     * DELETE /services/apexrest/safehaven/v1/documents/{documentId}
     */
    @HttpDelete
    global static DeleteResponse deleteDocument() {
        RestRequest req = RestContext.request;
        String documentId = req.requestURI.substringAfterLast('/');

        try {
            // Find and delete document
            List<Verified_Document__c> documents = [
                SELECT Id FROM Verified_Document__c WHERE Id = :documentId LIMIT 1
            ];

            if (documents.isEmpty()) {
                return new DeleteResponse(false, 'Document not found');
            }

            delete documents[0];

            return new DeleteResponse(true, 'Document deleted successfully');

        } catch (Exception e) {
            return new DeleteResponse(false, 'Error deleting document: ' + e.getMessage());
        }
    }

    /**
     * Record document hash on blockchain (future enhancement)
     * POST /services/apexrest/safehaven/v1/documents/blockchain
     *
     * This would integrate with a blockchain service (e.g., Ethereum, Bitcoin)
     * to create an immutable timestamp proof of the document's existence
     */
    // Future implementation

    // ========== RESPONSE CLASSES ==========

    global class VerificationResponse {
        public Boolean success;
        public String message;
        public String documentId;
        public String sha256Hash;

        public VerificationResponse(Boolean success, String message, String documentId, String sha256Hash) {
            this.success = success;
            this.message = message;
            this.documentId = documentId;
            this.sha256Hash = sha256Hash;
        }
    }

    global class DocumentListResponse {
        public Boolean success;
        public String message;
        public List<Verified_Document__c> documents;

        public DocumentListResponse(Boolean success, String message, List<Verified_Document__c> documents) {
            this.success = success;
            this.message = message;
            this.documents = documents;
        }
    }

    global class DeleteResponse {
        public Boolean success;
        public String message;

        public DeleteResponse(Boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
