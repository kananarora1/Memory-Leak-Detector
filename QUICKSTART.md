# Quick Start Guide

## Step 1: Install Java 17

### Download & Install
1. Visit: https://adoptium.net/temurin/releases/?version=17
2. Download **Windows x64 JDK** (MSI installer)
3. Run the installer
4. ✓ Check "Set JAVA_HOME variable"
5. ✓ Check "Add to PATH"
6. Click Install

### Verify Installation
Open a **NEW** Command Prompt (must be new after installation):
```bash
java -version
```

Expected output:
```
openjdk version "17.0.x"
```

## Step 2: Build the Project

```bash
cd C:\Users\hp\IdeaProjects\DevOpsProj

# Build the project
mvnw.cmd clean install
```

This will:
- Download dependencies (~5 minutes first time)
- Compile code
- Run tests
- Create JAR file

## Step 3: Run the Application

```bash
mvnw.cmd spring-boot:run
```

Wait for:
```
Started DevOpsProjApplication in X.XXX seconds
```

## Step 4: Test the APIs

### Open Swagger UI
Open browser: http://localhost:8080/swagger-ui.html

### Test Workflow

#### 1. Allocate Objects
```bash
curl -X POST http://localhost:8080/simulate/allocate ^
  -H "Content-Type: application/json" ^
  -d "{\"count\":50,\"sizeInKB\":100,\"objectType\":\"User\",\"createReferences\":true}"
```

#### 2. View Heap State
```bash
curl http://localhost:8080/simulate/heap
```

#### 3. Dereference Some Objects (simulate leak)
```bash
curl -X POST http://localhost:8080/simulate/dereference ^
  -H "Content-Type: application/json" ^
  -d "{\"objectIds\":[1,2,3,4,5]}"
```

#### 4. Run Garbage Collection
```bash
curl -X POST http://localhost:8080/simulate/gc/minor
```

#### 5. Generate Leak Report
```bash
curl http://localhost:8080/analyze/leak
```

## Step 5: Simulate a Memory Leak

Try this workflow to create a detectable leak:

```bash
# 1. Allocate many objects
curl -X POST http://localhost:8080/simulate/allocate ^
  -H "Content-Type: application/json" ^
  -d "{\"count\":200,\"sizeInKB\":50,\"objectType\":\"Session\",\"createReferences\":true}"

# 2. Wait, allocate more (simulating continuous allocation)
curl -X POST http://localhost:8080/simulate/allocate ^
  -H "Content-Type: application/json" ^
  -d "{\"count\":200,\"sizeInKB\":50,\"objectType\":\"Cache\",\"createReferences\":true}"

# 3. Run GC (shouldn't reclaim much because everything is referenced)
curl -X POST http://localhost:8080/simulate/gc/minor

# 4. Allocate even more
curl -X POST http://localhost:8080/simulate/allocate ^
  -H "Content-Type: application/json" ^
  -d "{\"count\":200,\"sizeInKB\":50,\"objectType\":\"Data\",\"createReferences\":true}"

# 5. Run analysis - should show HIGH_PROBABILITY_LEAK
curl http://localhost:8080/analyze/leak
```

## Step 6: View Reports in Browser

Navigate to: http://localhost:8080/swagger-ui.html

1. Expand **"Analysis"** section
2. Click **GET /analyze/leak**
3. Click **"Try it out"**
4. Click **"Execute"**

You'll see:
- Suspicion Score (0-100)
- Verdict (NO_LEAK, POSSIBLE_LEAK, HIGH_PROBABILITY_LEAK)
- Heap Growth Rate
- GC Efficiency
- Leaking Object Types

## Troubleshooting

### Issue: "java is not recognized"
**Solution**: Restart Command Prompt after installing Java

### Issue: Port 8080 already in use
**Solution**:
1. Kill the process using port 8080
2. Or change port in `src/main/resources/application.yml`:
```yaml
server:
  port: 8081
```

### Issue: Tests failing
**Solution**: Run without tests first:
```bash
mvnw.cmd clean install -DskipTests
```

## Next Steps

1. ✓ Application running
2. ✓ APIs tested
3. **View H2 Database**: http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:mem:leakdetectordb`
   - Username: `sa`
   - Password: (leave blank)
4. **Run Docker**: `docker build -t leak-detector .`
5. **Setup Git & GitHub Actions**: Initialize repo and push
6. **Explore Code**: Check out implementation in `src/main/java`

## Demo Script for Presentation

Use this for demonstrating the application:

```bash
# Terminal 1: Start the application
mvnw.cmd spring-boot:run

# Terminal 2: Run simulation commands
# Scenario: Memory leak detection

# Step 1: Initial allocation
curl -X POST http://localhost:8080/simulate/allocate -H "Content-Type: application/json" -d "{\"count\":100,\"sizeInKB\":75,\"objectType\":\"UserSession\",\"createReferences\":true}"

# Step 2: Check heap
curl http://localhost:8080/simulate/heap

# Step 3: More allocation (simulating leak)
curl -X POST http://localhost:8080/simulate/allocate -H "Content-Type: application/json" -d "{\"count\":150,\"sizeInKB\":75,\"objectType\":\"CachedData\",\"createReferences\":true}"

# Step 4: Run GC
curl -X POST http://localhost:8080/simulate/gc/minor

# Step 5: More allocation
curl -X POST http://localhost:8080/simulate/allocate -H "Content-Type: application/json" -d "{\"count\":150,\"sizeInKB\":75,\"objectType\":\"TempData\",\"createReferences\":true}"

# Step 6: Analyze - Should detect leak!
curl http://localhost:8080/analyze/leak

# Step 7: View history
curl http://localhost:8080/analyze/history
```

## Success Criteria ✓

- [x] Application builds successfully
- [x] All tests pass (80%+ coverage)
- [x] Application starts without errors
- [x] APIs accessible via Swagger UI
- [x] Leak detection algorithm works
- [x] GC simulation functional
- [x] Docker image builds
- [x] CI/CD pipeline configured

## Need Help?

Check the full **README.md** for:
- Detailed API documentation
- Architecture overview
- Development guidelines
- Docker deployment
- CI/CD setup

Enjoy exploring the Java Memory Leak Detection Simulator! 🚀
