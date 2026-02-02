# GraphQL Test Guide - Quick Reference

## Overview

A comprehensive Python test suite using `urllib` to validate all GraphQL endpoints for the Spring Boot Incident Tracker application.

## Quick Start

### Prerequisites
- Python 3.x installed
- Spring Boot application running on `http://localhost:8081/graphql`
- PostgreSQL database with incident data

### Run Tests
```bash
python3 test_graphql.py
```

## Test Script Details

**Location:** `/home/enrique/CLAUDE/springboot_python_postgres_project/test_graphql.py`

**Size:** ~500 lines of Python code

**Dependencies:** None (uses only Python standard library)

**Configuration:**
- GraphQL Endpoint: `http://localhost:8081/graphql`
- Request Timeout: 10 seconds
- Response Format: JSON

## Test Coverage (9 Tests)

### 1. Query: Get All Incidents
```graphql
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
**Purpose:** Retrieve complete incident list
**Expected:** 200 status, array of incident objects

### 2. Query: Get Incident by ID
```graphql
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
```
**Purpose:** Retrieve single incident with details
**Expected:** 200 status, single incident object or null

### 3. Query: Filter by Status
```graphql
{
  incidentsByStatus(status: OPEN) {
    id
    title
    status
  }
}
```
**Purpose:** Filter incidents by status (OPEN, IN_PROGRESS, RESOLVED, CLOSED)
**Expected:** 200 status, filtered incident array

### 4. Query: Filter by Priority
```graphql
{
  incidentsByPriority(priority: CRITICAL) {
    id
    title
    priority
  }
}
```
**Purpose:** Filter incidents by priority (LOW, MEDIUM, HIGH, CRITICAL)
**Expected:** 200 status, filtered incident array

### 5. Query: Filter by Assignee
```graphql
{
  incidentsByAssignee(assignee: "Alice") {
    id
    title
    assignee
  }
}
```
**Purpose:** Filter incidents by assigned person
**Expected:** 200 status, filtered incident array

### 6. Mutation: Create Incident
```graphql
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
```
**Purpose:** Create new incident
**Expected:** 200 status, new incident with auto-generated ID
**Side Effect:** Inserts record in database

### 7. Mutation: Update Incident
```graphql
mutation {
  updateIncident(id: 108, input: {
    title: "Updated via GraphQL"
    priority: CRITICAL
  }) {
    id
    title
    priority
    updatedAt
  }
}
```
**Purpose:** Update existing incident fields
**Expected:** 200 status, updated incident object
**Side Effect:** Modifies record in database

### 8. Mutation: Update Status
```graphql
mutation {
  updateStatus(id: 108, status: IN_PROGRESS) {
    id
    status
    updatedAt
  }
}
```
**Purpose:** Update incident status with auto-timestamp
**Expected:** 200 status, incident with new status
**Side Effect:** Sets resolvedAt timestamp if status = RESOLVED

### 9. Mutation: Delete Incident
```graphql
mutation {
  deleteIncident(id: 108)
}
```
**Purpose:** Delete incident created in Test 6
**Expected:** 200 status, true response
**Side Effect:** Removes record from database

## Output Interpretation

### Console Colors
- **Green (✓):** Test passed
- **Red (✗):** Test failed
- **Yellow (⚠):** Warning or skipped
- **Blue:** Section headers

### Sample Output
```
TEST 1: Get all incidents
--------------------------------------------------------------------------------
Query:
{
  incidents {
    ...
  }
}
Status Code: 200
Response: {...}
✓ PASS: Retrieved 100 incidents

TEST SUMMARY
================================================================================
Total Tests: 9
Passed: 9
Failed: 0
Skipped: 0

Pass Rate: 100.0% (9/9)

✓ Test 1: Get all incidents
✓ Test 2: Get incident by ID
✓ Test 3: Filter by status
✓ Test 4: Filter by priority
✓ Test 5: Filter by assignee
✓ Test 6: Create incident
✓ Test 7: Update incident
✓ Test 8: Update status
✓ Test 9: Delete incident

All tests passed!
```

## Customization

### Change GraphQL Endpoint
Edit line 17 in `test_graphql.py`:
```python
GRAPHQL_URL = "http://your-server:port/graphql"
```

### Change Timeout
Edit line 18:
```python
TIMEOUT = 30  # seconds
```

### Modify Filter Values
For Test 5, change assignee in the query:
```python
query = """
{
  incidentsByAssignee(assignee: "Charlie") {
    ...
  }
}
"""
```

### Add New Tests
Add a new function following the pattern:
```python
def test_10_custom_operation() -> bool:
    """Test description"""
    test_num = 10
    test_name = "Custom operation"
    print_test_info(test_num, test_name)

    query = """{ custom query }"""

    status_code, response = execute_graphql_query(query)

    passed = status_code == 200
    if passed:
        print_result("PASS", "Operation succeeded")
    else:
        print_result("FAIL", "Operation failed")

    test_results.append({"test": test_num, "name": test_name, "passed": passed})
    return passed
```

Then call it in `main()`:
```python
def main():
    ...
    test_1_get_all_incidents()
    ...
    test_10_custom_operation()  # Add this
    ...
```

## Troubleshooting

### "Connection error"
- Ensure Spring Boot application is running
- Verify endpoint URL is correct
- Check firewall rules

### "HTTP 400 Bad Request"
- Check GraphQL query syntax
- Verify field names match schema
- Ensure proper JSON formatting

### "HTTP 404 Not Found"
- Verify GraphQL endpoint path: `/graphql`
- Check application is serving on expected port

### "HTTP 500 Internal Server Error"
- Check application logs
- Verify database is accessible
- Ensure data integrity

### Tests Pass Individually but Fail Together
- May indicate database transaction issues
- Check for resource cleanup between tests
- Review database constraints

## API Reference

### Enum Values
**Priority:** `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
**Status:** `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`

### Input Types

**CreateIncidentInput:**
```graphql
{
  title: String!          # Required
  description: String     # Optional
  priority: Priority      # Optional, default: LOW
  assignee: String        # Optional
}
```

**UpdateIncidentInput:**
```graphql
{
  title: String           # Optional
  description: String     # Optional
  priority: Priority      # Optional
  status: Status          # Optional
  assignee: String        # Optional
}
```

### Response Type: Incident
```graphql
{
  id: ID!                 # Auto-generated
  title: String!          # User-provided
  description: String     # User-provided
  priority: Priority!     # Default: LOW
  status: Status!         # Default: OPEN
  assignee: String        # Optional
  createdAt: String!      # Auto-set (ISO 8601)
  updatedAt: String!      # Auto-set (ISO 8601)
  resolvedAt: String      # Set when status = RESOLVED
}
```

## Performance Metrics

Expected response times:
- Single incident query: < 50ms
- Filtered queries: < 100ms
- Mutations: < 100ms
- Bulk operations (all incidents): < 150ms

## CI/CD Integration

### GitHub Actions Example
```yaml
- name: Run GraphQL Tests
  run: python3 test_graphql.py
```

### Jenkins Pipeline Example
```groovy
stage('GraphQL Tests') {
    steps {
        sh 'python3 test_graphql.py'
    }
}
```

### Docker Example
```bash
docker run -it \
  -e GRAPHQL_URL=http://app:8081/graphql \
  python:3.x \
  python3 test_graphql.py
```

## Files Generated

During test execution:
- Test results printed to console
- No files written by default
- Console output can be redirected:
```bash
python3 test_graphql.py > test_results.log 2>&1
```

## Related Documentation

- GraphQL Schema: `/src/main/resources/graphql/schema.graphqls`
- GraphQL Controller: `/src/main/java/com/example/incidenttracker/graphql/IncidentGraphQLController.java`
- Test Results: `GRAPHQL_TEST_RESULTS.md`
- REST API Tests: `test_api.py`

## Support

For issues or questions:
1. Review test output for error messages
2. Check application logs
3. Verify database state
4. Consult GRAPHQL_TEST_RESULTS.md for full test report
5. Review Spring Boot logs for detailed errors
