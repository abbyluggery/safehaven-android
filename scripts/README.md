# SafeHaven Deployment Scripts

This directory contains automated deployment scripts for the SafeHaven Salesforce backend.

---

## Scripts

### `deploy-to-salesforce.sh` (Linux/macOS)

Automated deployment script for Unix-based systems.

**Usage:**

```bash
cd /path/to/SafeHaven-Build
./scripts/deploy-to-salesforce.sh
```

**What it does:**
1. Checks if Salesforce CLI is installed (installs if missing)
2. Authenticates to your Salesforce org (if needed)
3. Deploys 6 custom objects
4. Deploys 8 Apex classes (4 APIs + 4 test classes)
5. Runs all Apex tests to verify deployment
6. Displays next steps

**Requirements:**
- Node.js (for Salesforce CLI)
- Internet connection
- Salesforce org credentials

---

### `deploy-to-salesforce.bat` (Windows)

Automated deployment script for Windows systems.

**Usage:**

```cmd
cd C:\path\to\SafeHaven-Build
scripts\deploy-to-salesforce.bat
```

Same functionality as the shell script, adapted for Windows.

---

## Manual Deployment

If you prefer to deploy manually or encounter issues with the scripts, see:

- **salesforce/DEPLOYMENT_GUIDE.md** - Comprehensive deployment instructions
- **salesforce/OAUTH_SETUP.md** - OAuth Connected App setup guide

---

## Deployment Methods

### Method 1: Automated Script (Easiest)

```bash
./scripts/deploy-to-salesforce.sh
```

### Method 2: Salesforce CLI Commands

```bash
# Authenticate
sf org login web --instance-url https://login.salesforce.com --alias safehaven-org

# Deploy objects
sf project deploy start --source-dir salesforce/force-app/main/default/objects

# Deploy classes
sf project deploy start --source-dir salesforce/force-app/main/default/classes

# Run tests
sf apex run test --test-level RunLocalTests --code-coverage
```

### Method 3: Deploy All at Once

```bash
sf project deploy start --source-dir salesforce/force-app/main/default
```

### Method 4: Workbench (UI-based)

1. Create zip package: `zip -r metadata.zip salesforce/force-app/main/default/`
2. Go to https://workbench.developerforce.com/
3. Migration → Deploy
4. Upload zip file

---

## Post-Deployment Checklist

After running the deployment script:

- [ ] Verify all 6 custom objects deployed
- [ ] Verify all 8 Apex classes deployed
- [ ] Run Apex tests (should show 75%+ coverage)
- [ ] Update field-level security permissions
- [ ] Create Permission Set: `SafeHaven_User_Access`
- [ ] Assign permissions to users
- [ ] Set up OAuth Connected App
- [ ] Test API endpoints

---

## Troubleshooting

**Script fails with "sf: command not found"**
- Install Salesforce CLI: `npm install -g @salesforce/cli`

**Authentication fails**
- Ensure you have Salesforce org credentials
- Check if firewall is blocking Salesforce

**Deployment fails with "Insufficient Privileges"**
- Ensure you're logged in as System Administrator
- Check object and field permissions

**Tests fail with coverage < 75%**
- All 4 test classes should deploy together
- Re-run: `sf apex run test --test-level RunLocalTests`

---

## What Gets Deployed

### Custom Objects (6)
- SafeHaven_Profile__c
- Incident_Report__c
- Evidence_Item__c
- Verified_Document__c
- Legal_Resource__c
- Survivor_Profile__c

### Apex Classes (8)
- SafeHavenSyncAPI + SafeHavenSyncAPITest
- DocumentVerificationAPI + DocumentVerificationAPITest
- ResourceMatchingAPI + ResourceMatchingAPITest
- PanicDeleteAPI + PanicDeleteAPITest

**Total**: 120+ fields, 4 REST APIs, 32 test methods

---

## Support

For detailed deployment instructions, see:
- **salesforce/DEPLOYMENT_GUIDE.md**

For OAuth setup after deployment, see:
- **salesforce/OAUTH_SETUP.md**
