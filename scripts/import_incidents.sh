#!/bin/bash

################################################################################
# Incident Tracker - CSV Data Import Script
#
# Purpose: Import incidents from CSV file into PostgreSQL database
# Usage: ./import_incidents.sh [OPTIONS]
#
# Options:
#   -f, --file FILE       CSV file to import (default: ../data/incidents_import.csv)
#   -h, --host HOST       PostgreSQL host (default: localhost)
#   -p, --port PORT       PostgreSQL port (default: 5432)
#   -d, --database DB     Database name (default: incidents)
#   -u, --user USER       Database user (default: postgres)
#   -P, --password PASS   Database password (default: postgres)
#   --help                Show this help message
#
# Examples:
#   ./import_incidents.sh
#   ./import_incidents.sh -f my_incidents.csv
#   ./import_incidents.sh -h localhost -d incidents -u postgres
#
################################################################################

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
CSV_FILE="../data/incidents_import.csv"
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="incidents"
DB_USER="postgres"
DB_PASSWORD="postgres"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Functions
print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

show_help() {
    grep '^#' "$0" | grep -E '^\s*#' | head -30
    exit 0
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -f|--file)
            CSV_FILE="$2"
            shift 2
            ;;
        -h|--host)
            DB_HOST="$2"
            shift 2
            ;;
        -p|--port)
            DB_PORT="$2"
            shift 2
            ;;
        -d|--database)
            DB_NAME="$2"
            shift 2
            ;;
        -u|--user)
            DB_USER="$2"
            shift 2
            ;;
        -P|--password)
            DB_PASSWORD="$2"
            shift 2
            ;;
        --help)
            show_help
            ;;
        *)
            print_error "Unknown option: $1"
            show_help
            ;;
    esac
done

print_header "Incident Tracker - CSV Import Script"

# Validate CSV file exists
if [[ ! -f "$CSV_FILE" ]]; then
    # Try relative to script directory
    if [[ ! -f "${SCRIPT_DIR}/${CSV_FILE}" ]]; then
        print_error "CSV file not found: $CSV_FILE"
        exit 1
    fi
    CSV_FILE="${SCRIPT_DIR}/${CSV_FILE}"
fi

print_info "CSV File: $CSV_FILE"
print_info "Database: $DB_NAME"
print_info "Host: $DB_HOST:$DB_PORT"
print_info "User: $DB_USER"

# Count rows in CSV
TOTAL_ROWS=$(wc -l < "$CSV_FILE")
TOTAL_ROWS=$((TOTAL_ROWS - 1)) # Subtract header row

print_info "Total records to import: $TOTAL_ROWS"

# Check PostgreSQL connectivity
print_info "Checking database connection..."

if ! PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1" > /dev/null 2>&1; then
    print_error "Cannot connect to database"
    print_error "Connection parameters:"
    print_error "  Host: $DB_HOST"
    print_error "  Port: $DB_PORT"
    print_error "  Database: $DB_NAME"
    print_error "  User: $DB_USER"
    exit 1
fi

print_success "Database connection successful"

# Backup current data
print_info "Creating backup of current data..."

BACKUP_FILE="incidents_backup_$(date +%Y%m%d_%H%M%S).sql"
if PGPASSWORD="$DB_PASSWORD" pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t incidents > "$BACKUP_FILE" 2>/dev/null; then
    print_success "Backup created: $BACKUP_FILE"
else
    print_warning "Could not create backup (proceeding anyway)"
fi

# Create temporary SQL file from CSV
print_info "Generating SQL statements from CSV..."

TEMP_SQL_FILE="/tmp/import_incidents_$$.sql"
cat > "$TEMP_SQL_FILE" << 'SQLEOF'
-- Incident Tracker - Auto-generated import script
-- Generated: $(date)

BEGIN;

-- Check if table exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_name = 'incidents'
    ) THEN
        RAISE EXCEPTION 'incidents table does not exist';
    END IF;
END $$;

-- Disable triggers if any
ALTER TABLE incidents DISABLE TRIGGER ALL;

-- Insert data from CSV
\COPY incidents (title, description, priority, status, assignee, created_at, updated_at)
FROM STDIN WITH (FORMAT csv, HEADER true, NULL AS '')
SQLEOF

# Append CSV data
tail -n +2 "$CSV_FILE" >> "$TEMP_SQL_FILE"

# Add SQL epilogue
cat >> "$TEMP_SQL_FILE" << 'SQLEOF'
;

-- Re-enable triggers
ALTER TABLE incidents ENABLE TRIGGER ALL;

-- Update sequence to avoid ID conflicts
SELECT setval('incidents_id_seq', (SELECT MAX(id) FROM incidents) + 1);

-- Verify import
SELECT COUNT(*) as total_records_after_import FROM incidents;

COMMIT;
SQLEOF

print_success "SQL script generated"

# Execute import
print_info "Importing data into database..."
echo ""

if PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" < "$TEMP_SQL_FILE"; then
    print_success "Data import completed successfully"
else
    print_error "Import failed"
    rm "$TEMP_SQL_FILE"
    exit 1
fi

echo ""

# Verify import results
print_info "Verifying import..."

# Get counts
FINAL_COUNT=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM incidents;")
OPEN_COUNT=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM incidents WHERE status = 'OPEN';")
CRITICAL_COUNT=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM incidents WHERE priority = 'CRITICAL';")

# Trim whitespace
FINAL_COUNT=$(echo $FINAL_COUNT | tr -d ' ')
OPEN_COUNT=$(echo $OPEN_COUNT | tr -d ' ')
CRITICAL_COUNT=$(echo $CRITICAL_COUNT | tr -d ' ')

echo ""
print_success "Import Statistics:"
echo "   Total incidents: $FINAL_COUNT"
echo "   OPEN status: $OPEN_COUNT"
echo "   CRITICAL priority: $CRITICAL_COUNT"
echo ""

# Show data by priority
print_info "Distribution by Priority:"
PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" << SQL
SELECT priority, COUNT(*) as count
FROM incidents
GROUP BY priority
ORDER BY CASE priority WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 WHEN 'LOW' THEN 4 END;
SQL

echo ""

# Show data by status
print_info "Distribution by Status:"
PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" << SQL
SELECT status, COUNT(*) as count
FROM incidents
GROUP BY status
ORDER BY count DESC;
SQL

echo ""

# Show data by assignee
print_info "Distribution by Assignee:"
PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" << SQL
SELECT assignee, COUNT(*) as count
FROM incidents
WHERE assignee IS NOT NULL
GROUP BY assignee
ORDER BY count DESC;
SQL

echo ""

# Cleanup
rm "$TEMP_SQL_FILE"

print_header "Import Completed Successfully"
print_success "All $FINAL_COUNT incidents imported"
print_info "Backup file: $BACKUP_FILE (keep safe)"
print_info "You can test the data using the SQL queries in DATABASE.md"

echo ""
echo "Next steps:"
echo "  1. View all incidents: SELECT * FROM incidents LIMIT 10;"
echo "  2. Test REST API: curl http://localhost:8081/api/incidents"
echo "  3. Test GraphQL: curl -X POST http://localhost:8081/graphql -d '{\"query\":\"{ incidents { id title priority } }\" }'"
echo ""
