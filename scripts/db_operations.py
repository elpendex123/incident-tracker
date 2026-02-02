#!/usr/bin/env python3
"""
Database operations script for Incident Tracker.

Provides CLI interface for database operations including:
- Query incident statistics and data
- Seed test data
- Clean up old records
- Generate summary reports

Usage:
    python db_operations.py --action query --query counts
    python db_operations.py --action seed --count 50
    python db_operations.py --action cleanup --days 90
    python db_operations.py --action report --output report.txt

Environment Variables:
    DB_HOST - PostgreSQL host (default: localhost)
    DB_PORT - PostgreSQL port (default: 5432)
    DB_NAME - Database name (default: incidents)
    DB_USER - Database user (default: postgres)
    DB_PASSWORD - Database password (required)
"""

import argparse
import os
import sys
from datetime import datetime, timedelta
from typing import List, Dict
import random

import psycopg2
from psycopg2 import sql
from dotenv import load_dotenv
from tabulate import tabulate

# Load environment variables from .env file
load_dotenv()

# Database connection parameters
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': int(os.getenv('DB_PORT', '5432')),
    'database': os.getenv('DB_NAME', 'incidents'),
    'user': os.getenv('DB_USER', 'postgres'),
    'password': os.getenv('DB_PASSWORD', 'postgres')
}


def get_connection():
    """Establish database connection."""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        return conn
    except psycopg2.Error as e:
        print(f"Error connecting to database: {e}")
        sys.exit(1)


def query_counts(conn):
    """Display incident counts by status."""
    cursor = conn.cursor()
    query = """
        SELECT status, COUNT(*) as count
        FROM incidents
        GROUP BY status
        ORDER BY count DESC
    """
    try:
        cursor.execute(query)
        results = cursor.fetchall()

        print("\n=== Incident Counts by Status ===")
        print(tabulate(results, headers=['Status', 'Count'], tablefmt='grid'))
    finally:
        cursor.close()


def query_priority(conn):
    """Display incidents by priority."""
    cursor = conn.cursor()
    query = """
        SELECT priority, COUNT(*) as count
        FROM incidents
        GROUP BY priority
        ORDER BY
            CASE priority
                WHEN 'CRITICAL' THEN 1
                WHEN 'HIGH' THEN 2
                WHEN 'MEDIUM' THEN 3
                WHEN 'LOW' THEN 4
            END
    """
    try:
        cursor.execute(query)
        results = cursor.fetchall()

        print("\n=== Incident Counts by Priority ===")
        print(tabulate(results, headers=['Priority', 'Count'], tablefmt='grid'))
    finally:
        cursor.close()


def query_open(conn):
    """Display all open incidents."""
    cursor = conn.cursor()
    query = """
        SELECT id, title, priority, assignee, created_at
        FROM incidents
        WHERE status = 'OPEN'
        ORDER BY priority, created_at DESC
    """
    try:
        cursor.execute(query)
        results = cursor.fetchall()

        print("\n=== Open Incidents ===")
        print(tabulate(results,
                       headers=['ID', 'Title', 'Priority', 'Assignee', 'Created At'],
                       tablefmt='grid'))
    finally:
        cursor.close()


def query_overdue(conn, days: int = 7):
    """Display overdue incidents."""
    cursor = conn.cursor()
    cutoff_date = datetime.now() - timedelta(days=days)

    query = """
        SELECT id, title, priority, status, assignee, created_at,
               EXTRACT(DAY FROM NOW() - created_at) as days_old
        FROM incidents
        WHERE status IN ('OPEN', 'IN_PROGRESS')
          AND created_at < %s
        ORDER BY created_at ASC
    """
    try:
        cursor.execute(query, (cutoff_date,))
        results = cursor.fetchall()

        print(f"\n=== Incidents Older Than {days} Days ===")
        print(tabulate(results,
                       headers=['ID', 'Title', 'Priority', 'Status', 'Assignee',
                               'Created At', 'Days Old'],
                       tablefmt='grid'))
    finally:
        cursor.close()


def seed_data(conn, count: int = 10):
    """Insert sample test data."""
    cursor = conn.cursor()

    priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
    statuses = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED']
    assignees = ['Alice', 'Bob', 'Charlie', 'Diana', None]

    print(f"\nSeeding {count} sample incidents...")

    try:
        for i in range(count):
            title = f"Sample Incident #{i+1}"
            description = f"This is a test incident created by automation script #{i+1}"
            priority = random.choice(priorities)
            status = random.choice(statuses)
            assignee = random.choice(assignees)

            query = """
                INSERT INTO incidents (title, description, priority, status, assignee, created_at, updated_at)
                VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
            """
            cursor.execute(query, (title, description, priority, status, assignee))

        conn.commit()
        print(f"✓ Successfully inserted {count} incidents.")
    except psycopg2.Error as e:
        conn.rollback()
        print(f"✗ Error seeding data: {e}")
    finally:
        cursor.close()


def cleanup_old_incidents(conn, days: int = 30):
    """Remove old closed incidents."""
    cursor = conn.cursor()
    cutoff_date = datetime.now() - timedelta(days=days)

    query = """
        DELETE FROM incidents
        WHERE status = 'CLOSED'
          AND updated_at < %s
    """
    try:
        cursor.execute(query, (cutoff_date,))
        deleted_count = cursor.rowcount
        conn.commit()

        print(f"\n✓ Deleted {deleted_count} closed incidents older than {days} days.")
    except psycopg2.Error as e:
        conn.rollback()
        print(f"✗ Error cleaning up: {e}")
    finally:
        cursor.close()


def generate_report(conn, output_file: str = None):
    """Generate summary report."""
    cursor = conn.cursor()

    report = []
    report.append("=" * 60)
    report.append("INCIDENT TRACKER - SUMMARY REPORT")
    report.append(f"Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    report.append("=" * 60)

    # Overall statistics
    try:
        query_total = "SELECT COUNT(*) FROM incidents"
        cursor.execute(query_total)
        total_count = cursor.fetchone()[0]
        report.append(f"\nTotal Incidents: {total_count}")

        # By status
        query_status = """
            SELECT status, COUNT(*) as count
            FROM incidents
            GROUP BY status
        """
        cursor.execute(query_status)
        status_results = cursor.fetchall()

        report.append("\n--- By Status ---")
        report.append(tabulate(status_results, headers=['Status', 'Count'], tablefmt='simple'))

        # By priority
        query_priority = """
            SELECT priority, COUNT(*) as count
            FROM incidents
            GROUP BY priority
        """
        cursor.execute(query_priority)
        priority_results = cursor.fetchall()

        report.append("\n--- By Priority ---")
        report.append(tabulate(priority_results, headers=['Priority', 'Count'], tablefmt='simple'))

        # Average resolution time
        query_avg_resolution = """
            SELECT AVG(EXTRACT(EPOCH FROM (resolved_at - created_at))/3600) as avg_hours
            FROM incidents
            WHERE resolved_at IS NOT NULL
        """
        cursor.execute(query_avg_resolution)
        avg_resolution = cursor.fetchone()[0]

        if avg_resolution:
            report.append(f"\nAverage Resolution Time: {avg_resolution:.2f} hours")

        report.append("\n" + "=" * 60)

        report_text = "\n".join(report)

        if output_file:
            with open(output_file, 'w') as f:
                f.write(report_text)
            print(f"✓ Report saved to {output_file}")
        else:
            print(report_text)

    except psycopg2.Error as e:
        print(f"✗ Error generating report: {e}")
    finally:
        cursor.close()


def main():
    parser = argparse.ArgumentParser(
        description='Database operations for Incident Tracker',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python db_operations.py --action query --query counts
  python db_operations.py --action seed --count 20
  python db_operations.py --action cleanup --days 30
  python db_operations.py --action report --output report.txt
        """
    )

    parser.add_argument('--action', required=True,
                        choices=['query', 'seed', 'cleanup', 'report'],
                        help='Action to perform')

    parser.add_argument('--query', choices=['counts', 'priority', 'open', 'overdue'],
                        help='Query type (required when action=query)')

    parser.add_argument('--count', type=int, default=10,
                        help='Number of incidents to seed (default: 10)')

    parser.add_argument('--days', type=int, default=30,
                        help='Number of days for cleanup/overdue queries (default: 30)')

    parser.add_argument('--output', help='Output file for report')

    args = parser.parse_args()

    # Validate arguments
    if args.action == 'query' and not args.query:
        parser.error("--query is required when --action=query")

    # Connect to database
    conn = get_connection()

    try:
        if args.action == 'query':
            if args.query == 'counts':
                query_counts(conn)
            elif args.query == 'priority':
                query_priority(conn)
            elif args.query == 'open':
                query_open(conn)
            elif args.query == 'overdue':
                query_overdue(conn, args.days)

        elif args.action == 'seed':
            seed_data(conn, args.count)

        elif args.action == 'cleanup':
            cleanup_old_incidents(conn, args.days)

        elif args.action == 'report':
            generate_report(conn, args.output)

    finally:
        conn.close()


if __name__ == '__main__':
    main()
