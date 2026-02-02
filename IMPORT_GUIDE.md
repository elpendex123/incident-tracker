# Data Import Guide - Incident Tracker

This guide explains how to import 100+ test incidents from CSV into the PostgreSQL database.

---

## Table of Contents

1. [Overview](#overview)
2. [Files Included](#files-included)
3. [Import Methods](#import-methods)
4. [Prerequisites](#prerequisites)
5. [Detailed Instructions](#detailed-instructions)
6. [CSV Format](#csv-format)
7. [Troubleshooting](#troubleshooting)
8. [Verifying Import](#verifying-import)
9. [Post-Import Testing](#post-import-testing)

---

## Overview

The import process adds 100+ realistic test incidents to your database, allowing you to:
- Test filtering and search functionality
- Verify sorting and pagination
- Test dashboard and reporting features
- Load test the application
- Demonstrate various priority and status combinations

### What Gets Imported

- **Total Records**: 100+ incidents
- **Priority Distribution**:
  - CRITICAL: ~20 incidents
  - HIGH: ~25 incidents
  - MEDIUM: ~35 incidents
  - LOW: ~20 incidents
- **Status Distribution**:
  - OPEN: ~60 incidents
  - IN_PROGRESS: ~20 incidents
  - RESOLVED: ~15 incidents
  - CLOSED: ~5 incidents
- **Assignees**: Alice, Bob, Charlie, David, Eve, Frank

---

## Files Included

### 1. CSV Data File
**Location**: `data/incidents_import.csv`

```
├── data/
│   └── incidents_import.csv (100+ incident records)
```

**Format**: CSV with headers
- `title` - Incident title
- `description` - Detailed description
- `priority` - CRITICAL, HIGH, MEDIUM, LOW
- `status` - OPEN, IN_PROGRESS, RESOLVED, CLOSED
- `assignee` - Person responsible (optional)

### 2. Bash Import Script
**Location**: `scripts/import_incidents.sh`

```bash
# Features:
- Validates CSV file
- Creates automatic backup
- Batch imports data
- Displays statistics
- Provides colorized output
```

**Usage**:
```bash
./scripts/import_incidents.sh [OPTIONS]
```

### 3. Python Import Script
**Location**: `scripts/import_incidents.py`

```python
# Features:
- Cross-platform compatibility
- Detailed error handling
- Progress indication
- Statistics and reporting
- Flexible batch sizing
```

**Usage**:
```bash
python3 scripts/import_incidents.py [OPTIONS]
```

---

## Import Methods

You have **3 options** to import the data:

### Option 1: Bash Script (Recommended for Linux/Mac)
```bash
cd /home/enrique/CLAUDE/springboot_python_postgres_project
./scripts/import_incidents.sh
```

**Advantages**:
- No dependencies (uses psql)
- Fastest execution
- Full bash integration

**Disadvantages**:
- Requires PostgreSQL client tools
- Linux/Mac only

---

### Option 2: Python Script (Recommended for Flexibility)
```bash
cd /home/enrique/CLAUDE/springboot_python_postgres_project
python3 scripts/import_incidents.py
```

**Advantages**:
- Cross-platform (Windows, Mac, Linux)
- Better error handling
- More control over batch processing
- Beautiful colored output

**Disadvantages**:
- Requires Python 3 and psycopg2
- Slightly slower than bash

---

### Option 3: Direct psql Command
```bash
psql -h localhost -U postgres -d incidents < data/import_raw.sql
```

**Advantages**:
- No scripts needed
- Direct database connection

**Disadvantages**:
- Manual SQL generation
- No progress indication

---

## Prerequisites

### Required
- ✅ PostgreSQL 14+ installed and running
- ✅ Database `incidents` created
- ✅ Table `incidents` exists (auto-created by Spring Boot)
- ✅ `postgres` user with password `postgres`

### For Bash Script
```bash
# Check psql is installed
psql --version
```

### For Python Script
```bash
# Install psycopg2
pip3 install psycopg2-binary

# Or via requirements.txt in project
pip3 install -r requirements.txt
```

### Verify Connection
```bash
# Test database connection
psql -h localhost -U postgres -d incidents -c "SELECT COUNT(*) FROM incidents;"
```

---

## Detailed Instructions

### Method 1: Using Bash Script (Step-by-Step)

#### Step 1: Navigate to Project Directory
```bash
cd /home/enrique/CLAUDE/springboot_python_postgres_project
```

#### Step 2: Run Import Script
```bash
./scripts/import_incidents.sh
```

**Expected Output**:
```
==================================================
Incident Tracker - CSV Import Script
==================================================
ℹ CSV File: /path/to/incidents_import.csv
ℹ Database: incidents
ℹ Host: localhost:5432
ℹ User: postgres
ℹ Total records to import: 100
✓ Database connection successful
✓ Backup created: incidents_backup_20260201_201234.sql
✓ SQL script generated
✓ Data import completed successfully

✓ Import Statistics:
   Total incidents: 100
   OPEN status: 60
   CRITICAL priority: 20

ℹ Distribution by Priority:
 priority | count
----------+-------
 CRITICAL |    20
 HIGH     |    25
 MEDIUM   |    35
 LOW      |    20
(4 rows)

...
```

---

### Method 2: Using Python Script (Step-by-Step)

#### Step 1: Navigate to Project Directory
```bash
cd /home/enrique/CLAUDE/springboot_python_postgres_project
```

#### Step 2: Install Dependencies (if needed)
```bash
pip3 install psycopg2-binary
# Or: pip3 install -r scripts/requirements.txt
```

#### Step 3: Run Import Script
```bash
python3 scripts/import_incidents.py
```

**Expected Output**:
```
==================================================
Incident Tracker - CSV Import Script
==================================================
ℹ CSV File: /path/to/incidents_import.csv
ℹ Database: incidents
ℹ Host: localhost:5432
ℹ User: postgres
✓ Read 100 records from CSV
✓ Database connection successful
ℹ Creating backup of current data...
✓ Backup created: incidents_backup_20260201_201234.sql
ℹ Starting import...
ℹ Inserting 100 records in batches of 100...
  Progress: 100/100 (100%)
✓ Inserted 100 records

ℹ Gathering statistics...

✓ Import Statistics:
   Total incidents: 100

ℹ Distribution by Priority:
   CRITICAL   :   20 ( 20.0%)
   HIGH       :   25 ( 25.0%)
   MEDIUM     :   35 ( 35.0%)
   LOW        :   20 ( 20.0%)

ℹ Distribution by Status:
   OPEN           :   60 ( 60.0%)
   IN_PROGRESS    :   20 ( 20.0%)
   RESOLVED       :   15 ( 15.0%)
   CLOSED         :    5 (  5.0%)

ℹ Top 10 Assignees:
   Alice          :   18 ( 18.0%)
   Bob            :   16 ( 16.0%)
   Charlie        :   15 ( 15.0%)
   David          :   15 ( 15.0%)
   Eve            :   15 ( 15.0%)
   Frank          :   6  (  6.0%)

==================================================
Import Completed Successfully
==================================================
✓ All 100 incidents imported
ℹ Backup file: incidents_backup_20260201_201234.sql (keep safe)

Next steps:
  1. Test REST API: curl http://localhost:8081/api/incidents
  2. Test GraphQL: Visit http://localhost:8081/graphiql
  3. View Swagger: Visit http://localhost:8081/swagger-ui.html
```

---

### Custom Import Options

#### Using Bash Script with Custom Database
```bash
./scripts/import_incidents.sh \
  --host localhost \
  --port 5432 \
  --database incidents \
  --user postgres \
  --password postgres
```

#### Using Python Script with Custom Options
```bash
python3 scripts/import_incidents.py \
  --host localhost \
  --port 5432 \
  --database incidents \
  --user postgres \
  --password postgres \
  --batch-size 50
```

#### Show Help
```bash
# Bash script
./scripts/import_incidents.sh --help

# Python script
python3 scripts/import_incidents.py --help
```

---

## CSV Format

### Header Row
```
title,description,priority,status,assignee
```

### Column Details

#### title (Required)
- Type: String (max 200 characters)
- Example: "Database Connection Timeout"
- Cannot be empty

#### description (Optional)
- Type: String (max 2000 characters)
- Example: "Connection to production database is timing out frequently"
- Can be empty

#### priority (Required)
- Type: Enum
- Valid values: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- Default if missing: `LOW`
- Example: "CRITICAL"

#### status (Required)
- Type: Enum
- Valid values: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`
- Default if missing: `OPEN`
- Example: "IN_PROGRESS"

#### assignee (Optional)
- Type: String (max 100 characters)
- Example: "Alice", "Bob", "team@company.com"
- Can be empty (NULL in database)

### Sample CSV Rows
```csv
Database Connection Timeout,Connection to production database is timing out frequently,CRITICAL,OPEN,Alice
API Gateway Down,Main API gateway returning 503 errors,CRITICAL,OPEN,Bob
Memory Leak in Service,Service consuming too much memory and causing crashes,CRITICAL,IN_PROGRESS,Charlie
Server CPU Usage High,Production server CPU at 95% consistently,CRITICAL,OPEN,Alice
```

### Creating Your Own CSV

```bash
# Create custom CSV file
cat > custom_incidents.csv << EOF
title,description,priority,status,assignee
Your Issue 1,Description here,HIGH,OPEN,Alice
Your Issue 2,Another description,MEDIUM,IN_PROGRESS,Bob
EOF

# Import custom file
python3 scripts/import_incidents.py -f custom_incidents.csv
```

---

## Troubleshooting

### Issue: "Connection refused"

**Cause**: PostgreSQL not running or wrong host/port

**Solution**:
```bash
# Check if PostgreSQL is running
sudo systemctl status postgresql

# Start PostgreSQL if stopped
sudo systemctl start postgresql

# Verify connection
psql -h localhost -U postgres -c "SELECT 1"
```

---

### Issue: "Database does not exist"

**Cause**: Database `incidents` not created yet

**Solution**:
```bash
# Create database
sudo -u postgres psql -c "CREATE DATABASE incidents;"

# Or via application startup (it creates automatically)
java -Dspring.datasource.password=postgres -jar target/incident-tracker-1.0.0.jar
```

---

### Issue: "Table does not exist"

**Cause**: `incidents` table not created

**Solution**:
```bash
# Start the Spring Boot application to auto-create schema
java -Dspring.datasource.password=postgres -jar target/incident-tracker-1.0.0.jar

# Wait for startup message showing "Started IncidentTrackerApplication"
```

---

### Issue: "Authentication failed for user postgres"

**Cause**: Wrong password or authentication method

**Solution**:
```bash
# Reset PostgreSQL password
sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'postgres';"

# Or use a different password
./scripts/import_incidents.sh -P your_actual_password
```

---

### Issue: "psycopg2 module not found"

**Cause**: Python psycopg2 not installed

**Solution**:
```bash
# Install psycopg2
pip3 install psycopg2-binary

# Or install from requirements
pip3 install -r scripts/requirements.txt
```

---

### Issue: "Permission denied when running script"

**Cause**: Script not executable

**Solution**:
```bash
# Make scripts executable
chmod +x scripts/import_incidents.sh
chmod +x scripts/import_incidents.py

# Run again
./scripts/import_incidents.sh
```

---

### Issue: "File not found" or "CSV file not found"

**Cause**: CSV file path incorrect

**Solution**:
```bash
# Check file exists
ls -la data/incidents_import.csv

# Run from correct directory
cd /home/enrique/CLAUDE/springboot_python_postgres_project
./scripts/import_incidents.sh

# Or specify full path
./scripts/import_incidents.sh -f /full/path/to/incidents_import.csv
```

---

## Verifying Import

### Method 1: Using psql

```bash
# Connect to database
psql -h localhost -U postgres -d incidents

# Inside psql, run queries:

-- Count all records
SELECT COUNT(*) FROM incidents;

-- Count by priority
SELECT priority, COUNT(*) FROM incidents GROUP BY priority;

-- Count by status
SELECT status, COUNT(*) FROM incidents GROUP BY status;

-- View sample records
SELECT id, title, priority, status, assignee FROM incidents LIMIT 5;

-- View oldest incidents
SELECT id, title, created_at FROM incidents ORDER BY created_at ASC LIMIT 5;
```

### Method 2: Using REST API

```bash
# Get count of all incidents
curl http://localhost:8081/api/incidents | wc -l

# Get first 5 incidents
curl "http://localhost:8081/api/incidents?limit=5" | json_pp

# Filter by priority
curl "http://localhost:8081/api/incidents?priority=CRITICAL" | json_pp

# Filter by status
curl "http://localhost:8081/api/incidents?status=OPEN" | json_pp
```

### Method 3: Using GraphQL

```bash
# Query via GraphiQL
# Visit: http://localhost:8081/graphiql

# Run query:
{
  incidents {
    id
    title
    priority
    status
    assignee
  }
}
```

### Method 4: Bash Script Count

```bash
# Count records in CSV
wc -l data/incidents_import.csv

# Should show: 101 (100 data rows + 1 header row)
```

---

## Post-Import Testing

### 1. REST API Testing

```bash
# Get all incidents (100 total)
curl http://localhost:8081/api/incidents | python -m json.tool

# Filter by priority
curl http://localhost:8081/api/incidents?priority=CRITICAL

# Filter by status
curl http://localhost:8081/api/incidents?status=OPEN

# Get specific incident
curl http://localhost:8081/api/incidents/1

# Count CRITICAL incidents
curl http://localhost:8081/api/incidents?priority=CRITICAL | \
  python -c "import sys, json; data=json.load(sys.stdin); print(f'Total: {len(data)}')"
```

### 2. GraphQL Testing

```bash
# Visit GraphiQL: http://localhost:8081/graphiql

# Query all incidents
query {
  incidents {
    id
    title
    priority
    status
    assignee
  }
}

# Filter by status
query {
  incidentsByStatus(status: OPEN) {
    id
    title
    priority
    assignee
  }
}

# Filter by priority
query {
  incidentsByPriority(priority: CRITICAL) {
    id
    title
    status
    assignee
  }
}

# Filter by assignee
query {
  incidentsByAssignee(assignee: "Alice") {
    id
    title
    priority
    status
  }
}
```

### 3. Database Testing

```bash
# Run dashboard queries from DATABASE.md
psql -h localhost -U postgres -d incidents

-- Dashboard Summary
SELECT
    COUNT(*) as total_incidents,
    SUM(CASE WHEN status = 'OPEN' THEN 1 ELSE 0 END) as open,
    SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress,
    SUM(CASE WHEN status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved
FROM incidents;

-- Incidents by team member
SELECT assignee, COUNT(*) as count
FROM incidents
WHERE assignee IS NOT NULL
GROUP BY assignee
ORDER BY count DESC;
```

### 4. Swagger UI Testing

1. Visit: `http://localhost:8081/swagger-ui.html`
2. Click "Try it out" on `/api/incidents` GET endpoint
3. Click "Execute"
4. Should see 100+ incidents in response

---

## Backup & Recovery

### Viewing Backups

```bash
# List backup files created during import
ls -lah incidents_backup_*.sql

# Example output:
-rw-r--r-- 1 user staff 50K Feb  1 20:12 incidents_backup_20260201_201234.sql
```

### Restoring from Backup

```bash
# Restore from backup file
psql -h localhost -U postgres -d incidents < incidents_backup_20260201_201234.sql

# Or via pg_restore if using custom format
pg_restore -h localhost -U postgres -d incidents incidents_backup.dump
```

### Clearing Data Before Re-import

```bash
# Delete all incidents
psql -h localhost -U postgres -d incidents -c "DELETE FROM incidents; ALTER SEQUENCE incidents_id_seq RESTART WITH 1;"

# Then re-import
python3 scripts/import_incidents.py
```

---

## Performance Notes

### Import Time
- **Bash script**: ~2-5 seconds for 100 records
- **Python script**: ~5-10 seconds for 100 records
- **Direct psql**: ~1-2 seconds

### Database Impact
- **Disk Space**: ~100KB per 100 records
- **Index Update**: Minimal impact (table is small)
- **Connection Pool**: No impact (single connection)

### Scaling
- **For 1,000 records**: ~5-10 seconds
- **For 10,000 records**: ~30-50 seconds
- **For 100,000 records**: ~3-5 minutes

---

## Tips & Tricks

### Import Multiple CSV Files

```bash
# Bash: Import multiple files sequentially
for file in data/*.csv; do
    ./scripts/import_incidents.sh -f "$file"
done

# Python: Create loop in script
python3 << 'EOF'
import subprocess
import glob

for csv_file in glob.glob('data/*.csv'):
    print(f"Importing {csv_file}...")
    subprocess.run(['python3', 'scripts/import_incidents.py', '-f', csv_file])
EOF
```

### Export Data as CSV

```bash
# Export all incidents to CSV
psql -h localhost -U postgres -d incidents \
    -c "COPY incidents TO STDOUT WITH CSV HEADER" > export_incidents.csv

# Export specific query results
psql -h localhost -U postgres -d incidents \
    -c "COPY (SELECT * FROM incidents WHERE priority = 'CRITICAL') TO STDOUT WITH CSV HEADER" > critical_incidents.csv
```

### Generate Custom CSV from Database

```python
import psycopg2
import csv

conn = psycopg2.connect(host="localhost", database="incidents", user="postgres", password="postgres")
cursor = conn.cursor()

cursor.execute("SELECT title, description, priority, status, assignee FROM incidents")

with open('export.csv', 'w', newline='') as f:
    writer = csv.writer(f)
    writer.writerow(['title', 'description', 'priority', 'status', 'assignee'])
    writer.writerows(cursor.fetchall())

conn.close()
```

---

## Summary

| Method | Speed | Platform | Complexity |
|--------|-------|----------|-----------|
| Bash Script | Fast | Linux/Mac | Low |
| Python Script | Medium | Any | Low |
| Direct psql | Fastest | Any | Medium |

**Recommended**: Use **Python Script** for best compatibility and features.

---

**Last Updated**: February 1, 2026
**Status**: Ready for use
**Test Data**: 100+ realistic incidents
