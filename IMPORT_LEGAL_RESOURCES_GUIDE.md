# Import 510 Legal Resources to Salesforce

The 510 legal resources are ready in `salesforce/data/Legal_Resource__c.csv`.

However, the bulk import failed because the CSV uses `External_ID__c` field which may not exist on your Legal_Resource__c object.

---

## Option 1: Import via Salesforce Data Import Wizard (Easiest)

### Steps:

1. **Login to Salesforce**
   - Go to: https://login.salesforce.com
   - Login with: abbyluggery179@agentforce.com

2. **Open Data Import Wizard**
   - Click Setup (gear icon)
   - Search for "Data Import Wizard"
   - Click "Launch Wizard"

3. **Configure Import**
   - **What are you importing?** Select "Custom objects"
   - **Which object?** Select "Legal Resources" (Legal_Resource__c)
   - **What do you want to do?** Select "Add new records"
   - Click "Next"

4. **Upload CSV**
   - Click "Choose File"
   - Select: `C:\Users\Abbyl\OneDrive\Desktop\Salesforce Training\SafeHaven-Build\salesforce\data\Legal_Resource__c.csv`
   - Click "Next"

5. **Map Fields**
   - **IMPORTANT**: Salesforce will try to auto-map fields
   - Review the mappings - most should auto-map correctly
   - For `External_ID__c` → Map to `Name` field instead (or skip if field doesn't exist)
   - Click "Next"

6. **Review and Start Import**
   - Review settings
   - Click "Start Import"

7. **Wait for Completion**
   - Import will process 510 records
   - You'll get an email when complete (usually 1-2 minutes)
   - Check for any errors in the email

---

## Option 2: Import via Salesforce CLI (if External_ID field exists)

If you created the External_ID__c field manually:

```bash
cd "C:\Users\Abbyl\OneDrive\Desktop\Salesforce Training\SafeHaven-Build"
sf data import bulk --sobject Legal_Resource__c --file salesforce/data/Legal_Resource__c.csv --target-org abbyluggery179@agentforce.com --wait 10
```

---

## Option 3: Use Simple CSV (Without External_ID)

I can create a simplified CSV that only uses standard fields. Let me know if you need this option.

---

## Verification After Import

Run this query in Developer Console or Workbench:

```sql
SELECT COUNT() FROM Legal_Resource__c
```

**Expected result**: 510 records

Check specific filters:

```sql
-- Trans-inclusive resources
SELECT COUNT() FROM Legal_Resource__c WHERE Trans_Inclusive__c = true

-- No ICE contact (critical for undocumented survivors)
SELECT COUNT() FROM Legal_Resource__c WHERE No_ICE_Contact__c = true

-- 24/7 availability
SELECT COUNT() FROM Legal_Resource__c WHERE Is_24_7__c = true

-- Free resources
SELECT COUNT() FROM Legal_Resource__c WHERE Is_Free__c = true
```

---

## What I've Completed:

✅ **OAuth Connected App Setup Guide** - Created `OAUTH_CONNECTED_APP_SETUP.md` with step-by-step instructions

✅ **Legal Resources CSV** - Transformed 510 Android resources to Salesforce format

✅ **CSV Location**: `salesforce/data/Legal_Resource__c.csv` (ready to import)

---

## Next Steps After Import:

Once resources are imported:
1. Test ResourceMatchingAPI endpoint
2. Verify intersectional filtering works
3. Test from Android app

**Recommendation**: Use Option 1 (Data Import Wizard) - it's the most reliable for first-time imports and will show you exactly which fields mapped correctly.
