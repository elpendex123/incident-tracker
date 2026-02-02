# Quick Start Guide - Incident Tracker

## 🚀 Application is Running

The Incident Tracker application is currently deployed and running at **http://localhost:8081**

---

## 📝 REST API Quick Commands

### Create an Incident
```bash
curl -X POST http://localhost:8081/api/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Server Down",
    "description": "Production server is unresponsive",
    "priority": "CRITICAL",
    "assignee": "Alice"
  }'
```

### Get All Incidents
```bash
curl http://localhost:8081/api/incidents
```

### Get Specific Incident
```bash
curl http://localhost:8081/api/incidents/1
```

### Update an Incident
```bash
curl -X PUT http://localhost:8081/api/incidents/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Server Down - URGENT",
    "priority": "CRITICAL",
    "assignee": "Bob"
  }'
```

### Update Status Only
```bash
curl -X PATCH http://localhost:8081/api/incidents/1/status \
  -H "Content-Type: application/json" \
  -d '"IN_PROGRESS"'
```

### Delete an Incident
```bash
curl -X DELETE http://localhost:8081/api/incidents/1
```

### Filter by Status
```bash
curl http://localhost:8081/api/incidents?status=OPEN
```

### Filter by Priority
```bash
curl http://localhost:8081/api/incidents?priority=HIGH
```

---

## 📊 GraphQL Quick Commands

### Query All Incidents
```bash
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ incidents { id title priority status assignee } }"
  }'
```

### Query Specific Incident
```bash
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ incident(id: 1) { id title description priority status assignee createdAt } }"
  }'
```

### Query by Status
```bash
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ incidentsByStatus(status: OPEN) { id title assignee } }"
  }'
```

### Query by Priority
```bash
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ incidentsByPriority(priority: CRITICAL) { id title status } }"
  }'
```

### Query by Assignee
```bash
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ incidentsByAssignee(assignee: \"Alice\") { id title priority } }"
  }'
```

### Create Incident (Mutation)
```bash
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { createIncident(input: { title: \"New Issue\", priority: HIGH, assignee: \"Bob\" }) { id title priority status } }"
  }'
```

### Update Status (Mutation)
```bash
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { updateStatus(id: 1, status: RESOLVED) { id title status resolvedAt } }"
  }'
```

### Delete Incident (Mutation)
```bash
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation { deleteIncident(id: 1) }"
  }'
```

---

## 🖥️ Web Interfaces

### Swagger UI (REST API Documentation)
- **URL**: http://localhost:8081/swagger-ui.html
- **Use**: Interactive REST API testing and documentation
- **Features**: Try-it-out functionality, request/response examples

### GraphiQL (GraphQL Playground)
- **URL**: http://localhost:8081/graphiql
- **Use**: Interactive GraphQL query/mutation testing
- **Features**: Syntax highlighting, auto-completion, documentation

### OpenAPI Specification
- **URL**: http://localhost:8081/api-docs
- **Use**: Machine-readable API specification (JSON)

### Health Check
- **URL**: http://localhost:8081/actuator/health
- **Response**: `{"status":"UP"}`

---

## 🏗️ Application Architecture

```
┌─────────────────────────────────────────────────┐
│          Incident Tracker Application            │
│                Spring Boot 3.2.2                 │
├─────────────────────────────────────────────────┤
│  REST API          │  GraphQL API               │
│  (6 Endpoints)     │  (8+ Resolvers)            │
├─────────────────────────────────────────────────┤
│         Service Layer (Business Logic)          │
├─────────────────────────────────────────────────┤
│      Repository Layer (Data Access)             │
├─────────────────────────────────────────────────┤
│  PostgreSQL Database (incidents table)          │
└─────────────────────────────────────────────────┘
```

---

## 📋 Data Model

### Incident Entity

```json
{
  "id": 1,
  "title": "Database Connection Timeout",
  "description": "Connection to production database is timing out",
  "priority": "CRITICAL",
  "status": "OPEN",
  "assignee": "Alice",
  "createdAt": "2026-02-01T20:12:19.691670",
  "updatedAt": "2026-02-01T20:12:19.691670",
  "resolvedAt": null
}
```

### Enums

**Priority**: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`

**Status**: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`

---

## ✅ Validation Rules

- **Title**: Required, max 200 characters
- **Description**: Optional, max 2000 characters
- **Priority**: Optional, defaults to LOW
- **Status**: Optional, defaults to OPEN
- **Assignee**: Optional, max 100 characters
- **resolvedAt**: Auto-set when status becomes RESOLVED

---

## 🔍 Status Codes

| Code | Meaning | Example |
|------|---------|---------|
| 200 | OK | GET successful |
| 201 | Created | POST successful |
| 204 | No Content | DELETE successful |
| 400 | Bad Request | Invalid input |
| 404 | Not Found | Incident doesn't exist |
| 500 | Server Error | Database error |

---

## 🐳 Running the Application

### Start Application
```bash
# The app is already running on PID from /tmp/app.pid
# To restart:
kill $(cat /tmp/app.pid)

# Start again:
cd /home/enrique/CLAUDE/springboot_python_postgres_project
java -Dspring.datasource.password=postgres -jar target/incident-tracker-1.0.0.jar
```

### View Logs
```bash
tail -f /tmp/app.log
```

### Stop Application
```bash
kill $(cat /tmp/app.pid)
```

---

## 📊 Database Connection

**Database**: `incidents`
**Host**: `localhost`
**Port**: `5432`
**User**: `postgres`
**Password**: `postgres`

### Connect via psql
```bash
psql -h localhost -U postgres -d incidents
```

### View Database Schema
```sql
\dt incidents
\d incidents
SELECT * FROM incidents;
```

---

## 🎯 Common Use Cases

### Scenario 1: Create and Track an Issue

```bash
# Create
curl -X POST http://localhost:8081/api/incidents \
  -H "Content-Type: application/json" \
  -d '{"title":"API Gateway Down","priority":"CRITICAL","assignee":"Alice"}'

# Check status (Note the ID from response)
curl http://localhost:8081/api/incidents/1

# Update to in-progress
curl -X PATCH http://localhost:8081/api/incidents/1/status \
  -H "Content-Type: application/json" \
  -d '"IN_PROGRESS"'

# Resolve
curl -X PATCH http://localhost:8081/api/incidents/1/status \
  -H "Content-Type: application/json" \
  -d '"RESOLVED"'
```

### Scenario 2: Get All Critical Issues

```bash
# Via REST
curl http://localhost:8081/api/incidents?priority=CRITICAL

# Via GraphQL
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ incidentsByPriority(priority: CRITICAL) { id title status assignee } }"}'
```

### Scenario 3: Get Alice's Assignments

```bash
# Via REST (need to fetch all and filter)
curl http://localhost:8081/api/incidents

# Via GraphQL (more efficient)
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ incidentsByAssignee(assignee: \"Alice\") { id title priority status } }"}'
```

---

## 🚨 Troubleshooting

### Application Won't Start
```bash
# Check if port 8081 is in use
lsof -i :8081

# Check PostgreSQL is running
psql -h localhost -U postgres -c "SELECT 1"

# Check logs
tail -100 /tmp/app.log
```

### Database Connection Error
```bash
# Verify database exists
psql -h localhost -U postgres -c "\l" | grep incidents

# Verify password is correct
psql -h localhost -U postgres -c "SELECT 1"
```

### GraphQL Not Working
- Use GraphiQL at `/graphiql` to test queries
- Check query syntax in terminal
- Verify schema at `/graphql` endpoint

### REST API Returning Errors
- Check request format (JSON, Content-Type header)
- Verify all required fields are present
- Check validation messages in error response

---

## 📞 Support

For detailed documentation, see:
- `DEPLOYMENT.md` - Deployment instructions
- `API_TEST_RESULTS.md` - Full test results
- `README.md` - Project overview

Application is running and ready to use! 🎉

---

**Last Updated**: February 1, 2026
**Status**: ✅ Production Ready
