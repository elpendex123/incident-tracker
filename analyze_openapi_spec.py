#!/usr/bin/env python3
"""
OpenAPI Specification Analysis Script

Provides detailed analysis of the OpenAPI specification including:
- Endpoint details
- Request/response schemas
- Parameters
- Data types
"""

import requests
import json
import sys
from typing import Dict, Any, List
from urllib.parse import urljoin

# Configuration
BASE_URL = "http://localhost:8081"
TIMEOUT = 10

# ANSI color codes
GREEN = "\033[92m"
RED = "\033[91m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
CYAN = "\033[96m"
RESET = "\033[0m"
BOLD = "\033[1m"


class OpenAPIAnalyzer:
    """Analyze and display OpenAPI specification details"""

    def __init__(self, base_url: str):
        self.base_url = base_url
        self.spec = None
        self.session = requests.Session()

    def fetch_spec(self) -> bool:
        """Fetch the OpenAPI specification"""
        try:
            url = urljoin(self.base_url, "/api-docs")
            print(f"\n{BOLD}Fetching OpenAPI specification from: {url}{RESET}")

            response = self.session.get(url, timeout=TIMEOUT)
            if response.status_code == 200:
                self.spec = response.json()
                print(f"{GREEN}✓ Successfully retrieved OpenAPI specification{RESET}")
                return True
            else:
                print(f"{RED}✗ Failed to fetch specification (Status: {response.status_code}){RESET}")
                return False
        except Exception as e:
            print(f"{RED}✗ Error fetching specification: {str(e)}{RESET}")
            return False

    def print_api_info(self):
        """Print API information"""
        if not self.spec:
            return

        print(f"\n{BOLD}{CYAN}API Information{RESET}")
        print("=" * 80)

        info = self.spec.get("info", {})
        print(f"{BOLD}Title:{RESET} {info.get('title', 'N/A')}")
        print(f"{BOLD}Version:{RESET} {info.get('version', 'N/A')}")
        print(f"{BOLD}Description:{RESET} {info.get('description', 'N/A')}")

        if "contact" in info:
            contact = info["contact"]
            print(f"\n{BOLD}Contact Information:{RESET}")
            if "name" in contact:
                print(f"  Name: {contact['name']}")
            if "email" in contact:
                print(f"  Email: {contact['email']}")
            if "url" in contact:
                print(f"  URL: {contact['url']}")

        if "license" in info:
            license_info = info["license"]
            print(f"\n{BOLD}License:{RESET}")
            print(f"  {license_info.get('name', 'N/A')}")

    def print_server_info(self):
        """Print server information"""
        if not self.spec:
            return

        servers = self.spec.get("servers", [])
        if not servers:
            return

        print(f"\n{BOLD}{CYAN}Server Configuration{RESET}")
        print("=" * 80)

        for i, server in enumerate(servers, 1):
            print(f"\n{BOLD}Server {i}:{RESET}")
            print(f"  URL: {server.get('url', 'N/A')}")
            if "description" in server:
                print(f"  Description: {server['description']}")
            if "variables" in server:
                print(f"  Variables: {json.dumps(server['variables'], indent=2)}")

    def print_paths_summary(self):
        """Print paths summary"""
        if not self.spec:
            return

        paths = self.spec.get("paths", {})
        if not paths:
            print(f"\n{RED}No paths found in specification{RESET}")
            return

        print(f"\n{BOLD}{CYAN}API Endpoints Summary{RESET}")
        print("=" * 80)
        print(f"\n{BOLD}Total Paths: {len(paths)}{RESET}\n")

        for path, methods in sorted(paths.items()):
            method_list = []
            for method in methods.keys():
                if method.lower() in ["get", "post", "put", "patch", "delete"]:
                    method_upper = method.upper()
                    method_list.append(method_upper)

            if method_list:
                methods_str = f"{BOLD}{', '.join(method_list)}{RESET}"
                print(f"{methods_str} {CYAN}{path}{RESET}")

    def print_endpoint_details(self):
        """Print detailed information for each endpoint"""
        if not self.spec:
            return

        paths = self.spec.get("paths", {})
        if not paths:
            return

        print(f"\n{BOLD}{CYAN}Detailed Endpoint Information{RESET}")
        print("=" * 80)

        for path, methods in sorted(paths.items()):
            for method, details in methods.items():
                if method.lower() not in ["get", "post", "put", "patch", "delete"]:
                    continue

                self._print_endpoint(method.upper(), path, details)

    def _print_endpoint(self, method: str, path: str, details: Dict[str, Any]):
        """Print details for a single endpoint"""
        # Determine method color
        method_color = {
            "GET": BLUE,
            "POST": GREEN,
            "PUT": YELLOW,
            "PATCH": CYAN,
            "DELETE": RED,
        }.get(method, RESET)

        print(f"\n{BOLD}{method_color}{method}{RESET} {CYAN}{path}{RESET}")
        print("-" * 80)

        # Summary/Description
        if "summary" in details:
            print(f"{BOLD}Summary:{RESET} {details['summary']}")
        if "description" in details:
            print(f"{BOLD}Description:{RESET} {details['description']}")

        # Operation ID
        if "operationId" in details:
            print(f"{BOLD}Operation ID:{RESET} {details['operationId']}")

        # Tags
        if "tags" in details:
            tags = ", ".join(details["tags"])
            print(f"{BOLD}Tags:{RESET} {tags}")

        # Parameters
        parameters = details.get("parameters", [])
        if parameters:
            print(f"\n{BOLD}Parameters:{RESET}")
            for param in parameters:
                param_name = param.get("name", "unknown")
                param_in = param.get("in", "unknown")
                param_required = "required" if param.get("required", False) else "optional"
                param_type = param.get("schema", {}).get("type", "unknown")
                param_desc = param.get("description", "")

                print(f"  • {param_name} ({param_in}, {param_type}, {param_required})")
                if param_desc:
                    print(f"    Description: {param_desc}")

        # Request Body
        request_body = details.get("requestBody", {})
        if request_body:
            print(f"\n{BOLD}Request Body:{RESET}")
            required = "required" if request_body.get("required", False) else "optional"
            print(f"  Status: {required}")

            content = request_body.get("content", {})
            for content_type in content.keys():
                print(f"  Content-Type: {content_type}")
                schema = content[content_type].get("schema", {})
                if "$ref" in schema:
                    print(f"  Schema Reference: {schema['$ref']}")
                elif "type" in schema:
                    print(f"  Type: {schema['type']}")

        # Responses
        responses = details.get("responses", {})
        if responses:
            print(f"\n{BOLD}Responses:{RESET}")
            for status_code, response_details in responses.items():
                description = response_details.get("description", "")
                print(f"  {status_code}: {description}")

                content = response_details.get("content", {})
                if content:
                    for content_type in content.keys():
                        schema = content[content_type].get("schema", {})
                        if "$ref" in schema:
                            print(f"    Schema Reference: {schema['$ref']}")

    def print_schemas(self):
        """Print component schemas"""
        if not self.spec:
            return

        components = self.spec.get("components", {})
        schemas = components.get("schemas", {})

        if not schemas:
            return

        print(f"\n{BOLD}{CYAN}Component Schemas{RESET}")
        print("=" * 80)
        print(f"\n{BOLD}Defined Schemas: {len(schemas)}{RESET}\n")

        for schema_name, schema_details in sorted(schemas.items()):
            print(f"{BOLD}{schema_name}{RESET}")
            print("-" * 40)

            schema_type = schema_details.get("type", "unknown")
            print(f"Type: {schema_type}")

            if "description" in schema_details:
                print(f"Description: {schema_details['description']}")

            properties = schema_details.get("properties", {})
            if properties:
                print(f"Properties:")
                for prop_name, prop_details in properties.items():
                    prop_type = prop_details.get("type", "unknown")
                    prop_format = prop_details.get("format", "")
                    format_str = f" ({prop_format})" if prop_format else ""
                    required_mark = "*" if prop_name in schema_details.get("required", []) else ""

                    print(f"  • {prop_name}{required_mark}: {prop_type}{format_str}")
                    if "description" in prop_details:
                        print(f"    {prop_details['description']}")

            required = schema_details.get("required", [])
            if required:
                print(f"Required Fields: {', '.join(required)}")

            print()

    def print_statistics(self):
        """Print specification statistics"""
        if not self.spec:
            return

        print(f"\n{BOLD}{CYAN}Specification Statistics{RESET}")
        print("=" * 80)

        paths = self.spec.get("paths", {})
        components = self.spec.get("components", {})
        schemas = components.get("schemas", {})

        # Count operations
        total_ops = 0
        ops_by_method = {}
        for methods in paths.values():
            for method in methods.keys():
                if method.lower() in ["get", "post", "put", "patch", "delete"]:
                    total_ops += 1
                    method_upper = method.upper()
                    ops_by_method[method_upper] = ops_by_method.get(method_upper, 0) + 1

        print(f"\n{BOLD}Endpoints:{RESET}")
        print(f"  Total Paths: {len(paths)}")
        print(f"  Total Operations: {total_ops}")
        print(f"\n{BOLD}Operations by HTTP Method:{RESET}")
        for method in sorted(ops_by_method.keys()):
            count = ops_by_method[method]
            print(f"  {method}: {count}")

        print(f"\n{BOLD}Schemas:{RESET}")
        print(f"  Total Defined Schemas: {len(schemas)}")
        if schemas:
            print(f"  Schema Names: {', '.join(sorted(schemas.keys()))}")

        security = self.spec.get("security", [])
        print(f"\n{BOLD}Security:{RESET}")
        print(f"  Security Schemes: {len(self.spec.get('components', {}).get('securitySchemes', {}))}")
        print(f"  Global Security Requirements: {len(security)}")

    def run_analysis(self):
        """Run complete analysis"""
        print(f"\n{BOLD}{YELLOW}{'=' * 80}{RESET}")
        print(f"{BOLD}{YELLOW}OpenAPI Specification Analysis{RESET}")
        print(f"{BOLD}{YELLOW}{'=' * 80}{RESET}")

        if not self.fetch_spec():
            sys.exit(1)

        self.print_api_info()
        self.print_server_info()
        self.print_statistics()
        self.print_paths_summary()
        self.print_endpoint_details()
        self.print_schemas()

        print(f"\n{BOLD}{YELLOW}{'=' * 80}{RESET}")
        print(f"{BOLD}{GREEN}✓ Analysis Complete{RESET}")
        print(f"{BOLD}{YELLOW}{'=' * 80}{RESET}\n")


def main():
    """Main entry point"""
    analyzer = OpenAPIAnalyzer(BASE_URL)
    analyzer.run_analysis()


if __name__ == "__main__":
    main()
