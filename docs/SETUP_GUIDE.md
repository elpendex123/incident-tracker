# Incident Tracker - Local Setup Guide

Quick start guide for setting up and running the Incident Tracker API locally.

## Prerequisites

Before starting, ensure you have:
- Java 17+ installed
- Maven 3.6.3+ installed
- PostgreSQL 15+ running locally
- Python 3.9+ installed
- Git installed

## 1. PostgreSQL Setup (One-time)

### Start PostgreSQL

**Ubuntu/Debian**:
```bash
sudo systemctl start postgresql
```

**macOS**:
```bash
brew services start postgresql@15
```

**Windows**: Start PostgreSQL service or run the PostgreSQL application

### Create Database & User

```bash
sudo -u postgres psql
```

Then run these SQL commands:

```sql
CREATE DATABASE incidents;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE incidents TO postgres;
\q
```

### Verify Connection

```bash
psql -h localhost -U postgres -d incidents
# When prompted, enter password: postgres
```

## 2. Clone and Build

```bash
# Clone the repository
git clone git@github.com:elpendex123/incident-tracker.git
cd incident-tracker

# Build the application
mvn clean package

# This will:
# - Download all dependencies
# - Compile source code
# - Run tests (if available)
# - Create incident-tracker-1.0.0.jar
```

## 3. Run the Application

### Option A: Using Maven (Recommended for Development)

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

### Option B: Using JAR File

```bash
java -jar target/incident-tracker-1.0.0.jar
```

## 4. Test the Application

Once running, visit these URLs in your browser:

- **Swagger UI** (REST API docs): http://localhost:8081/swagger-ui.html
- **GraphiQL** (GraphQL playground): http://localhost:8081/graphiql
- **Health Check**: http://localhost:8081/actuator/health
- **API Docs**: http://localhost:8081/api-docs

### Quick API Test

**Create an incident**:
```bash
curl -X POST http://localhost:8081/api/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Incident",
    "description": "This is a test",
    "priority": "HIGH",
    "assignee": "John"
  }'
```

**List all incidents**:
```bash
curl http://localhost:8081/api/incidents
```

## 5. Python Scripts Setup

### Install Dependencies

```bash
cd scripts
pip3 install -r requirements.txt
```

### Configure Environment

```bash
cp .env.example .env
# Edit .env with your database credentials (if different from defaults)
```

### Run Scripts

```bash
# Query incident statistics
python3 db_operations.py --action query --query counts

# Seed test data
python3 db_operations.py --action seed --count 20

# View open incidents
python3 db_operations.py --action query --query open

# Generate report
python3 db_operations.py --action report --output report.txt
```

## 6. Stop the Application

**If running with `mvn spring-boot:run`**: Press `Ctrl+C`

**If running in background**:
```bash
pkill -f incident-tracker
```

## Common Issues

### Port 8081 Already in Use

```bash
# Find what's using port 8081
lsof -i :8081

# Kill the process
kill -9 <PID>
```

### Database Connection Failed

```bash
# Verify PostgreSQL is running
psql -h localhost -U postgres -d incidents

# Check application.yml configuration
cat src/main/resources/application.yml
```

### Maven Build Fails

```bash
# Clean Maven cache
mvn clean install

# Or rebuild without tests
mvn clean package -DskipTests
```

### Python Script Connection Error

```bash
# Verify .env file
cat scripts/.env

# Test database connection
python3 -c "import psycopg2; print('psycopg2 installed')"
```

## Development Workflow

### Make Changes

```bash
# Edit files in your IDE
# Example: src/main/java/com/example/incidenttracker/...
```

### Commit Changes

```bash
git add .
git commit -m "feat: Add new feature"
git push origin main
```

### Rebuild and Test

```bash
mvn clean package
mvn spring-boot:run
```

## API Examples

### REST API

See `README.md` for comprehensive API documentation.

### GraphQL API

Open http://localhost:8081/graphiql and try these queries:

**Query all incidents**:
```graphql
query {
  incidents {
    id
    title
    priority
    status
  }
}
```

**Create incident**:
```graphql
mutation {
  createIncident(input: {
    title: "New incident"
    priority: HIGH
  }) {
    id
    title
    createdAt
  }
}
```

## Next Steps

1. Review the comprehensive `README.md` for complete documentation
2. Set up Jenkins locally for CI/CD (optional)
3. Implement unit and integration tests
4. Deploy to production environment

## Getting Help

- **REST API Docs**: http://localhost:8081/swagger-ui.html
- **GraphQL Playground**: http://localhost:8081/graphiql
- **Project README**: See `README.md`
- **Implementation Plan**: See `CLAUDE.md`
