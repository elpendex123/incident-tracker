# Manual Build & Deployment Guide

## Prerequisites

Before deploying manually, ensure:

1. **Java 17+** installed
   ```bash
   java -version
   ```

2. **Maven 3.6.3+** installed
   ```bash
   mvn -version
   ```

3. **PostgreSQL 15** running and accessible
   ```bash
   # Verify PostgreSQL is running (depends on your system)
   # Ubuntu/Debian:
   sudo systemctl status postgresql
   
   # macOS:
   brew services list | grep postgres
   ```

4. **Database created** with correct credentials
   ```bash
   # Connect to PostgreSQL
   psql -h localhost -U postgres
   
   # Run in PostgreSQL shell:
   CREATE DATABASE incidents;
   \q
   ```

## Build Steps

### Step 1: Clean and Build

```bash
# Navigate to project directory
cd /home/enrique/CLAUDE/springboot_python_postgres_project

# Build without tests (creates JAR in target/)
mvn clean package -DskipTests

# Output: target/incident-tracker-1.0.0.jar (59MB)
```

### Step 2: Set Environment Variables

Create a `.env` file or set environment variables:

```bash
# Database configuration
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=incidents
export DB_USER=postgres
export DB_PASSWORD=postgres  # Change to your actual password

# Optional: Server port
export SERVER_PORT=8081
```

### Step 3: Start the Application

```bash
# Option A: With default profile (requires PostgreSQL)
java -jar target/incident-tracker-1.0.0.jar

# Option B: With environment variables
java -Dspring.datasource.url=jdbc:postgresql://localhost:5432/incidents \
     -Dspring.datasource.username=postgres \
     -Dspring.datasource.password=postgres \
     -jar target/incident-tracker-1.0.0.jar

# Option C: Run in background
java -jar target/incident-tracker-1.0.0.jar > logs/app.log 2>&1 &
```

### Step 4: Verify Application is Running

```bash
# Check health endpoint
curl http://localhost:8081/actuator/health

# Expected output:
# {"status":"UP"}
```

## API Endpoints

Once running, access:

### REST API
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **API Docs**: http://localhost:8081/api-docs
- **List Incidents**: http://localhost:8081/api/incidents
- **Health Check**: http://localhost:8081/actuator/health

### GraphQL
- **GraphQL Endpoint**: http://localhost:8081/graphql
- **GraphiQL Playground**: http://localhost:8081/graphiql

## Testing the APIs

### Create an Incident (REST)

```bash
curl -X POST http://localhost:8081/api/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Database connection slow",
    "description": "Queries taking longer than usual",
    "priority": "HIGH",
    "assignee": "Alice"
  }'
```

### Query All Incidents (REST)

```bash
curl http://localhost:8081/api/incidents
```

### GraphQL Query

```bash
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "{ incidents { id title priority status } }"
  }'
```

## Stopping the Application

If running in background:

```bash
# Find process
ps aux | grep incident-tracker

# Kill by PID
kill <PID>

# Or kill all Java processes running the app
pkill -f incident-tracker
```

## Troubleshooting

### PostgreSQL Connection Error

```
org.postgresql.util.PSQLException: FATAL: password authentication failed
```

**Solution**: 
- Verify PostgreSQL is running: `psql -U postgres`
- Check database credentials in application.yml
- Ensure database `incidents` exists

### H2 Driver Not Found

```
Cannot load driver class: org.h2.Driver
```

**Solution**: 
- H2 is only available in test profile
- For production, use PostgreSQL connection
- Or rebuild with H2 as runtime dependency

### Port Already in Use

```
Address already in use
```

**Solution**:
```bash
# Find process on port 8081
lsof -i :8081

# Kill it
kill -9 <PID>

# Or use different port
java -Dserver.port=8082 -jar target/incident-tracker-1.0.0.jar
```

## Logs

Application logs are configured to output to:
- **Console**: Real-time logs
- **File**: `logs/incident-tracker.log` (created on first run)
- **Retention**: 30 days of daily log files

View logs:

```bash
# Real-time (if running in foreground)
# Ctrl+C to exit

# From file
tail -f logs/incident-tracker.log

# Last 50 lines
tail -50 logs/incident-tracker.log
```

## Performance Notes

- **Build Time**: ~13-15 seconds (with network downloads on first build)
- **Startup Time**: ~15-20 seconds
- **JAR Size**: 59MB (includes all dependencies)
- **Memory Usage**: ~500MB-1GB typical

## Next Steps

After successful deployment:

1. Test all API endpoints (see samples above)
2. Create test incidents via REST or GraphQL
3. Monitor logs for any errors
4. Use Swagger UI for interactive API documentation
5. Optional: Set up Python automation scripts

```bash
cd scripts
python3 db_operations.py --action query --query counts
python3 db_operations.py --action seed --count 10
```

