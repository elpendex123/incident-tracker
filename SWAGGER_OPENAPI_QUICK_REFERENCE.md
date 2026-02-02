# Swagger UI & OpenAPI Quick Reference Guide

**Last Updated:** 2026-02-01
**Application:** Incident Tracker API
**Base URL:** http://localhost:8081

---

## Quick Access Links

### Interactive Documentation
- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **Alternative URL:** http://localhost:8081/swagger-ui/index.html

### Machine-Readable Specifications
- **OpenAPI JSON:** http://localhost:8081/api-docs
- **Alternative:** http://localhost:8081/v3/api-docs (currently unavailable - 500 error)

---

## API Overview

| Aspect | Value |
|--------|-------|
| **API Name** | Incident Tracker API |
| **Version** | 1.0.0 |
| **Base URL** | http://localhost:8081 |
| **API Prefix** | /api |
| **Documentation** | OpenAPI 3.0 spec |
| **Contact** | support@example.com |
| **License** | Apache 2.0 |

---

## REST Endpoints Quick Reference

### Incidents Management

#### Create Incident
```bash
POST /api/incidents
Content-Type: application/json

{
  "title": "Server down",
  "description": "Production server unresponsive",
  "priority": "CRITICAL",
  "status": "OPEN",
  "assignee": "John Doe"
}
```
**Response:** 201 Created

#### List All Incidents
```bash
GET /api/incidents
GET /api/incidents?status=OPEN
GET /api/incidents?priority=HIGH
GET /api/incidents?status=OPEN&priority=CRITICAL
```
**Response:** 200 OK

#### Get Incident by ID
```bash
GET /api/incidents/{id}
```
**Response:** 200 OK or 404 Not Found

#### Update Incident
```bash
PUT /api/incidents/{id}
Content-Type: application/json

{
  "title": "Updated title",
  "description": "Updated description",
  "priority": "HIGH",
  "status": "IN_PROGRESS",
  "assignee": "Jane Doe"
}
```
**Response:** 200 OK or 404 Not Found

#### Update Incident Status Only
```bash
PATCH /api/incidents/{id}/status
Content-Type: application/json

"RESOLVED"
```
**Response:** 200 OK or 404 Not Found

#### Delete Incident
```bash
DELETE /api/incidents/{id}
```
**Response:** 204 No Content or 404 Not Found

---

## Using Swagger UI

### Access Swagger UI
1. Open browser and navigate to: http://localhost:8081/swagger-ui.html
2. You'll see all available endpoints listed

### Test an Endpoint
1. Click on an endpoint to expand it
2. Click "Try it out" button
3. Fill in required parameters
4. Click "Execute"
5. View the response

### View Request/Response Details
1. Each endpoint shows:
   - Request parameters and schema
   - Response schema and examples
   - Possible HTTP status codes
   - Required vs optional fields

---

## Using OpenAPI Specification

### Download the Specification
```bash
curl -o api-spec.json http://localhost:8081/api-docs
```

### View the Specification
```bash
# Pretty print JSON
curl http://localhost:8081/api-docs | jq .

# View with Python
python3 -c "import json, requests; print(json.dumps(requests.get('http://localhost:8081/api-docs').json(), indent=2))"
```

### Generate Client Code
```bash
# Using OpenAPI Generator
openapi-generator-cli generate -i http://localhost:8081/api-docs \
  -g python -o ./python-client

openapi-generator-cli generate -i http://localhost:8081/api-docs \
  -g typescript-axios -o ./typescript-client

openapi-generator-cli generate -i http://localhost:8081/api-docs \
  -g java -o ./java-client
```

---

## Common curl Commands

### Create Incident
```bash
curl -X POST http://localhost:8081/api/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Critical Issue",
    "description": "Urgent fix required",
    "priority": "CRITICAL"
  }'
```

### List Incidents
```bash
curl http://localhost:8081/api/incidents
```

### List Incidents by Status
```bash
curl "http://localhost:8081/api/incidents?status=OPEN"
curl "http://localhost:8081/api/incidents?status=IN_PROGRESS"
curl "http://localhost:8081/api/incidents?status=RESOLVED"
curl "http://localhost:8081/api/incidents?status=CLOSED"
```

### List Incidents by Priority
```bash
curl "http://localhost:8081/api/incidents?priority=LOW"
curl "http://localhost:8081/api/incidents?priority=MEDIUM"
curl "http://localhost:8081/api/incidents?priority=HIGH"
curl "http://localhost:8081/api/incidents?priority=CRITICAL"
```

### Get Incident by ID
```bash
curl http://localhost:8081/api/incidents/1
```

### Update Incident
```bash
curl -X PUT http://localhost:8081/api/incidents/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Title",
    "description": "Updated Description",
    "priority": "HIGH",
    "status": "IN_PROGRESS",
    "assignee": "John Doe"
  }'
```

### Update Status Only
```bash
curl -X PATCH http://localhost:8081/api/incidents/1/status \
  -H "Content-Type: application/json" \
  -d '"RESOLVED"'
```

### Delete Incident
```bash
curl -X DELETE http://localhost:8081/api/incidents/1
```

---

## Python Examples

### Using requests library

```python
import requests
import json

BASE_URL = "http://localhost:8081"

# Create incident
def create_incident(title, description, priority="MEDIUM"):
    response = requests.post(
        f"{BASE_URL}/api/incidents",
        json={
            "title": title,
            "description": description,
            "priority": priority,
            "status": "OPEN"
        }
    )
    return response.json()

# Get all incidents
def get_all_incidents():
    response = requests.get(f"{BASE_URL}/api/incidents")
    return response.json()

# Get incident by ID
def get_incident(incident_id):
    response = requests.get(f"{BASE_URL}/api/incidents/{incident_id}")
    return response.json()

# Update incident
def update_incident(incident_id, data):
    response = requests.put(
        f"{BASE_URL}/api/incidents/{incident_id}",
        json=data
    )
    return response.json()

# Update status
def update_status(incident_id, status):
    response = requests.patch(
        f"{BASE_URL}/api/incidents/{incident_id}/status",
        json=status
    )
    return response.json()

# Delete incident
def delete_incident(incident_id):
    response = requests.delete(f"{BASE_URL}/api/incidents/{incident_id}")
    return response.status_code

# Example usage
if __name__ == "__main__":
    # Create
    new_incident = create_incident("Test Issue", "This is a test", "HIGH")
    print(f"Created: {new_incident}")

    # List
    incidents = get_all_incidents()
    print(f"Total incidents: {len(incidents)}")

    # Get one
    incident = get_incident(new_incident['id'])
    print(f"Retrieved: {incident}")

    # Update status
    updated = update_status(new_incident['id'], "IN_PROGRESS")
    print(f"Updated status: {updated}")

    # Delete
    status = delete_incident(new_incident['id'])
    print(f"Delete status: {status}")
```

---

## JavaScript/Node.js Examples

### Using fetch API

```javascript
const BASE_URL = "http://localhost:8081";

// Create incident
async function createIncident(title, description, priority = "MEDIUM") {
  const response = await fetch(`${BASE_URL}/api/incidents`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      title,
      description,
      priority,
      status: "OPEN"
    })
  });
  return response.json();
}

// Get all incidents
async function getAllIncidents() {
  const response = await fetch(`${BASE_URL}/api/incidents`);
  return response.json();
}

// Get incident by ID
async function getIncident(id) {
  const response = await fetch(`${BASE_URL}/api/incidents/${id}`);
  return response.json();
}

// Update incident
async function updateIncident(id, data) {
  const response = await fetch(`${BASE_URL}/api/incidents/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data)
  });
  return response.json();
}

// Update status
async function updateStatus(id, status) {
  const response = await fetch(`${BASE_URL}/api/incidents/${id}/status`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(status)
  });
  return response.json();
}

// Delete incident
async function deleteIncident(id) {
  const response = await fetch(`${BASE_URL}/api/incidents/${id}`, {
    method: "DELETE"
  });
  return response.status;
}

// Example usage
(async () => {
  // Create
  const newIncident = await createIncident("Test Issue", "This is a test", "HIGH");
  console.log("Created:", newIncident);

  // List
  const incidents = await getAllIncidents();
  console.log("Total incidents:", incidents.length);

  // Get one
  const incident = await getIncident(newIncident.id);
  console.log("Retrieved:", incident);

  // Update status
  const updated = await updateStatus(newIncident.id, "IN_PROGRESS");
  console.log("Updated status:", updated);

  // Delete
  const status = await deleteIncident(newIncident.id);
  console.log("Delete status:", status);
})();
```

---

## Data Models

### Incident Model

**Request (IncidentRequest):**
```json
{
  "title": "string (required)",
  "description": "string (optional)",
  "priority": "string (optional: LOW, MEDIUM, HIGH, CRITICAL)",
  "status": "string (optional: OPEN, IN_PROGRESS, RESOLVED, CLOSED)",
  "assignee": "string (optional)"
}
```

**Response (IncidentResponse):**
```json
{
  "id": "integer (auto-generated)",
  "title": "string",
  "description": "string",
  "priority": "string",
  "status": "string",
  "assignee": "string",
  "createdAt": "ISO 8601 datetime",
  "updatedAt": "ISO 8601 datetime",
  "resolvedAt": "ISO 8601 datetime (nullable)"
}
```

---

## Status Codes

| Code | Meaning | Example |
|------|---------|---------|
| 200 | Success | GET request successful |
| 201 | Created | POST request created resource |
| 204 | No Content | DELETE request successful |
| 400 | Bad Request | Invalid request body |
| 404 | Not Found | Resource doesn't exist |
| 500 | Server Error | Internal server error |

---

## Query Parameters

### List Incidents Filters

```
GET /api/incidents?status=OPEN
GET /api/incidents?priority=HIGH
GET /api/incidents?status=OPEN&priority=CRITICAL
```

**Supported Status Values:** OPEN, IN_PROGRESS, RESOLVED, CLOSED
**Supported Priority Values:** LOW, MEDIUM, HIGH, CRITICAL

---

## Path Parameters

### ID Parameter

All endpoints that take an ID use a path parameter:
```
GET /api/incidents/{id}
PUT /api/incidents/{id}
DELETE /api/incidents/{id}
PATCH /api/incidents/{id}/status
```

Replace `{id}` with the actual incident ID (integer).

---

## Headers

### Required Headers

For requests with body:
```
Content-Type: application/json
```

### Optional Headers

```
Accept: application/json
```

---

## Testing Endpoints

### Using Swagger UI (Interactive)

1. Go to http://localhost:8081/swagger-ui.html
2. Find the endpoint you want to test
3. Click "Try it out"
4. Fill in the parameters
5. Click "Execute"

### Using command line (curl)

```bash
# Test list endpoint
curl http://localhost:8081/api/incidents -v

# Test create endpoint
curl -X POST http://localhost:8081/api/incidents \
  -H "Content-Type: application/json" \
  -d '{"title":"Test"}' \
  -v
```

### Using IDE plugins

- **VS Code:** REST Client extension
- **IntelliJ:** Built-in HTTP Client
- **Postman:** Import OpenAPI spec

---

## Common Issues & Solutions

### Issue: Swagger UI not loading

**Solution:** Ensure application is running on port 8081
```bash
curl http://localhost:8081/swagger-ui.html
```

### Issue: OpenAPI spec returns 500

**Solution:** Try alternative endpoint `/api-docs` instead of `/v3/api-docs`

### Issue: Cannot reach application

**Solution:** Verify application is running
```bash
curl http://localhost:8081/api/incidents
```

### Issue: Invalid request body

**Solution:** Check JSON format and required fields. View examples in Swagger UI.

---

## Integration Examples

### Postman

1. Import OpenAPI spec:
   - Go to Postman → Import
   - Select "Link"
   - Enter: `http://localhost:8081/api-docs`
   - Click Import

2. Start making requests from Postman collections

### IntelliJ / VS Code

1. Create `.http` file with requests
2. Use REST Client to make requests
3. IntelliJ shows Swagger documentation in editor

### Generate TypeScript Types

```bash
# From OpenAPI spec
openapi-generator-cli generate \
  -i http://localhost:8081/api-docs \
  -g typescript \
  -o ./types
```

---

## Next Steps

1. **Explore Swagger UI** - Visit http://localhost:8081/swagger-ui.html
2. **Download OpenAPI Spec** - Save locally with `curl http://localhost:8081/api-docs > spec.json`
3. **Generate Client Code** - Use OpenAPI Generator for your language
4. **Integrate with Your App** - Use the generated client or make direct HTTP calls
5. **Check GraphQL** - Additional GraphQL API may be available

---

## Useful Resources

- **Swagger/OpenAPI Docs:** https://swagger.io/
- **OpenAPI Specification:** https://spec.openapis.org/
- **OpenAPI Generator:** https://openapi-generator.tech/
- **Postman Docs:** https://learning.postman.com/

---

**Last Updated:** 2026-02-01
**Status:** ✓ All Endpoints Operational
