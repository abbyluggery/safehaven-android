# OAuth Connected App Setup for SafeHaven Android App

**Purpose**: Enable Android app to authenticate and sync data to Salesforce using OAuth 2.0

---

## Step-by-Step Setup (5 minutes)

### **Step 1: Create Connected App**

1. In Salesforce Setup, search for **"App Manager"**
2. Click **"New Connected App"** (top right)
3. Fill in basic information:
   - **Connected App Name**: `SafeHaven Android`
   - **API Name**: `SafeHaven_Android`
   - **Contact Email**: Your email address
   - **Description**: `OAuth authentication for SafeHaven domestic violence safety app`

### **Step 2: Enable OAuth Settings**

4. Check **"Enable OAuth Settings"**
5. **Callback URL**:
   ```
   safehaven://oauth/callback
   ```

6. **Selected OAuth Scopes** - Add these scopes (in order):
   - ✅ **Access and manage your data (api)**
   - ✅ **Perform requests on your behalf at any time (refresh_token, offline_access)**
   - ✅ **Access your basic information (id, profile, email, address, phone)**

7. **Enable for Device Flow**: ❌ Leave unchecked
8. **Require Proof Key for Code Exchange (PKCE)**: ✅ Check this (more secure)
9. **Enable Client Credentials Flow**: ❌ Leave unchecked

### **Step 3: Additional Settings**

10. Scroll down to **"IP Relaxation"**
    - Select: **"Relax IP restrictions"** (allows mobile access)

11. **Refresh Token Policy**:
    - Select: **"Refresh token is valid until revoked"**

12. Click **"Save"**

### **Step 4: Get Consumer Key and Secret**

13. After saving, Salesforce will show a success page
14. Click **"Manage Consumer Details"** (or "Continue")
15. You may need to verify with a verification code sent to your email
16. **CRITICAL**: Copy these values (you'll need them for Android app):
    - **Consumer Key** (Client ID) - long alphanumeric string
    - **Consumer Secret** (Client Secret) - click "Click to reveal" to see it

### **Step 5: Save Credentials Securely**

17. Create a file to store these credentials:

**File**: `SafeHaven-Build/android-oauth-credentials.txt`

```
SALESFORCE OAUTH CREDENTIALS FOR SAFEHAVEN ANDROID APP
=======================================================

Consumer Key (Client ID):
[PASTE YOUR CONSUMER KEY HERE]

Consumer Secret (Client Secret):
[PASTE YOUR CONSUMER SECRET HERE]

Callback URL:
safehaven://oauth/callback

Salesforce Instance URL:
https://login.salesforce.com

Selected Scopes:
- api
- refresh_token
- offline_access
- id
- profile
- email

Generated Date: 2025-11-17
```

**⚠️ SECURITY NOTE**:
- Add `android-oauth-credentials.txt` to `.gitignore` (DO NOT commit to GitHub)
- Consumer Secret is sensitive - treat like a password

---

## Verification Steps

### **Test in Postman (Optional)**

1. Open Postman
2. Create new request: **Authorization** tab
3. Type: **OAuth 2.0**
4. Configure:
   - **Grant Type**: Authorization Code
   - **Callback URL**: safehaven://oauth/callback
   - **Auth URL**: https://login.salesforce.com/services/oauth2/authorize
   - **Access Token URL**: https://login.salesforce.com/services/oauth2/token
   - **Client ID**: [Your Consumer Key]
   - **Client Secret**: [Your Consumer Secret]
   - **Scope**: api refresh_token
5. Click **"Get New Access Token"**
6. If successful, you'll see an access token - OAuth is working!

---

## Android App Integration

Once you have Consumer Key and Secret, update Android app configuration:

**File**: `app/src/main/res/values/salesforce_config.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- DO NOT commit this file to public repos -->
    <string name="salesforce_consumer_key">YOUR_CONSUMER_KEY_HERE</string>
    <string name="salesforce_redirect_uri">safehaven://oauth/callback</string>
    <string name="salesforce_login_url">https://login.salesforce.com</string>

    <!-- OAuth Scopes -->
    <string-array name="salesforce_oauth_scopes">
        <item>api</item>
        <item>refresh_token</item>
        <item>offline_access</item>
    </string-array>
</resources>
```

**Note**: Consumer Secret should be stored securely on device, not in XML file. Use Android Keystore for production.

---

## Troubleshooting

### Error: "redirect_uri_mismatch"
- **Fix**: Ensure callback URL in Salesforce matches exactly: `safehaven://oauth/callback`

### Error: "invalid_client_id"
- **Fix**: Double-check Consumer Key was copied correctly (no extra spaces)

### Error: "invalid_grant"
- **Fix**: User needs to authorize the app. Re-run OAuth flow.

### Error: "user hasn't approved this consumer"
- **Fix**: First-time users must approve the Connected App. This is expected behavior.

---

## Security Best Practices

1. ✅ **Never commit Consumer Secret to GitHub**
2. ✅ **Use PKCE** (already enabled)
3. ✅ **Rotate secrets** if compromised
4. ✅ **Monitor Connected App usage** (Setup → Connected Apps OAuth Usage)
5. ✅ **Revoke access** for testing devices when done

---

## Next Steps

After OAuth setup:
1. ✅ Update Android app with Consumer Key
2. ✅ Test OAuth login flow on physical device
3. ✅ Verify sync works (create incident on Android → check Salesforce)

---

**Setup Complete!** ✅

Your SafeHaven Android app can now authenticate to Salesforce and sync encrypted data.
