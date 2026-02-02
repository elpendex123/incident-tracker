#!/usr/bin/env python3
"""
Swagger UI and OpenAPI Endpoint Testing Script

Tests the Swagger UI and OpenAPI specification endpoints for the
Spring Boot Incident Tracker application.
"""

import requests
import json
import sys
from typing import Tuple, Dict, Any
from urllib.parse import urljoin

# Configuration
BASE_URL = "http://localhost:8081"
TIMEOUT = 10

# ANSI color codes for output
GREEN = "\033[92m"
RED = "\033[91m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
RESET = "\033[0m"
BOLD = "\033[1m"

# Expected REST endpoints
EXPECTED_ENDPOINTS = {
    "POST /api/incidents": "Create a new incident",
    "GET /api/incidents": "List all incidents",
    "GET /api/incidents/{id}": "Get incident by ID",
    "PUT /api/incidents/{id}": "Update incident",
    "PATCH /api/incidents/{id}/status": "Update incident status",
    "DELETE /api/incidents/{id}": "Delete incident",
}


class SwaggerOpenAPITester:
    """Test Swagger UI and OpenAPI endpoints"""

    def __init__(self, base_url: str):
        self.base_url = base_url
        self.results = []
        self.session = requests.Session()

    def test_endpoint(
        self, method: str, endpoint: str, expected_status: int = 200
    ) -> Tuple[bool, Dict[str, Any]]:
        """
        Test a single endpoint

        Args:
            method: HTTP method (GET, POST, etc.)
            endpoint: Endpoint path
            expected_status: Expected HTTP status code

        Returns:
            Tuple of (success, result_dict)
        """
        url = urljoin(self.base_url, endpoint)
        result = {
            "method": method,
            "url": url,
            "endpoint": endpoint,
            "status_code": None,
            "content_type": None,
            "success": False,
            "message": "",
            "data": None,
        }

        try:
            if method.upper() == "GET":
                response = self.session.get(url, timeout=TIMEOUT)
            else:
                response = self.session.request(method, url, timeout=TIMEOUT)

            result["status_code"] = response.status_code
            result["content_type"] = response.headers.get("content-type", "N/A")
            result["success"] = response.status_code == expected_status

            if result["success"]:
                result["message"] = f"OK ({response.status_code})"
                # Try to parse JSON if available
                try:
                    result["data"] = response.json()
                except:
                    result["data"] = response.text[:200] if response.text else None
            else:
                result["message"] = f"Expected {expected_status}, got {response.status_code}"

        except requests.exceptions.Timeout:
            result["message"] = f"Timeout after {TIMEOUT}s"
        except requests.exceptions.ConnectionError:
            result["message"] = "Connection failed - application may not be running"
        except Exception as e:
            result["message"] = f"Error: {str(e)}"

        self.results.append(result)
        return result["success"], result

    def test_swagger_ui(self) -> bool:
        """Test Swagger UI accessibility"""
        print(f"\n{BOLD}{BLUE}[1] Testing Swagger UI Endpoints{RESET}")
        print("-" * 70)

        # Test main Swagger UI endpoint
        success1, result1 = self.test_endpoint("GET", "/swagger-ui.html")
        self._print_result(result1)

        # Test alternative Swagger UI endpoint
        success2, result2 = self.test_endpoint("GET", "/swagger-ui/index.html")
        self._print_result(result2)

        return success1 or success2

    def test_openapi_spec(self) -> Tuple[bool, Dict[str, Any]]:
        """Test OpenAPI specification endpoint"""
        print(f"\n{BOLD}{BLUE}[2] Testing OpenAPI Specification Endpoints{RESET}")
        print("-" * 70)

        # Test /api-docs endpoint
        success1, result1 = self.test_endpoint("GET", "/api-docs")
        self._print_result(result1)

        # Test /v3/api-docs endpoint
        success2, result2 = self.test_endpoint("GET", "/v3/api-docs")
        self._print_result(result2)

        # Return the first successful result
        if success1:
            return True, result1
        elif success2:
            return True, result2
        else:
            return False, result1

    def test_rest_endpoints_in_spec(self, spec_data: Dict[str, Any]) -> bool:
        """
        Verify all expected REST endpoints are documented in OpenAPI spec

        Args:
            spec_data: Parsed OpenAPI specification JSON

        Returns:
            True if all endpoints are found, False otherwise
        """
        print(f"\n{BOLD}{BLUE}[3] Verifying REST Endpoints in OpenAPI Spec{RESET}")
        print("-" * 70)

        if not spec_data:
            print(f"{RED}✗ FAILED: No OpenAPI spec data available{RESET}")
            return False

        # Extract paths from OpenAPI spec
        paths = spec_data.get("paths", {})

        if not paths:
            print(f"{RED}✗ FAILED: No paths found in OpenAPI spec{RESET}")
            return False

        print(f"Found {len(paths)} documented paths in OpenAPI spec\n")

        # Map endpoint paths to methods
        documented_endpoints = {}
        for path, methods in paths.items():
            for method in methods.keys():
                if method.lower() in ["get", "post", "put", "patch", "delete"]:
                    documented_endpoints[f"{method.upper()} {path}"] = True

        # Check each expected endpoint
        all_found = True
        for expected_endpoint, description in EXPECTED_ENDPOINTS.items():
            found = False
            # Check exact match and variations
            for doc_endpoint in documented_endpoints.keys():
                if doc_endpoint.endswith(expected_endpoint.split()[-1]):
                    found = True
                    break

            if found:
                print(f"{GREEN}✓ {expected_endpoint}{RESET}")
                print(f"  Description: {description}\n")
            else:
                print(f"{RED}✗ {expected_endpoint}{RESET}")
                print(f"  Description: {description}")
                print(f"  Status: NOT FOUND in OpenAPI spec\n")
                all_found = False

        return all_found

    def print_openapi_summary(self, spec_data: Dict[str, Any]):
        """Print summary of OpenAPI specification"""
        if not spec_data:
            return

        print(f"\n{BOLD}{BLUE}OpenAPI Specification Summary{RESET}")
        print("-" * 70)

        # Basic info
        info = spec_data.get("info", {})
        print(f"Title: {info.get('title', 'N/A')}")
        print(f"Version: {info.get('version', 'N/A')}")
        print(f"Description: {info.get('description', 'N/A')}\n")

        # Paths and operations
        paths = spec_data.get("paths", {})
        total_operations = sum(
            len([m for m in methods.keys() if m.lower() in ["get", "post", "put", "patch", "delete"]])
            for methods in paths.values()
        )

        print(f"Total Documented Paths: {len(paths)}")
        print(f"Total Operations: {total_operations}\n")

        # List all paths
        print("Documented Paths:")
        for path in sorted(paths.keys()):
            methods = [m.upper() for m in paths[path].keys() if m.lower() in ["get", "post", "put", "patch", "delete"]]
            print(f"  {', '.join(methods)} {path}")

    def _print_result(self, result: Dict[str, Any]):
        """Pretty print a test result"""
        status_str = f"{GREEN}✓ PASSED{RESET}" if result["success"] else f"{RED}✗ FAILED{RESET}"
        print(f"\n{BOLD}Request:{RESET} {result['method']} {result['endpoint']}")
        print(f"{BOLD}URL:{RESET} {result['url']}")
        print(f"{BOLD}Status:{RESET} {result['status_code']} - {result['message']}")
        if result["content_type"]:
            print(f"{BOLD}Content-Type:{RESET} {result['content_type']}")
        print(f"{BOLD}Result:{RESET} {status_str}")

    def run_all_tests(self) -> Dict[str, Any]:
        """Run all tests and return summary"""
        print(f"\n{BOLD}{YELLOW}========================================{RESET}")
        print(f"{BOLD}{YELLOW}Swagger UI & OpenAPI Endpoint Testing{RESET}")
        print(f"{BOLD}{YELLOW}========================================{RESET}")
        print(f"Base URL: {self.base_url}")
        print(f"Timeout: {TIMEOUT}s")

        # Test Swagger UI
        swagger_ok = self.test_swagger_ui()

        # Test OpenAPI spec
        openapi_ok, openapi_result = self.test_openapi_spec()

        # If OpenAPI spec available, verify endpoints
        endpoints_ok = False
        spec_data = None
        if openapi_ok and openapi_result.get("data"):
            try:
                spec_data = openapi_result["data"]
                if isinstance(spec_data, str):
                    spec_data = json.loads(spec_data)
                endpoints_ok = self.test_rest_endpoints_in_spec(spec_data)
                self.print_openapi_summary(spec_data)
            except Exception as e:
                print(f"{RED}Error parsing OpenAPI spec: {e}{RESET}")

        # Generate summary
        summary = self._generate_summary(swagger_ok, openapi_ok, endpoints_ok)
        self._print_summary(summary)

        return summary

    def _generate_summary(
        self, swagger_ok: bool, openapi_ok: bool, endpoints_ok: bool
    ) -> Dict[str, Any]:
        """Generate test summary"""
        total_tests = len(self.results)
        passed_tests = sum(1 for r in self.results if r["success"])

        return {
            "total_tests": total_tests,
            "passed_tests": passed_tests,
            "failed_tests": total_tests - passed_tests,
            "swagger_accessible": swagger_ok,
            "openapi_available": openapi_ok,
            "endpoints_documented": endpoints_ok,
            "overall_success": swagger_ok and openapi_ok,
        }

    def _print_summary(self, summary: Dict[str, Any]):
        """Print final summary"""
        print(f"\n{BOLD}{YELLOW}========================================{RESET}")
        print(f"{BOLD}{YELLOW}Test Summary{RESET}")
        print(f"{BOLD}{YELLOW}========================================{RESET}")

        print(f"\n{BOLD}Test Execution Results:{RESET}")
        print(f"  Total Tests: {summary['total_tests']}")
        print(f"  Passed: {summary['passed_tests']}")
        print(f"  Failed: {summary['failed_tests']}")

        print(f"\n{BOLD}Swagger UI Status:{RESET}")
        status = f"{GREEN}✓ ACCESSIBLE{RESET}" if summary["swagger_accessible"] else f"{RED}✗ NOT ACCESSIBLE{RESET}"
        print(f"  {status}")

        print(f"\n{BOLD}OpenAPI Specification:{RESET}")
        status = f"{GREEN}✓ AVAILABLE{RESET}" if summary["openapi_available"] else f"{RED}✗ NOT AVAILABLE{RESET}"
        print(f"  {status}")

        print(f"\n{BOLD}REST Endpoints Documentation:{RESET}")
        if summary["openapi_available"]:
            status = (
                f"{GREEN}✓ FULLY DOCUMENTED{RESET}"
                if summary["endpoints_documented"]
                else f"{YELLOW}⚠ PARTIALLY DOCUMENTED{RESET}"
            )
        else:
            status = f"{RED}✗ UNABLE TO VERIFY{RESET}"
        print(f"  {status}")

        print(f"\n{BOLD}Overall Status:{RESET}")
        if summary["overall_success"]:
            print(f"  {GREEN}✓ SUCCESS - All documentation endpoints are accessible{RESET}")
        else:
            print(f"  {RED}✗ FAILED - Some endpoints are not accessible{RESET}")


def main():
    """Main entry point"""
    tester = SwaggerOpenAPITester(BASE_URL)
    summary = tester.run_all_tests()

    # Exit with appropriate code
    sys.exit(0 if summary["overall_success"] else 1)


if __name__ == "__main__":
    main()
