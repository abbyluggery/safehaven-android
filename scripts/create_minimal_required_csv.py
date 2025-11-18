#!/usr/bin/env python3
"""
Create CSV with only required fields for Salesforce Legal_Resource__c import
"""
import csv
from pathlib import Path
from datetime import datetime

# Paths
android_csv = Path('app/src/main/assets/legal_resources.csv')
output_csv = Path('salesforce/data/Legal_Resource__c_required.csv')

print(f"Reading {android_csv}...")
with open(android_csv, 'r', encoding='utf-8') as f:
    reader = csv.DictReader(f)
    rows = list(reader)

print(f"Found {len(rows)} resources")

# Get current timestamp in Salesforce format (ISO 8601)
current_timestamp = datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%S.000Z')

# Required fields according to error message
required_rows = []
for row in rows:
    required_rows.append({
        'Name': row['organizationName'],
        'Organization_Name__c': row['organizationName'],
        'Resource_Type__c': row['resourceType'],
        'City__c': row['city'],
        'State__c': row['state'],
        'Latitude__c': row['latitude'],
        'Longitude__c': row['longitude'],
        'Created_Timestamp__c': current_timestamp,
        'Last_Modified_Timestamp__c': current_timestamp
    })

# Write CSV with required fields
print(f"Writing {output_csv}...")
fieldnames = [
    'Name',
    'Organization_Name__c',
    'Resource_Type__c',
    'City__c',
    'State__c',
    'Latitude__c',
    'Longitude__c',
    'Created_Timestamp__c',
    'Last_Modified_Timestamp__c'
]

with open(output_csv, 'w', encoding='utf-8', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=fieldnames)
    writer.writeheader()
    writer.writerows(required_rows)

print(f"Success! Created {output_csv}")
print(f"{len(required_rows)} resources ready with required fields")
print(f"\nRequired fields included:")
for field in fieldnames:
    print(f"  - {field}")
