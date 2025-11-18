#!/usr/bin/env python3
"""
Create simplified CSV for Salesforce import using only Name field
"""

import csv
from pathlib import Path

# Read the full CSV
android_csv = Path('app/src/main/assets/legal_resources.csv')
simple_csv = Path('salesforce/data/Legal_Resource__c_simple.csv')

print(f"Reading {android_csv}...")
with open(android_csv, 'r', encoding='utf-8') as f:
    reader = csv.DictReader(f)
    rows = list(reader)

print(f"Found {len(rows)} resources")

# Create simple CSV with just Name (Organization Name)
simple_rows = []
for row in rows:
    simple_rows.append({
        'Name': row['organizationName']
    })

# Write simple CSV
print(f"Writing {simple_csv}...")
with open(simple_csv, 'w', encoding='utf-8', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=['Name'])
    writer.writeheader()
    writer.writerows(simple_rows)

print(f"Success! Created {simple_csv}")
print(f"{len(simple_rows)} resources ready")
