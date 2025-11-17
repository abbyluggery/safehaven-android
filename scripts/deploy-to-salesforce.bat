@echo off
REM SafeHaven Salesforce Deployment Script for Windows
REM This script deploys all custom objects and Apex classes to your Salesforce org

echo.
echo ========================================
echo SafeHaven Salesforce Deployment Script
echo ========================================
echo.

REM Check if Salesforce CLI is installed
where sf >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Salesforce CLI not found. Installing...
    npm install -g @salesforce/cli
    echo Salesforce CLI installed
)

echo Current Salesforce CLI version:
call sf --version
echo.

REM Check for authenticated orgs
echo Checking for authenticated orgs...
call sf org list
echo.

echo Please authenticate to your Salesforce org if not already done:
echo.
set /p IS_PROD="Is this a Production/Developer org? (y/n): "

if /i "%IS_PROD%"=="y" (
    echo Authenticating to Production/Developer org...
    call sf org login web --instance-url https://login.salesforce.com --alias safehaven-org --set-default
) else (
    echo Authenticating to Sandbox org...
    call sf org login web --instance-url https://test.salesforce.com --alias safehaven-sandbox --set-default
)

echo.
echo Deploying SafeHaven metadata to Salesforce...
echo.

REM Deploy Custom Objects
echo 1. Deploying Custom Objects (6 objects)...
call sf project deploy start --source-dir salesforce/force-app/main/default/objects --wait 10

if %ERRORLEVEL% EQU 0 (
    echo    Custom Objects deployed successfully
) else (
    echo    Custom Objects deployment failed
    exit /b 1
)

echo.

REM Deploy Apex Classes
echo 2. Deploying Apex Classes (8 classes)...
call sf project deploy start --source-dir salesforce/force-app/main/default/classes --wait 10

if %ERRORLEVEL% EQU 0 (
    echo    Apex Classes deployed successfully
) else (
    echo    Apex Classes deployment failed
    exit /b 1
)

echo.

REM Run Apex Tests
echo 3. Running Apex Tests...
echo    (This may take 1-2 minutes)
call sf apex run test --test-level RunLocalTests --result-format human --code-coverage --wait 10

echo.
echo ========================================
echo Deployment Complete!
echo ========================================
echo.
echo Next Steps:
echo 1. Update field-level security (see DEPLOYMENT_GUIDE.md)
echo 2. Create Permission Set for SafeHaven users
echo 3. Set up OAuth Connected App (see OAUTH_SETUP.md)
echo 4. Import 510 legal resources CSV
echo.
echo To verify deployment:
echo   sf org list metadata --metadata-type CustomObject
echo   sf org list metadata --metadata-type ApexClass
echo.
pause
