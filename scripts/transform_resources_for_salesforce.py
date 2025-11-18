#!/usr/bin/env python3
"""
Transform Android CSV to Salesforce-compatible format for Legal_Resource__c import
"""

import csv
import json
from pathlib import Path

# Field mapping: Android CSV → Salesforce field name
FIELD_MAPPING = {
    'id': 'External_ID__c',
    'resourceType': 'Resource_Type__c',
    'organizationName': 'Organization_Name__c',
    'phone': 'Phone__c',
    'website': 'Website__c',
    'email': 'Email__c',
    'address': 'Address__c',
    'city': 'City__c',
    'state': 'State__c',
    'zipCode': 'Zip_Code__c',
    'latitude': 'Latitude__c',
    'longitude': 'Longitude__c',
    'is24_7': 'Is_24_7__c',
    'servesLGBTQIA': 'Serves_LGBTQIA__c',
    'lgbtqSpecialized': 'LGBTQ_Specialized__c',
    'transInclusive': 'Trans_Inclusive__c',
    'nonBinaryInclusive': 'Non_Binary_Inclusive__c',
    'servesBIPOC': 'Serves_BIPOC__c',
    'bipocLed': 'BIPOC_Led__c',
    'servesMaleIdentifying': 'Serves_Male_Identifying__c',
    'servesUndocumented': 'Serves_Undocumented__c',
    'uVisaSupport': 'U_Visa_Support__c',
    'vawaSupport': 'VAWA_Support__c',
    'noICEContact': 'No_ICE_Contact__c',
    'servesDisabled': 'Serves_Disabled__c',
    'wheelchairAccessible': 'Wheelchair_Accessible__c',
    'servesDeaf': 'Serves_Deaf__c',
    'aslInterpreter': 'ASL_Interpreter__c',
    'isFree': 'Is_Free__c',
    'slidingScale': 'Sliding_Scale__c',
}

def parse_json_field(value):
    """Parse JSON string fields"""
    if not value or value == 'N/A' or value == '[]':
        return ''
    try:
        parsed = json.loads(value)
        if isinstance(parsed, list):
            return ';'.join(parsed)
        return str(parsed)
    except:
        return value

def transform_boolean(value):
    """Transform boolean values for Salesforce"""
    if value in ['1', 'true', 'True', 'TRUE']:
        return 'TRUE'
    elif value in ['0', 'false', 'False', 'FALSE', '']:
        return 'FALSE'
    return 'FALSE'

def transform_resource_type(value):
    """Map resource types to Salesforce picklist values"""
    type_mapping = {
        'shelter': 'Shelter',
        'legal_aid': 'Legal Aid',
        'hotline': 'Hotline',
        'therapy': 'Therapy',
        'financial_assistance': 'Financial Assistance',
        'immigration_support': 'Immigration Support',
        'housing_assistance': 'Housing Assistance',
    }
    return type_mapping.get(value, value.title())

def transform_row(row):
    """Transform a single row from Android format to Salesforce format"""
    sf_row = {}

    for android_field, sf_field in FIELD_MAPPING.items():
        value = row.get(android_field, '')

        # Special handling for specific fields
        if android_field == 'resourceType':
            sf_row[sf_field] = transform_resource_type(value)
        elif sf_field.endswith('__c') and android_field.startswith('serves') or android_field.startswith('is') or android_field.endswith('Inclusive') or android_field.endswith('Specialized') or android_field.endswith('Support') or android_field.endswith('Accessible') or android_field.endswith('Scale'):
            # Boolean fields
            sf_row[sf_field] = transform_boolean(value)
        else:
            sf_row[sf_field] = value if value and value != 'N/A' else ''

    # Handle JSON fields
    if 'servicesJson' in row:
        sf_row['Services__c'] = parse_json_field(row['servicesJson'])

    if 'languagesJson' in row:
        sf_row['Languages_Supported__c'] = parse_json_field(row['languagesJson'])

    if 'culturallySpecificJson' in row:
        sf_row['Culturally_Specific__c'] = parse_json_field(row['culturallySpecificJson'])

    # Add description (combine services if not provided)
    if 'Description__c' not in sf_row or not sf_row.get('Description__c'):
        services = parse_json_field(row.get('servicesJson', ''))
        if services:
            sf_row['Description__c'] = f"Services: {services}"

    return sf_row

def main():
    # File paths
    android_csv = Path('app/src/main/assets/legal_resources.csv')
    salesforce_csv = Path('salesforce/data/Legal_Resource__c.csv')

    # Create output directory
    salesforce_csv.parent.mkdir(parents=True, exist_ok=True)

    # Read Android CSV
    print(f"Reading {android_csv}...")
    with open(android_csv, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        android_rows = list(reader)

    print(f"Found {len(android_rows)} resources")

    # Transform rows
    print("Transforming to Salesforce format...")
    sf_rows = [transform_row(row) for row in android_rows]

    # Write Salesforce CSV
    if sf_rows:
        fieldnames = list(sf_rows[0].keys())
        print(f"Writing {salesforce_csv}...")
        with open(salesforce_csv, 'w', encoding='utf-8', newline='') as f:
            writer = csv.DictWriter(f, fieldnames=fieldnames)
            writer.writeheader()
            writer.writerows(sf_rows)

        print(f"✅ Success! Created {salesforce_csv}")
        print(f"   {len(sf_rows)} resources ready for Salesforce import")
        print(f"   {len(fieldnames)} fields mapped")
    else:
        print("❌ Error: No rows transformed")

if __name__ == '__main__':
    main()
