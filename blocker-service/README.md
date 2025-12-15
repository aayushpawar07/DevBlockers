# Blocker Service

Core blocker management microservice for creating, updating, resolving blockers with tagging, assignment, and duplicate detection.

## Features

- Create, update, and resolve blockers
- Tagging system for categorization
- Assignment to users and teams
- Severity levels (CRITICAL, HIGH, MEDIUM, LOW, TRIVIAL)
- Status tracking (OPEN, IN_PROGRESS, RESOLVED, CLOSED, DUPLICATE)
- Advanced filtering and pagination
- Event-driven architecture
- Duplicate detection (stub for search-service integration)
- OpenAPI/Swagger documentation
- Health check endpoints
- Docker support

## Tech Stack

- **Java 21**
- **Spring Boot 4.0.0**
- **PostgreSQL** - Database
- **RabbitMQ** - Event messaging
- **OpenAPI/Swagger** - API documentation
- **Docker** - Containerization

## Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 16+
- RabbitMQ (for event messaging)
- Docker & Docker Compose (optional)

## Setup

### 1. Database Setup

Create a PostgreSQL database:
```sql
CREATE DATABASE blockerdb;
```

### 2. Configuration

Update `application.properties` or use environment variables:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/blockerdb
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

### 3. Run with Docker Compose

```bash
docker-compose up -d
```

### 4. Run Locally

```bash
mvn spring-boot:run
```

## API Endpoints

### Blocker Endpoints

#### Create Blocker
```http
POST /api/v1/blockers
Content-Type: application/json

{
  "title": "Database connection timeout",
  "description": "Application fails to connect to database after 30 seconds",
  "severity": "HIGH",
  "createdBy": "uuid-here",
  "assignedTo": "uuid-here",
  "teamId": "uuid-here",
  "tags": ["database", "timeout", "critical"]
}
```

#### Get Blockers (with filters)
```http
GET /api/v1/blockers?status=OPEN&severity=HIGH&teamId={uuid}&tag=database&page=0&size=20
```

**Query Parameters:**
- `status` - Filter by status (OPEN, IN_PROGRESS, RESOLVED, CLOSED, DUPLICATE)
- `severity` - Filter by severity (CRITICAL, HIGH, MEDIUM, LOW, TRIVIAL)
- `createdBy` - Filter by creator user ID
- `assignedTo` - Filter by assigned user ID
- `teamId` - Filter by team ID
- `tag` - Filter by tag
- `page` - Page number (default: 0)
- `size` - Page size (default: 20)

#### Get Blocker
```http
GET /api/v1/blockers/{id}
```

#### Update Blocker
```http
PUT /api/v1/blockers/{id}
Content-Type: application/json

{
  "title": "Updated title",
  "description": "Updated description",
  "severity": "MEDIUM",
  "assignedTo": "uuid-here",
  "tags": ["updated", "tag"]
}
```

#### Resolve Blocker
```http
POST /api/v1/blockers/{id}/resolve
Content-Type: application/json
X-User-Id: {resolver-user-id}

{
  "bestSolutionId": "uuid-here"
}
```

### Health Check
```http
GET /actuator/health
```

### API Documentation
- Swagger UI: http://localhost:8083/swagger-ui.html
- OpenAPI Docs: http://localhost:8083/api-docs

## Database Schema

### Blockers Table
```sql
CREATE TABLE blockers (
    blocker_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    created_by UUID NOT NULL,
    assigned_to UUID,
    team_id UUID,
    best_solution_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

CREATE TABLE blocker_tags (
    blocker_id UUID NOT NULL,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (blocker_id, tag),
    FOREIGN KEY (blocker_id) REFERENCES blockers(blocker_id) ON DELETE CASCADE
);
```

## Events

### Published Events

**BlockerCreated** (when blocker is created)
- Exchange: `blocker.events`
- Routing Key: `blocker.created`
- Event Structure:
```json
{
  "blockerId": "uuid",
  "title": "Database connection timeout",
  "description": "...",
  "status": "OPEN",
  "severity": "HIGH",
  "createdBy": "uuid",
  "assignedTo": "uuid",
  "teamId": "uuid",
  "tags": ["database", "timeout"],
  "createdAt": "2024-01-01T00:00:00"
}
```

**BlockerUpdated** (when blocker is updated)
- Exchange: `blocker.events`
- Routing Key: `blocker.updated`

**BlockerResolved** (when blocker is resolved)
- Exchange: `blocker.events`
- Routing Key: `blocker.resolved`
- Event Structure:
```json
{
  "blockerId": "uuid",
  "title": "Database connection timeout",
  "bestSolutionId": "uuid",
  "resolvedBy": "uuid",
  "resolvedAt": "2024-01-01T00:00:00"
}
```

### Consumed Events

**AttachmentUploaded** (from file-service)
- Exchange: `blocker.events` (or other service exchange)
- Routing Key: `attachment.uploaded`
- Action: Attaches file reference to blocker

**SolutionAdded** (from solution-service)
- Exchange: `blocker.events` (or other service exchange)
- Routing Key: `solution.added`
- Action: Updates best solution if none exists

## Duplicate Detection

The service includes a stub for duplicate detection that will call the search-service when available. Currently, it's disabled by default but can be enabled via configuration:

```properties
duplicate.detection.enabled=true
search.service.url=http://localhost:8084
```

## Transaction Synchronization

Events are published **after transaction commit** using Spring's `TransactionSynchronizationManager` to ensure:
- Events are only published if the database transaction succeeds
- No events are published if the transaction rolls back
- Reliable event delivery

## Docker

### Build Image
```bash
docker build -t blocker-service:latest .
```

### Run Container
```bash
docker run -p 8083:8083 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/blockerdb \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e RABBITMQ_HOST=host.docker.internal \
  -e RABBITMQ_PORT=5672 \
  blocker-service:latest
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/blockerdb` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `postgres` |
| `RABBITMQ_HOST` | RabbitMQ host | `localhost` |
| `RABBITMQ_PORT` | RabbitMQ port | `5672` |
| `RABBITMQ_USERNAME` | RabbitMQ username | `guest` |
| `RABBITMQ_PASSWORD` | RabbitMQ password | `guest` |
| `SEARCH_SERVICE_URL` | Search service URL for duplicate detection | `http://localhost:8084` |
| `DUPLICATE_DETECTION_ENABLED` | Enable duplicate detection | `false` |

## Acceptance Criteria

✅ Create blocker persists to database  
✅ BlockerCreated event published after DB commit  
✅ List/filter blockers works with pagination  
✅ Update blocker works and publishes BlockerUpdated event  
✅ Resolve blocker works and publishes BlockerResolved event  
✅ Duplicate detection stub calls search-service (when enabled)  
✅ OpenAPI documentation available  
✅ Docker image builds and runs  

## License

MIT

