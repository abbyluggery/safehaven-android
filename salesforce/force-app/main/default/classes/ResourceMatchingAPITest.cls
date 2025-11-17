/**
 * Test class for ResourceMatchingAPI
 * Tests intersectional resource matching algorithm
 */
@isTest
private class ResourceMatchingAPITest {

    @testSetup
    static void setupTestData() {
        // Create survivor profile with intersectional identity
        Survivor_Profile__c profile = new Survivor_Profile__c(
            User_ID__c = 'test_user_resource',
            Is_LGBTQIA__c = true,
            Is_Transgender__c = true,
            Is_Nonbinary__c = false,
            Is_BIPOC__c = true,
            Is_Male_Identifying__c = false,
            Is_Undocumented__c = true,
            Is_Disabled__c = false,
            Is_Deaf_HOH__c = false,
            Wheelchair_User__c = false,
            Has_Children__c = true,
            Has_Pets__c = true,
            Needs_U_Visa_Support__c = true,
            Needs_Reproductive_Healthcare__c = true,
            Created_Timestamp__c = DateTime.now(),
            Last_Modified_Timestamp__c = DateTime.now()
        );
        insert profile;

        // Create test resources with different intersectional features
        List<Legal_Resource__c> resources = new List<Legal_Resource__c>{
            // Perfect match - trans-inclusive, undocumented-friendly, U-Visa support
            new Legal_Resource__c(
                Name = 'Perfect Match Shelter',
                Resource_Type__c = 'shelter',
                Organization_Name__c = 'Trans Justice Shelter',
                Phone_Number__c = '555-1111',
                City__c = 'San Francisco',
                State__c = 'CA',
                Latitude__c = 37.7749,
                Longitude__c = -122.4194,
                Is_24_7__c = true,
                Is_Free__c = true,
                Verified__c = true,
                Serves_LGBTQIA__c = true,
                Serves_Trans__c = true,
                Serves_Undocumented__c = true,
                U_Visa_Support__c = true,
                BIPOC_Led__c = true,
                Pets_Allowed__c = true,
                Children_Allowed__c = true,
                Reproductive_Healthcare__c = true,
                Created_Timestamp__c = DateTime.now(),
                Last_Modified_Timestamp__c = DateTime.now()
            ),
            // Partial match - LGBTQ+ but no trans-specific services
            new Legal_Resource__c(
                Name = 'LGBTQ Shelter',
                Resource_Type__c = 'shelter',
                Organization_Name__c = 'Rainbow House',
                Phone_Number__c = '555-2222',
                City__c = 'Oakland',
                State__c = 'CA',
                Latitude__c = 37.8044,
                Longitude__c = -122.2712,
                Is_24_7__c = false,
                Is_Free__c = true,
                Verified__c = true,
                Serves_LGBTQIA__c = true,
                Serves_Trans__c = false,
                Serves_Undocumented__c = false,
                Created_Timestamp__c = DateTime.now(),
                Last_Modified_Timestamp__c = DateTime.now()
            ),
            // Low match - generic shelter
            new Legal_Resource__c(
                Name = 'Generic Shelter',
                Resource_Type__c = 'shelter',
                Organization_Name__c = 'SafeSpace',
                Phone_Number__c = '555-3333',
                City__c = 'San Jose',
                State__c = 'CA',
                Latitude__c = 37.3382,
                Longitude__c = -121.8863,
                Is_24_7__c = true,
                Is_Free__c = false,
                Verified__c = true,
                Created_Timestamp__c = DateTime.now(),
                Last_Modified_Timestamp__c = DateTime.now()
            ),
            // Different resource type
            new Legal_Resource__c(
                Name = 'Legal Aid',
                Resource_Type__c = 'legal_aid',
                Organization_Name__c = 'Justice For All',
                Phone_Number__c = '555-4444',
                City__c = 'San Francisco',
                State__c = 'CA',
                Latitude__c = 37.7849,
                Longitude__c = -122.4094,
                Is_Free__c = true,
                Verified__c = true,
                U_Visa_Support__c = true,
                Created_Timestamp__c = DateTime.now(),
                Last_Modified_Timestamp__c = DateTime.now()
            )
        };
        insert resources;
    }

    @isTest
    static void testMatchResources_WithProfile() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/resources/match';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'userId' => 'test_user_resource',
            'resourceType' => 'shelter',
            'latitude' => 37.7749,
            'longitude' => -122.4194,
            'maxDistance' => 50
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));
        RestContext.request = req;

        Test.startTest();
        ResourceMatchingAPI.MatchResponse response = ResourceMatchingAPI.matchResources();
        Test.stopTest();

        System.assertEquals(true, response.success);
        System.assertNotEquals(null, response.resources);
        System.assert(response.resources.size() > 0);

        // Verify perfect match has highest score
        ResourceMatchingAPI.ScoredResource topMatch = response.resources[0];
        System.assertEquals('Perfect Match Shelter', topMatch.resource.Name);
        System.assert(topMatch.score > 50, 'Perfect match should have high score');
    }

    @isTest
    static void testMatchResources_NoProfile() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/resources/match';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'userId' => 'user_without_profile',
            'resourceType' => 'shelter',
            'latitude' => 37.7749,
            'longitude' => -122.4194,
            'maxDistance' => 50
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));
        RestContext.request = req;

        Test.startTest();
        ResourceMatchingAPI.MatchResponse response = ResourceMatchingAPI.matchResources();
        Test.stopTest();

        System.assertEquals(true, response.success);
        // Should still return resources, just without identity matching bonuses
        System.assert(response.resources.size() > 0);
    }

    @isTest
    static void testMatchResources_NoLocation() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/resources/match';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'userId' => 'test_user_resource',
            'resourceType' => 'shelter'
            // No latitude/longitude
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));
        RestContext.request = req;

        Test.startTest();
        ResourceMatchingAPI.MatchResponse response = ResourceMatchingAPI.matchResources();
        Test.stopTest();

        System.assertEquals(true, response.success);
        // Should return all resources without distance filtering
        System.assert(response.resources.size() > 0);
    }

    @isTest
    static void testSearchResources_ByType() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/resources/search?type=legal_aid';
        req.httpMethod = 'GET';

        Map<String, String> params = new Map<String, String>{
            'type' => 'legal_aid'
        };
        req.params = params;
        RestContext.request = req;

        Test.startTest();
        ResourceMatchingAPI.SearchResponse response = ResourceMatchingAPI.searchResources();
        Test.stopTest();

        System.assertEquals(true, response.success);
        System.assertEquals(1, response.resources.size());
        System.assertEquals('Legal Aid', response.resources[0].Name);
    }

    @isTest
    static void testSearchResources_ByStateAndCity() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/resources/search?state=CA&city=San Francisco';
        req.httpMethod = 'GET';

        Map<String, String> params = new Map<String, String>{
            'state' => 'CA',
            'city' => 'San Francisco'
        };
        req.params = params;
        RestContext.request = req;

        Test.startTest();
        ResourceMatchingAPI.SearchResponse response = ResourceMatchingAPI.searchResources();
        Test.stopTest();

        System.assertEquals(true, response.success);
        System.assert(response.resources.size() >= 2);

        // Verify all results are in San Francisco, CA
        for (Legal_Resource__c resource : response.resources) {
            System.assertEquals('San Francisco', resource.City__c);
            System.assertEquals('CA', resource.State__c);
        }
    }

    @isTest
    static void testSearchResources_NoFilters() {
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/resources/search';
        req.httpMethod = 'GET';
        req.params = new Map<String, String>();
        RestContext.request = req;

        Test.startTest();
        ResourceMatchingAPI.SearchResponse response = ResourceMatchingAPI.searchResources();
        Test.stopTest();

        System.assertEquals(true, response.success);
        System.assertEquals(4, response.resources.size()); // All test resources
    }

    @isTest
    static void testDistanceCalculation() {
        // This tests the Haversine formula implementation indirectly
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/v1/resources/match';
        req.httpMethod = 'POST';

        Map<String, Object> requestBody = new Map<String, Object>{
            'userId' => 'test_user_resource',
            'resourceType' => 'shelter',
            'latitude' => 37.7749,  // San Francisco
            'longitude' => -122.4194,
            'maxDistance' => 10  // Only 10 miles
        };

        req.requestBody = Blob.valueOf(JSON.serialize(requestBody));
        RestContext.request = req;

        Test.startTest();
        ResourceMatchingAPI.MatchResponse response = ResourceMatchingAPI.matchResources();
        Test.stopTest();

        System.assertEquals(true, response.success);

        // Should exclude San Jose (too far), include SF and Oakland (within range)
        for (ResourceMatchingAPI.ScoredResource scoredResource : response.resources) {
            if (scoredResource.distanceMiles != null) {
                System.assert(scoredResource.distanceMiles <= 10, 'Distance should be within max');
            }
        }
    }
}
