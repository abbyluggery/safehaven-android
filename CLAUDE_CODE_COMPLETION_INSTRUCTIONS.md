# SafeHaven Completion Instructions for Claude Code

**Previous Branch**: `claude/safehaven-android-app-013udX3wnRYCxzmvCZP4mL97`
**Status**: Backend 100% complete, UI 0% complete, Salesforce backend missing
**Review Document**: See `CLAUDE_CODE_BUILD_REVIEW.md` for detailed analysis

---

## What You Already Built (DO NOT REBUILD)

You completed an **excellent Android backend foundation**:

✅ **Complete** (DO NOT TOUCH):
- All 6 Room entities (SafeHavenProfile, IncidentReport, VerifiedDocument, EvidenceItem, LegalResource, SurvivorProfile)
- All 6 DAOs with Flow-based queries
- SafeHavenCrypto.kt (AES-256-GCM encryption) - PERFECT implementation
- SilentCameraManager.kt (silent photo capture) - PERFECT implementation
- MetadataStripper.kt (GPS removal)
- ShakeDetector.kt + PanicDeleteUseCase.kt - PERFECT implementation
- DocumentVerificationService.kt (SHA-256 hashing)
- IntersectionalResourceMatcher.kt (scoring algorithm) - PERFECT implementation
- Modular architecture (app/ + safehaven-core/)

**Your previous work is production-quality. Do not modify these files.**

---

## Your Mission: Complete the Missing Components

### Priority 1: Build Salesforce Backend (REQUIRED - User explicitly requested)
### Priority 2: Build Android UI (REQUIRED - App not usable without UI)
### Priority 3: Import Legal Resources Data (REQUIRED - Core feature)
### Priority 4: Write Tests (REQUIRED - Production readiness)

---

## PRIORITY 1: Salesforce Backend (8-12 hours)

**User's Requirement**: "I asked that it build for a stand alone app and a **app intigration for Salesforce**"

You built the Android-side sync infrastructure (syncedToSalesforce fields, getUnsyncedRecords() queries), but **did not build the Salesforce backend**. Complete this now.

### Task 1.1: Create 6 Salesforce Custom Objects

Create these files in a new `salesforce/` directory:

#### File: `salesforce/objects/SafeHaven_Profile__c.object`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<CustomObject xmlns="http://soap.sforce.com/2006/04/metadata">
    <label>SafeHaven Profile</label>
    <pluralLabel>SafeHaven Profiles</pluralLabel>
    <nameField>
        <label>Profile Name</label>
        <type>Text</type>
    </nameField>
    <deploymentStatus>Deployed</deploymentStatus>
    <sharingModel>Private</sharingModel>

    <fields>
        <fullName>User_Id__c</fullName>
        <label>User ID</label>
        <type>Text</type>
        <length>255</length>
        <unique>true</unique>
        <externalId>true</externalId>
        <required>true</required>
    </fields>

    <fields>
        <fullName>Hashed_Primary_Password__c</fullName>
        <label>Hashed Primary Password</label>
        <type>Text</type>
        <length>255</length>
        <required>true</required>
    </fields>

    <fields>
        <fullName>Hashed_Decoy_Password__c</fullName>
        <label>Hashed Decoy Password</label>
        <type>Text</type>
        <length>255</length>
        <required>false</required>
    </fields>

    <fields>
        <fullName>Is_Shake_To_Delete_Enabled__c</fullName>
        <label>Is Shake to Delete Enabled</label>
        <type>Checkbox</type>
        <defaultValue>true</defaultValue>
    </fields>

    <fields>
        <fullName>Auto_Delete_Days__c</fullName>
        <label>Auto Delete Days</label>
        <type>Number</type>
        <precision>3</precision>
        <scale>0</scale>
        <required>false</required>
    </fields>

    <fields>
        <fullName>Last_Android_Sync__c</fullName>
        <label>Last Android Sync</label>
        <type>DateTime</type>
        <required>false</required>
    </fields>
</CustomObject>
```

#### File: `salesforce/objects/Incident_Report__c.object`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<CustomObject xmlns="http://soap.sforce.com/2006/04/metadata">
    <label>Incident Report</label>
    <pluralLabel>Incident Reports</pluralLabel>
    <nameField>
        <label>Report Number</label>
        <type>AutoNumber</type>
        <displayFormat>IR-{00000}</displayFormat>
    </nameField>
    <deploymentStatus>Deployed</deploymentStatus>
    <sharingModel>Private</sharingModel>

    <fields>
        <fullName>Report_Id__c</fullName>
        <label>Report ID</label>
        <type>Text</type>
        <length>255</length>
        <unique>true</unique>
        <externalId>true</externalId>
        <required>true</required>
    </fields>

    <fields>
        <fullName>User_Id__c</fullName>
        <label>User ID</label>
        <type>Text</type>
        <length>255</length>
        <required>true</required>
    </fields>

    <fields>
        <fullName>Incident_Timestamp__c</fullName>
        <label>Incident Timestamp</label>
        <type>DateTime</type>
        <required>true</required>
    </fields>

    <fields>
        <fullName>Incident_Type__c</fullName>
        <label>Incident Type</label>
        <type>Picklist</type>
        <required>true</required>
        <valueSet>
            <valueSetDefinition>
                <value>
                    <fullName>Physical Violence</fullName>
                    <default>false</default>
                </value>
                <value>
                    <fullName>Emotional Abuse</fullName>
                    <default>false</default>
                </value>
                <value>
                    <fullName>Financial Abuse</fullName>
                    <default>false</default>
                </value>
                <value>
                    <fullName>Sexual Abuse</fullName>
                    <default>false</default>
                </value>
                <value>
                    <fullName>Stalking</fullName>
                    <default>false</default>
                </value>
                <value>
                    <fullName>Digital Abuse</fullName>
                    <default>false</default>
                </value>
                <value>
                    <fullName>Threats</fullName>
                    <default>false</default>
                </value>
            </valueSetDefinition>
        </valueSet>
    </fields>

    <fields>
        <fullName>Severity_Score__c</fullName>
        <label>Severity Score</label>
        <type>Number</type>
        <precision>2</precision>
        <scale>0</scale>
        <required>true</required>
    </fields>

    <fields>
        <fullName>Encrypted_Description__c</fullName>
        <label>Encrypted Description</label>
        <type>EncryptedText</type>
        <length>32768</length>
        <maskChar>X</maskChar>
        <maskType>all</maskType>
        <required>false</required>
    </fields>

    <fields>
        <fullName>Encrypted_Witnesses__c</fullName>
        <label>Encrypted Witnesses</label>
        <type>EncryptedText</type>
        <length>4096</length>
        <maskChar>X</maskChar>
        <maskType>all</maskType>
        <required>false</required>
    </fields>

    <fields>
        <fullName>Encrypted_Location__c</fullName>
        <label>Encrypted Location</label>
        <type>EncryptedText</type>
        <length>1024</length>
        <maskChar>X</maskChar>
        <maskType>all</maskType>
        <required>false</required>
    </fields>

    <fields>
        <fullName>Police_Report_Number__c</fullName>
        <label>Police Report Number</label>
        <type>Text</type>
        <length>255</length>
        <required>false</required>
    </fields>

    <fields>
        <fullName>Last_Android_Sync__c</fullName>
        <label>Last Android Sync</label>
        <type>DateTime</type>
        <required>false</required>
    </fields>
</CustomObject>
```

#### File: `salesforce/objects/Evidence_Item__c.object`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<CustomObject xmlns="http://soap.sforce.com/2006/04/metadata">
    <label>Evidence Item</label>
    <pluralLabel>Evidence Items</pluralLabel>
    <nameField>
        <label>Evidence Number</label>
        <type>AutoNumber</type>
        <displayFormat>EV-{00000}</displayFormat>
    </nameField>
    <deploymentStatus>Deployed</deploymentStatus>
    <sharingModel>Private</sharingModel>

    <fields>
        <fullName>Evidence_Id__c</fullName>
        <label>Evidence ID</label>
        <type>Text</type>
        <length>255</length>
        <unique>true</unique>
        <externalId>true</externalId>
        <required>true</required>
    </fields>

    <fields>
        <fullName>User_Id__c</fullName>
        <label>User ID</label>
        <type>Text</type>
        <length>255</length>
        <required>true</required>
    </fields>

    <fields>
        <fullName>Incident_Report__c</fullName>
        <label>Incident Report</label>
        <type>Lookup</type>
        <referenceTo>Incident_Report__c</referenceTo>
        <relationshipLabel>Evidence Items</relationshipLabel>
        <relationshipName>Evidence_Items</relationshipName>
        <required>false</required>
    </fields>

    <fields>
        <fullName>Evidence_Type__c</fullName>
        <label>Evidence Type</label>
        <type>Picklist</type>
        <required>true</required>
        <valueSet>
            <valueSetDefinition>
                <value>
                    <fullName>Photo</fullName>
                    <default>false</default>
                </value>
                <value>
                    <fullName>Video</fullName>
                    <default>false</default>
                </value>
                <value>
                    <fullName>Audio</fullName>
                    <default>false</default>
                </value>
                <value>
                    <fullName>Text Message</fullName>
                    <default>false</default>
                </value>
                <value>
                    <fullName>Email</fullName>
                    <default>false</default>
                </value>
            </valueSetDefinition>
        </valueSet>
    </fields>

    <fields>
        <fullName>Evidence_Timestamp__c</fullName>
        <label>Evidence Timestamp</label>
        <type>DateTime</type>
        <required>true</required>
    </fields>

    <fields>
        <fullName>Original_File_Name__c</fullName>
        <label>Original File Name</label>
        <type>Text</type>
        <length>255</length>
        <required>true</required>
    </fields>

    <fields>
        <fullName>File_Size_Bytes__c</fullName>
        <label>File Size (Bytes)</label>
        <type>Number</type>
        <precision>18</precision>
        <scale>0</scale>
        <required>true</required>
    </fields>

    <fields>
        <fullName>Mime_Type__c</fullName>
        <label>MIME Type</label>
        <type>Text</type>
        <length>100</length>
        <required>true</required>
    </fields>

    <fields>
        <fullName>Salesforce_Files_Link__c</fullName>
        <label>Salesforce Files Link</label>
        <type>Url</type>
        <required>false</required>
    </fields>

    <fields>
        <fullName>Last_Android_Sync__c</fullName>
        <label>Last Android Sync</label>
        <type>DateTime</type>
        <required>false</required>
    </fields>
</CustomObject>
```

#### File: `salesforce/objects/Verified_Document__c.object`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<CustomObject xmlns="http://soap.sforce.com/2006/04/metadata">
    <label>Verified Document</label>
    <pluralLabel>Verified Documents</pluralLabel>
    <nameField>
        <label>Document Number</label>
        <type>AutoNumber</type>
        <displayFormat>VD-{00000}</displayFormat>
    </nameField>
    <deploymentStatus>Deployed</deploymentStatus>
    <sharingModel>Private</sharingModel>

    <fields>
        <fullName>Document_Id__c</fullName>
        <label>Document ID</label>
        <type>Text</type>
        <length>255</length>
        <unique>true</unique>
        <externalId>true</externalId>
        <required>true</required>
    </fields>

    <fields>
        <fullName>User_Id__c</fullName>
        <label>User ID</label>
        <type>Text</type>
        <length>255</length>
        <required>true</required>
    </fields>

    <fields>
        <fullName>Generated_Timestamp__c</fullName>
        <label>Generated Timestamp</label>
        <type>DateTime</type>
        <required>true</required>
    </fields>

    <fields>
        <fullName>Document_Hash_SHA256__c</fullName>
        <label>Document Hash (SHA-256)</label>
        <type>Text</type>
        <length>64</length>
        <required>true</required>
        <unique>true</unique>
    </fields>

    <fields>
        <fullName>Blockchain_Transaction_Hash__c</fullName>
        <label>Blockchain Transaction Hash</label>
        <type>Text</type>
        <length>66</length>
        <required>false</required>
    </fields>

    <fields>
        <fullName>Blockchain_Network__c</fullName>
        <label>Blockchain Network</label>
        <type>Picklist</type>
        <required>false</required>
        <valueSet>
            <valueSetDefinition>
                <value>
                    <fullName>Polygon Mainnet</fullName>
                    <default>true</default>
                </value>
                <value>
                    <fullName>Polygon Mumbai Testnet</fullName>
                    <default>false</default>
                </value>
            </valueSetDefinition>
        </valueSet>
    </fields>

    <fields>
        <fullName>Salesforce_Files_Link__c</fullName>
        <label>Salesforce Files Link</label>
        <type>Url</type>
        <required>false</required>
    </fields>

    <fields>
        <fullName>Last_Android_Sync__c</fullName>
        <label>Last Android Sync</label>
        <type>DateTime</type>
        <required>false</required>
    </fields>
</CustomObject>
```

#### File: `salesforce/objects/Legal_Resource__c.object`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<CustomObject xmlns="http://soap.sforce.com/2006/04/metadata">
    <label>Legal Resource</label>
    <pluralLabel>Legal Resources</pluralLabel>
    <nameField>
        <label>Resource Name</label>
        <type>Text</type>
    </nameField>
    <deploymentStatus>Deployed</deploymentStatus>
    <sharingModel>ReadWrite</sharingModel>

    <fields>
        <fullName>Resource_Id__c</fullName>
        <label>Resource ID</label>
        <type>Text</type>
        <length>255</length>
        <unique>true</unique>
        <externalId>true</externalId>
        <required>true</required>
    </fields>

    <fields>
        <fullName>Organization_Name__c</fullName>
        <label>Organization Name</label>
        <type>Text</type>
        <length>255</length>
        <required>true</required>
    </fields>

    <fields>
        <fullName>Resource_Type__c</fullName>
        <label>Resource Type</label>
        <type>Picklist</type>
        <required>true</required>
        <valueSet>
            <valueSetDefinition>
                <value><fullName>Shelter</fullName><default>false</default></value>
                <value><fullName>Legal Aid</fullName><default>false</default></value>
                <value><fullName>Hotline</fullName><default>false</default></value>
                <value><fullName>Therapy</fullName><default>false</default></value>
                <value><fullName>Financial Assistance</fullName><default>false</default></value>
                <value><fullName>Immigration Support</fullName><default>false</default></value>
                <value><fullName>Housing Assistance</fullName><default>false</default></value>
            </valueSetDefinition>
        </valueSet>
    </fields>

    <fields>
        <fullName>Description__c</fullName>
        <label>Description</label>
        <type>LongTextArea</type>
        <length>4096</length>
        <required>false</required>
    </fields>

    <fields>
        <fullName>Phone__c</fullName>
        <label>Phone</label>
        <type>Phone</type>
        <required>false</required>
    </fields>

    <fields>
        <fullName>Email__c</fullName>
        <label>Email</label>
        <type>Email</type>
        <required>false</required>
    </fields>

    <fields>
        <fullName>Website__c</fullName>
        <label>Website</label>
        <type>Url</type>
        <required>false</required>
    </fields>

    <fields>
        <fullName>Address__c</fullName>
        <label>Address</label>
        <type>TextArea</type>
        <required>false</required>
    </fields>

    <fields>
        <fullName>Latitude__c</fullName>
        <label>Latitude</label>
        <type>Number</type>
        <precision>10</precision>
        <scale>6</scale>
        <required>false</required>
    </fields>

    <fields>
        <fullName>Longitude__c</fullName>
        <label>Longitude</label>
        <type>Number</type>
        <precision>10</precision>
        <scale>6</scale>
        <required>false</required>
    </fields>

    <!-- INTERSECTIONAL FILTERS (26 checkboxes) -->

    <fields>
        <fullName>Serves_LGBTQIA__c</fullName>
        <label>Serves LGBTQIA+</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>LGBTQ_Specialized__c</fullName>
        <label>LGBTQ+ Specialized</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Trans_Inclusive__c</fullName>
        <label>Trans Inclusive</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Non_Binary_Inclusive__c</fullName>
        <label>Non-Binary Inclusive</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Serves_BIPOC__c</fullName>
        <label>Serves BIPOC</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>BIPOC_Led__c</fullName>
        <label>BIPOC-Led Organization</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Serves_Undocumented__c</fullName>
        <label>Serves Undocumented</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>U_Visa_Support__c</fullName>
        <label>U-Visa Support</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>VAWA_Support__c</fullName>
        <label>VAWA Support</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>No_ICE_Contact__c</fullName>
        <label>No ICE Contact</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
        <description>CRITICAL: Organization does not contact ICE</description>
    </fields>

    <fields>
        <fullName>Serves_Male_Identifying__c</fullName>
        <label>Serves Male-Identifying Survivors</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Serves_Disabled__c</fullName>
        <label>Serves Disabled</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Wheelchair_Accessible__c</fullName>
        <label>Wheelchair Accessible</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Serves_Deaf__c</fullName>
        <label>Serves Deaf</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>ASL_Interpreter__c</fullName>
        <label>ASL Interpreter Available</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Is_Free__c</fullName>
        <label>Is Free</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Sliding_Scale__c</fullName>
        <label>Sliding Scale Payment</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Is_24_7__c</fullName>
        <label>24/7 Availability</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Languages_Supported__c</fullName>
        <label>Languages Supported</label>
        <type>MultiselectPicklist</type>
        <required>false</required>
        <valueSet>
            <valueSetDefinition>
                <value><fullName>English</fullName><default>true</default></value>
                <value><fullName>Spanish</fullName><default>false</default></value>
                <value><fullName>Mandarin</fullName><default>false</default></value>
                <value><fullName>Arabic</fullName><default>false</default></value>
                <value><fullName>Vietnamese</fullName><default>false</default></value>
                <value><fullName>Tagalog</fullName><default>false</default></value>
                <value><fullName>Korean</fullName><default>false</default></value>
                <value><fullName>ASL</fullName><default>false</default></value>
            </valueSetDefinition>
        </valueSet>
    </fields>
</CustomObject>
```

#### File: `salesforce/objects/Survivor_Profile__c.object`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<CustomObject xmlns="http://soap.sforce.com/2006/04/metadata">
    <label>Survivor Profile</label>
    <pluralLabel>Survivor Profiles</pluralLabel>
    <nameField>
        <label>Profile Number</label>
        <type>AutoNumber</type>
        <displayFormat>SP-{00000}</displayFormat>
    </nameField>
    <deploymentStatus>Deployed</deploymentStatus>
    <sharingModel>Private</sharingModel>

    <fields>
        <fullName>Profile_Id__c</fullName>
        <label>Profile ID</label>
        <type>Text</type>
        <length>255</length>
        <unique>true</unique>
        <externalId>true</externalId>
        <required>true</required>
    </fields>

    <fields>
        <fullName>User_Id__c</fullName>
        <label>User ID</label>
        <type>Text</type>
        <length>255</length>
        <required>true</required>
    </fields>

    <!-- INTERSECTIONAL IDENTITY (all optional) -->

    <fields>
        <fullName>Is_LGBTQIA__c</fullName>
        <label>Is LGBTQIA+</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Is_Trans__c</fullName>
        <label>Is Trans</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Is_Non_Binary__c</fullName>
        <label>Is Non-Binary</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Is_BIPOC__c</fullName>
        <label>Is BIPOC</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Is_Undocumented__c</fullName>
        <label>Is Undocumented</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Is_Male_Identifying__c</fullName>
        <label>Is Male-Identifying</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Is_Disabled__c</fullName>
        <label>Is Disabled</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Is_Deaf__c</fullName>
        <label>Is Deaf</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <!-- SERVICE NEEDS -->

    <fields>
        <fullName>Needs_Legal_Aid__c</fullName>
        <label>Needs Legal Aid</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Needs_Shelter__c</fullName>
        <label>Needs Shelter</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Needs_Financial_Assistance__c</fullName>
        <label>Needs Financial Assistance</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Needs_Therapy__c</fullName>
        <label>Needs Therapy</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Has_Children__c</fullName>
        <label>Has Children</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Has_Pets__c</fullName>
        <label>Has Pets</label>
        <type>Checkbox</type>
        <defaultValue>false</defaultValue>
    </fields>

    <fields>
        <fullName>Preferred_Languages__c</fullName>
        <label>Preferred Languages</label>
        <type>MultiselectPicklist</type>
        <required>false</required>
        <valueSet>
            <valueSetDefinition>
                <value><fullName>English</fullName><default>true</default></value>
                <value><fullName>Spanish</fullName><default>false</default></value>
                <value><fullName>Mandarin</fullName><default>false</default></value>
                <value><fullName>Arabic</fullName><default>false</default></value>
                <value><fullName>Vietnamese</fullName><default>false</default></value>
                <value><fullName>Tagalog</fullName><default>false</default></value>
                <value><fullName>Korean</fullName><default>false</default></value>
            </valueSetDefinition>
        </valueSet>
    </fields>

    <fields>
        <fullName>Last_Android_Sync__c</fullName>
        <label>Last Android Sync</label>
        <type>DateTime</type>
        <required>false</required>
    </fields>
</CustomObject>
```

### Task 1.2: Create 4 Apex REST API Classes

#### File: `salesforce/classes/SafeHavenSyncAPI.cls`
```apex
@RestResource(urlMapping='/safehaven/sync/*')
global with sharing class SafeHavenSyncAPI {

    // POST /services/apexrest/safehaven/sync/profiles
    @HttpPost
    global static SyncResponse syncProfiles(List<ProfileDTO> profiles) {
        SyncResponse response = new SyncResponse();

        try {
            List<SafeHaven_Profile__c> recordsToUpsert = new List<SafeHaven_Profile__c>();

            for (ProfileDTO dto : profiles) {
                SafeHaven_Profile__c record = new SafeHaven_Profile__c(
                    User_Id__c = dto.userId,
                    Hashed_Primary_Password__c = dto.hashedPrimaryPassword,
                    Hashed_Decoy_Password__c = dto.hashedDecoyPassword,
                    Is_Shake_To_Delete_Enabled__c = dto.isShakeToDeleteEnabled,
                    Auto_Delete_Days__c = dto.autoDeleteDays,
                    Last_Android_Sync__c = System.now()
                );
                recordsToUpsert.add(record);
            }

            // Upsert by external ID (User_Id__c)
            Database.UpsertResult[] results = Database.upsert(recordsToUpsert, SafeHaven_Profile__c.User_Id__c, false);

            for (Integer i = 0; i < results.size(); i++) {
                if (results[i].isSuccess()) {
                    response.successCount++;
                    response.syncedIds.add(profiles[i].userId);
                } else {
                    response.failureCount++;
                    response.errors.add(new ErrorDTO(profiles[i].userId, results[i].getErrors()[0].getMessage()));
                }
            }

            response.success = true;

        } catch (Exception e) {
            response.success = false;
            response.errors.add(new ErrorDTO('SYSTEM_ERROR', e.getMessage()));
        }

        return response;
    }

    // DTO Classes
    global class ProfileDTO {
        public String userId;
        public String hashedPrimaryPassword;
        public String hashedDecoyPassword;
        public Boolean isShakeToDeleteEnabled;
        public Integer autoDeleteDays;
    }

    global class SyncResponse {
        public Boolean success;
        public Integer successCount = 0;
        public Integer failureCount = 0;
        public List<String> syncedIds = new List<String>();
        public List<ErrorDTO> errors = new List<ErrorDTO>();
    }

    global class ErrorDTO {
        public String recordId;
        public String errorMessage;

        public ErrorDTO(String id, String msg) {
            this.recordId = id;
            this.errorMessage = msg;
        }
    }
}
```

#### File: `salesforce/classes/IncidentReportSyncAPI.cls`
```apex
@RestResource(urlMapping='/safehaven/sync/incidents')
global with sharing class IncidentReportSyncAPI {

    @HttpPost
    global static SafeHavenSyncAPI.SyncResponse syncIncidents(List<IncidentDTO> incidents) {
        SafeHavenSyncAPI.SyncResponse response = new SafeHavenSyncAPI.SyncResponse();

        try {
            List<Incident_Report__c> recordsToUpsert = new List<Incident_Report__c>();

            for (IncidentDTO dto : incidents) {
                Incident_Report__c record = new Incident_Report__c(
                    Report_Id__c = dto.reportId,
                    User_Id__c = dto.userId,
                    Incident_Timestamp__c = dto.timestamp,
                    Incident_Type__c = dto.incidentType,
                    Severity_Score__c = dto.severityScore,
                    Encrypted_Description__c = dto.encryptedDescription,
                    Encrypted_Witnesses__c = dto.encryptedWitnesses,
                    Encrypted_Location__c = dto.encryptedLocation,
                    Police_Report_Number__c = dto.policeReportNumber,
                    Last_Android_Sync__c = System.now()
                );
                recordsToUpsert.add(record);
            }

            Database.UpsertResult[] results = Database.upsert(recordsToUpsert, Incident_Report__c.Report_Id__c, false);

            for (Integer i = 0; i < results.size(); i++) {
                if (results[i].isSuccess()) {
                    response.successCount++;
                    response.syncedIds.add(incidents[i].reportId);
                } else {
                    response.failureCount++;
                    response.errors.add(new SafeHavenSyncAPI.ErrorDTO(incidents[i].reportId, results[i].getErrors()[0].getMessage()));
                }
            }

            response.success = true;

        } catch (Exception e) {
            response.success = false;
            response.errors.add(new SafeHavenSyncAPI.ErrorDTO('SYSTEM_ERROR', e.getMessage()));
        }

        return response;
    }

    global class IncidentDTO {
        public String reportId;
        public String userId;
        public DateTime timestamp;
        public String incidentType;
        public Integer severityScore;
        public String encryptedDescription;
        public String encryptedWitnesses;
        public String encryptedLocation;
        public String policeReportNumber;
    }
}
```

#### File: `salesforce/classes/LegalResourceAPI.cls`
```apex
@RestResource(urlMapping='/safehaven/resources/search')
global with sharing class LegalResourceAPI {

    @HttpGet
    global static ResourceResponse searchResources() {
        ResourceResponse response = new ResourceResponse();

        try {
            RestRequest req = RestContext.request;

            // Parse query parameters
            String resourceType = req.params.get('resourceType');
            Boolean servesLGBTQIA = Boolean.valueOf(req.params.get('servesLGBTQIA'));
            Boolean transInclusive = Boolean.valueOf(req.params.get('transInclusive'));
            Boolean servesUndocumented = Boolean.valueOf(req.params.get('servesUndocumented'));
            Boolean noICEContact = Boolean.valueOf(req.params.get('noICEContact'));
            Boolean servesMaleIdentifying = Boolean.valueOf(req.params.get('servesMaleIdentifying'));
            Boolean servesBIPOC = Boolean.valueOf(req.params.get('servesBIPOC'));
            Boolean servesDisabled = Boolean.valueOf(req.params.get('servesDisabled'));
            Boolean servesDeaf = Boolean.valueOf(req.params.get('servesDeaf'));
            Boolean isFree = Boolean.valueOf(req.params.get('isFree'));

            // Build dynamic SOQL query
            String query = 'SELECT Id, Resource_Id__c, Organization_Name__c, Resource_Type__c, ' +
                           'Description__c, Phone__c, Email__c, Website__c, Address__c, ' +
                           'Latitude__c, Longitude__c, Serves_LGBTQIA__c, Trans_Inclusive__c, ' +
                           'Serves_Undocumented__c, No_ICE_Contact__c, Serves_Male_Identifying__c, ' +
                           'Serves_BIPOC__c, Serves_Disabled__c, Serves_Deaf__c, Is_Free__c, ' +
                           'Is_24_7__c FROM Legal_Resource__c WHERE 1=1';

            if (String.isNotBlank(resourceType)) {
                query += ' AND Resource_Type__c = \'' + String.escapeSingleQuotes(resourceType) + '\'';
            }
            if (servesLGBTQIA != null && servesLGBTQIA) {
                query += ' AND Serves_LGBTQIA__c = true';
            }
            if (transInclusive != null && transInclusive) {
                query += ' AND Trans_Inclusive__c = true';
            }
            if (servesUndocumented != null && servesUndocumented) {
                query += ' AND Serves_Undocumented__c = true';
            }
            if (noICEContact != null && noICEContact) {
                query += ' AND No_ICE_Contact__c = true';
            }
            if (servesMaleIdentifying != null && servesMaleIdentifying) {
                query += ' AND Serves_Male_Identifying__c = true';
            }
            if (servesBIPOC != null && servesBIPOC) {
                query += ' AND Serves_BIPOC__c = true';
            }
            if (servesDisabled != null && servesDisabled) {
                query += ' AND Serves_Disabled__c = true';
            }
            if (servesDeaf != null && servesDeaf) {
                query += ' AND Serves_Deaf__c = true';
            }
            if (isFree != null && isFree) {
                query += ' AND Is_Free__c = true';
            }

            query += ' LIMIT 1000';

            response.resources = Database.query(query);
            response.count = response.resources.size();
            response.success = true;

        } catch (Exception e) {
            response.success = false;
            response.errorMessage = e.getMessage();
        }

        return response;
    }

    global class ResourceResponse {
        public Boolean success;
        public Integer count;
        public List<Legal_Resource__c> resources;
        public String errorMessage;
    }
}
```

#### File: `salesforce/classes/DocumentVerificationAPI.cls`
```apex
@RestResource(urlMapping='/safehaven/documents/*')
global with sharing class DocumentVerificationAPI {

    // POST /services/apexrest/safehaven/documents/verify
    @HttpPost
    global static VerificationResponse verifyDocument(String documentHash) {
        VerificationResponse response = new VerificationResponse();

        try {
            List<Verified_Document__c> docs = [
                SELECT Id, Document_Id__c, Generated_Timestamp__c,
                       Document_Hash_SHA256__c, Blockchain_Transaction_Hash__c,
                       Blockchain_Network__c
                FROM Verified_Document__c
                WHERE Document_Hash_SHA256__c = :documentHash
                LIMIT 1
            ];

            if (!docs.isEmpty()) {
                response.verified = true;
                response.document = docs[0];
                response.message = 'Document verified in Salesforce records';
            } else {
                response.verified = false;
                response.message = 'Document not found in Salesforce records';
            }

            response.success = true;

        } catch (Exception e) {
            response.success = false;
            response.message = e.getMessage();
        }

        return response;
    }

    global class VerificationResponse {
        public Boolean success;
        public Boolean verified;
        public String message;
        public Verified_Document__c document;
    }
}
```

### Task 1.3: Create Apex Test Classes

#### File: `salesforce/classes/SafeHavenSyncAPITest.cls`
```apex
@IsTest
private class SafeHavenSyncAPITest {

    @IsTest
    static void testSyncProfiles_Success() {
        // Setup
        List<SafeHavenSyncAPI.ProfileDTO> profiles = new List<SafeHavenSyncAPI.ProfileDTO>();

        SafeHavenSyncAPI.ProfileDTO dto = new SafeHavenSyncAPI.ProfileDTO();
        dto.userId = 'test-user-123';
        dto.hashedPrimaryPassword = 'abc123';
        dto.hashedDecoyPassword = 'def456';
        dto.isShakeToDeleteEnabled = true;
        dto.autoDeleteDays = 30;
        profiles.add(dto);

        // Execute
        Test.startTest();
        SafeHavenSyncAPI.SyncResponse response = SafeHavenSyncAPI.syncProfiles(profiles);
        Test.stopTest();

        // Verify
        System.assert(response.success, 'Sync should succeed');
        System.assertEquals(1, response.successCount, 'Should sync 1 profile');
        System.assertEquals(0, response.failureCount, 'Should have no failures');

        // Verify record created
        List<SafeHaven_Profile__c> records = [SELECT User_Id__c FROM SafeHaven_Profile__c WHERE User_Id__c = 'test-user-123'];
        System.assertEquals(1, records.size(), 'Profile should be created');
    }

    @IsTest
    static void testSyncProfiles_Upsert() {
        // Create existing record
        SafeHaven_Profile__c existing = new SafeHaven_Profile__c(
            User_Id__c = 'test-user-456',
            Hashed_Primary_Password__c = 'old-password'
        );
        insert existing;

        // Update via sync
        List<SafeHavenSyncAPI.ProfileDTO> profiles = new List<SafeHavenSyncAPI.ProfileDTO>();
        SafeHavenSyncAPI.ProfileDTO dto = new SafeHavenSyncAPI.ProfileDTO();
        dto.userId = 'test-user-456';
        dto.hashedPrimaryPassword = 'new-password';
        dto.isShakeToDeleteEnabled = false;
        profiles.add(dto);

        Test.startTest();
        SafeHavenSyncAPI.SyncResponse response = SafeHavenSyncAPI.syncProfiles(profiles);
        Test.stopTest();

        System.assert(response.success);

        // Verify record updated
        SafeHaven_Profile__c updated = [SELECT Hashed_Primary_Password__c FROM SafeHaven_Profile__c WHERE User_Id__c = 'test-user-456'];
        System.assertEquals('new-password', updated.Hashed_Primary_Password__c);
    }
}
```

#### File: `salesforce/classes/LegalResourceAPITest.cls`
```apex
@IsTest
private class LegalResourceAPITest {

    @IsTest
    static void testSearchResources_ByType() {
        // Create test resources
        Legal_Resource__c resource1 = new Legal_Resource__c(
            Resource_Id__c = 'res-001',
            Organization_Name__c = 'Test Shelter',
            Resource_Type__c = 'Shelter',
            Serves_LGBTQIA__c = true,
            Trans_Inclusive__c = true,
            Is_Free__c = true
        );
        insert resource1;

        Legal_Resource__c resource2 = new Legal_Resource__c(
            Resource_Id__c = 'res-002',
            Organization_Name__c = 'Legal Aid',
            Resource_Type__c = 'Legal Aid',
            Serves_Undocumented__c = true,
            No_ICE_Contact__c = true
        );
        insert resource2;

        // Setup REST request
        RestRequest req = new RestRequest();
        req.requestURI = '/services/apexrest/safehaven/resources/search';
        req.params.put('resourceType', 'Shelter');
        req.params.put('servesLGBTQIA', 'true');
        RestContext.request = req;

        // Execute
        Test.startTest();
        LegalResourceAPI.ResourceResponse response = LegalResourceAPI.searchResources();
        Test.stopTest();

        // Verify
        System.assert(response.success);
        System.assertEquals(1, response.count, 'Should find 1 shelter');
        System.assertEquals('Test Shelter', response.resources[0].Organization_Name__c);
    }
}
```

### Task 1.4: Create package.xml for Deployment

#### File: `salesforce/package.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Package xmlns="http://soap.sforce.com/2006/04/metadata">
    <types>
        <members>SafeHaven_Profile__c</members>
        <members>Incident_Report__c</members>
        <members>Evidence_Item__c</members>
        <members>Verified_Document__c</members>
        <members>Legal_Resource__c</members>
        <members>Survivor_Profile__c</members>
        <name>CustomObject</name>
    </types>
    <types>
        <members>SafeHavenSyncAPI</members>
        <members>IncidentReportSyncAPI</members>
        <members>LegalResourceAPI</members>
        <members>DocumentVerificationAPI</members>
        <members>SafeHavenSyncAPITest</members>
        <members>LegalResourceAPITest</members>
        <name>ApexClass</name>
    </types>
    <version>60.0</version>
</Package>
```

### Task 1.5: Create Deployment Instructions

#### File: `salesforce/DEPLOYMENT_GUIDE.md`
```markdown
# SafeHaven Salesforce Deployment Guide

## Prerequisites
1. Salesforce org with Platform Shield enabled (for field-level encryption)
2. Salesforce CLI installed
3. Connected App for OAuth 2.0

## Step 1: Deploy Custom Objects and Apex Classes

```bash
cd salesforce
sf project deploy start --manifest package.xml --target-org YOUR_ORG_ALIAS
```

## Step 2: Enable Platform Shield Encryption

1. Setup → Platform Encryption → Encryption Policy
2. Encrypt these fields:
   - Incident_Report__c.Encrypted_Description__c
   - Incident_Report__c.Encrypted_Witnesses__c
   - Incident_Report__c.Encrypted_Location__c

## Step 3: Create Connected App for Android OAuth

1. Setup → App Manager → New Connected App
2. Name: SafeHaven Android
3. Enable OAuth Settings
4. Callback URL: `safehaven://oauth/callback`
5. Selected OAuth Scopes:
   - Access and manage your data (api)
   - Perform requests on your behalf at any time (refresh_token, offline_access)
6. Save and note: Consumer Key, Consumer Secret

## Step 4: Configure Remote Site Settings

1. Setup → Remote Site Settings → New Remote Site
2. Name: SafeHavenAndroid
3. URL: https://login.salesforce.com
4. Active: Checked

## Step 5: Run Apex Tests

```bash
sf apex run test --test-level RunLocalTests --target-org YOUR_ORG_ALIAS --wait 10 --result-format human
```

Required: 75% code coverage for production deployment.

## Step 6: Update Android App Configuration

Update `app/src/main/res/values/salesforce_config.xml`:

```xml
<resources>
    <string name="salesforce_consumer_key">YOUR_CONSUMER_KEY</string>
    <string name="salesforce_redirect_uri">safehaven://oauth/callback</string>
    <string name="salesforce_login_url">https://login.salesforce.com</string>
</resources>
```

## Step 7: Test Android-Salesforce Sync

1. Build Android app
2. Login with Salesforce credentials
3. Create incident report on Android
4. Verify sync to Salesforce via Workbench or Salesforce UI
```

---

## PRIORITY 2: Build Android UI (16-24 hours)

You built zero UI screens. Build all 12 Jetpack Compose screens now.

### Task 2.1: Setup Navigation and Theme

#### File: `app/src/main/java/app/neurothrive/safehaven/ui/theme/Color.kt`
```kotlin
package app.neurothrive.safehaven.ui.theme

import androidx.compose.ui.graphics.Color

// Light theme colors (purple/teal - calming, professional)
val Purple80 = Color(0xFF6750A4)
val PurpleGrey80 = Color(0xFF625B71)
val Pink80 = Color(0xFF7D5260)

// Dark theme colors
val Purple40 = Color(0xFFD0BCFF)
val PurpleGrey40 = Color(0xFFCCC2DC)
val Pink40 = Color(0xFFEFB8C8)

// Accent colors
val TealAccent = Color(0xFF26A69A)
val RedError = Color(0xFFB3261E)
val GreenSuccess = Color(0xFF4CAF50)
```

#### File: `app/src/main/java/app/neurothrive/safehaven/ui/theme/Theme.kt`
```kotlin
package app.neurothrive.safehaven.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    error = RedError,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    error = RedError,
    onPrimary = Color(0xFF371E73),
    onSecondary = Color(0xFF332D41),
    onTertiary = Color(0xFF492532),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    onError = Color(0xFF601410)
)

@Composable
fun SafeHavenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

#### File: `app/src/main/java/app/neurothrive/safehaven/ui/navigation/SafeHavenNavGraph.kt`
```kotlin
package app.neurothrive.safehaven.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.neurothrive.safehaven.ui.screens.*

@Composable
fun SafeHavenNavGraph(
    navController: NavHostController,
    startDestination: String = "onboarding"
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") {
            OnboardingScreen(
                onContinue = { navController.navigate("login") }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("home") {
                    popUpTo("onboarding") { inclusive = true }
                } }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateTo = { route -> navController.navigate(route) }
            )
        }

        composable("silent_camera") {
            SilentCameraScreen(
                onPhotoSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable("incident_report") {
            IncidentReportScreen(
                onReportSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable("evidence_gallery") {
            EvidenceGalleryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("document_verification") {
            DocumentVerificationScreen(
                onDocumentGenerated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable("resource_search") {
            ResourceSearchScreen(
                onResourceClick = { resourceId ->
                    navController.navigate("resource_detail/$resourceId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("resource_detail/{resourceId}") { backStackEntry ->
            val resourceId = backStackEntry.arguments?.getString("resourceId") ?: ""
            ResourceDetailScreen(
                resourceId = resourceId,
                onBack = { navController.popBackStack() }
            )
        }

        composable("survivor_profile") {
            SurvivorProfileScreen(
                onSave = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Home : Screen("home")
    object SilentCamera : Screen("silent_camera")
    object IncidentReport : Screen("incident_report")
    object EvidenceGallery : Screen("evidence_gallery")
    object DocumentVerification : Screen("document_verification")
    object ResourceSearch : Screen("resource_search")
    object ResourceDetail : Screen("resource_detail/{resourceId}")
    object SurvivorProfile : Screen("survivor_profile")
    object Settings : Screen("settings")
}
```

### Task 2.2: Build 12 Jetpack Compose Screens

For each screen below, create the file and implement the UI. **Use Material Design 3 components**.

#### File: `app/src/main/java/app/neurothrive/safehaven/ui/screens/OnboardingScreen.kt`
```kotlin
package app.neurothrive.safehaven.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(
    onContinue: () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }

    val pages = listOf(
        OnboardingPage(
            title = "Welcome to SafeHaven",
            description = "A free, encrypted safety planning app for domestic violence survivors. Your data is protected with military-grade encryption."
        ),
        OnboardingPage(
            title = "Dual Password System",
            description = "Set a primary password (real data) and optional decoy password (shows empty app). This protects you if forced to unlock."
        ),
        OnboardingPage(
            title = "Panic Delete",
            description = "Shake your phone rapidly to securely delete all evidence. Files are overwritten to prevent forensic recovery."
        ),
        OnboardingPage(
            title = "Silent Camera",
            description = "Take photos with no sound, no flash, and GPS removed. Evidence is immediately encrypted."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Page content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = pages[currentPage].title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = pages[currentPage].description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Page indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(pages.size) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentPage) 12.dp else 8.dp)
                        .background(
                            if (index == currentPage)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentPage > 0) {
                TextButton(onClick = { currentPage-- }) {
                    Text("Back")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Button(
                onClick = {
                    if (currentPage < pages.size - 1) {
                        currentPage++
                    } else {
                        onContinue()
                    }
                }
            ) {
                Text(if (currentPage < pages.size - 1) "Next" else "Get Started")
            }
        }
    }
}

data class OnboardingPage(
    val title: String,
    val description: String
)
```

#### File: `app/src/main/java/app/neurothrive/safehaven/ui/screens/HomeScreen.kt`
```kotlin
package app.neurothrive.safehaven.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateTo: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SafeHaven") },
                actions = {
                    IconButton(onClick = { onNavigateTo("settings") }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ActionCard(
                        title = "Silent Camera",
                        icon = Icons.Default.CameraAlt,
                        onClick = { onNavigateTo("silent_camera") }
                    )
                }

                item {
                    ActionCard(
                        title = "New Incident",
                        icon = Icons.Default.Description,
                        onClick = { onNavigateTo("incident_report") }
                    )
                }

                item {
                    ActionCard(
                        title = "Evidence Gallery",
                        icon = Icons.Default.PhotoLibrary,
                        onClick = { onNavigateTo("evidence_gallery") }
                    )
                }

                item {
                    ActionCard(
                        title = "Find Resources",
                        icon = Icons.Default.Search,
                        onClick = { onNavigateTo("resource_search") }
                    )
                }

                item {
                    ActionCard(
                        title = "Verify Document",
                        icon = Icons.Default.VerifiedUser,
                        onClick = { onNavigateTo("document_verification") }
                    )
                }

                item {
                    ActionCard(
                        title = "My Profile",
                        icon = Icons.Default.Person,
                        onClick = { onNavigateTo("survivor_profile") }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
```

**Continue with remaining 10 screens:**

- LoginScreen.kt (password entry, biometric)
- SilentCameraScreen.kt (CameraX preview, capture button)
- IncidentReportScreen.kt (form with incident type, severity, description)
- EvidenceGalleryScreen.kt (grid of encrypted photos/videos)
- DocumentVerificationScreen.kt (generate PDF, show SHA-256 hash)
- ResourceSearchScreen.kt (intersectional filters, search results)
- ResourceDetailScreen.kt (organization info, contact buttons)
- SurvivorProfileScreen.kt (intersectional identity checkboxes)
- SettingsScreen.kt (panic delete config, Salesforce sync)
- PanicDeleteConfirmationScreen.kt ("All data deleted" message)

**IMPORTANT**: Each screen must:
1. Use Material Design 3 components (Card, Button, TextField, etc.)
2. Have proper loading states (CircularProgressIndicator)
3. Have error handling (Snackbar for errors)
4. Connect to existing ViewModels/use cases you already built
5. Use Hilt dependency injection

### Task 2.3: Create ViewModels

Create ViewModels for each screen that connect to your existing use cases.

**Example**: `app/src/main/java/app/neurothrive/safehaven/ui/viewmodels/SilentCameraViewModel.kt`

```kotlin
@HiltViewModel
class SilentCameraViewModel @Inject constructor(
    private val silentCameraManager: SilentCameraManager,
    private val evidenceItemDao: EvidenceItemDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Ready)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun capturePhoto(userId: String, incidentId: String?) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Capturing

            val result = silentCameraManager.capturePhotoSilently(userId, incidentId)

            result.fold(
                onSuccess = { evidenceItem ->
                    evidenceItemDao.insert(evidenceItem)
                    _uiState.value = CameraUiState.Success(evidenceItem)
                },
                onFailure = { error ->
                    _uiState.value = CameraUiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }
}

sealed class CameraUiState {
    object Ready : CameraUiState()
    object Capturing : CameraUiState()
    data class Success(val evidenceItem: EvidenceItem) : CameraUiState()
    data class Error(val message: String) : CameraUiState()
}
```

Create similar ViewModels for:
- IncidentReportViewModel
- EvidenceGalleryViewModel
- DocumentVerificationViewModel
- ResourceSearchViewModel
- SurvivorProfileViewModel
- SettingsViewModel

---

## PRIORITY 3: Import Legal Resources Data (4-8 hours)

You built the LegalResource entity and IntersectionalResourceMatcher algorithm, but **the database is empty**. Import 1,000+ resources now.

### Task 3.1: Create CSV with 1,000+ Resources

#### File: `data/legal_resources.csv`

Create CSV with these columns (26 intersectional filters):
```csv
resourceId,organizationName,resourceType,description,phone,email,website,address,latitude,longitude,servesLGBTQIA,lgbtqSpecialized,transInclusive,nonBinaryInclusive,servesBIPOC,bipocLed,servesUndocumented,uVisaSupport,vawaSupport,noICEContact,servesMaleIdentifying,servesDisabled,wheelchairAccessible,servesDeaf,aslInterpreter,isFree,slidingScale,is24_7,languagesSupported
1,National Domestic Violence Hotline,hotline,"24/7 crisis hotline for DV survivors",800-799-7233,info@thehotline.org,https://www.thehotline.org,"",40.7128,-74.0060,true,false,true,true,true,false,true,false,false,true,true,true,false,true,false,true,false,true,"English;Spanish;ASL"
2,The Trevor Project,hotline,"24/7 LGBTQ+ youth crisis hotline",866-488-7386,help@thetrevorproject.org,https://www.thetrevorproject.org,"PO Box 69232 West Hollywood CA 90069",34.0900,-118.3617,true,true,true,true,true,false,false,false,false,true,false,false,false,false,false,true,false,true,"English;Spanish"
3,RAINN National Sexual Assault Hotline,hotline,"24/7 sexual assault support",800-656-4673,info@rainn.org,https://www.rainn.org,"1220 L St NW Suite 505 Washington DC 20005",38.9072,-77.0369,true,false,true,true,true,false,true,false,false,true,true,true,false,true,false,true,false,true,"English;Spanish"
...
```

**Data Sources to Scrape**:
1. National DV Hotline database (5,000+ shelters)
2. LGBT National Help Center (500+ LGBTQ+ resources)
3. Immigrant Legal Resource Center (1,000+ immigration attorneys)
4. National Coalition Against DV (2,000+ state coalitions)
5. Deaf Abused Women's Network (50+ deaf-accessible)

**Minimum 1,000 resources** with accurate intersectional filters.

### Task 3.2: Create Import Script

#### File: `app/src/main/java/app/neurothrive/safehaven/data/import/ResourceImporter.kt`
```kotlin
package app.neurothrive.safehaven.data.import

import android.content.Context
import app.neurothrive.safehaven.data.database.dao.LegalResourceDao
import app.neurothrive.safehaven.data.database.entities.LegalResource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

class ResourceImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val resourceDao: LegalResourceDao
) {
    suspend fun importFromCSV(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.assets.open("legal_resources.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))

            val resources = mutableListOf<LegalResource>()
            reader.readLine() // Skip header

            reader.forEachLine { line ->
                val fields = line.split(",")

                val resource = LegalResource(
                    resourceId = fields[0],
                    organizationName = fields[1],
                    resourceType = fields[2],
                    description = fields[3],
                    phone = fields[4].ifBlank { null },
                    email = fields[5].ifBlank { null },
                    website = fields[6].ifBlank { null },
                    address = fields[7].ifBlank { null },
                    latitude = fields[8].toDoubleOrNull(),
                    longitude = fields[9].toDoubleOrNull(),
                    servesLGBTQIA = fields[10].toBoolean(),
                    lgbtqSpecialized = fields[11].toBoolean(),
                    transInclusive = fields[12].toBoolean(),
                    nonBinaryInclusive = fields[13].toBoolean(),
                    servesBIPOC = fields[14].toBoolean(),
                    bipocLed = fields[15].toBoolean(),
                    servesUndocumented = fields[16].toBoolean(),
                    uVisaSupport = fields[17].toBoolean(),
                    vawaSupport = fields[18].toBoolean(),
                    noICEContact = fields[19].toBoolean(),
                    servesMaleIdentifying = fields[20].toBoolean(),
                    servesDisabled = fields[21].toBoolean(),
                    wheelchairAccessible = fields[22].toBoolean(),
                    servesDeaf = fields[23].toBoolean(),
                    aslInterpreter = fields[24].toBoolean(),
                    isFree = fields[25].toBoolean(),
                    slidingScale = fields[26].toBoolean(),
                    is24_7 = fields[27].toBoolean(),
                    languagesSupported = fields[28].split(";")
                )

                resources.add(resource)
            }

            resourceDao.insertAll(resources)
            reader.close()

            Result.success(resources.size)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Task 3.3: Run Import on First Launch

Update `MainActivity.onCreate()` to import resources on first run:

```kotlin
viewModel.checkFirstRun { isFirstRun ->
    if (isFirstRun) {
        viewModel.importResources()
    }
}
```

---

## PRIORITY 4: Write Tests (8-12 hours)

You wrote zero tests. Write comprehensive test suite now.

### Task 4.1: Unit Tests for Encryption

#### File: `app/src/test/java/app/neurothrive/safehaven/util/crypto/SafeHavenCryptoTest.kt`
```kotlin
package app.neurothrive.safehaven.util.crypto

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class SafeHavenCryptoTest {

    @Before
    fun setup() {
        // Initialize crypto (mock Android KeyStore for testing)
    }

    @Test
    fun `encrypt then decrypt returns original text`() {
        val plaintext = "Sensitive incident description"
        val encrypted = SafeHavenCrypto.encrypt(plaintext)
        val decrypted = SafeHavenCrypto.decrypt(encrypted)

        assertEquals(plaintext, decrypted)
        assertNotEquals(plaintext, encrypted)
    }

    @Test
    fun `encrypt empty string returns empty string`() {
        val encrypted = SafeHavenCrypto.encrypt("")
        assertEquals("", encrypted)
    }

    @Test
    fun `encrypt same plaintext twice produces different ciphertext`() {
        val plaintext = "Test data"
        val encrypted1 = SafeHavenCrypto.encrypt(plaintext)
        val encrypted2 = SafeHavenCrypto.encrypt(plaintext)

        assertNotEquals(encrypted1, encrypted2) // Different IVs
        assertEquals(plaintext, SafeHavenCrypto.decrypt(encrypted1))
        assertEquals(plaintext, SafeHavenCrypto.decrypt(encrypted2))
    }

    @Test
    fun `generateSHA256 produces 64 character hex string`() {
        val file = File.createTempFile("test", ".txt")
        file.writeText("Test content")

        val hash = SafeHavenCrypto.generateSHA256(file)

        assertEquals(64, hash.length)
        assertTrue(hash.matches(Regex("[0-9a-f]{64}")))

        file.delete()
    }

    @Test
    fun `generateSHA256 same file produces same hash`() {
        val file = File.createTempFile("test", ".txt")
        file.writeText("Test content")

        val hash1 = SafeHavenCrypto.generateSHA256(file)
        val hash2 = SafeHavenCrypto.generateSHA256(file)

        assertEquals(hash1, hash2)

        file.delete()
    }
}
```

### Task 4.2: Unit Tests for Intersectional Resource Matching

#### File: `app/src/test/java/app/neurothrive/safehaven/domain/usecases/IntersectionalResourceMatcherTest.kt`
```kotlin
package app.neurothrive.safehaven.domain.usecases

import org.junit.Test
import org.junit.Assert.*

class IntersectionalResourceMatcherTest {

    @Test
    fun `trans survivor gets 30 point bonus for trans-inclusive resource`() {
        val profile = SurvivorProfile(
            profileId = "test",
            userId = "user1",
            isTrans = true,
            isLGBTQIA = true,
            // ... other fields
        )

        val resource = LegalResource(
            resourceId = "res1",
            organizationName = "Trans-Inclusive Shelter",
            resourceType = "shelter",
            transInclusive = true,
            servesLGBTQIA = true,
            // ... other fields
        )

        val matcher = IntersectionalResourceMatcher(mockDao)
        val score = matcher.calculateScore(resource, profile, null, null)

        assertTrue(score >= 60.0) // Base 10 + Trans 30 + LGBTQ 20
    }

    @Test
    fun `undocumented survivor prioritizes no ICE contact resources`() {
        val profile = SurvivorProfile(
            profileId = "test",
            userId = "user1",
            isUndocumented = true,
            // ... other fields
        )

        val resourceWithICE = LegalResource(
            resourceId = "res1",
            organizationName = "Legal Aid",
            resourceType = "legal_aid",
            servesUndocumented = true,
            noICEContact = false,
            // ... other fields
        )

        val resourceNoICE = LegalResource(
            resourceId = "res2",
            organizationName = "Immigrant Support",
            resourceType = "legal_aid",
            servesUndocumented = true,
            noICEContact = true,
            uVisaSupport = true,
            // ... other fields
        )

        val matcher = IntersectionalResourceMatcher(mockDao)
        val score1 = matcher.calculateScore(resourceWithICE, profile, null, null)
        val score2 = matcher.calculateScore(resourceNoICE, profile, null, null)

        assertTrue(score2 > score1) // No ICE gets +10 pts
    }

    @Test
    fun `distance under 5 miles gets 10 point bonus`() {
        val profile = SurvivorProfile(profileId = "test", userId = "user1")

        val nearbyResource = LegalResource(
            resourceId = "res1",
            organizationName = "Local Shelter",
            resourceType = "shelter",
            latitude = 40.7128,
            longitude = -74.0060,
            // ... other fields
        )

        val matcher = IntersectionalResourceMatcher(mockDao)

        // Current location: 40.7100, -74.0050 (< 5 miles)
        val score = matcher.calculateScore(nearbyResource, profile, 40.7100, -74.0050)

        assertTrue(score >= 20.0) // Base 10 + Distance 10
    }
}
```

### Task 4.3: Integration Tests

Write integration tests for:
- SilentCameraManager (capture → encrypt → save to DB)
- DocumentVerificationService (generate PDF → hash → save)
- PanicDeleteUseCase (delete all evidence files + DB records)

**Minimum 50% code coverage required.**

---

## OPTIONAL: Blockchain Deployment (LOW PRIORITY)

**Status**: You have mock blockchain hashes, which is **acceptable for MVP**.

If you want to deploy real blockchain timestamping:

1. Deploy Solidity smart contract to Polygon Mumbai testnet
2. Integrate Web3j in Android
3. Update `DocumentVerificationService.kt` to call contract

**This can be deferred to post-MVP.**

---

## Testing Strategy

After building all components:

### Test 1: Salesforce Sync
1. Create incident report on Android
2. Verify sync to Salesforce
3. Create resource on Salesforce
4. Verify download to Android

### Test 2: Silent Camera
1. Capture photo with silent camera
2. Verify no shutter sound
3. Verify GPS stripped from EXIF
4. Verify file encrypted
5. Verify temp file deleted

### Test 3: Panic Delete
1. Create evidence (photos, incidents)
2. Trigger shake detector
3. Verify all files deleted
4. Verify database cleared

### Test 4: Resource Matching
1. Set survivor profile (trans, undocumented)
2. Search for shelters
3. Verify trans-inclusive resources ranked first
4. Verify no-ICE resources prioritized

---

## Deliverables Checklist

When you submit the completed build, ensure:

- [ ] All 6 Salesforce custom objects deployed
- [ ] All 4 Apex REST APIs deployed with tests (75% coverage)
- [ ] Connected App configured for Android OAuth
- [ ] All 12 Jetpack Compose screens implemented
- [ ] Navigation working between all screens
- [ ] Material Design 3 theme applied
- [ ] All ViewModels connect to existing use cases
- [ ] 1,000+ legal resources imported from CSV
- [ ] Resource matching algorithm tested with real data
- [ ] Unit tests written (50%+ code coverage)
- [ ] Integration tests for camera, panic delete, document verification
- [ ] End-to-end test of Salesforce sync
- [ ] README.md updated with setup instructions
- [ ] DEPLOYMENT_GUIDE.md created for Salesforce deployment

---

## Timeline Estimate

- Priority 1 (Salesforce Backend): **8-12 hours**
- Priority 2 (Android UI): **16-24 hours**
- Priority 3 (Resource Data): **4-8 hours**
- Priority 4 (Tests): **8-12 hours**

**Total: 36-56 hours** (1.5-2.5 sprint cycles)

---

## Important Notes

1. **DO NOT modify your existing backend code** - It's perfect. Only add UI layer on top.

2. **Use dependency injection (Hilt)** - All your use cases are already injectable.

3. **Follow Material Design 3 guidelines** - Use Card, Button, TextField, TopAppBar, etc.

4. **Test on physical device** - Silent camera requires real hardware.

5. **Salesforce Platform Shield** - Required for field-level encryption. Ensure user's org has it enabled.

6. **Keep survivor safety first** - Every feature should protect survivors from abusers.

---

## Questions?

If you encounter blockers:

1. Check `CLAUDE_CODE_BUILD_REVIEW.md` for detailed analysis of what you already built
2. Reference your existing code in `safehaven-core/` - it's production-quality
3. Follow Android best practices (MVVM, Jetpack Compose, Hilt)

**Your previous backend work is excellent. Now finish the UI and Salesforce integration to make this app usable.**

---

**Generated**: 2025-11-17
**For**: Claude Code GitHub Agent
**User**: abbyluggery
**Repository**: https://github.com/abbyluggery/SafeHaven-Build
**Previous Branch**: claude/safehaven-android-app-013udX3wnRYCxzmvCZP4mL97
