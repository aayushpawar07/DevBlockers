# User Service

User Profile, Reputation, and Team Management microservice built with Spring Boot.

## Features

- User profile management (CRUD operations)
- Reputation system with increment API
- Team membership management
- Event-driven architecture (listens to UserRegistered, publishes UserUpdated)
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
CREATE DATABASE userdb;
```

### 2. Configuration

Update `application.properties` or use environment variables:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/userdb
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

### User Profile Endpoints

#### Get User Profile
```http
GET /api/v1/users/{id}
```

#### Update User Profile
```http
PUT /api/v1/users/{id}
Content-Type: application/json

{
  "name": "John Doe",
  "avatarUrl": "https://example.com/avatar.jpg",
  "teamId": "uuid-here"
}
```

### Reputation Endpoints

#### Get User Reputation
```http
GET /api/v1/users/{id}/reputation
```

#### Increment User Reputation
```http
POST /api/v1/users/{id}/reputation/increment
Content-Type: application/json

{
  "points": 10
}
```

### Team Endpoints

#### Get Team Members
```http
GET /api/v1/teams/{teamId}/members
```

### Health Check
```http
GET /actuator/health
```

### API Documentation
- Swagger UI: http://localhost:8082/swagger-ui.html
- OpenAPI Docs: http://localhost:8082/api-docs

## Database Schema

### Profiles Table
```sql
CREATE TABLE profiles (
    user_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    team_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Reputation Table
```sql
CREATE TABLE reputation (
    user_id UUID PRIMARY KEY,
    points INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Teams Table
```sql
CREATE TABLE teams (
    team_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## Events

### Subscribed Events

**UserRegistered** (from auth-service)
- Exchange: `user.events`
- Routing Key: `user.registered`
- Action: Automatically creates a profile and initializes reputation (0 points) for the new user

### Published Events

**UserUpdated** (when profile is updated)
- Exchange: `user.events`
- Routing Key: `user.updated`
- Event Structure:
```json
{
  "userId": "uuid",
  "name": "John Doe",
  "avatarUrl": "https://example.com/avatar.jpg",
  "teamId": "uuid",
  "updatedAt": "2024-01-01T00:00:00"
}
```

## Docker

### Build Image
```bash
docker build -t user-service:latest .
```

### Run Container
```bash
docker run -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/userdb \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e RABBITMQ_HOST=host.docker.internal \
  -e RABBITMQ_PORT=5672 \
  user-service:latest
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/userdb` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `postgres` |
| `RABBITMQ_HOST` | RabbitMQ host | `localhost` |
| `RABBITMQ_PORT` | RabbitMQ port | `5672` |
| `RABBITMQ_USERNAME` | RabbitMQ username | `guest` |
| `RABBITMQ_PASSWORD` | RabbitMQ password | `guest` |

## Acceptance Criteria

✅ Profile created automatically on user registration  
✅ Reputation can be incremented via API  
✅ User profile can be retrieved and updated  
✅ Team members can be retrieved  
✅ UserUpdated event published on profile changes  
✅ OpenAPI documentation available  
✅ Docker image builds and runs  

## License

MIT

