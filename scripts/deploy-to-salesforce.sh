#!/bin/bash

# SafeHaven Salesforce Deployment Script
# This script deploys all custom objects and Apex classes to your Salesforce org

set -e  # Exit on error

echo "🚀 SafeHaven Salesforce Deployment Script"
echo "=========================================="
echo ""

# Check if Salesforce CLI is installed
if ! command -v sf &> /dev/null; then
    echo "❌ Salesforce CLI not found. Installing..."
    npm install -g @salesforce/cli
    echo "✅ Salesforce CLI installed"
fi

echo "📋 Current Salesforce CLI version:"
sf --version
echo ""

# Check if user is authenticated
echo "🔐 Checking for authenticated orgs..."
ORG_COUNT=$(sf org list --json 2>/dev/null | jq '.result.nonScratchOrgs | length' || echo "0")

if [ "$ORG_COUNT" -eq "0" ]; then
    echo "⚠️  No authenticated orgs found."
    echo ""
    echo "Please authenticate to your Salesforce org:"
    echo ""
    read -p "Is this a Production/Developer org? (y/n): " IS_PROD

    if [ "$IS_PROD" = "y" ]; then
        echo "🌐 Authenticating to Production/Developer org..."
        sf org login web --instance-url https://login.salesforce.com --alias safehaven-org --set-default
    else
        echo "🧪 Authenticating to Sandbox org..."
        sf org login web --instance-url https://test.salesforce.com --alias safehaven-sandbox --set-default
    fi
else
    echo "✅ Found $ORG_COUNT authenticated org(s)"
    sf org list
    echo ""
fi

echo ""
echo "📦 Deploying SafeHaven metadata to Salesforce..."
echo ""

# Step 1: Deploy Custom Objects
echo "1️⃣  Deploying Custom Objects (6 objects)..."
sf project deploy start \
    --source-dir salesforce/force-app/main/default/objects \
    --wait 10

if [ $? -eq 0 ]; then
    echo "   ✅ Custom Objects deployed successfully"
else
    echo "   ❌ Custom Objects deployment failed"
    exit 1
fi

echo ""

# Step 2: Deploy Apex Classes
echo "2️⃣  Deploying Apex Classes (8 classes)..."
sf project deploy start \
    --source-dir salesforce/force-app/main/default/classes \
    --wait 10

if [ $? -eq 0 ]; then
    echo "   ✅ Apex Classes deployed successfully"
else
    echo "   ❌ Apex Classes deployment failed"
    exit 1
fi

echo ""

# Step 3: Run Apex Tests
echo "3️⃣  Running Apex Tests..."
echo "   (This may take 1-2 minutes)"
sf apex run test \
    --test-level RunLocalTests \
    --result-format human \
    --code-coverage \
    --wait 10

if [ $? -eq 0 ]; then
    echo "   ✅ All tests passed"
else
    echo "   ⚠️  Some tests may have failed. Check output above."
fi

echo ""
echo "=========================================="
echo "🎉 Deployment Complete!"
echo "=========================================="
echo ""
echo "Next Steps:"
echo "1. ✅ Custom Objects and Apex Classes deployed"
echo "2. ⚠️  Update field-level security (see DEPLOYMENT_GUIDE.md)"
echo "3. ⚠️  Create Permission Set for SafeHaven users"
echo "4. ⚠️  Set up OAuth Connected App (see OAUTH_SETUP.md)"
echo "5. ⚠️  Import 510 legal resources CSV"
echo ""
echo "To verify deployment:"
echo "  sf org list metadata --metadata-type CustomObject | grep SafeHaven"
echo "  sf org list metadata --metadata-type ApexClass | grep -E '(SafeHaven|Document|Resource|Panic)'"
echo ""
