#!/usr/bin/env python3
"""
Incident Tracker - CSV Data Import Script

Purpose: Import incidents from CSV file into PostgreSQL database

Usage:
    python3 import_incidents.py [OPTIONS]

Options:
    -f, --file FILE         CSV file to import (default: ../data/incidents_import.csv)
    -h, --host HOST         PostgreSQL host (default: localhost)
    -p, --port PORT         PostgreSQL port (default: 5432)
    -d, --database DB       Database name (default: incidents)
    -u, --user USER         Database user (default: postgres)
    -P, --password PASS     Database password (default: postgres)
    --batch-size SIZE       Batch size for inserts (default: 100)
    --help                  Show this help message

Examples:
    python3 import_incidents.py
    python3 import_incidents.py -f my_incidents.csv
    python3 import_incidents.py --host localhost --database incidents
"""

import argparse
import csv
import sys
import os
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Tuple
import subprocess

try:
    import psycopg2
    from psycopg2.extras import execute_batch
except ImportError:
    print("Error: psycopg2 not installed. Install it with: pip install psycopg2-binary")
    sys.exit(1)

# Color codes
class Colors:
    RED = '\033[0;31m'
    GREEN = '\033[0;32m'
    YELLOW = '\033[1;33m'
    BLUE = '\033[0;34m'
    NC = '\033[0m'  # No Color

def print_header(text: str) -> None:
    """Print formatted header"""
    print(f"{Colors.BLUE}{'=' * 50}{Colors.NC}")
    print(f"{Colors.BLUE}{text}{Colors.NC}")
    print(f"{Colors.BLUE}{'=' * 50}{Colors.NC}")

def print_success(text: str) -> None:
    """Print success message"""
    print(f"{Colors.GREEN}✓ {text}{Colors.NC}")

def print_error(text: str) -> None:
    """Print error message"""
    print(f"{Colors.RED}✗ {text}{Colors.NC}")

def print_warning(text: str) -> None:
    """Print warning message"""
    print(f"{Colors.YELLOW}⚠ {text}{Colors.NC}")

def print_info(text: str) -> None:
    """Print info message"""
    print(f"{Colors.BLUE}ℹ {text}{Colors.NC}")

def parse_arguments() -> argparse.Namespace:
    """Parse command line arguments"""
    parser = argparse.ArgumentParser(
        description='Import incidents from CSV file into PostgreSQL database',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        add_help=True
    )

    parser.add_argument('-f', '--file',
                        default='../data/incidents_import.csv',
                        help='CSV file to import (default: ../data/incidents_import.csv)')
    parser.add_argument('--host',
                        default='localhost',
                        help='PostgreSQL host (default: localhost)')
    parser.add_argument('-p', '--port',
                        type=int,
                        default=5432,
                        help='PostgreSQL port (default: 5432)')
    parser.add_argument('-d', '--database',
                        default='incidents',
                        help='Database name (default: incidents)')
    parser.add_argument('-u', '--user',
                        default='postgres',
                        help='Database user (default: postgres)')
    parser.add_argument('-P', '--password',
                        default='postgres',
                        help='Database password (default: postgres)')
    parser.add_argument('--batch-size',
                        type=int,
                        default=100,
                        help='Batch size for inserts (default: 100)')

    return parser.parse_args()

def validate_csv_file(file_path: str) -> Path:
    """Validate and return CSV file path"""
    # Try direct path
    path = Path(file_path)
    if path.exists() and path.is_file():
        return path

    # Try relative to script directory
    script_dir = Path(__file__).parent
    relative_path = script_dir / file_path
    if relative_path.exists() and relative_path.is_file():
        return relative_path

    raise FileNotFoundError(f"CSV file not found: {file_path}")

def read_csv_file(file_path: Path) -> Tuple[int, List[Dict]]:
    """Read and parse CSV file"""
    records = []

    with open(file_path, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)

        for row in reader:
            # Add timestamps
            row['created_at'] = datetime.now()
            row['updated_at'] = datetime.now()

            # Handle empty values
            for key in row:
                if row[key] == '':
                    row[key] = None

            records.append(row)

    return len(records), records

def create_backup(conn, db_name: str) -> str:
    """Create backup of current data"""
    print_info("Creating backup of current data...")

    backup_file = f"incidents_backup_{datetime.now().strftime('%Y%m%d_%H%M%S')}.sql"

    try:
        cursor = conn.cursor()
        cursor.execute("""
            SELECT tablename FROM pg_tables
            WHERE schemaname = 'public' AND tablename = 'incidents'
        """)

        if cursor.fetchone():
            cursor.close()
            # Use pg_dump to create backup
            cmd = f"pg_dump -h localhost -U postgres -t incidents > {backup_file}"
            subprocess.run(cmd, shell=True, check=True, capture_output=True)
            print_success(f"Backup created: {backup_file}")
        else:
            print_warning("incidents table does not exist, skipping backup")
    except Exception as e:
        print_warning(f"Could not create backup: {e} (proceeding anyway)")

    return backup_file

def verify_table(conn) -> bool:
    """Verify incidents table exists"""
    try:
        cursor = conn.cursor()
        cursor.execute("""
            SELECT EXISTS (
                SELECT 1 FROM information_schema.tables
                WHERE table_name = 'incidents'
            )
        """)
        exists = cursor.fetchone()[0]
        cursor.close()
        return exists
    except Exception as e:
        print_error(f"Error verifying table: {e}")
        return False

def insert_records(conn, records: List[Dict], batch_size: int) -> int:
    """Insert records into database"""
    if not records:
        return 0

    print_info(f"Inserting {len(records)} records in batches of {batch_size}...")

    cursor = conn.cursor()

    # Prepare insert statement
    insert_sql = """
        INSERT INTO incidents (title, description, priority, status, assignee, created_at, updated_at)
        VALUES (%(title)s, %(description)s, %(priority)s, %(status)s, %(assignee)s, %(created_at)s, %(updated_at)s)
    """

    try:
        # Insert in batches
        for i in range(0, len(records), batch_size):
            batch = records[i:i + batch_size]
            execute_batch(cursor, insert_sql, batch, page_size=batch_size)
            conn.commit()

            # Progress indicator
            processed = min(i + batch_size, len(records))
            print(f"  Progress: {processed}/{len(records)} ({100*processed//len(records)}%)", end='\r')

        # Update sequence
        cursor.execute("SELECT setval('incidents_id_seq', (SELECT MAX(id) FROM incidents) + 1)")
        conn.commit()

        print()  # New line after progress
        return len(records)

    except Exception as e:
        conn.rollback()
        raise e
    finally:
        cursor.close()

def get_statistics(conn) -> Dict:
    """Get import statistics"""
    cursor = conn.cursor()

    # Total count
    cursor.execute("SELECT COUNT(*) FROM incidents")
    total = cursor.fetchone()[0]

    # By priority
    cursor.execute("""
        SELECT priority, COUNT(*) as count FROM incidents
        GROUP BY priority ORDER BY priority
    """)
    by_priority = dict(cursor.fetchall())

    # By status
    cursor.execute("""
        SELECT status, COUNT(*) as count FROM incidents
        GROUP BY status ORDER BY status
    """)
    by_status = dict(cursor.fetchall())

    # By assignee
    cursor.execute("""
        SELECT assignee, COUNT(*) as count FROM incidents
        WHERE assignee IS NOT NULL
        GROUP BY assignee ORDER BY count DESC LIMIT 10
    """)
    by_assignee = dict(cursor.fetchall())

    cursor.close()

    return {
        'total': total,
        'by_priority': by_priority,
        'by_status': by_status,
        'by_assignee': by_assignee
    }

def main():
    """Main function"""
    args = parse_arguments()

    print_header("Incident Tracker - CSV Import Script")

    # Validate CSV file
    try:
        csv_file = validate_csv_file(args.file)
        print_info(f"CSV File: {csv_file}")
    except FileNotFoundError as e:
        print_error(str(e))
        sys.exit(1)

    # Display connection info
    print_info(f"Database: {args.database}")
    print_info(f"Host: {args.host}:{args.port}")
    print_info(f"User: {args.user}")

    # Read CSV file
    print_info("Reading CSV file...")
    try:
        total_rows, records = read_csv_file(csv_file)
        print_success(f"Read {total_rows} records from CSV")
    except Exception as e:
        print_error(f"Error reading CSV: {e}")
        sys.exit(1)

    # Connect to database
    print_info("Connecting to database...")
    try:
        conn = psycopg2.connect(
            host=args.host,
            port=args.port,
            database=args.database,
            user=args.user,
            password=args.password
        )
        print_success("Database connection successful")
    except psycopg2.Error as e:
        print_error(f"Cannot connect to database: {e}")
        sys.exit(1)

    # Verify table exists
    if not verify_table(conn):
        print_error("incidents table does not exist")
        conn.close()
        sys.exit(1)

    # Create backup
    backup_file = create_backup(conn, args.database)

    # Insert records
    print_info("Starting import...")
    try:
        inserted = insert_records(conn, records, args.batch_size)
        print_success(f"Inserted {inserted} records")
        conn.commit()
    except Exception as e:
        print_error(f"Import failed: {e}")
        conn.rollback()
        conn.close()
        sys.exit(1)

    # Get and display statistics
    print_info("Gathering statistics...")
    try:
        stats = get_statistics(conn)

        print()
        print_success("Import Statistics:")
        print(f"   Total incidents: {stats['total']}")

        print()
        print_info("Distribution by Priority:")
        for priority in ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW']:
            count = stats['by_priority'].get(priority, 0)
            pct = (count / stats['total'] * 100) if stats['total'] > 0 else 0
            print(f"   {priority:10} : {count:4} ({pct:5.1f}%)")

        print()
        print_info("Distribution by Status:")
        for status, count in sorted(stats['by_status'].items(), key=lambda x: -x[1]):
            pct = (count / stats['total'] * 100) if stats['total'] > 0 else 0
            print(f"   {status:15} : {count:4} ({pct:5.1f}%)")

        print()
        print_info("Top 10 Assignees:")
        for assignee, count in stats['by_assignee'].items():
            pct = (count / stats['total'] * 100) if stats['total'] > 0 else 0
            print(f"   {assignee:15} : {count:4} ({pct:5.1f}%)")

    except Exception as e:
        print_warning(f"Could not gather statistics: {e}")

    # Close connection
    conn.close()

    print()
    print_header("Import Completed Successfully")
    print_success(f"All {stats.get('total', total_rows)} incidents imported")
    print_info(f"Backup file: {backup_file} (keep safe)")

    print()
    print("Next steps:")
    print("  1. Test REST API: curl http://localhost:8081/api/incidents")
    print("  2. Test GraphQL: Visit http://localhost:8081/graphiql")
    print("  3. View Swagger: Visit http://localhost:8081/swagger-ui.html")
    print()

if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        print_warning("\nImport cancelled by user")
        sys.exit(0)
    except Exception as e:
        print_error(f"Unexpected error: {e}")
        sys.exit(1)
