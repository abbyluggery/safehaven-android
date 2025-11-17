/**
 * Resource Matching API
 * Intersectional resource matching algorithm
 *
 * Matches survivors with resources that serve their specific identity and needs:
 * - Trans survivors → Trans-inclusive shelters
 * - Male survivors → Male-serving programs
 * - Undocumented survivors → U-Visa legal support
 * - Deaf survivors → ASL interpreter services
 * - Survivors with pets → Pet-friendly shelters
 * etc.
 *
 * Scoring Algorithm:
 * - Base score: Distance-based (closer resources score higher)
 * - Identity match bonuses: +10 points per matching identity category
 * - Critical service bonuses: +15 points (e.g., U-Visa for undocumented, ASL for deaf)
 * - 24/7 availability: +5 points
 * - Free service: +5 points
 *
 * Endpoints:
 * POST /services/apexrest/safehaven/v1/resources/match - Match resources to survivor profile
 * GET /services/apexrest/safehaven/v1/resources/search - Search resources by type/location
 * GET /services/apexrest/safehaven/v1/resources/nearest - Find nearest resources
 */
@RestResource(urlMapping='/safehaven/v1/resources/*')
global with sharing class ResourceMatchingAPI {

    /**
     * Match resources to survivor's intersectional identity
     * POST /services/apexrest/safehaven/v1/resources/match
     *
     * Request body:
     * {
     *   "userId": "user_123",
     *   "resourceType": "shelter",  // Optional filter
     *   "latitude": 37.7749,
     *   "longitude": -122.4194,
     *   "maxDistance": 50  // miles
     * }
     */
    @HttpPost
    global static MatchResponse matchResources() {
        RestRequest req = RestContext.request;

        try {
            Map<String, Object> body = (Map<String, Object>) JSON.deserializeUntyped(req.requestBody.toString());

            String userId = (String) body.get('userId');
            String resourceType = (String) body.get('resourceType');
            Decimal userLat = body.get('latitude') != null ?
                Decimal.valueOf(String.valueOf(body.get('latitude'))) : null;
            Decimal userLon = body.get('longitude') != null ?
                Decimal.valueOf(String.valueOf(body.get('longitude'))) : null;
            Decimal maxDistance = body.get('maxDistance') != null ?
                Decimal.valueOf(String.valueOf(body.get('maxDistance'))) : 50;

            // Get survivor profile for intersectional matching
            List<Survivor_Profile__c> profiles = [
                SELECT Id, Is_LGBTQIA__c, Is_Transgender__c, Is_Nonbinary__c,
                       Is_BIPOC__c, Is_Male_Identifying__c, Is_Undocumented__c,
                       Is_Disabled__c, Is_Deaf_HOH__c, Wheelchair_User__c,
                       Has_Children__c, Has_Pets__c, Needs_U_Visa_Support__c,
                       Primary_Language__c, Needs_Interpreter__c,
                       Needs_Reproductive_Healthcare__c
                FROM Survivor_Profile__c
                WHERE User_ID__c = :userId
                LIMIT 1
            ];

            Survivor_Profile__c profile = profiles.isEmpty() ? null : profiles[0];

            // Query resources
            String query = 'SELECT Id, Name, Resource_Type__c, Organization_Name__c, ' +
                          'Phone_Number__c, Hotline_Number__c, Website__c, ' +
                          'Street_Address__c, City__c, State__c, Zip_Code__c, ' +
                          'Latitude__c, Longitude__c, Hours_Of_Operation__c, ' +
                          'Is_24_7__c, Is_Free__c, Languages_Spoken__c, ' +
                          'Serves_LGBTQIA__c, Serves_Trans__c, Serves_Nonbinary__c, ' +
                          'Serves_Men__c, BIPOC_Led__c, Serves_Undocumented__c, ' +
                          'U_Visa_Support__c, Wheelchair_Accessible__c, ASL_Interpreter__c, ' +
                          'Pets_Allowed__c, Children_Allowed__c, ' +
                          'Reproductive_Healthcare__c, STI_Testing__c, SANE_Exam__c, ' +
                          'Description__c, Verified__c ' +
                          'FROM Legal_Resource__c ' +
                          'WHERE Verified__c = true';

            if (String.isNotBlank(resourceType)) {
                query += ' AND Resource_Type__c = :resourceType';
            }

            query += ' ORDER BY Organization_Name__c ASC LIMIT 500';

            List<Legal_Resource__c> resources = Database.query(query);

            // Score and rank resources
            List<ScoredResource> scoredResources = new List<ScoredResource>();

            for (Legal_Resource__c resource : resources) {
                Decimal score = 0;

                // 1. Distance-based scoring (if location provided)
                Decimal distance = null;
                if (userLat != null && userLon != null && resource.Latitude__c != null && resource.Longitude__c != null) {
                    distance = calculateDistance(userLat, userLon, resource.Latitude__c, resource.Longitude__c);

                    if (distance <= maxDistance) {
                        // Closer = higher score (inverse relationship)
                        score += (maxDistance - distance) / maxDistance * 20;
                    } else {
                        continue; // Skip resources beyond max distance
                    }
                }

                // 2. Intersectional identity matching (if profile exists)
                if (profile != null) {
                    if (profile.Is_LGBTQIA__c && resource.Serves_LGBTQIA__c) {
                        score += 10;
                    }
                    if (profile.Is_Transgender__c && resource.Serves_Trans__c) {
                        score += 15; // Critical match
                    }
                    if (profile.Is_Nonbinary__c && resource.Serves_Nonbinary__c) {
                        score += 15;
                    }
                    if (profile.Is_Male_Identifying__c && resource.Serves_Men__c) {
                        score += 15; // Critical - most shelters don't serve men
                    }
                    if (profile.Is_BIPOC__c && resource.BIPOC_Led__c) {
                        score += 10;
                    }
                    if (profile.Is_Undocumented__c) {
                        if (resource.Serves_Undocumented__c) {
                            score += 10;
                        }
                        if (profile.Needs_U_Visa_Support__c && resource.U_Visa_Support__c) {
                            score += 20; // Critical legal support
                        }
                    }
                    if (profile.Wheelchair_User__c && resource.Wheelchair_Accessible__c) {
                        score += 15;
                    }
                    if (profile.Is_Deaf_HOH__c && resource.ASL_Interpreter__c) {
                        score += 15; // Critical communication support
                    }
                    if (profile.Has_Pets__c && resource.Pets_Allowed__c) {
                        score += 10; // Many survivors won't leave without pets
                    }
                    if (profile.Has_Children__c && resource.Children_Allowed__c) {
                        score += 5;
                    }
                    if (profile.Needs_Reproductive_Healthcare__c && resource.Reproductive_Healthcare__c) {
                        score += 15;
                    }
                }

                // 3. Service availability bonuses
                if (resource.Is_24_7__c) {
                    score += 5;
                }
                if (resource.Is_Free__c) {
                    score += 5;
                }

                scoredResources.add(new ScoredResource(resource, score, distance));
            }

            // Sort by score (descending)
            scoredResources.sort();

            return new MatchResponse(true, 'Success', scoredResources);

        } catch (Exception e) {
            return new MatchResponse(false, 'Error matching resources: ' + e.getMessage(), null);
        }
    }

    /**
     * Search resources by type and location
     * GET /services/apexrest/safehaven/v1/resources/search?type=shelter&state=CA&city=San%20Francisco
     */
    @HttpGet
    global static SearchResponse searchResources() {
        RestRequest req = RestContext.request;
        Map<String, String> params = req.params;

        try {
            String resourceType = params.get('type');
            String state = params.get('state');
            String city = params.get('city');

            String query = 'SELECT Id, Name, Resource_Type__c, Organization_Name__c, ' +
                          'Phone_Number__c, Hotline_Number__c, Website__c, ' +
                          'City__c, State__c, Hours_Of_Operation__c, ' +
                          'Is_24_7__c, Is_Free__c, Serves_LGBTQIA__c, ' +
                          'Serves_Trans__c, Serves_Men__c, Description__c ' +
                          'FROM Legal_Resource__c ' +
                          'WHERE Verified__c = true';

            if (String.isNotBlank(resourceType)) {
                query += ' AND Resource_Type__c = :resourceType';
            }
            if (String.isNotBlank(state)) {
                query += ' AND State__c = :state';
            }
            if (String.isNotBlank(city)) {
                query += ' AND City__c = :city';
            }

            query += ' ORDER BY Organization_Name__c ASC LIMIT 100';

            List<Legal_Resource__c> resources = Database.query(query);

            return new SearchResponse(true, 'Success', resources);

        } catch (Exception e) {
            return new SearchResponse(false, 'Error searching resources: ' + e.getMessage(), null);
        }
    }

    // ========== HELPER METHODS ==========

    /**
     * Calculate distance between two coordinates using Haversine formula
     * Returns distance in miles
     */
    private static Decimal calculateDistance(Decimal lat1, Decimal lon1, Decimal lat2, Decimal lon2) {
        Decimal earthRadiusMiles = 3959;
        Decimal dLat = (lat2 - lat1) * Math.PI / 180;
        Decimal dLon = (lon2 - lon1) * Math.PI / 180;

        Decimal a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        Decimal c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Decimal distance = earthRadiusMiles * c;

        return distance;
    }

    // ========== RESPONSE CLASSES ==========

    global class ScoredResource implements Comparable {
        public Legal_Resource__c resource;
        public Decimal score;
        public Decimal distanceMiles;

        public ScoredResource(Legal_Resource__c resource, Decimal score, Decimal distanceMiles) {
            this.resource = resource;
            this.score = score;
            this.distanceMiles = distanceMiles;
        }

        // Sort by score descending
        public Integer compareTo(Object compareTo) {
            ScoredResource other = (ScoredResource) compareTo;
            if (this.score > other.score) return -1;
            if (this.score < other.score) return 1;
            return 0;
        }
    }

    global class MatchResponse {
        public Boolean success;
        public String message;
        public List<ScoredResource> resources;

        public MatchResponse(Boolean success, String message, List<ScoredResource> resources) {
            this.success = success;
            this.message = message;
            this.resources = resources;
        }
    }

    global class SearchResponse {
        public Boolean success;
        public String message;
        public List<Legal_Resource__c> resources;

        public SearchResponse(Boolean success, String message, List<Legal_Resource__c> resources) {
            this.success = success;
            this.message = message;
            this.resources = resources;
        }
    }
}
