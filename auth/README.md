# Auth Service

Authentication and Authorization microservice built with Spring Boot, Spring Security, and JWT (RS256).

## Features

- User registration and login
- JWT-based authentication with RS256 algorithm
- Access and refresh token support
- Role-based access control (RBAC)
- Password hashing with BCrypt
- UserRegistered event publishing via RabbitMQ
- OpenAPI/Swagger documentation
- Health check endpoints
- Docker support

## Tech Stack

- **Java 21**
- **Spring Boot 4.0.0**
- **Spring Security** - Authentication and authorization
- **JWT (JJWT 0.12.3)** - Token generation and validation with RS256
- **MySQL** - Database
- **RabbitMQ** - Event publishing
- **OpenAPI/Swagger** - API documentation
- **Docker** - Containerization

## Prerequisites

- Java 21+
- Maven 3.9+
- MySQL 8.0+
- RabbitMQ (optional, for event publishing)
- Docker & Docker Compose (optional)

## Setup

### 1. Generate JWT Keys

The service uses RS256 algorithm which requires RSA key pairs. Generate them using:

**Linux/Mac:**
```bash
chmod +x scripts/generate-jwt-keys.sh
./scripts/generate-jwt-keys.sh
```

**Windows:**
```powershell
.\scripts\generate-jwt-keys.ps1
```

Or manually:
```bash
mkdir -p src/main/resources/jwt
openssl genpkey -algorithm RSA -out src/main/resources/jwt/private-key.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in src/main/resources/jwt/private-key.pem -out src/main/resources/jwt/public-key.pem
```

**Note:** For production, store keys securely and use environment variables or a secrets manager.

### 2. Database Setup

**Option A: Install MySQL Locally**

Download and install MySQL from [https://dev.mysql.com/downloads/installer/](https://dev.mysql.com/downloads/installer/)

After installation, the database will be created automatically when the application starts (due to `createDatabaseIfNotExist=true` parameter).

Or manually create it:
```sql
CREATE DATABASE authdb;
```

**Option B: Use Docker Compose** (see below) which will create it automatically.

### 3. Configuration

Update `application.properties` or use environment variables:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/authdb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root

jwt.private-key.path=classpath:jwt/private-key.pem
jwt.public-key.path=classpath:jwt/public-key.pem
```

### 4. Run with Docker Compose

```bash
docker-compose up -d
```

This will start:
- MySQL on port 3306
- RabbitMQ on ports 5672 (AMQP) and 15672 (Management UI)
- Auth Service on port 8080

### 5. Run Locally

```bash
mvn spring-boot:run
```

## API Endpoints

### Authentication Endpoints

#### Register User
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJSUzI1NiIs...",
  "tokenType": "Bearer"
}
```

#### Refresh Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJSUzI1NiIs..."
}
```

#### Get Current User
```http
GET /api/v1/auth/me
Authorization: Bearer <access_token>
```

### Health Check
```http
GET /actuator/health
```

### API Documentation
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Docs: http://localhost:8080/api-docs

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    user_id CHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Note: MySQL uses CHAR(36) for UUID storage instead of native UUID type.

## Events

The service publishes `UserRegistered` events to RabbitMQ when a new user registers:

- **Exchange:** `user.events`
- **Routing Key:** `user.registered`
- **Event Structure:**
```json
{
  "userId": "uuid",
  "email": "user@example.com",
  "role": "USER",
  "createdAt": "2024-01-01T00:00:00"
}
```

## Security

- Passwords are hashed using BCrypt
- JWT tokens use RS256 algorithm
- Access tokens expire in 15 minutes (configurable)
- Refresh tokens expire in 24 hours (configurable)
- Stateless authentication (no server-side sessions)

## Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Docker

### Build Image
```bash
docker build -t auth-service:latest .
```

### Run Container
```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/authdb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  auth-service:latest
```

## CI/CD

GitHub Actions workflow is configured in `.github/workflows/ci.yml`:
- Builds the application
- Runs tests
- Builds Docker image
- Tests Docker image

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | MySQL connection URL | `jdbc:mysql://localhost:3306/authdb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `root` |
| `JWT_PRIVATE_KEY_PATH` | Path to private key | `classpath:jwt/private-key.pem` |
| `JWT_PUBLIC_KEY_PATH` | Path to public key | `classpath:jwt/public-key.pem` |
| `JWT_ACCESS_TOKEN_EXPIRATION` | Access token expiration (ms) | `900000` (15 min) |
| `JWT_REFRESH_TOKEN_EXPIRATION` | Refresh token expiration (ms) | `86400000` (24 hours) |
| `RABBITMQ_HOST` | RabbitMQ host | `localhost` |
| `RABBITMQ_PORT` | RabbitMQ port | `5672` |
| `RABBITMQ_USERNAME` | RabbitMQ username | `guest` |
| `RABBITMQ_PASSWORD` | RabbitMQ password | `guest` |

## Acceptance Criteria

✅ Can register new users  
✅ Can login and receive JWT tokens  
✅ JWT tokens can be validated by gateway  
✅ Refresh token endpoint works  
✅ `/me` endpoint returns authenticated user  
✅ Health check endpoint available  
✅ OpenAPI documentation available  
✅ Docker image builds and runs  
✅ CI pipeline builds and tests Docker image  

## License

MIT

