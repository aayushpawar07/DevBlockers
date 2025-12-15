# Solution Service

Solution Management microservice for DevBlocker - handles adding solutions to blockers, upvoting, and accepting best solutions.

## Features

- Add solutions to blockers
- Upvote solutions (idempotent - one vote per user per solution)
- Accept solutions as best (marks as accepted and updates blocker)
- Event-driven architecture (publishes SolutionAdded, SolutionUpvoted, SolutionAccepted events)
- Inter-service communication with blocker-service
- OpenAPI/Swagger documentation
- Health check endpoints
- Docker support

## Tech Stack

- **Java 21**
- **Spring Boot 4.0.0**
- **PostgreSQL** - Database
- **RabbitMQ** - Event messaging
- **WebClient** - REST client for inter-service communication
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
CREATE DATABASE solutiondb;
```

### 2. Configuration

Update `application.properties` or use environment variables:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/solutiondb
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

services.blocker.url=http://localhost:8083
services.user.url=http://localhost:8082
services.auth.url=http://localhost:8081
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

### Add Solution to Blocker

```http
POST /api/v1/blockers/{blockerId}/solutions
Content-Type: application/json
Authorization: Bearer <token>

{
  "content": "Here's how to fix this issue...",
  "userId": "uuid-here"
}
```

**Response:**
```json
{
  "solutionId": "uuid",
  "blockerId": "uuid",
  "userId": "uuid",
  "content": "Here's how to fix this issue...",
  "upvotes": 0,
  "accepted": false,
  "createdAt": "2024-01-01T12:00:00"
}
```

**Event Published:** `SolutionAdded`

### Get Solutions for Blocker

```http
GET /api/v1/blockers/{blockerId}/solutions
```

**Response:**
```json
[
  {
    "solutionId": "uuid",
    "blockerId": "uuid",
    "userId": "uuid",
    "content": "Solution content...",
    "upvotes": 5,
    "accepted": false,
    "createdAt": "2024-01-01T12:00:00"
  }
]
```

Solutions are ordered by:
1. Upvotes (descending)
2. Creation date (ascending)

### Upvote Solution

```http
POST /api/v1/solutions/{solutionId}/upvote
Content-Type: application/json

{
  "userId": "uuid-here"
}
```

**Response:**
```json
{
  "solutionId": "uuid",
  "blockerId": "uuid",
  "userId": "uuid",
  "content": "Solution content...",
  "upvotes": 6,
  "accepted": false,
  "createdAt": "2024-01-01T12:00:00"
}
```

**Note:** Upvoting is idempotent - a user can only upvote once per solution. Subsequent upvote requests return the current state without error.

**Event Published:** `SolutionUpvoted`

### Accept Solution as Best

```http
POST /api/v1/solutions/{solutionId}/accept
Content-Type: application/json
Authorization: Bearer <token>

{
  "userId": "uuid-here"
}
```

**Response:**
```json
{
  "solutionId": "uuid",
  "blockerId": "uuid",
  "userId": "uuid",
  "content": "Solution content...",
  "upvotes": 10,
  "accepted": true,
  "createdAt": "2024-01-01T12:00:00"
}
```

**Actions:**
1. Marks solution as accepted
2. Updates blocker's `bestSolutionId` via REST call to blocker-service
3. Publishes `SolutionAccepted` event

**Note:** Only one solution per blocker can be accepted. If a blocker already has an accepted solution, accepting another will fail with 409 Conflict.

**Event Published:** `SolutionAccepted`

### Get Solution by ID

```http
GET /api/v1/solutions/{solutionId}
```

## Events

### SolutionAdded Event

Published when a new solution is added to a blocker.

```json
{
  "solutionId": "uuid",
  "blockerId": "uuid",
  "userId": "uuid",
  "content": "Solution content...",
  "createdAt": "2024-01-01T12:00:00"
}
```

**Exchange:** `solution.events`  
**Routing Key:** `solution.added`  
**Queue:** `solution.added.queue`

### SolutionUpvoted Event

Published when a solution is upvoted.

```json
{
  "solutionId": "uuid",
  "blockerId": "uuid",
  "userId": "uuid",
  "upvotes": 5,
  "upvotedAt": "2024-01-01T12:00:00"
}
```

**Exchange:** `solution.events`  
**Routing Key:** `solution.upvoted`  
**Queue:** `solution.upvoted.queue`

### SolutionAccepted Event

Published when a solution is accepted as the best solution.

```json
{
  "solutionId": "uuid",
  "blockerId": "uuid",
  "userId": "uuid",
  "acceptedBy": "uuid",
  "acceptedAt": "2024-01-01T12:00:00"
}
```

**Exchange:** `solution.events`  
**Routing Key:** `solution.accepted`  
**Queue:** `solution.accepted.queue`

## Database Schema

### solutions Table

| Column | Type | Description |
|--------|------|-------------|
| solution_id | UUID | Primary key |
| blocker_id | UUID | Foreign key to blocker |
| user_id | UUID | User who created the solution |
| content | TEXT | Solution content |
| upvotes | INTEGER | Number of upvotes (default: 0) |
| accepted | BOOLEAN | Whether solution is accepted (default: false) |
| created_at | TIMESTAMP | Creation timestamp |

### solution_upvotes Table

| Column | Type | Description |
|--------|------|-------------|
| upvote_id | UUID | Primary key |
| solution_id | UUID | Foreign key to solution |
| user_id | UUID | User who upvoted |
| created_at | TIMESTAMP | Upvote timestamp |

**Unique Constraint:** `(solution_id, user_id)` - ensures one vote per user per solution

## Inter-Service Communication

### Calls Blocker Service

- **Validate blocker exists** when adding a solution
- **Update blocker's bestSolutionId** when accepting a solution

### Service URLs

Configured in `application.properties`:
```properties
services.blocker.url=http://localhost:8083
services.user.url=http://localhost:8082
services.auth.url=http://localhost:8081
```

For Docker, use service names:
```properties
services.blocker.url=http://blocker-service:8083
services.user.url=http://user-service:8082
services.auth.url=http://auth-service:8081
```

## Health Check

```http
GET /actuator/health
```

**Response:**
```json
{
  "status": "UP"
}
```

## API Documentation

Swagger UI available at:
```
http://localhost:8084/swagger-ui.html
```

API Docs (JSON):
```
http://localhost:8084/api-docs
```

## Testing

### Manual Testing

1. **Add a solution:**
```bash
curl -X POST http://localhost:8084/api/v1/blockers/{blockerId}/solutions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "content": "Here is the solution...",
    "userId": "user-uuid"
  }'
```

2. **Get solutions:**
```bash
curl http://localhost:8084/api/v1/blockers/{blockerId}/solutions
```

3. **Upvote solution:**
```bash
curl -X POST http://localhost:8084/api/v1/solutions/{solutionId}/upvote \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-uuid"
  }'
```

4. **Accept solution:**
```bash
curl -X POST http://localhost:8084/api/v1/solutions/{solutionId}/accept \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "userId": "user-uuid"
  }'
```

## Acceptance Criteria ✅

- ✅ Add solution emits `SolutionAdded` event
- ✅ Upvote increments count (single vote per user - idempotent)
- ✅ Accept marks solution as accepted and updates blocker

## Troubleshooting

### RabbitMQ Connection Issues
- Check RabbitMQ is running: `docker ps | grep rabbitmq`
- Verify connection settings in `application.properties`
- Check RabbitMQ management UI: `http://localhost:15675`

### Blocker Service Not Found
- Verify blocker-service is running: `curl http://localhost:8083/actuator/health`
- Check service URL configuration
- Verify network connectivity

### Database Connection Issues
- Verify PostgreSQL is running
- Check database credentials
- Ensure database `solutiondb` exists

## Port Configuration

- **Service Port:** 8084
- **PostgreSQL:** 5435 (host) / 5432 (container)
- **RabbitMQ:** 5675 (host) / 5672 (container)
- **RabbitMQ Management:** 15675 (host) / 15672 (container)

## License

Copyright © 2024 DevBlocker Team

