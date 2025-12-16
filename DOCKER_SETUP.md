# Docker Compose Setup Guide

This project uses Docker Compose to run all services together. This guide will help you set up and run the entire DevBlocker application.

## Prerequisites

- Docker Desktop installed and running
- At least 4GB of RAM available
- Ports 3000, 3306, 5672, 15672, 8081-8086 available

## Quick Start

1. **Start all services:**
   ```bash
   docker-compose up -d
   ```

2. **View logs:**
   ```bash
   docker-compose logs -f
   ```

3. **Stop all services:**
   ```bash
   docker-compose down
   ```

4. **Stop and remove volumes (clean slate):**
   ```bash
   docker-compose down -v
   ```

## Services

The docker-compose setup includes:

### Infrastructure
- **MySQL** (Port 3306) - Database for all services
- **RabbitMQ** (Ports 5672, 15672) - Message broker

### Backend Services
- **auth-service** (Port 8081) - Authentication service
- **user-service** (Port 8082) - User management service
- **blocker-service** (Port 8083) - Blocker management service
- **solution-service** (Port 8084) - Solution management service
- **comment-service** (Port 8085) - Comment service
- **notification-service** (Port 8086) - Notification service

### Frontend
- **frontend** (Port 3000) - React frontend application

## Access Points

- **Frontend:** http://localhost:3000
- **RabbitMQ Management:** http://localhost:15672 (guest/guest)
- **Auth Service:** http://localhost:8081
- **User Service:** http://localhost:8082
- **Blocker Service:** http://localhost:8083
- **Solution Service:** http://localhost:8084
- **Comment Service:** http://localhost:8085
- **Notification Service:** http://localhost:8086

## Database Setup

All databases are automatically created on first startup:
- `authdb` - Auth service database
- `userdb` - User service database
- `blockerdb` - Blocker service database
- `solutiondb` - Solution service database
- `commentdb` - Comment service database
- `notificationdb` - Notification service database

## Environment Variables

All services are configured with environment variables in docker-compose.yml. You can modify them as needed.

## Volumes

- `mysql_data` - MySQL data persistence
- `blocker_uploads` - Blocker file uploads
- `solution_uploads` - Solution file uploads

## Troubleshooting

### Services not starting
1. Check if all ports are available:
   ```bash
   netstat -ano | findstr "3000 3306 5672 8081 8082 8083 8084 8085 8086"
   ```

2. Check service logs:
   ```bash
   docker-compose logs [service-name]
   ```

3. Restart a specific service:
   ```bash
   docker-compose restart [service-name]
   ```

### Database connection issues
- Wait for MySQL to be healthy before services start
- Check MySQL logs: `docker-compose logs mysql`

### Frontend not loading
- Ensure all backend services are healthy
- Check frontend logs: `docker-compose logs frontend`
- Verify environment variables are set correctly

## Building Services

To rebuild services after code changes:

```bash
# Rebuild all services
docker-compose build

# Rebuild specific service
docker-compose build [service-name]

# Rebuild and restart
docker-compose up -d --build [service-name]
```

## Development Mode

For development, you might want to run services individually outside Docker. The docker-compose setup is optimized for production-like environments.

## Production Considerations

For production deployment:
1. Change default passwords
2. Use environment files (.env) for sensitive data
3. Configure proper SSL/TLS
4. Set up proper backup strategies
5. Configure resource limits
6. Use production-grade database configurations

