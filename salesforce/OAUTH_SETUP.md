# Salesforce OAuth 2.0 Connected App Setup for SafeHaven

This guide explains how to set up OAuth 2.0 authentication for the SafeHaven Android app to securely communicate with the Salesforce backend.

---

## Overview

The SafeHaven Android app uses **OAuth 2.0** to authenticate with Salesforce and call the REST APIs:
- SafeHavenSyncAPI
- DocumentVerificationAPI
- ResourceMatchingAPI
- PanicDeleteAPI

**Flow**: Android app → OAuth login → Salesforce Connected App → REST API calls

---

## Prerequisites

- Salesforce Developer/Production Org
- System Administrator access
- SafeHaven custom objects deployed (6 objects)
- SafeHaven Apex classes deployed (4 APIs + 4 test classes)

---

## Step 1: Create Connected App in Salesforce

1. **Navigate to Setup**
   - Log in to Salesforce
   - Click the gear icon (⚙️) → Setup

2. **Create New Connected App**
   - In Quick Find box, search: "App Manager"
   - Click "App Manager"
   - Click "New Connected App" (top right)

3. **Basic Information**
   ```
   Connected App Name: SafeHaven Mobile App
   API Name: SafeHaven_Mobile_App
   Contact Email: [your-email@example.com]
   ```

4. **API (Enable OAuth Settings)**
   - ✅ Check "Enable OAuth Settings"

5. **Callback URL**
   ```
   safehaven://oauth/callback
   ```

   **For testing with localhost:**
   ```
   safehaven://oauth/callback
   http://localhost:8080/oauth/callback
   ```

6. **Selected OAuth Scopes** (Add these in order):
   - ✅ `Access the identity URL service (id, profile, email, address, phone)`
   - ✅ `Manage user data via APIs (api)`
   - ✅ `Perform requests at any time (refresh_token, offline_access)`
   - ✅ `Access unique user identifiers (openid)`

7. **Additional Settings**
   - ✅ Check "Require Secret for Web Server Flow" (for production)
   - ✅ Check "Require Secret for Refresh Token Flow"
   - ✅ Check "Enable Client Credentials Flow" (optional, for server-to-server)

8. **Save**
   - Click "Save"
   - Click "Continue"

---

## Step 2: Retrieve OAuth Credentials

After creating the Connected App, you'll see:

1. **Consumer Key (Client ID)**
   ```
   Example: 3MVG9...long string...ABC
   ```
   ⚠️ **Copy this immediately** - you'll need it for Android app

2. **Consumer Secret (Client Secret)**
   - Click "Click to reveal" next to Consumer Secret
   ```
   Example: 1234567890ABCDEFGHIJ
   ```
   ⚠️ **Copy this immediately** - you'll need it for Android app
   ⚠️ **Keep this SECRET** - never commit to version control

---

## Step 3: Configure Connected App Policies

1. **Navigate back to App Manager**
   - Setup → App Manager
   - Find "SafeHaven Mobile App"
   - Click dropdown arrow → "Manage"

2. **OAuth Policies**
   ```
   Permitted Users: All users may self-authorize
   IP Relaxation: Relax IP restrictions
   Refresh Token Policy: Refresh token is valid until revoked
   ```

3. **Save**

---

## Step 4: Create Permission Set (Recommended)

For security, create a permission set that grants access to SafeHaven objects:

1. **Create Permission Set**
   - Setup → Permission Sets
   - Click "New"
   ```
   Label: SafeHaven User Access
   API Name: SafeHaven_User_Access
   ```

2. **Object Settings** - Grant access to:
   - ✅ SafeHaven_Profile__c (Read, Create, Edit, Delete)
   - ✅ Incident_Report__c (Read, Create, Edit, Delete)
   - ✅ Evidence_Item__c (Read, Create, Edit, Delete)
   - ✅ Verified_Document__c (Read, Create, Edit, Delete)
   - ✅ Legal_Resource__c (Read only)
   - ✅ Survivor_Profile__c (Read, Create, Edit, Delete)

3. **Apex Class Access** - Enable:
   - ✅ SafeHavenSyncAPI
   - ✅ DocumentVerificationAPI
   - ✅ ResourceMatchingAPI
   - ✅ PanicDeleteAPI

4. **Assign to Users**
   - Permission Sets → SafeHaven User Access → Manage Assignments
   - Add all SafeHaven users

---

## Step 5: Android App Configuration

Add the OAuth credentials to your Android app:

### Option 1: Using local.properties (Recommended for Development)

Create/edit `local.properties` in your Android project root:

```properties
# Salesforce OAuth Configuration
salesforce.client.id=3MVG9...your_consumer_key...ABC
salesforce.client.secret=1234567890ABCDEFGHIJ
salesforce.instance.url=https://yourinstance.salesforce.com
salesforce.oauth.redirect.uri=safehaven://oauth/callback
```

**⚠️ IMPORTANT**: Add to `.gitignore`:
```
local.properties
```

### Option 2: Using BuildConfig (Production)

In `app/build.gradle`:

```gradle
android {
    defaultConfig {
        // Salesforce OAuth
        buildConfigField "String", "SF_CLIENT_ID", "\"${project.findProperty('salesforce.client.id') ?: ''}\""
        buildConfigField "String", "SF_CLIENT_SECRET", "\"${project.findProperty('salesforce.client.secret') ?: ''}\""
        buildConfigField "String", "SF_INSTANCE_URL", "\"${project.findProperty('salesforce.instance.url') ?: ''}\""
    }
}
```

### Option 3: Using Environment Variables (CI/CD)

For GitHub Actions or other CI/CD:

```yaml
env:
  SALESFORCE_CLIENT_ID: ${{ secrets.SALESFORCE_CLIENT_ID }}
  SALESFORCE_CLIENT_SECRET: ${{ secrets.SALESFORCE_CLIENT_SECRET }}
  SALESFORCE_INSTANCE_URL: ${{ secrets.SALESFORCE_INSTANCE_URL }}
```

---

## Step 6: Implement OAuth Flow in Android

### Kotlin Example:

```kotlin
// SalesforceAuthManager.kt
class SalesforceAuthManager(
    private val context: Context
) {
    companion object {
        private const val CLIENT_ID = BuildConfig.SF_CLIENT_ID
        private const val CLIENT_SECRET = BuildConfig.SF_CLIENT_SECRET
        private const val REDIRECT_URI = "safehaven://oauth/callback"
        private const val INSTANCE_URL = BuildConfig.SF_INSTANCE_URL
        private const val AUTH_URL = "$INSTANCE_URL/services/oauth2/authorize"
        private const val TOKEN_URL = "$INSTANCE_URL/services/oauth2/token"
    }

    // Step 1: Build authorization URL
    fun getAuthorizationUrl(): String {
        return "$AUTH_URL?" +
                "response_type=code&" +
                "client_id=$CLIENT_ID&" +
                "redirect_uri=${URLEncoder.encode(REDIRECT_URI, "UTF-8")}&" +
                "scope=${URLEncoder.encode("api refresh_token offline_access", "UTF-8")}"
    }

    // Step 2: Exchange authorization code for access token
    suspend fun getAccessToken(authorizationCode: String): OAuthTokenResponse {
        val requestBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", authorizationCode)
            .add("client_id", CLIENT_ID)
            .add("client_secret", CLIENT_SECRET)
            .add("redirect_uri", REDIRECT_URI)
            .build()

        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(requestBody)
            .build()

        val response = OkHttpClient().newCall(request).execute()
        val json = JSONObject(response.body?.string() ?: "")

        return OAuthTokenResponse(
            accessToken = json.getString("access_token"),
            refreshToken = json.getString("refresh_token"),
            instanceUrl = json.getString("instance_url")
        )
    }

    // Step 3: Make authenticated API call
    suspend fun callSalesforceAPI(
        endpoint: String,
        accessToken: String,
        method: String = "GET",
        body: RequestBody? = null
    ): Response {
        val request = Request.Builder()
            .url("$INSTANCE_URL$endpoint")
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/json")
            .method(method, body)
            .build()

        return OkHttpClient().newCall(request).execute()
    }
}

data class OAuthTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val instanceUrl: String
)
```

---

## Step 7: API Endpoint URLs

Once OAuth is set up, the Android app can call these endpoints:

### Sync API
```
POST https://yourinstance.salesforce.com/services/apexrest/safehaven/v1/sync/profile
POST https://yourinstance.salesforce.com/services/apexrest/safehaven/v1/sync/incident
POST https://yourinstance.salesforce.com/services/apexrest/safehaven/v1/sync/evidence
POST https://yourinstance.salesforce.com/services/apexrest/safehaven/v1/sync/survivorprofile
GET  https://yourinstance.salesforce.com/services/apexrest/safehaven/v1/sync/all/{userId}
```

### Document Verification API
```
POST   https://yourinstance.salesforce.com/services/apexrest/safehaven/v1/documents/verify
GET    https://yourinstance.salesforce.com/services/apexrest/safehaven/v1/documents/{userId}
DELETE https://yourinstance.salesforce.com/services/apexrest/safehaven/v1/documents/{documentId}
```

### Resource Matching API
```
POST https://yourinstance.salesforce.com/services/apexrest/safehaven/v1/resources/match
GET  https://yourinstance.salesforce.com/services/apexrest/safehaven/v1/resources/search
```

### Panic Delete API
```
POST https://yourinstance.salesforce.com/services/apexrest/safehaven/v1/panic/delete
POST https://yourinstance.salesforce.com/services/apexrest/safehaven/v1/panic/preview
```

---

## Step 8: Testing OAuth Flow

### Test with Postman:

1. **Get Authorization Code**
   - Open browser: `https://yourinstance.salesforce.com/services/oauth2/authorize?response_type=code&client_id=YOUR_CLIENT_ID&redirect_uri=http://localhost:8080/oauth/callback&scope=api refresh_token`
   - Log in to Salesforce
   - Copy the `code` parameter from redirect URL

2. **Exchange for Access Token**
   ```bash
   curl -X POST https://yourinstance.salesforce.com/services/oauth2/token \
     -d "grant_type=authorization_code" \
     -d "code=YOUR_AUTH_CODE" \
     -d "client_id=YOUR_CLIENT_ID" \
     -d "client_secret=YOUR_CLIENT_SECRET" \
     -d "redirect_uri=http://localhost:8080/oauth/callback"
   ```

3. **Test API Call**
   ```bash
   curl -X GET "https://yourinstance.salesforce.com/services/apexrest/safehaven/v1/sync/all/test_user_123" \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
     -H "Content-Type: application/json"
   ```

---

## Security Best Practices

### ✅ DO:
- Store OAuth credentials in environment variables or secure key storage
- Use HTTPS for all API calls
- Implement token refresh logic (access tokens expire after ~2 hours)
- Use Salesforce instance URL from token response (not hardcoded)
- Validate all API responses
- Log OAuth errors for debugging

### ❌ DON'T:
- Commit OAuth credentials to Git
- Hardcode client secret in source code
- Store access tokens in SharedPreferences without encryption
- Share OAuth credentials across environments (dev/staging/prod)
- Ignore token expiration

---

## Token Refresh Example

```kotlin
suspend fun refreshAccessToken(refreshToken: String): OAuthTokenResponse {
    val requestBody = FormBody.Builder()
        .add("grant_type", "refresh_token")
        .add("refresh_token", refreshToken)
        .add("client_id", CLIENT_ID)
        .add("client_secret", CLIENT_SECRET)
        .build()

    val request = Request.Builder()
        .url(TOKEN_URL)
        .post(requestBody)
        .build()

    val response = OkHttpClient().newCall(request).execute()
    val json = JSONObject(response.body?.string() ?: "")

    return OAuthTokenResponse(
        accessToken = json.getString("access_token"),
        refreshToken = refreshToken, // Reuse existing refresh token
        instanceUrl = json.getString("instance_url")
    )
}
```

---

## Troubleshooting

### Error: "redirect_uri_mismatch"
**Solution**: Verify callback URL in Connected App matches exactly (including protocol and case)

### Error: "invalid_client_id"
**Solution**: Double-check Consumer Key is correct, no extra spaces

### Error: "invalid_grant"
**Solution**: Authorization code expired (valid for 15 minutes) or already used. Get a new code.

### Error: "INVALID_SESSION_ID"
**Solution**: Access token expired. Use refresh token to get new access token.

### Error: "Insufficient Privileges"
**Solution**: Ensure user has permission to access SafeHaven objects and Apex classes

---

## Next Steps

1. ✅ Deploy custom objects to Salesforce
2. ✅ Deploy Apex classes and test classes
3. ✅ Create Connected App and copy OAuth credentials
4. ⚠️ Configure Android app with OAuth credentials
5. ⚠️ Implement OAuth flow in Android
6. ⚠️ Test end-to-end: Android → OAuth → Salesforce API
7. ⚠️ Import 510 legal resources to Legal_Resource__c object

---

## Additional Resources

- [Salesforce OAuth 2.0 Documentation](https://help.salesforce.com/s/articleView?id=sf.remoteaccess_oauth_web_server_flow.htm)
- [Connected App Best Practices](https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/intro_oauth_and_connected_apps.htm)
- [REST API Developer Guide](https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/)

---

**Status**: ✅ OAuth setup documentation complete
**Last Updated**: November 17, 2025
