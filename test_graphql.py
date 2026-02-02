#!/usr/bin/env python3
"""
GraphQL Test Suite for Spring Boot Incident Tracker
Tests all GraphQL queries and mutations
"""

import json
import urllib.request
import urllib.error
from typing import Dict, Tuple, Any
from datetime import datetime

# Configuration
GRAPHQL_URL = "http://localhost:8081/graphql"
TIMEOUT = 10

# Color codes for terminal output
class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    RESET = '\033[0m'
    BOLD = '\033[1m'

# Test results tracking
test_results = []
created_incident_id = None


def print_header(text: str) -> None:
    """Print a formatted header"""
    print(f"\n{Colors.BOLD}{Colors.BLUE}{'=' * 80}{Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.BLUE}{text.center(80)}{Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.BLUE}{'=' * 80}{Colors.RESET}\n")


def print_test_info(test_num: int, name: str) -> None:
    """Print test information"""
    print(f"\n{Colors.BOLD}TEST {test_num}: {name}{Colors.RESET}")
    print("-" * 80)


def print_result(status: str, message: str) -> None:
    """Print test result"""
    if status == "PASS":
        print(f"{Colors.GREEN}✓ {status}: {message}{Colors.RESET}")
    elif status == "FAIL":
        print(f"{Colors.RED}✗ {status}: {message}{Colors.RESET}")
    else:
        print(f"{Colors.YELLOW}⚠ {status}: {message}{Colors.RESET}")


def execute_graphql_query(query: str, variables: Dict[str, Any] = None) -> Tuple[int, Dict]:
    """
    Execute a GraphQL query and return the status code and response

    Args:
        query: GraphQL query/mutation string
        variables: Optional variables for the query

    Returns:
        Tuple of (status_code, response_dict)
    """
    payload = {"query": query}
    if variables:
        payload["variables"] = variables

    json_data = json.dumps(payload).encode('utf-8')
    headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }

    try:
        request = urllib.request.Request(
            GRAPHQL_URL,
            data=json_data,
            headers=headers,
            method='POST'
        )

        with urllib.request.urlopen(request, timeout=TIMEOUT) as response:
            status_code = response.status
            response_data = json.loads(response.read().decode('utf-8'))
            return status_code, response_data
    except urllib.error.HTTPError as e:
        try:
            response_data = json.loads(e.read().decode('utf-8'))
        except:
            response_data = {"error": str(e)}
        return e.code, response_data
    except Exception as e:
        return 0, {"error": f"Connection error: {str(e)}"}


def test_1_get_all_incidents() -> bool:
    """Test 1: Query all incidents"""
    test_num = 1
    test_name = "Get all incidents"
    print_test_info(test_num, test_name)

    query = """
    {
      incidents {
        id
        title
        priority
        status
        assignee
      }
    }
    """

    print(f"Query:\n{Colors.YELLOW}{query}{Colors.RESET}")

    status_code, response = execute_graphql_query(query)

    print(f"Status Code: {status_code}")
    print(f"Response: {json.dumps(response, indent=2)}")

    passed = status_code == 200 and "data" in response
    if passed and response.get("data", {}).get("incidents") is not None:
        incident_count = len(response["data"]["incidents"])
        print_result("PASS", f"Retrieved {incident_count} incidents")
    else:
        print_result("FAIL", "Failed to retrieve incidents")

    test_results.append({"test": test_num, "name": test_name, "passed": passed})
    return passed


def test_2_get_incident_by_id() -> bool:
    """Test 2: Query incident by ID"""
    test_num = 2
    test_name = "Get incident by ID"
    print_test_info(test_num, test_name)

    query = """
    {
      incident(id: 1) {
        id
        title
        description
        priority
        status
        assignee
        createdAt
      }
    }
    """

    print(f"Query:\n{Colors.YELLOW}{query}{Colors.RESET}")

    status_code, response = execute_graphql_query(query)

    print(f"Status Code: {status_code}")
    print(f"Response: {json.dumps(response, indent=2)}")

    passed = status_code == 200 and "data" in response
    if passed:
        incident = response.get("data", {}).get("incident")
        if incident:
            print_result("PASS", f"Retrieved incident ID: {incident.get('id')}")
        else:
            print_result("PASS", "Incident ID 1 not found (expected if no data)")
    else:
        print_result("FAIL", "Failed to retrieve incident by ID")

    test_results.append({"test": test_num, "name": test_name, "passed": passed})
    return passed


def test_3_filter_by_status() -> bool:
    """Test 3: Query incidents filtered by status"""
    test_num = 3
    test_name = "Filter by status"
    print_test_info(test_num, test_name)

    query = """
    {
      incidentsByStatus(status: OPEN) {
        id
        title
        status
      }
    }
    """

    print(f"Query:\n{Colors.YELLOW}{query}{Colors.RESET}")

    status_code, response = execute_graphql_query(query)

    print(f"Status Code: {status_code}")
    print(f"Response: {json.dumps(response, indent=2)}")

    passed = status_code == 200 and "data" in response
    if passed and response.get("data", {}).get("incidentsByStatus") is not None:
        incidents = response["data"]["incidentsByStatus"]
        print_result("PASS", f"Retrieved {len(incidents)} incidents with OPEN status")
    else:
        print_result("FAIL", "Failed to filter incidents by status")

    test_results.append({"test": test_num, "name": test_name, "passed": passed})
    return passed


def test_4_filter_by_priority() -> bool:
    """Test 4: Query incidents filtered by priority"""
    test_num = 4
    test_name = "Filter by priority"
    print_test_info(test_num, test_name)

    query = """
    {
      incidentsByPriority(priority: CRITICAL) {
        id
        title
        priority
      }
    }
    """

    print(f"Query:\n{Colors.YELLOW}{query}{Colors.RESET}")

    status_code, response = execute_graphql_query(query)

    print(f"Status Code: {status_code}")
    print(f"Response: {json.dumps(response, indent=2)}")

    passed = status_code == 200 and "data" in response
    if passed and response.get("data", {}).get("incidentsByPriority") is not None:
        incidents = response["data"]["incidentsByPriority"]
        print_result("PASS", f"Retrieved {len(incidents)} incidents with CRITICAL priority")
    else:
        print_result("FAIL", "Failed to filter incidents by priority")

    test_results.append({"test": test_num, "name": test_name, "passed": passed})
    return passed


def test_5_filter_by_assignee() -> bool:
    """Test 5: Query incidents filtered by assignee"""
    test_num = 5
    test_name = "Filter by assignee"
    print_test_info(test_num, test_name)

    query = """
    {
      incidentsByAssignee(assignee: "Alice") {
        id
        title
        assignee
      }
    }
    """

    print(f"Query:\n{Colors.YELLOW}{query}{Colors.RESET}")

    status_code, response = execute_graphql_query(query)

    print(f"Status Code: {status_code}")
    print(f"Response: {json.dumps(response, indent=2)}")

    passed = status_code == 200 and "data" in response
    if passed and response.get("data", {}).get("incidentsByAssignee") is not None:
        incidents = response["data"]["incidentsByAssignee"]
        print_result("PASS", f"Retrieved {len(incidents)} incidents assigned to Alice")
    else:
        print_result("FAIL", "Failed to filter incidents by assignee")

    test_results.append({"test": test_num, "name": test_name, "passed": passed})
    return passed


def test_6_create_incident() -> bool:
    """Test 6: Mutation to create incident"""
    global created_incident_id
    test_num = 6
    test_name = "Create incident"
    print_test_info(test_num, test_name)

    mutation = """
    mutation {
      createIncident(input: {
        title: "GraphQL Test Incident"
        description: "Created via GraphQL mutation"
        priority: HIGH
        assignee: "GraphQLTest"
      }) {
        id
        title
        createdAt
      }
    }
    """

    print(f"Mutation:\n{Colors.YELLOW}{mutation}{Colors.RESET}")

    status_code, response = execute_graphql_query(mutation)

    print(f"Status Code: {status_code}")
    print(f"Response: {json.dumps(response, indent=2)}")

    passed = status_code == 200 and "data" in response
    if passed:
        incident = response.get("data", {}).get("createIncident")
        if incident:
            created_incident_id = incident.get("id")
            print_result("PASS", f"Created incident with ID: {created_incident_id}")
        else:
            errors = response.get("errors", [])
            error_msg = errors[0].get("message") if errors else "Unknown error"
            print_result("FAIL", f"Creation failed: {error_msg}")
            passed = False
    else:
        print_result("FAIL", "Failed to create incident")

    test_results.append({"test": test_num, "name": test_name, "passed": passed})
    return passed


def test_7_update_incident() -> bool:
    """Test 7: Mutation to update incident"""
    test_num = 7
    test_name = "Update incident"
    print_test_info(test_num, test_name)

    # Use created incident if available, otherwise use ID 1
    incident_id = created_incident_id or "1"

    mutation = f"""
    mutation {{
      updateIncident(id: {incident_id}, input: {{
        title: "Updated via GraphQL"
        priority: CRITICAL
      }}) {{
        id
        title
        priority
        updatedAt
      }}
    }}
    """

    print(f"Mutation:\n{Colors.YELLOW}{mutation}{Colors.RESET}")

    status_code, response = execute_graphql_query(mutation)

    print(f"Status Code: {status_code}")
    print(f"Response: {json.dumps(response, indent=2)}")

    passed = status_code == 200 and "data" in response
    if passed:
        incident = response.get("data", {}).get("updateIncident")
        if incident:
            print_result("PASS", f"Updated incident ID: {incident.get('id')}")
        else:
            errors = response.get("errors", [])
            error_msg = errors[0].get("message") if errors else "Unknown error"
            print_result("FAIL", f"Update failed: {error_msg}")
            passed = False
    else:
        print_result("FAIL", "Failed to update incident")

    test_results.append({"test": test_num, "name": test_name, "passed": passed})
    return passed


def test_8_update_status() -> bool:
    """Test 8: Mutation to update incident status"""
    test_num = 8
    test_name = "Update status"
    print_test_info(test_num, test_name)

    # Use created incident if available, otherwise use ID 1
    incident_id = created_incident_id or "1"

    mutation = f"""
    mutation {{
      updateStatus(id: {incident_id}, status: IN_PROGRESS) {{
        id
        status
        updatedAt
      }}
    }}
    """

    print(f"Mutation:\n{Colors.YELLOW}{mutation}{Colors.RESET}")

    status_code, response = execute_graphql_query(mutation)

    print(f"Status Code: {status_code}")
    print(f"Response: {json.dumps(response, indent=2)}")

    passed = status_code == 200 and "data" in response
    if passed:
        incident = response.get("data", {}).get("updateStatus")
        if incident:
            print_result("PASS", f"Updated incident status to: {incident.get('status')}")
        else:
            errors = response.get("errors", [])
            error_msg = errors[0].get("message") if errors else "Unknown error"
            print_result("FAIL", f"Status update failed: {error_msg}")
            passed = False
    else:
        print_result("FAIL", "Failed to update incident status")

    test_results.append({"test": test_num, "name": test_name, "passed": passed})
    return passed


def test_9_delete_incident() -> bool:
    """Test 9: Mutation to delete incident"""
    test_num = 9
    test_name = "Delete incident"
    print_test_info(test_num, test_name)

    # Only delete if we created an incident
    if not created_incident_id:
        print_result("SKIP", "No incident created in test 6, skipping delete")
        test_results.append({"test": test_num, "name": test_name, "passed": True, "skipped": True})
        return True

    mutation = f"""
    mutation {{
      deleteIncident(id: {created_incident_id})
    }}
    """

    print(f"Mutation:\n{Colors.YELLOW}{mutation}{Colors.RESET}")

    status_code, response = execute_graphql_query(mutation)

    print(f"Status Code: {status_code}")
    print(f"Response: {json.dumps(response, indent=2)}")

    passed = status_code == 200 and "data" in response
    if passed:
        deleted = response.get("data", {}).get("deleteIncident")
        if deleted:
            print_result("PASS", f"Deleted incident ID: {created_incident_id}")
        else:
            errors = response.get("errors", [])
            error_msg = errors[0].get("message") if errors else "Unknown error"
            print_result("FAIL", f"Deletion failed: {error_msg}")
            passed = False
    else:
        print_result("FAIL", "Failed to delete incident")

    test_results.append({"test": test_num, "name": test_name, "passed": passed})
    return passed


def print_summary() -> None:
    """Print test summary"""
    print_header("TEST SUMMARY")

    total_tests = len(test_results)
    passed_tests = sum(1 for t in test_results if t["passed"] and not t.get("skipped", False))
    failed_tests = sum(1 for t in test_results if not t["passed"] and not t.get("skipped", False))
    skipped_tests = sum(1 for t in test_results if t.get("skipped", False))

    print(f"Total Tests: {total_tests}")
    print(f"Passed: {Colors.GREEN}{passed_tests}{Colors.RESET}")
    print(f"Failed: {Colors.RED}{failed_tests}{Colors.RESET}")
    print(f"Skipped: {Colors.YELLOW}{skipped_tests}{Colors.RESET}")

    # Pass rate
    testable = total_tests - skipped_tests
    if testable > 0:
        pass_rate = (passed_tests / testable) * 100
        print(f"\nPass Rate: {pass_rate:.1f}% ({passed_tests}/{testable})")

    # Detailed results
    print(f"\n{Colors.BOLD}Detailed Results:{Colors.RESET}")
    print("-" * 80)

    for result in test_results:
        status_icon = "✓" if result["passed"] else ("⊘" if result.get("skipped") else "✗")
        status_color = Colors.GREEN if result["passed"] else (Colors.YELLOW if result.get("skipped") else Colors.RED)
        skipped_text = " (SKIPPED)" if result.get("skipped") else ""
        print(f"{status_color}{status_icon} Test {result['test']}: {result['name']}{skipped_text}{Colors.RESET}")

    print("\n" + "=" * 80)

    if failed_tests == 0:
        print(f"{Colors.GREEN}{Colors.BOLD}All tests passed!{Colors.RESET}")
    else:
        print(f"{Colors.RED}{Colors.BOLD}{failed_tests} test(s) failed.{Colors.RESET}")


def main():
    """Run all tests"""
    print_header("GraphQL Test Suite for Incident Tracker")

    print(f"{Colors.BOLD}Test Configuration:{Colors.RESET}")
    print(f"GraphQL URL: {GRAPHQL_URL}")
    print(f"Timeout: {TIMEOUT}s")
    print(f"Timestamp: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")

    # Check if server is reachable
    print(f"\n{Colors.BOLD}Checking server connectivity...{Colors.RESET}")
    try:
        status_code, _ = execute_graphql_query("{ __typename }")
        if status_code == 200:
            print(f"{Colors.GREEN}✓ Server is reachable{Colors.RESET}")
        else:
            print(f"{Colors.YELLOW}⚠ Server responded with status {status_code}{Colors.RESET}")
    except Exception as e:
        print(f"{Colors.RED}✗ Cannot connect to server: {e}{Colors.RESET}")
        print(f"{Colors.RED}Make sure the Spring Boot application is running on {GRAPHQL_URL}{Colors.RESET}")
        return

    # Run all tests
    test_1_get_all_incidents()
    test_2_get_incident_by_id()
    test_3_filter_by_status()
    test_4_filter_by_priority()
    test_5_filter_by_assignee()
    test_6_create_incident()
    test_7_update_incident()
    test_8_update_status()
    test_9_delete_incident()

    # Print summary
    print_summary()


if __name__ == "__main__":
    main()
