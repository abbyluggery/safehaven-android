# SafeHaven Salesforce Deployment Guide

This guide will walk you through deploying the SafeHaven custom objects and Apex classes to your Salesforce org.

---

## Prerequisites

- Salesforce org (Developer, Production, or Sandbox)
- System Administrator access
- Node.js installed (for Salesforce CLI)
- Git repository cloned to your local machine

---

## Method 1: Salesforce CLI Deployment (Recommended)

### Step 1: Install Salesforce CLI

If not already installed:

```bash
npm install -g @salesforce/cli
```

Verify installation:

```bash
sf --version
```

### Step 2: Navigate to Project Directory

```bash
cd /path/to/SafeHaven-Build
```

### Step 3: Authenticate to Your Salesforce Org

**For Production/Developer Edition:**

```bash
sf org login web --instance-url https://login.salesforce.com --alias safehaven-org --set-default
```

**For Sandbox:**

```bash
sf org login web --instance-url https://test.salesforce.com --alias safehaven-sandbox --set-default
```

This will open a browser window. Log in with your Salesforce credentials.

### Step 4: Verify Connection

```bash
sf org display --target-org safehaven-org
```

### Step 5: Deploy Custom Objects

Deploy all 6 custom objects:

```bash
sf project deploy start --source-dir salesforce/force-app/main/default/objects --target-org safehaven-org
```

Expected output:
```
Deploying to <your-org>...
✓ Deploy succeeded
```

### Step 6: Deploy Apex Classes

Deploy all 4 API classes and 4 test classes:

```bash
sf project deploy start --source-dir salesforce/force-app/main/default/classes --target-org safehaven-org
```

### Step 7: Run Apex Tests (Required for Production)

```bash
sf apex run test --test-level RunLocalTests --target-org safehaven-org --result-format human --code-coverage
```

Verify that all tests pass with 75%+ code coverage.

### Step 8: Deploy Everything at Once (Alternative)

Deploy all metadata in one command:

```bash
sf project deploy start --source-dir salesforce/force-app/main/default --target-org safehaven-org
```

---

## Method 2: Manual Deployment via Workbench

If you prefer a UI-based approach:

### Step 1: Create Deployment Package

Zip the metadata:

```bash
cd salesforce/force-app/main/default
zip -r ~/safehaven-metadata.zip objects/ classes/
```

### Step 2: Access Workbench

1. Go to https://workbench.developerforce.com/
2. Log in with your Salesforce credentials
3. Select API version: 60.0

### Step 3: Deploy Metadata

1. Go to: **Migration** → **Deploy**
2. Click "Choose File" and select `safehaven-metadata.zip`
3. Check "Rollback On Error"
4. Check "Run All Tests" (for production)
5. Click "Next" → "Deploy"

### Step 4: Monitor Deployment

Watch the deployment progress. It should complete in 1-2 minutes.

---

## Method 3: Manual Creation via Setup (Not Recommended)

You can manually create each custom object and field through Setup → Object Manager, but this is very time-consuming (100+ fields across 6 objects).

---

## Post-Deployment: Update Field-Level Security

As you mentioned, you'll need to manually update field-level security permissions:

### Step 1: Create/Update Permission Set

1. **Setup** → **Permission Sets**
2. Click "New" or select existing permission set
3. Name: `SafeHaven_User_Access`

### Step 2: Grant Object Permissions

For each custom object, grant permissions:

- **SafeHaven_Profile__c**: Read, Create, Edit, Delete
- **Incident_Report__c**: Read, Create, Edit, Delete
- **Evidence_Item__c**: Read, Create, Edit, Delete
- **Verified_Document__c**: Read, Create, Edit, Delete
- **Legal_Resource__c**: Read (Create, Edit only for admins)
- **Survivor_Profile__c**: Read, Create, Edit, Delete

### Step 3: Grant Field-Level Security

For each object:

1. **Permission Sets** → **SafeHaven_User_Access** → **Object Settings**
2. Select object (e.g., SafeHaven_Profile__c)
3. Click "Edit"
4. Check "Visible" for all fields
5. Save

### Step 4: Grant Apex Class Access

1. **Permission Sets** → **SafeHaven_User_Access** → **Apex Class Access**
2. Click "Edit"
3. Add these classes:
   - SafeHavenSyncAPI
   - DocumentVerificationAPI
   - ResourceMatchingAPI
   - PanicDeleteAPI
4. Save

### Step 5: Assign Permission Set to Users

1. **Permission Sets** → **SafeHaven_User_Access** → **Manage Assignments**
2. Click "Add Assignments"
3. Select users who need access
4. Click "Assign"

---

## Verification

### Verify Custom Objects

```bash
sf org list metadata --metadata-type CustomObject --target-org safehaven-org | grep SafeHaven
```

Expected output:
```
CustomObject: SafeHaven_Profile__c
CustomObject: Incident_Report__c
CustomObject: Evidence_Item__c
CustomObject: Verified_Document__c
CustomObject: Legal_Resource__c
CustomObject: Survivor_Profile__c
```

### Verify Apex Classes

```bash
sf org list metadata --metadata-type ApexClass --target-org safehaven-org | grep -E "(SafeHaven|Document|Resource|Panic)"
```

Expected output:
```
ApexClass: SafeHavenSyncAPI
ApexClass: SafeHavenSyncAPITest
ApexClass: DocumentVerificationAPI
ApexClass: DocumentVerificationAPITest
ApexClass: ResourceMatchingAPI
ApexClass: ResourceMatchingAPITest
ApexClass: PanicDeleteAPI
ApexClass: PanicDeleteAPITest
```

### Test API Endpoints

Open **Developer Console** → **Debug** → **Open Execute Anonymous Window**

Paste this test code:

```apex
// Test SafeHavenSyncAPI is accessible
RestRequest req = new RestRequest();
req.requestURI = '/services/apexrest/safehaven/v1/sync/profile';
req.httpMethod = 'POST';
req.requestBody = Blob.valueOf('{"userId":"test"}');
RestContext.request = req;

SafeHavenSyncAPI.SyncResponse response = SafeHavenSyncAPI.syncProfile();
System.debug('API Response: ' + response.message);
```

Click "Execute" - should run without compilation errors.

---

## Troubleshooting

### Error: "Insufficient Privileges"

**Solution**: Update field-level security and object permissions (see Post-Deployment section)

### Error: "Unknown custom object type"

**Solution**: Custom objects didn't deploy. Re-run deployment:
```bash
sf project deploy start --source-dir salesforce/force-app/main/default/objects
```

### Error: "Test coverage less than 75%"

**Solution**: Run tests first to verify coverage:
```bash
sf apex run test --test-level RunLocalTests --code-coverage
```

All test classes should pass with 75%+ coverage.

### Error: "This metadata type cannot be retrieved"

**Solution**: Check API version compatibility. Ensure you're using API version 60.0 or higher.

---

## What Gets Deployed

### Custom Objects (6)
- SafeHaven_Profile__c (12 fields)
- Incident_Report__c (14 fields)
- Evidence_Item__c (14 fields)
- Verified_Document__c (12 fields)
- Legal_Resource__c (35+ fields)
- Survivor_Profile__c (30+ fields)

### Apex Classes (8)
- SafeHavenSyncAPI.cls + test class
- DocumentVerificationAPI.cls + test class
- ResourceMatchingAPI.cls + test class
- PanicDeleteAPI.cls + test class

### Total: 120+ fields, 4 REST APIs, 32 test methods

---

## Next Steps After Deployment

1. ✅ Update field-level security (see above)
2. ⚠️ Create OAuth Connected App (see OAUTH_SETUP.md)
3. ⚠️ Import 510 legal resources CSV to Legal_Resource__c
4. ⚠️ Test API endpoints with Postman
5. ⚠️ Integrate Android app with Salesforce

---

## Support

If you encounter deployment errors, check:
- Salesforce CLI logs: `~/.sf/sf.log`
- Salesforce Setup → Deployment Status
- API version compatibility (must be 60.0+)

---

**Status**: Ready for deployment
**API Version**: 60.0
**Test Coverage**: 75%+
