# Incident Tracker API - Project Plan for Claude Code

## Project Overview

Build a local development environment with a Spring Boot application exposing both REST and GraphQL APIs, deployed via Jenkins pipelines in Docker, with a PostgreSQL database and Python scripts for database operations.

**Tech Stack:**
- Spring Boot 3.x (Java 17+)
- Maven (not Gradle)
- PostgreSQL (Docker)
- REST API (Spring Web)
- GraphQL API (Spring for GraphQL)
- Docker + Docker Compose
- Jenkins (Docker)
- Python 3 + psycopg2 (for DB scripts)
- GitHub for source control

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Docker Network                          │
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
└─────────────────────────────────────────────────────────────┘
```

---

## Project Structure

```
incident-tracker/
├── src/
│   └── main/
│       ├── java/com/example/incidenttracker/
│       │   ├── IncidentTrackerApplication.java
│       │   ├── controller/
│       │   │   └── IncidentController.java
│       │   ├── graphql/
│       │   │   └── IncidentGraphQLController.java
│       │   ├── model/
│       │   │   ├── Incident.java
│       │   │   ├── Priority.java
│       │   │   └── Status.java
│       │   ├── repository/
│       │   │   └── IncidentRepository.java
│       │   ├── service/
│       │   │   └── IncidentService.java
│       │   └── dto/
│       │       ├── IncidentRequest.java
│       │       └── IncidentResponse.java
│       └── resources/
│           ├── application.yml
│           ├── application-docker.yml
│           └── graphql/
│               └── schema.graphqls
├── src/test/
│   └── java/com/example/incidenttracker/
│       ├── controller/
│       │   └── IncidentControllerTest.java
│       └── service/
│           └── IncidentServiceTest.java
├── scripts/
│   ├── db_operations.py
│   └── requirements.txt
├── docker/
│   └── docker-compose.yml
├── Dockerfile
├── Jenkinsfile
├── Jenkinsfile.db-ops
├── pom.xml
└── README.md
```

---

## Phase 1: Project Foundation

### Step 1.1: Create pom.xml

Create Maven POM with these dependencies:
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-graphql
- spring-boot-starter-validation
- postgresql driver
- lombok
- spring-boot-starter-test
- spring-graphql-test

Use Spring Boot 3.2.x or later, Java 17.

### Step 1.2: Create Application Entry Point

Create `IncidentTrackerApplication.java` with `@SpringBootApplication`.

### Step 1.3: Create application.yml

Configure:
- Server port: 8081
- PostgreSQL connection (use environment variables with defaults)
- JPA/Hibernate settings (ddl-auto: update for dev)
- GraphQL settings (graphiql enabled for testing)

Create `application-docker.yml` profile for Docker deployment with container hostnames.

---

## Phase 2: Data Model

### Step 2.1: Create Enums

**Priority.java:**
```java
public enum Priority {
    LOW, MEDIUM, HIGH, CRITICAL
}
```

**Status.java:**
```java
public enum Status {
    OPEN, IN_PROGRESS, RESOLVED, CLOSED
}
```

### Step 2.2: Create Incident Entity

**Incident.java** with fields:
- id (Long, auto-generated)
- title (String, required, max 200 chars)
- description (String, max 2000 chars)
- priority (Priority enum, default LOW)
- status (Status enum, default OPEN)
- assignee (String, max 100 chars)
- createdAt (LocalDateTime, auto-set)
- updatedAt (LocalDateTime, auto-updated)
- resolvedAt (LocalDateTime, nullable)

Use JPA annotations: `@Entity`, `@Id`, `@GeneratedValue`, `@Enumerated(EnumType.STRING)`, `@CreationTimestamp`, `@UpdateTimestamp`.

### Step 2.3: Create Repository

**IncidentRepository.java** extending JpaRepository with custom queries:
- findByStatus(Status status)
- findByPriority(Priority priority)
- findByAssignee(String assignee)
- findByStatusAndPriority(Status status, Priority priority)

---

## Phase 3: Service Layer

### Step 3.1: Create IncidentService

Implement business logic:
- getAllIncidents()
- getIncidentById(Long id)
- createIncident(IncidentRequest request)
- updateIncident(Long id, IncidentRequest request)
- updateStatus(Long id, Status status) - auto-set resolvedAt when status = RESOLVED
- deleteIncident(Long id)
- getIncidentsByStatus(Status status)
- getIncidentsByPriority(Priority priority)

Handle exceptions properly (throw ResourceNotFoundException for missing incidents).

---

## Phase 4: REST API

### Step 4.1: Create DTOs

**IncidentRequest.java** - input DTO for create/update:
- title (required)
- description
- priority
- status
- assignee

**IncidentResponse.java** - output DTO (or use entity directly for simplicity).

### Step 4.2: Create REST Controller

**IncidentController.java** with endpoints:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/incidents | List all incidents (optional filters: status, priority) |
| GET | /api/incidents/{id} | Get incident by ID |
| POST | /api/incidents | Create new incident |
| PUT | /api/incidents/{id} | Update incident |
| PATCH | /api/incidents/{id}/status | Update status only |
| DELETE | /api/incidents/{id} | Delete incident |

Use `@RestController`, `@RequestMapping("/api/incidents")`, proper HTTP status codes.

---

## Phase 5: GraphQL API

### Step 5.1: Create GraphQL Schema

**schema.graphqls:**
```graphql
type Query {
    incidents: [Incident!]!
    incident(id: ID!): Incident
    incidentsByStatus(status: Status!): [Incident!]!
    incidentsByPriority(priority: Priority!): [Incident!]!
}

type Mutation {
    createIncident(input: CreateIncidentInput!): Incident!
    updateIncident(id: ID!, input: UpdateIncidentInput!): Incident!
    updateStatus(id: ID!, status: Status!): Incident!
    deleteIncident(id: ID!): Boolean!
}

type Incident {
    id: ID!
    title: String!
    description: String
    priority: Priority!
    status: Status!
    assignee: String
    createdAt: String!
    updatedAt: String!
    resolvedAt: String
}

enum Priority {
    LOW
    MEDIUM
    HIGH
    CRITICAL
}

enum Status {
    OPEN
    IN_PROGRESS
    RESOLVED
    CLOSED
}

input CreateIncidentInput {
    title: String!
    description: String
    priority: Priority
    assignee: String
}

input UpdateIncidentInput {
    title: String
    description: String
    priority: Priority
    status: Status
    assignee: String
}
```

### Step 5.2: Create GraphQL Controller

**IncidentGraphQLController.java** using `@Controller` with:
- `@QueryMapping` methods for queries
- `@MutationMapping` methods for mutations
- `@Argument` for input parameters

---

## Phase 6: Docker Setup

### Step 6.1: Create Dockerfile

Multi-stage build:
1. Build stage: Maven build with `mvn clean package -DskipTests`
2. Runtime stage: Eclipse Temurin JRE 17, copy JAR, expose 8081

### Step 6.2: Create docker-compose.yml

Services:
1. **postgres:**
   - Image: postgres:15
   - Port: 5432:5432
   - Environment: POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD
   - Volume for data persistence
   - Healthcheck

2. **app:**
   - Build from Dockerfile
   - Port: 8081:8081
   - Depends on postgres
   - Environment variables for DB connection
   - Profile: docker

3. **jenkins:**
   - Image: jenkins/jenkins:lts
   - Ports: 8080:8080, 50000:50000
   - Volumes: jenkins_home, docker socket (for Docker-in-Docker)
   - Note: Will need Docker CLI and Python installed in Jenkins

Create named network for all services.

---

## Phase 7: Python DB Operations

### Step 7.1: Create requirements.txt

```
psycopg2-binary==2.9.9
python-dotenv==1.0.0
tabulate==0.9.0
```

### Step 7.2: Create db_operations.py

Script with CLI arguments using argparse:

**Actions:**
- `--action query` - Run predefined queries
- `--action seed` - Insert sample test data
- `--action cleanup` - Remove old closed incidents
- `--action report` - Generate summary report

**Query options (when action=query):**
- `--query counts` - Incident counts by status
- `--query priority` - Incidents by priority
- `--query open` - All open incidents
- `--query overdue` - Incidents open > X days

**Environment variables for connection:**
- DB_HOST (default: localhost)
- DB_PORT (default: 5432)
- DB_NAME (default: incidents)
- DB_USER (default: postgres)
- DB_PASSWORD

**Example usage:**
```bash
python db_operations.py --action query --query counts
python db_operations.py --action seed --count 10
python db_operations.py --action cleanup --days 30
python db_operations.py --action report --output report.txt
```

---

## Phase 8: Jenkins Pipelines

### Step 8.1: Create Jenkinsfile (Main App)

Pipeline stages:
1. **Checkout** - Clone from GitHub
2. **Build** - `mvn clean package -DskipTests`
3. **Test** - `mvn test`
4. **Build Docker Image** - `docker build -t incident-tracker:latest .`
5. **Deploy** - `docker-compose up -d app`

Use declarative pipeline syntax. Include:
- Environment variables
- Post actions (always, success, failure)
- Agent specification

### Step 8.2: Create Jenkinsfile.db-ops (Python Scripts)

Pipeline with parameters:
- ACTION (choice: query, seed, cleanup, report)
- QUERY_TYPE (choice: counts, priority, open, overdue) - when ACTION=query
- SEED_COUNT (string, default: 10) - when ACTION=seed
- CLEANUP_DAYS (string, default: 30) - when ACTION=cleanup

Stages:
1. **Checkout** - Clone repo
2. **Setup Python** - Install dependencies from requirements.txt
3. **Run Script** - Execute db_operations.py with parameters

Include credentials binding for DB_PASSWORD.

---

## Phase 9: Testing

### Step 9.1: Unit Tests

**IncidentServiceTest.java:**
- Test createIncident
- Test updateStatus (verify resolvedAt is set)
- Test getIncidentById (success and not found)

Use `@MockBean` for repository, `@ExtendWith(MockitoExtension.class)`.

### Step 9.2: Integration Tests

**IncidentControllerTest.java:**
- Test REST endpoints with `@WebMvcTest` or `@SpringBootTest`
- Use `MockMvc` for HTTP testing
- Verify JSON responses

---

## Phase 10: Documentation

### Step 10.1: Create README.md

Include:
- Project description
- Prerequisites (Java 17, Docker, Maven)
- Local development setup
- Docker deployment instructions
- API documentation (REST endpoints, GraphQL examples)
- Jenkins pipeline usage
- Python script usage

---

## Build Order (Execute in Sequence)

1. Create project directory structure
2. Create pom.xml
3. Create application.yml and application-docker.yml
4. Create enums (Priority, Status)
5. Create Incident entity
6. Create IncidentRepository
7. Create IncidentService
8. Create IncidentController (REST)
9. Create schema.graphqls
10. Create IncidentGraphQLController
11. Create IncidentTrackerApplication.java
12. Create unit tests
13. Verify local build: `mvn clean package`
14. Create Dockerfile
15. Create docker-compose.yml
16. Test Docker deployment locally
17. Create db_operations.py and requirements.txt
18. Test Python script locally
19. Create Jenkinsfile
20. Create Jenkinsfile.db-ops
21. Create README.md
22. Initialize Git repo and push to GitHub

---

## Verification Checkpoints

After each phase, verify:

**Phase 2:** Entity compiles, no errors
**Phase 3:** Service layer compiles
**Phase 4:** `mvn spring-boot:run` starts, REST endpoints respond
**Phase 5:** GraphiQL accessible at http://localhost:8081/graphiql
**Phase 6:** `docker-compose up` starts all services
**Phase 7:** Python script connects and queries work
**Phase 8:** Jenkins builds trigger successfully

---

## Sample Test Commands

**REST API:**
```bash
# Create incident
curl -X POST http://localhost:8081/api/incidents \
  -H "Content-Type: application/json" \
  -d '{"title":"Server down","description":"Production server unresponsive","priority":"CRITICAL"}'

# List all
curl http://localhost:8081/api/incidents

# Update status
curl -X PATCH http://localhost:8081/api/incidents/1/status \
  -H "Content-Type: application/json" \
  -d '"IN_PROGRESS"'
```

**GraphQL:**
```graphql
# Query all incidents
query {
  incidents {
    id
    title
    status
    priority
  }
}

# Create incident
mutation {
  createIncident(input: {
    title: "Database slow"
    description: "Queries taking too long"
    priority: HIGH
  }) {
    id
    title
    createdAt
  }
}
```

---

## Notes for Claude Code

- Always use Maven commands, not Gradle
- Use Java 17+ features where appropriate
- Follow Spring Boot 3.x conventions
- Use constructor injection (not field injection)
- Add proper validation annotations
- Include meaningful error messages
- Use Lombok to reduce boilerplate (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)
- GraphQL controller uses @Controller not @RestController
- Test with both H2 (unit tests) and PostgreSQL (integration/docker)
