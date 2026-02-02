# Incident Tracker API

A comprehensive Spring Boot microservices application providing REST and GraphQL APIs for incident management with PostgreSQL persistence, Python automation scripts, and Jenkins CI/CD integration.

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.2.x
- **Build Tool**: Maven 3.6.3+
- **Database**: PostgreSQL 15 (local)
- **APIs**:
  - REST (Spring Web)
  - GraphQL (Spring for GraphQL)
- **Documentation**: OpenAPI/Swagger
- **Automation**: Python 3 + psycopg2
- **CI/CD**: Jenkins (local)
- **Version Control**: Git

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Local Machine                           │
│                                                             │
│  ┌─────────────┐   ┌─────────────────┐   ┌──────────────┐  │
│  │   Jenkins   │   │  Spring Boot    │   │  PostgreSQL  │  │
│  │   :8080     │   │  App :8081      │   │    :5432     │  │
│  └─────────────┘   └─────────────────┘   └──────────────┘  │
│         │                  │                    │           │
│         │                  └────────────────────┘           │
│         │                                                   │
│         └──────────── Python scripts ───────────────────────┘
│                    (executed by Jenkins)                    │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Git Repository (Local or GitHub)                   │   │
│  │  - Jenkins pulls from here                          │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

**Package Structure**: `com.example.incidenttracker`

## Prerequisites

### Required
- Java 17+ (`java -version`)
- Maven 3.6.3+ (`mvn -version`)
- PostgreSQL 15 (local instance)
- Python 3.9+ (`python3 --version`)
- Git (`git --version`)

### Optional
- Jenkins (for CI/CD - local installation)
- curl or Postman (for API testing)

## Quick Start

### 1. PostgreSQL Setup

**Install PostgreSQL** (if not already installed):

```bash
# Ubuntu/Debian
sudo apt install postgresql postgresql-contrib

# macOS
brew install postgresql@15

# Windows - Download installer from postgresql.org
```

**Start PostgreSQL**:

```bash
# Ubuntu/Debian
sudo systemctl start postgresql

# macOS
brew services start postgresql@15
```

**Create database and user**:

```bash
sudo -u postgres psql
```

```sql
CREATE DATABASE incidents;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE incidents TO postgres;
\q
```

**Verify connection**:

```bash
psql -h localhost -U postgres -d incidents
# Enter password when prompted
```

### 2. Application Setup

**Clone and navigate to project**:

```bash
git clone <repository-url>
cd incident-tracker
```

**Build the application**:

```bash
mvn clean package
```

**Run the application**:

```bash
mvn spring-boot:run
```

**Access the application**:
- REST API: http://localhost:8081/api/incidents
- Swagger UI: http://localhost:8081/swagger-ui.html
- GraphiQL: http://localhost:8081/graphiql
- Health Check: http://localhost:8081/actuator/health

### 3. Python Scripts Setup

**Navigate to scripts directory**:

```bash
cd scripts
```

**Install Python dependencies**:

```bash
pip3 install -r requirements.txt
```

**Create .env file** (copy from .env.example):

```bash
cp .env.example .env
# Edit .env with your database credentials
```

**Run scripts**:

```bash
python3 db_operations.py --action query --query counts
python3 db_operations.py --action seed --count 20
python3 db_operations.py --action cleanup --days 30
python3 db_operations.py --action report --output report.txt
```

## API Documentation

### REST Endpoints

All endpoints require valid JSON input and return JSON responses.

#### Incident Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/incidents` | List all incidents (with optional filters) |
| GET | `/api/incidents?status=OPEN` | Filter incidents by status |
| GET | `/api/incidents?priority=HIGH` | Filter incidents by priority |
| GET | `/api/incidents/{id}` | Get incident by ID |
| POST | `/api/incidents` | Create new incident |
| PUT | `/api/incidents/{id}` | Update entire incident |
| PATCH | `/api/incidents/{id}/status` | Update status only |
| DELETE | `/api/incidents/{id}` | Delete incident |

#### Request/Response Examples

**Create Incident**:

```bash
curl -X POST http://localhost:8081/api/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Database connection slow",
    "description": "Queries are taking longer than usual",
    "priority": "HIGH",
    "assignee": "Alice"
  }'
```

**List All Incidents**:

```bash
curl http://localhost:8081/api/incidents
```

**Filter by Status**:

```bash
curl http://localhost:8081/api/incidents?status=OPEN
```

**Update Status**:

```bash
curl -X PATCH http://localhost:8081/api/incidents/1/status \
  -H "Content-Type: application/json" \
  -d '"RESOLVED"'
```

**Delete Incident**:

```bash
curl -X DELETE http://localhost:8081/api/incidents/1
```

### GraphQL API

Access GraphiQL interactive playground at: http://localhost:8081/graphiql

#### Query Examples

**Get All Incidents**:

```graphql
query {
  incidents {
    id
    title
    priority
    status
    assignee
    createdAt
  }
}
```

**Get Incident by ID**:

```graphql
query {
  incident(id: "1") {
    id
    title
    description
    priority
    status
    assignee
    createdAt
    updatedAt
    resolvedAt
  }
}
```

**Filter by Status**:

```graphql
query {
  incidentsByStatus(status: OPEN) {
    id
    title
    priority
    assignee
  }
}
```

**Filter by Priority**:

```graphql
query {
  incidentsByPriority(priority: CRITICAL) {
    id
    title
    status
    assignee
  }
}
```

#### Mutation Examples

**Create Incident**:

```graphql
mutation {
  createIncident(input: {
    title: "Server down"
    description: "Production server is unresponsive"
    priority: CRITICAL
    assignee: "Bob"
  }) {
    id
    title
    createdAt
  }
}
```

**Update Incident**:

```graphql
mutation {
  updateIncident(id: "1", input: {
    title: "Updated title"
    priority: HIGH
    status: IN_PROGRESS
  }) {
    id
    title
    priority
    status
    updatedAt
  }
}
```

**Update Status**:

```graphql
mutation {
  updateStatus(id: "1", status: RESOLVED) {
    id
    status
    resolvedAt
  }
}
```

**Delete Incident**:

```graphql
mutation {
  deleteIncident(id: "1")
}
```

### Swagger/OpenAPI Documentation

Interactive API documentation available at: http://localhost:8081/swagger-ui.html

- Full endpoint documentation
- Request/response schemas
- Try-it-out functionality
- Error code documentation

## Python Automation Scripts

### Database Operations Script

Located in `scripts/db_operations.py`

**Usage**:

```bash
python3 db_operations.py --action <action> [options]
```

#### Query Operations

Display incident statistics and data:

```bash
# Count incidents by status
python3 db_operations.py --action query --query counts

# Count incidents by priority
python3 db_operations.py --action query --query priority

# List all open incidents
python3 db_operations.py --action query --query open

# List incidents older than 7 days
python3 db_operations.py --action query --query overdue --days 7
```

#### Seed Data

Generate test data for development:

```bash
# Seed 50 random incidents
python3 db_operations.py --action seed --count 50
```

#### Cleanup

Remove old records:

```bash
# Delete closed incidents older than 90 days
python3 db_operations.py --action cleanup --days 90
```

#### Generate Report

Create summary reports:

```bash
# Generate report and display in terminal
python3 db_operations.py --action report

# Save report to file
python3 db_operations.py --action report --output incident_report.txt
```

## Jenkins Integration

### Setup Prerequisites

1. **Install Jenkins** (download from https://www.jenkins.io/download/)
2. **Ensure Java 17 is installed**
3. **Ensure Maven 3.6.3 is installed**
4. **Ensure Python 3 is installed**

### Start Jenkins

```bash
# Linux/Mac
java -jar jenkins.war --httpPort=8080

# Or if installed via package manager
sudo systemctl start jenkins
```

### Initial Configuration

1. Access http://localhost:8080
2. Complete setup wizard
3. Install suggested plugins + these additional ones:
   - Git plugin
   - Pipeline plugin
   - Credentials Binding plugin

### Configure Tools

1. **Maven**: Manage Jenkins > Tools > Maven installations
   - Name: `Maven 3.6.3`
   - MAVEN_HOME: (path to your Maven installation)

2. **JDK**: Manage Jenkins > Tools > JDK installations
   - Name: `JDK 17`
   - JAVA_HOME: (path to your Java 17 installation)

3. **Credentials**: Manage Jenkins > Credentials
   - Add credential for `db-password` (secret text)

### Create Pipeline Jobs

#### Main Application Build Job

1. New Item > Pipeline
2. **Name**: `incident-tracker-build`
3. **Pipeline definition**: Pipeline script from SCM
4. **SCM**: Git
5. **Repository URL**: (your Git repository URL)
6. **Branch**: `*/main`
7. **Script Path**: `Jenkinsfile`
8. **Save**

#### Database Operations Job

1. New Item > Pipeline
2. **Name**: `incident-tracker-db-ops`
3. **This build is parameterized**: Check
4. **Parameters**:
   - ACTION (choice): query, seed, cleanup, report
   - QUERY_TYPE (choice): counts, priority, open, overdue
   - SEED_COUNT (string, default: 10)
   - CLEANUP_DAYS (string, default: 30)
   - REPORT_OUTPUT (string, default: incident_report.txt)
5. **Pipeline definition**: Pipeline script from SCM
6. **Script Path**: `Jenkinsfile.db-ops`
7. **Save**

### GitHub Integration (Optional)

1. **In GitHub repository**:
   - Settings > Webhooks
   - Add webhook: `http://<jenkins-url>:8080/github-webhook/`
   - Content type: `application/json`
   - Events: Push events

2. **In Jenkins job**:
   - Build Triggers > GitHub hook trigger for GITScm polling

## Data Model

### Incident Entity

```java
@Entity
@Table(name = "incidents")
public class Incident {
    Long id                        // Auto-generated primary key
    String title                   // Required, max 200 chars
    String description             // Optional, max 2000 chars
    Priority priority              // Enum: LOW, MEDIUM, HIGH, CRITICAL (default: LOW)
    Status status                  // Enum: OPEN, IN_PROGRESS, RESOLVED, CLOSED (default: OPEN)
    String assignee                // Optional, max 100 chars
    LocalDateTime createdAt        // Auto-set on creation
    LocalDateTime updatedAt        // Auto-updated on modification
    LocalDateTime resolvedAt       // Auto-set when status = RESOLVED
}
```

### Database Schema

```sql
CREATE TABLE incidents (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    priority VARCHAR(20) NOT NULL DEFAULT 'LOW',
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    assignee VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP
);

CREATE INDEX idx_incidents_status ON incidents(status);
CREATE INDEX idx_incidents_priority ON incidents(priority);
CREATE INDEX idx_incidents_assignee ON incidents(assignee);
CREATE INDEX idx_incidents_created_at ON incidents(created_at);
```

## Project Structure

```
incident-tracker/
├── src/main/
│   ├── java/com/example/incidenttracker/
│   │   ├── IncidentTrackerApplication.java       # Entry point
│   │   ├── config/
│   │   │   └── OpenAPIConfig.java                # Swagger/OpenAPI setup
│   │   ├── model/
│   │   │   ├── Incident.java                     # Domain entity
│   │   │   ├── Priority.java                     # Enum
│   │   │   └── Status.java                       # Enum
│   │   ├── repository/
│   │   │   └── IncidentRepository.java           # Data access
│   │   ├── service/
│   │   │   ├── IncidentService.java              # Interface
│   │   │   └── impl/
│   │   │       └── IncidentServiceImpl.java       # Implementation
│   │   ├── dto/
│   │   │   ├── IncidentRequest.java              # Input DTO
│   │   │   ├── IncidentResponse.java             # Output DTO
│   │   │   └── ErrorResponse.java                # Error DTO
│   │   ├── exception/
│   │   │   ├── ResourceNotFoundException.java
│   │   │   ├── ValidationException.java
│   │   │   └── GlobalExceptionHandler.java       # Exception handling
│   │   ├── controller/
│   │   │   └── IncidentController.java           # REST endpoints
│   │   └── graphql/
│   │       └── IncidentGraphQLController.java    # GraphQL resolvers
│   └── resources/
│       ├── application.yml                       # Main config
│       ├── application-test.yml                  # Test config
│       ├── logback-spring.xml                    # Logging
│       └── graphql/
│           └── schema.graphqls                   # GraphQL schema
├── src/test/
│   └── java/com/example/incidenttracker/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       └── graphql/
├── scripts/
│   ├── db_operations.py                          # DB automation
│   ├── requirements.txt                          # Python deps
│   └── .env.example                              # Env template
├── pom.xml                                        # Maven config
├── Jenkinsfile                                    # Build pipeline
├── Jenkinsfile.db-ops                             # DB ops pipeline
├── .gitignore                                     # Git exclusions
└── README.md                                      # This file
```

## Testing

### Run Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=IncidentServiceImplTest

# Run integration tests only
mvn test -Dtest=*ControllerTest
```

### Test Coverage

- Unit tests: Service layer with mocked dependencies
- Integration tests: REST controller with MockMvc
- GraphQL tests: Schema and resolver validation
- Repository tests: Database queries

Target: >80% code coverage

## Configuration

### Environment Variables

```bash
# Database
DB_HOST=localhost                 # PostgreSQL host
DB_PORT=5432                      # PostgreSQL port
DB_NAME=incidents                 # Database name
DB_USER=postgres                  # Database user
DB_PASSWORD=postgres              # Database password
```

### Application Profiles

- **default**: Local development with PostgreSQL
- **test**: Testing with H2 in-memory database
- **docker**: Docker deployment configuration

### Logging Configuration

Configured in `src/main/resources/logback-spring.xml`:

- **Console output**: Full colored logs
- **File output**: `logs/incident-tracker.log` with rolling policy
- **Retention**: 30 days or 1GB (whichever comes first)
- **Log levels**:
  - `com.example.incidenttracker`: DEBUG
  - `org.hibernate.SQL`: DEBUG
  - `org.springframework.web`: INFO

## Features Implemented

✅ **REST API**
- Full CRUD operations
- Query filtering (status, priority)
- Proper HTTP status codes
- Input validation

✅ **GraphQL API**
- Query and mutation support
- Flexible filtering options
- Type-safe operations

✅ **OpenAPI/Swagger Documentation**
- Interactive API explorer
- Request/response schemas
- API endpoint documentation

✅ **Exception Handling**
- Global exception handler
- Custom error responses
- Meaningful error messages

✅ **Input Validation**
- Bean Validation annotations
- Field-level constraints
- Validation error feedback

✅ **Logging**
- SLF4j/Logback configuration
- Console and file appenders
- Rolling file policy
- Structured log format

✅ **Database**
- PostgreSQL integration
- Automatic schema generation
- Custom queries
- Transaction management

✅ **Python Automation**
- Database query utilities
- Test data seeding
- Record cleanup
- Report generation

✅ **Jenkins CI/CD**
- Build pipeline
- Test execution
- Local deployment
- Database operations pipeline

## Git Workflow

### Branch Strategy

- `main` - Production-ready code
- `develop` - Integration branch (optional)
- `feature/*` - Feature branches
- `bugfix/*` - Bug fix branches

### Commit Convention (Conventional Commits)

```
feat:     New feature
fix:      Bug fix
docs:     Documentation
test:     Tests
refactor: Code refactoring
ci:       CI/CD changes
chore:    Build process changes
```

### Example Workflow

```bash
# Create feature branch
git checkout -b feature/add-assignee-filter

# Make changes and commit
git add .
git commit -m "feat: Add assignee filter to REST API"

# Push to remote
git push -u origin feature/add-assignee-filter

# Create pull request on GitHub
# After review, merge to main
# Jenkins automatically builds on merge
```

## Troubleshooting

### Application Won't Start

**Issue**: Port 8081 already in use

```bash
# Find and kill process using port 8081
lsof -i :8081
kill -9 <PID>
```

**Issue**: Database connection failed

```bash
# Verify PostgreSQL is running
psql -h localhost -U postgres -d incidents

# Check application.yml configuration
cat src/main/resources/application.yml
```

### Tests Failing

**Issue**: H2 database not initialized properly

```bash
# Clean Maven cache and rebuild
mvn clean test
```

**Issue**: Port conflicts in tests

```bash
# Stop running application
pkill -f incident-tracker
```

### Python Script Errors

**Issue**: psycopg2 connection error

```bash
# Verify .env file exists and is correct
cat scripts/.env

# Test database connection
python3 -c "import psycopg2; psycopg2.connect(**{'host': 'localhost', ...})"
```

## Performance Tips

- Use GraphQL for complex queries to avoid over-fetching
- REST API queries are cached by status/priority
- Database indexes on status, priority, and createdAt columns
- Connection pooling configured in application.yml
- Async logging for better performance

## Security Notes

- Input validation on all endpoints
- SQL injection prevention via parameterized queries
- CSRF protection on mutations
- No sensitive data in logs
- Database credentials via environment variables (never hardcoded)

## Future Enhancements

- Spring Security with JWT authentication
- Redis caching layer
- Flyway database migrations
- Pagination and sorting
- WebSocket notifications
- Prometheus metrics and Grafana dashboards
- Docker containerization
- Kubernetes deployment manifests
- Multi-environment configurations

## Support & Documentation

- **API Docs**: http://localhost:8081/swagger-ui.html
- **GraphQL Playground**: http://localhost:8081/graphiql
- **Health Status**: http://localhost:8081/actuator/health
- **Project Plan**: See `CLAUDE.md` for detailed implementation guide

## License

Apache 2.0

## Contributors

- Enrique Coello (initial development)
- Claude Code (implementation assistance)
