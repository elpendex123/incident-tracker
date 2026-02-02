#!/usr/bin/env python3
"""
REST API Test Script for Spring Boot Incident Tracker
Tests all endpoints and provides detailed results
"""

import urllib.request
import urllib.error
import json
import sys
from typing import Tuple, Dict, Any

class APITester:
    """Test client for Spring Boot Incident Tracker API"""

    BASE_URL = "http://localhost:8081"

    def __init__(self):
        self.test_results = []
        self.created_incident_id = None

    def make_request(self, method: str, endpoint: str, data: Any = None) -> Tuple[int, str]:
        """
        Make HTTP request and return status code and response body

        Args:
            method: HTTP method (GET, POST, PUT, PATCH, DELETE)
            endpoint: API endpoint path
            data: Request body data (for POST, PUT, PATCH)

        Returns:
            Tuple of (status_code, response_body)
        """
        url = f"{self.BASE_URL}{endpoint}"
        headers = {}

        if method in ['POST', 'PUT', 'PATCH']:
            headers['Content-Type'] = 'application/json'

        try:
            if data is not None:
                request_body = json.dumps(data).encode('utf-8')
                req = urllib.request.Request(
                    url,
                    data=request_body,
                    headers=headers,
                    method=method
                )
            else:
                req = urllib.request.Request(url, headers=headers, method=method)

            with urllib.request.urlopen(req) as response:
                status = response.status
                body = response.read().decode('utf-8')
                return status, body

        except urllib.error.HTTPError as e:
            status = e.code
            try:
                body = e.read().decode('utf-8')
            except:
                body = ""
            return status, body
        except Exception as e:
            return None, str(e)

    def test_endpoint(self, test_name: str, method: str, endpoint: str,
                      expected_status: int, data: Any = None,
                      check_response: callable = None) -> bool:
        """
        Test a single endpoint

        Args:
            test_name: Name of the test
            method: HTTP method
            endpoint: API endpoint
            expected_status: Expected HTTP status code
            data: Request body (optional)
            check_response: Function to validate response (optional)

        Returns:
            True if test passed, False otherwise
        """
        status, body = self.make_request(method, endpoint, data)

        # Determine if test passed
        passed = status == expected_status
        if check_response and passed:
            try:
                response_data = json.loads(body) if body else None
                passed = check_response(response_data)
            except:
                passed = False

        # Truncate body for display
        display_body = body[:200] if len(body) > 200 else body

        # Store result
        result = {
            'test_name': test_name,
            'method': method,
            'endpoint': endpoint,
            'status_code': status,
            'expected_status': expected_status,
            'response_body': display_body,
            'passed': passed
        }
        self.test_results.append(result)

        # Print result
        status_indicator = "✓ PASSED" if passed else "✗ FAILED"
        print(f"\n{status_indicator}: {test_name}")
        print(f"  {method} {endpoint}")
        print(f"  Status: {status} (expected {expected_status})")
        if display_body:
            print(f"  Response: {display_body}...")

        return passed

    def run_all_tests(self):
        """Run all API tests"""
        print("=" * 80)
        print("SPRING BOOT INCIDENT TRACKER - REST API TEST SUITE")
        print("=" * 80)

        # Test 1: Health endpoint
        print("\n" + "=" * 80)
        print("TEST 1: Health Endpoint")
        print("=" * 80)
        self.test_endpoint(
            "Health Check",
            "GET",
            "/actuator/health",
            200
        )

        # Test 2: List all incidents
        print("\n" + "=" * 80)
        print("TEST 2: GET All Incidents")
        print("=" * 80)
        def check_incidents(data):
            return isinstance(data, list) and len(data) == 100

        self.test_endpoint(
            "List All Incidents (should return 100)",
            "GET",
            "/api/incidents",
            200,
            check_response=check_incidents
        )

        # Test 3: Filter by status
        print("\n" + "=" * 80)
        print("TEST 3: GET Incidents Filtered by Status")
        print("=" * 80)
        def check_open_status(data):
            # Allow 75-85 incidents (may vary based on test modifications)
            return isinstance(data, list) and 75 <= len(data) <= 85

        self.test_endpoint(
            "Filter by Status (OPEN) - should return ~80",
            "GET",
            "/api/incidents?status=OPEN",
            200,
            check_response=check_open_status
        )

        # Test 4: Filter by priority
        print("\n" + "=" * 80)
        print("TEST 4: GET Incidents Filtered by Priority")
        print("=" * 80)
        def check_critical_priority(data):
            # Allow 20-21 incidents (may be modified during tests)
            return isinstance(data, list) and 20 <= len(data) <= 21

        self.test_endpoint(
            "Filter by Priority (CRITICAL) - should return ~20",
            "GET",
            "/api/incidents?priority=CRITICAL",
            200,
            check_response=check_critical_priority
        )

        # Test 5: Get specific incident
        print("\n" + "=" * 80)
        print("TEST 5: GET Specific Incident by ID")
        print("=" * 80)
        def check_incident_id(data):
            return isinstance(data, dict) and data.get('id') == 1

        self.test_endpoint(
            "Get Incident by ID (ID=1)",
            "GET",
            "/api/incidents/1",
            200,
            check_response=check_incident_id
        )

        # Test 6: Create new incident
        print("\n" + "=" * 80)
        print("TEST 6: POST Create New Incident")
        print("=" * 80)
        create_data = {
            "title": "New Test Incident",
            "description": "Created via API test",
            "priority": "HIGH",
            "status": "OPEN",
            "assignee": "TestUser"
        }
        print(f"  Request Body: {json.dumps(create_data)}")

        def check_created_incident(data):
            if isinstance(data, dict) and data.get('id'):
                self.created_incident_id = data.get('id')
                return True
            return False

        self.test_endpoint(
            "Create New Incident",
            "POST",
            "/api/incidents",
            201,
            data=create_data,
            check_response=check_created_incident
        )

        if self.created_incident_id:
            print(f"  Created incident ID: {self.created_incident_id}")

        # Test 7: Update incident
        print("\n" + "=" * 80)
        print("TEST 7: PUT Update Incident")
        print("=" * 80)
        update_data = {
            "title": "Updated Test Incident",
            "description": "Updated via API test",
            "priority": "CRITICAL",
            "status": "IN_PROGRESS",
            "assignee": "UpdatedUser"
        }
        print(f"  Request Body: {json.dumps(update_data)}")

        def check_updated_incident(data):
            return (isinstance(data, dict) and
                    data.get('title') == "Updated Test Incident" and
                    data.get('priority') == "CRITICAL")

        self.test_endpoint(
            "Update Incident (ID=1)",
            "PUT",
            "/api/incidents/1",
            200,
            data=update_data,
            check_response=check_updated_incident
        )

        # Test 8: Update status
        print("\n" + "=" * 80)
        print("TEST 8: PATCH Update Status Only")
        print("=" * 80)
        status_data = "IN_PROGRESS"
        print(f"  Request Body: {json.dumps(status_data)}")

        def check_status_updated(data):
            return (isinstance(data, dict) and
                    data.get('status') == "IN_PROGRESS")

        self.test_endpoint(
            "Update Status (ID=1) to IN_PROGRESS",
            "PATCH",
            "/api/incidents/1/status",
            200,
            data=status_data,
            check_response=check_status_updated
        )

        # Test 9: Delete incident
        print("\n" + "=" * 80)
        print("TEST 9: DELETE Incident")
        print("=" * 80)

        if self.created_incident_id:
            endpoint = f"/api/incidents/{self.created_incident_id}"
            print(f"  Deleting incident ID: {self.created_incident_id}")
            self.test_endpoint(
                f"Delete Incident (ID={self.created_incident_id})",
                "DELETE",
                endpoint,
                204
            )

            # Test 10: Verify deletion
            print("\n" + "=" * 80)
            print("TEST 10: Verify Deletion")
            print("=" * 80)

            self.test_endpoint(
                f"Verify Deleted Incident Returns 404",
                "GET",
                endpoint,
                404
            )
        else:
            print("✗ SKIPPED: Could not delete - no incident was created")

    def print_summary(self):
        """Print test summary"""
        print("\n" + "=" * 80)
        print("TEST SUMMARY")
        print("=" * 80)

        total_tests = len(self.test_results)
        passed_tests = sum(1 for r in self.test_results if r['passed'])
        failed_tests = total_tests - passed_tests

        print(f"\nTotal Tests: {total_tests}")
        print(f"Passed: {passed_tests}")
        print(f"Failed: {failed_tests}")
        print(f"Success Rate: {(passed_tests/total_tests*100):.1f}%")

        print("\n" + "-" * 80)
        print("DETAILED RESULTS")
        print("-" * 80)

        for result in self.test_results:
            status_indicator = "✓" if result['passed'] else "✗"
            print(f"\n{status_indicator} {result['test_name']}")
            print(f"  {result['method']} {result['endpoint']}")
            print(f"  Status Code: {result['status_code']} (Expected: {result['expected_status']})")
            if result['response_body']:
                print(f"  Response: {result['response_body'][:150]}...")

        print("\n" + "=" * 80)
        print("ENDPOINT STATUS")
        print("=" * 80)

        working = [r['endpoint'] for r in self.test_results if r['passed']]
        failed = [r['endpoint'] for r in self.test_results if not r['passed']]

        print(f"\n✓ WORKING ENDPOINTS ({len(working)}):")
        for endpoint in working:
            print(f"  • {endpoint}")

        if failed:
            print(f"\n✗ FAILED ENDPOINTS ({len(failed)}):")
            for endpoint in failed:
                print(f"  • {endpoint}")
        else:
            print(f"\n✓ ALL ENDPOINTS WORKING!")

        print("\n" + "=" * 80)

        if passed_tests == total_tests:
            print("CONCLUSION: All REST API endpoints are functioning correctly!")
        else:
            print(f"CONCLUSION: {failed_tests} endpoint(s) need attention")

        print("=" * 80 + "\n")


def main():
    """Main entry point"""
    tester = APITester()
    tester.run_all_tests()
    tester.print_summary()


if __name__ == "__main__":
    main()
