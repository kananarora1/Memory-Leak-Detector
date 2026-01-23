# Memory Leak Detector

A Spring Boot application that simulates heap memory behavior and detects potential memory leaks.

## What it does

- Simulates object allocation in JVM heap (Young and Old generation)
- Runs garbage collection cycles (Minor and Major GC)
- Analyzes heap patterns to detect memory leaks
- Provides REST APIs to trigger simulations and generate reports

## Requirements

- Java 17 or higher
- Maven 3.6+

## Running Locally

### 1. Clone the repository

```bash
git clone <repository-url>
cd DevOpsProj
```

### 2. Build the project

```bash
./mvnw clean package
```

On Windows:
```bash
mvnw.cmd clean package
```

### 3. Run the application

```bash
./mvnw spring-boot:run
```

Or run the JAR directly:
```bash
java -jar target/DevOpsProj-0.0.1-SNAPSHOT.jar
```

### 4. Access the application

- Application: http://localhost:8080
- H2 Database Console: http://localhost:8080/h2-console
- API Documentation: http://localhost:8080/swagger-ui.html

## API Endpoints

### Simulate heap allocation
```bash
POST /simulate/heap?count=100&sizeKB=50&type=TestObject
```

### Run garbage collection
```bash
POST /simulate/gc/minor
POST /simulate/gc/major
```

### Generate leak report
```bash
GET /analyze/report
```

## Using Docker

Build and run with Docker:

```bash
docker build -t leak-detector .
docker run -p 8080:8080 leak-detector
```

Using Docker Compose:

```bash
docker-compose up
```

## Configuration

Edit `src/main/resources/application.yml` to configure:
- Server port
- Database settings
- Simulation parameters
