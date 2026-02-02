# Database Documentation - Incident Tracker

**Database**: PostgreSQL 14
**Database Name**: `incidents`
**User**: `postgres`
**Password**: `postgres`
**Host**: `localhost`
**Port**: `5432`

---

## Table of Contents

1. [Database Overview](#database-overview)
2. [Entity Relationship Diagram](#entity-relationship-diagram)
3. [Table Structures](#table-structures)
4. [Data Types & Constraints](#data-types--constraints)
5. [CRUD Operations](#crud-operations)
6. [Sample Queries](#sample-queries)
7. [Useful Commands](#useful-commands)
8. [Indexes & Performance](#indexes--performance)
9. [Backup & Recovery](#backup--recovery)

---

## Database Overview

### Architecture

```
┌─────────────────────────────────────────────────────┐
│           PostgreSQL Database: incidents            │
├─────────────────────────────────────────────────────┤
│                                                     │
│    ┌──────────────────────────────────────────┐   │
│    │          Incidents Table                 │   │
│    │  (Primary data storage for incidents)    │   │
│    │                                          │   │
│    │  - 100+ records (test data)              │   │
│    │  - Full CRUD operations                  │   │
│    │  - Automatic timestamps                  │   │
│    │  - Enum validation (Priority, Status)    │   │
│    │                                          │   │
│    └──────────────────────────────────────────┘   │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### Current Tables

| Table Name | Rows | Purpose |
|-----------|------|---------|
| **incidents** | 100+ | Main incident tracking data |

### Database Statistics

```sql
-- Connection Details
SELECT datname as database,
       pg_database.datistemplate as is_template,
       pg_size_pretty(pg_database_size(datname)) as size
FROM pg_database
WHERE datname = 'incidents';

-- Table Size
SELECT schemaname, tablename,
       pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables
WHERE schemaname NOT IN ('pg_catalog', 'information_schema');
```

---

## Entity Relationship Diagram

### Visual ERD

```
┌─────────────────────────────────────────────────────┐
│                   INCIDENTS                         │
├─────────────────────────────────────────────────────┤
│ PK │ id (BIGSERIAL)              [AUTO]             │
├─────────────────────────────────────────────────────┤
│    │ title (VARCHAR 200)         [REQUIRED]         │
│    │ description (VARCHAR 2000)  [OPTIONAL]         │
│    │ priority (VARCHAR 20)       [ENUM]             │
│    │ status (VARCHAR 20)         [ENUM]             │
│    │ assignee (VARCHAR 100)      [OPTIONAL]         │
│    │ created_at (TIMESTAMP)      [AUTO]             │
│    │ updated_at (TIMESTAMP)      [AUTO]             │
│    │ resolved_at (TIMESTAMP)     [NULLABLE]         │
└─────────────────────────────────────────────────────┘

Enums:
├── Priority: LOW, MEDIUM, HIGH, CRITICAL
└── Status: OPEN, IN_PROGRESS, RESOLVED, CLOSED
```

---

## Table Structures

### INCIDENTS Table

Complete schema with all column definitions:

```sql
CREATE TABLE public.incidents (
    id bigserial NOT NULL,
    title varchar(200) NOT NULL,
    description varchar(2000),
    priority varchar(20) NOT NULL DEFAULT 'LOW',
    status varchar(20) NOT NULL DEFAULT 'OPEN',
    assignee varchar(100),
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NOT NULL,
    resolved_at timestamp(6),
    PRIMARY KEY (id),
    CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'))
);
```

#### Column Definitions

| Column | Type | Nullable | Default | Constraint | Purpose |
|--------|------|----------|---------|-----------|---------|
| `id` | BIGSERIAL | NO | Auto | PK | Unique identifier |
| `title` | VARCHAR(200) | NO | - | NOT NULL | Incident title/summary |
| `description` | VARCHAR(2000) | YES | NULL | - | Detailed description |
| `priority` | VARCHAR(20) | NO | 'LOW' | CHECK | Severity level |
| `status` | VARCHAR(20) | NO | 'OPEN' | CHECK | Current state |
| `assignee` | VARCHAR(100) | YES | NULL | - | Person assigned |
| `created_at` | TIMESTAMP(6) | NO | CURRENT | - | Creation timestamp |
| `updated_at` | TIMESTAMP(6) | NO | CURRENT | - | Last update timestamp |
| `resolved_at` | TIMESTAMP(6) | YES | NULL | - | Resolution timestamp |

---

## Data Types & Constraints

### Enum Values

#### Priority Enum
```
LOW      - Low priority, can be handled anytime
MEDIUM   - Medium priority, should be handled soon
HIGH     - High priority, needs quick attention
CRITICAL - Critical priority, requires immediate action
```

#### Status Enum
```
OPEN         - Newly created, not yet assigned
IN_PROGRESS  - Currently being worked on
RESOLVED     - Issue fixed, pending verification
CLOSED       - Verified and closed permanently
```

### Constraints

#### PRIMARY KEY
- Column: `id`
- Type: BIGSERIAL (auto-incrementing)
- Ensures unique identification

#### CHECK Constraints
```sql
-- Priority must be one of these values
CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))

-- Status must be one of these values
CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'))
```

#### NOT NULL Constraints
- `id` - Always required
- `title` - Always required (incident must have title)
- `priority` - Always required (defaults to LOW)
- `status` - Always required (defaults to OPEN)
- `created_at` - Always required (auto-set)
- `updated_at` - Always required (auto-set)

#### NULLABLE Columns
- `description` - Optional detailed information
- `assignee` - Optional person responsible
- `resolved_at` - Only set when status becomes RESOLVED

---

## CRUD Operations

### CREATE (INSERT)

#### Basic Insert
```sql
-- Insert with all fields
INSERT INTO incidents (
    title,
    description,
    priority,
    status,
    assignee,
    created_at,
    updated_at
) VALUES (
    'Database Connection Timeout',
    'Connection to production database is timing out',
    'CRITICAL',
    'OPEN',
    'Alice',
    NOW(),
    NOW()
) RETURNING id, title, priority, status;
```

#### Insert with Defaults
```sql
-- Insert with minimal fields (uses defaults)
INSERT INTO incidents (title, created_at, updated_at)
VALUES ('Server Down', NOW(), NOW())
RETURNING *;
```

#### Bulk Insert
```sql
-- Insert multiple records
INSERT INTO incidents (title, priority, status, assignee, created_at, updated_at)
VALUES
    ('Issue 1', 'HIGH', 'OPEN', 'Bob', NOW(), NOW()),
    ('Issue 2', 'MEDIUM', 'OPEN', 'Alice', NOW(), NOW()),
    ('Issue 3', 'LOW', 'OPEN', 'Charlie', NOW(), NOW())
RETURNING id, title, priority;
```

#### Insert and Return Generated ID
```sql
-- Insert and get the generated ID
INSERT INTO incidents (
    title,
    priority,
    created_at,
    updated_at
) VALUES (
    'New Incident',
    'HIGH',
    NOW(),
    NOW()
) RETURNING id;
-- Returns: id = 101 (or next available)
```

---

### READ (SELECT)

#### Get All Incidents
```sql
-- Simple query
SELECT * FROM incidents;

-- With specific columns
SELECT id, title, priority, status, assignee FROM incidents;

-- With ordering
SELECT * FROM incidents ORDER BY created_at DESC;

-- With limit
SELECT * FROM incidents ORDER BY created_at DESC LIMIT 10;
```

#### Get Single Incident
```sql
-- By ID
SELECT * FROM incidents WHERE id = 1;

-- By title
SELECT * FROM incidents WHERE title ILIKE '%timeout%';
```

#### Filter Operations

##### By Status
```sql
-- Get all OPEN incidents
SELECT * FROM incidents WHERE status = 'OPEN';

-- Get all IN_PROGRESS incidents
SELECT * FROM incidents WHERE status = 'IN_PROGRESS';

-- Get all RESOLVED incidents
SELECT * FROM incidents WHERE status = 'RESOLVED';

-- Get all CLOSED incidents
SELECT * FROM incidents WHERE status = 'CLOSED';
```

##### By Priority
```sql
-- Get all CRITICAL incidents
SELECT * FROM incidents WHERE priority = 'CRITICAL';

-- Get HIGH and CRITICAL incidents
SELECT * FROM incidents WHERE priority IN ('HIGH', 'CRITICAL');

-- Get all non-LOW priority incidents
SELECT * FROM incidents WHERE priority != 'LOW';
```

##### By Assignee
```sql
-- Get incidents assigned to Alice
SELECT * FROM incidents WHERE assignee = 'Alice';

-- Get unassigned incidents
SELECT * FROM incidents WHERE assignee IS NULL;

-- Get incidents assigned to specific people
SELECT * FROM incidents WHERE assignee IN ('Alice', 'Bob', 'Charlie');
```

#### Complex Queries

##### Combined Filters
```sql
-- Get open incidents assigned to Alice
SELECT * FROM incidents
WHERE status = 'OPEN' AND assignee = 'Alice';

-- Get critical incidents that are not resolved
SELECT * FROM incidents
WHERE priority = 'CRITICAL' AND status != 'RESOLVED';

-- Get high priority incidents assigned to anyone
SELECT * FROM incidents
WHERE priority IN ('HIGH', 'CRITICAL') AND assignee IS NOT NULL;
```

##### Date-based Queries
```sql
-- Get incidents created today
SELECT * FROM incidents
WHERE DATE(created_at) = CURRENT_DATE;

-- Get incidents created in last 7 days
SELECT * FROM incidents
WHERE created_at >= NOW() - INTERVAL '7 days';

-- Get incidents created today and not yet resolved
SELECT * FROM incidents
WHERE DATE(created_at) = CURRENT_DATE
  AND status != 'CLOSED';
```

##### Search Queries
```sql
-- Case-insensitive title search
SELECT * FROM incidents
WHERE title ILIKE '%database%';

-- Search in description
SELECT * FROM incidents
WHERE description ILIKE '%timeout%';

-- Search in either title or description
SELECT * FROM incidents
WHERE title ILIKE '%error%' OR description ILIKE '%error%';
```

#### Aggregation Queries

##### Count Queries
```sql
-- Total incidents
SELECT COUNT(*) as total FROM incidents;

-- Count by status
SELECT status, COUNT(*) as count
FROM incidents
GROUP BY status
ORDER BY count DESC;

-- Count by priority
SELECT priority, COUNT(*) as count
FROM incidents
GROUP BY priority
ORDER BY count DESC;

-- Count by assignee
SELECT assignee, COUNT(*) as count
FROM incidents
WHERE assignee IS NOT NULL
GROUP BY assignee
ORDER BY count DESC;
```

##### Statistics
```sql
-- Total incidents by status
SELECT
    COUNT(*) as total_incidents,
    SUM(CASE WHEN status = 'OPEN' THEN 1 ELSE 0 END) as open,
    SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress,
    SUM(CASE WHEN status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved,
    SUM(CASE WHEN status = 'CLOSED' THEN 1 ELSE 0 END) as closed
FROM incidents;

-- Priority distribution
SELECT
    COUNT(*) as total,
    SUM(CASE WHEN priority = 'CRITICAL' THEN 1 ELSE 0 END) as critical,
    SUM(CASE WHEN priority = 'HIGH' THEN 1 ELSE 0 END) as high,
    SUM(CASE WHEN priority = 'MEDIUM' THEN 1 ELSE 0 END) as medium,
    SUM(CASE WHEN priority = 'LOW' THEN 1 ELSE 0 END) as low
FROM incidents;
```

---

### UPDATE

#### Update Single Field
```sql
-- Update title
UPDATE incidents
SET title = 'Updated Title', updated_at = NOW()
WHERE id = 1;

-- Update assignee
UPDATE incidents
SET assignee = 'Bob', updated_at = NOW()
WHERE id = 2;
```

#### Update Status
```sql
-- Mark as in progress
UPDATE incidents
SET status = 'IN_PROGRESS', updated_at = NOW()
WHERE id = 1;

-- Resolve incident
UPDATE incidents
SET status = 'RESOLVED',
    resolved_at = NOW(),
    updated_at = NOW()
WHERE id = 1;

-- Close incident
UPDATE incidents
SET status = 'CLOSED', updated_at = NOW()
WHERE id = 1;
```

#### Update Multiple Fields
```sql
-- Complete update
UPDATE incidents
SET
    title = 'New Title',
    description = 'New description',
    priority = 'CRITICAL',
    assignee = 'Alice',
    updated_at = NOW()
WHERE id = 1;
```

#### Bulk Update

```sql
-- Update all OPEN incidents assigned to Alice to IN_PROGRESS
UPDATE incidents
SET status = 'IN_PROGRESS', updated_at = NOW()
WHERE status = 'OPEN' AND assignee = 'Alice';

-- Resolve all LOW priority incidents
UPDATE incidents
SET status = 'RESOLVED', resolved_at = NOW(), updated_at = NOW()
WHERE priority = 'LOW' AND status = 'OPEN';

-- Change assignee for multiple incidents
UPDATE incidents
SET assignee = 'Charlie', updated_at = NOW()
WHERE priority = 'CRITICAL' AND assignee IS NULL;
```

#### Conditional Update
```sql
-- Update only if current status is OPEN
UPDATE incidents
SET status = 'IN_PROGRESS', updated_at = NOW()
WHERE id = 1 AND status = 'OPEN';

-- Increment-like operation (set resolved_at only once)
UPDATE incidents
SET resolved_at = NOW(), updated_at = NOW()
WHERE id = 1 AND resolved_at IS NULL AND status = 'RESOLVED';
```

---

### DELETE

#### Delete Single Record
```sql
-- Delete by ID
DELETE FROM incidents WHERE id = 1;

-- Delete with confirmation (shows affected rows)
DELETE FROM incidents WHERE id = 1 RETURNING id, title, priority;
```

#### Delete with Conditions
```sql
-- Delete closed incidents
DELETE FROM incidents WHERE status = 'CLOSED';

-- Delete very old closed incidents (older than 6 months)
DELETE FROM incidents
WHERE status = 'CLOSED'
  AND created_at < NOW() - INTERVAL '6 months';

-- Delete unassigned incidents
DELETE FROM incidents WHERE assignee IS NULL;
```

#### Bulk Delete
```sql
-- Delete multiple specific incidents
DELETE FROM incidents WHERE id IN (1, 2, 3, 4, 5);

-- Delete all LOW priority resolved incidents
DELETE FROM incidents
WHERE priority = 'LOW' AND status = 'RESOLVED';

-- Delete all incidents by a specific person
DELETE FROM incidents WHERE assignee = 'Alice';
```

#### Safe Delete (View First)
```sql
-- See what will be deleted
SELECT id, title, status FROM incidents WHERE status = 'CLOSED';

-- Then delete
DELETE FROM incidents WHERE status = 'CLOSED';
```

---

## Sample Queries

### Dashboard Queries

#### Open Issues Summary
```sql
SELECT
    priority,
    COUNT(*) as count,
    STRING_AGG(DISTINCT assignee, ', ') as assignees
FROM incidents
WHERE status IN ('OPEN', 'IN_PROGRESS')
GROUP BY priority
ORDER BY CASE priority
    WHEN 'CRITICAL' THEN 1
    WHEN 'HIGH' THEN 2
    WHEN 'MEDIUM' THEN 3
    WHEN 'LOW' THEN 4
END;
```

#### Incidents by Team Member
```sql
SELECT
    assignee,
    COUNT(*) as total_assigned,
    SUM(CASE WHEN status = 'OPEN' THEN 1 ELSE 0 END) as open,
    SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress,
    SUM(CASE WHEN status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved
FROM incidents
WHERE assignee IS NOT NULL
GROUP BY assignee
ORDER BY total_assigned DESC;
```

#### Oldest Unresolved Incidents
```sql
SELECT
    id,
    title,
    priority,
    status,
    assignee,
    created_at,
    EXTRACT(DAY FROM NOW() - created_at) as days_open
FROM incidents
WHERE status IN ('OPEN', 'IN_PROGRESS')
ORDER BY created_at ASC
LIMIT 10;
```

#### Recent Activity
```sql
SELECT
    id,
    title,
    status,
    updated_at,
    EXTRACT(HOUR FROM NOW() - updated_at) as hours_since_update
FROM incidents
ORDER BY updated_at DESC
LIMIT 20;
```

#### Resolution Time Analysis
```sql
SELECT
    AVG(EXTRACT(EPOCH FROM (resolved_at - created_at))/3600) as avg_hours_to_resolve,
    MIN(EXTRACT(EPOCH FROM (resolved_at - created_at))/3600) as min_hours,
    MAX(EXTRACT(EPOCH FROM (resolved_at - created_at))/3600) as max_hours
FROM incidents
WHERE status IN ('RESOLVED', 'CLOSED') AND resolved_at IS NOT NULL;
```

### Reporting Queries

#### Incident Status Report
```sql
SELECT
    status,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM incidents), 2) as percentage
FROM incidents
GROUP BY status
ORDER BY count DESC;
```

#### Priority Distribution
```sql
SELECT
    priority,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM incidents), 2) as percentage
FROM incidents
GROUP BY priority
ORDER BY CASE priority
    WHEN 'CRITICAL' THEN 1
    WHEN 'HIGH' THEN 2
    WHEN 'MEDIUM' THEN 3
    WHEN 'LOW' THEN 4
END;
```

#### Incidents Created Per Day
```sql
SELECT
    DATE(created_at) as date,
    COUNT(*) as count
FROM incidents
GROUP BY DATE(created_at)
ORDER BY date DESC
LIMIT 30;
```

---

## Useful Commands

### Connect to Database

#### Via psql
```bash
# Connect to database
psql -h localhost -U postgres -d incidents

# With password prompt
psql -h localhost -U postgres -d incidents -W

# Run a single command
psql -h localhost -U postgres -d incidents -c "SELECT COUNT(*) FROM incidents;"
```

#### Via Docker (if containerized)
```bash
docker exec -it postgres psql -U postgres -d incidents
```

### Database Information

#### View Tables
```sql
-- List all tables
\dt

-- List all tables with size
SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename))
FROM pg_tables WHERE schemaname NOT IN ('pg_catalog', 'information_schema');
```

#### View Table Structure
```sql
-- Describe table
\d incidents

-- Get column info
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'incidents'
ORDER BY ordinal_position;
```

#### View Constraints
```sql
-- Get all constraints
SELECT constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_name = 'incidents';

-- Get check constraints
SELECT constraint_name, check_clause
FROM information_schema.check_constraints
WHERE constraint_schema = 'public';
```

### Database Maintenance

#### Get Database Size
```sql
SELECT
    datname,
    pg_size_pretty(pg_database_size(datname)) as size
FROM pg_database
WHERE datname = 'incidents';
```

#### Get Table Statistics
```sql
SELECT
    schemaname,
    tablename,
    n_live_tup as row_count,
    n_dead_tup as dead_rows,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_stat_user_tables
WHERE tablename = 'incidents';
```

#### Vacuum and Analyze (Maintenance)
```sql
-- Vacuum to clean up dead rows
VACUUM incidents;

-- Analyze to update statistics
ANALYZE incidents;

-- Both together
VACUUM ANALYZE incidents;
```

---

## Indexes & Performance

### Current Indexes

```sql
-- View all indexes
\di

-- View indexes on incidents table
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'incidents';
```

### Creating Indexes (Recommendations)

```sql
-- Index on status (frequently filtered)
CREATE INDEX idx_incidents_status ON incidents(status);

-- Index on priority (frequently filtered)
CREATE INDEX idx_incidents_priority ON incidents(priority);

-- Index on assignee (frequently filtered)
CREATE INDEX idx_incidents_assignee ON incidents(assignee);

-- Index on created_at (frequently sorted)
CREATE INDEX idx_incidents_created_at ON incidents(created_at DESC);

-- Composite index for common queries
CREATE INDEX idx_incidents_status_priority ON incidents(status, priority);

-- Index for date range queries
CREATE INDEX idx_incidents_created_range ON incidents(created_at DESC NULLS LAST);
```

### Query Performance

```sql
-- Explain query plan
EXPLAIN SELECT * FROM incidents WHERE status = 'OPEN';

-- Analyze query performance
EXPLAIN ANALYZE SELECT * FROM incidents WHERE status = 'OPEN';

-- Verbose analysis
EXPLAIN ANALYZE VERBOSE SELECT * FROM incidents WHERE status = 'OPEN' LIMIT 10;
```

---

## Backup & Recovery

### Backup Operations

#### Full Database Backup
```bash
# Backup entire database
pg_dump -h localhost -U postgres -d incidents > incidents_backup.sql

# Backup with compression
pg_dump -h localhost -U postgres -d incidents | gzip > incidents_backup.sql.gz

# Backup table only
pg_dump -h localhost -U postgres -d incidents -t incidents > incidents_table_backup.sql

# Backup with data only
pg_dump -h localhost -U postgres -d incidents --data-only > incidents_data_backup.sql
```

#### Custom Format Backup
```bash
# Backup in custom format (more efficient)
pg_dump -h localhost -U postgres -d incidents -Fc > incidents_backup.dump

# Restore from custom format
pg_restore -h localhost -U postgres -d incidents incidents_backup.dump
```

### Restore Operations

#### Full Restore
```bash
# Restore from SQL file
psql -h localhost -U postgres -d incidents < incidents_backup.sql

# Restore from compressed backup
gunzip -c incidents_backup.sql.gz | psql -h localhost -U postgres -d incidents
```

#### Partial Restore
```bash
# Restore specific table
psql -h localhost -U postgres -d incidents < incidents_table_backup.sql
```

#### Before Restore
```sql
-- Check current data
SELECT COUNT(*) FROM incidents;

-- Backup current data
CREATE TABLE incidents_backup AS SELECT * FROM incidents;

-- Truncate before restore (if needed)
TRUNCATE incidents;

-- Or delete specific records
DELETE FROM incidents WHERE id > 100;
```

---

## Connection Examples

### Python
```python
import psycopg2

# Connect
conn = psycopg2.connect(
    host="localhost",
    database="incidents",
    user="postgres",
    password="postgres"
)

# Query
cur = conn.cursor()
cur.execute("SELECT * FROM incidents LIMIT 5")
for row in cur.fetchall():
    print(row)

conn.close()
```

### Node.js
```javascript
const { Client } = require('pg');

const client = new Client({
    host: 'localhost',
    port: 5432,
    database: 'incidents',
    user: 'postgres',
    password: 'postgres'
});

client.connect();
client.query('SELECT * FROM incidents LIMIT 5', (err, res) => {
    console.log(res.rows);
    client.end();
});
```

### Java/JDBC
```java
String url = "jdbc:postgresql://localhost:5432/incidents";
String user = "postgres";
String password = "postgres";

Connection conn = DriverManager.getConnection(url, user, password);
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery("SELECT * FROM incidents LIMIT 5");

while(rs.next()) {
    System.out.println(rs.getInt("id") + " - " + rs.getString("title"));
}
```

---

## Data Validation Rules

### Field Validation

#### Title
- **Type**: VARCHAR(200)
- **Required**: Yes
- **Max Length**: 200 characters
- **Examples**: "Database Down", "API Response Slow"

#### Description
- **Type**: VARCHAR(2000)
- **Required**: No
- **Max Length**: 2000 characters
- **Examples**: "Connection timeout when accessing production database"

#### Priority
- **Type**: VARCHAR(20)
- **Required**: Yes (defaults to LOW)
- **Valid Values**: LOW, MEDIUM, HIGH, CRITICAL
- **Examples**: "HIGH", "CRITICAL"

#### Status
- **Type**: VARCHAR(20)
- **Required**: Yes (defaults to OPEN)
- **Valid Values**: OPEN, IN_PROGRESS, RESOLVED, CLOSED
- **Examples**: "OPEN", "IN_PROGRESS"

#### Assignee
- **Type**: VARCHAR(100)
- **Required**: No
- **Max Length**: 100 characters
- **Examples**: "Alice", "Bob", "team-lead@company.com"

#### Timestamps
- **created_at**: Set automatically to NOW() on insert
- **updated_at**: Set automatically to NOW() on insert, updated on every change
- **resolved_at**: Set manually when status = RESOLVED

---

## Common Use Cases

### Scenario 1: Create and Track an Issue

```sql
-- Step 1: Create incident
INSERT INTO incidents (title, description, priority, created_at, updated_at)
VALUES ('API Gateway Down', 'Gateway returning 503', 'CRITICAL', NOW(), NOW())
RETURNING id;

-- Returns: id = 101
-- Step 2: Assign to person
UPDATE incidents
SET assignee = 'Alice', updated_at = NOW()
WHERE id = 101;

-- Step 3: Mark as in progress
UPDATE incidents
SET status = 'IN_PROGRESS', updated_at = NOW()
WHERE id = 101;

-- Step 4: Check status
SELECT * FROM incidents WHERE id = 101;

-- Step 5: Resolve
UPDATE incidents
SET status = 'RESOLVED', resolved_at = NOW(), updated_at = NOW()
WHERE id = 101;

-- Step 6: Close
UPDATE incidents
SET status = 'CLOSED', updated_at = NOW()
WHERE id = 101;
```

### Scenario 2: Dashboard Report

```sql
-- Get current status overview
SELECT
    status,
    COUNT(*) as count
FROM incidents
WHERE created_at >= NOW() - INTERVAL '7 days'
GROUP BY status;
```

### Scenario 3: Find Unresolved Issues

```sql
-- Get all issues older than 7 days that are still open
SELECT
    id,
    title,
    priority,
    assignee,
    created_at,
    EXTRACT(DAY FROM NOW() - created_at) as days_open
FROM incidents
WHERE status IN ('OPEN', 'IN_PROGRESS')
  AND created_at < NOW() - INTERVAL '7 days'
ORDER BY created_at ASC;
```

---

## Performance Tips

1. **Always use WHERE clauses** - Don't select all rows if not needed
2. **Use indexes wisely** - Create indexes on frequently filtered columns
3. **Limit result sets** - Use LIMIT for large queries
4. **Use EXPLAIN** - Analyze query plans before running expensive queries
5. **Regular maintenance** - Run VACUUM and ANALYZE periodically
6. **Archive old data** - Move resolved incidents older than 1 year to archive table

---

## Troubleshooting

### Cannot Connect
```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Check port is listening
lsof -i :5432

# Verify credentials
psql -h localhost -U postgres -c "SELECT 1"
```

### Query Errors
```sql
-- Check table exists
SELECT * FROM information_schema.tables WHERE table_name = 'incidents';

-- Check columns
SELECT column_name FROM information_schema.columns WHERE table_name = 'incidents';

-- Check constraints
SELECT constraint_name FROM information_schema.table_constraints WHERE table_name = 'incidents';
```

### Performance Issues
```sql
-- Check table size
SELECT pg_size_pretty(pg_total_relation_size('incidents'));

-- Check row count
SELECT COUNT(*) FROM incidents;

-- Check indexes
SELECT * FROM pg_stat_user_indexes WHERE relname = 'incidents';
```

---

**Last Updated**: February 1, 2026
**Database Version**: PostgreSQL 14
**Status**: Production Ready
