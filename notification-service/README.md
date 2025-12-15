# Notification Service

Notification Management microservice for DevBlocker - handles in-app and email notifications by consuming events from other services.

## Features

- Consumes events from other services (BlockerCreated, CommentAdded, SolutionAdded, SolutionAccepted)
- Creates notification entries in database
- Provides API to fetch user notifications
- Marks notifications as read
- Sends email notifications for critical events (configurable)
- Pagination support
- Unread notification count
- OpenAPI/Swagger documentation
- Health check endpoints
- Docker support

## Tech Stack

- **Java 21**
- **Spring Boot 4.0.0**
- **PostgreSQL** - Database for notifications
- **Redis** - Caching/rate limiting (optional)
- **RabbitMQ** - Event consumption
- **Spring Mail** - Email notifications
- **OpenAPI/Swagger** - API documentation
- **Docker** - Containerization

## Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 16+
- Redis (optional, for caching)
- RabbitMQ (for event consumption)
- SMTP server or email provider (for email notifications)
- Docker & Docker Compose (optional)

## Setup

### 1. Database Setup

Create a PostgreSQL database:
```sql
CREATE DATABASE notificationdb;
```

### 2. Configuration

Update `application.properties` or use environment variables:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/notificationdb
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Email Configuration (optional)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# Enable email notifications
notification.email.enabled=true
notification.email.critical-events=BLOCKER_CREATED,SOLUTION_ACCEPTED,USER_MENTIONED
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

### Get User Notifications

```http
GET /api/v1/notifications?userId={userId}&unreadOnly=false&page=0&size=20
```

**Query Parameters:**
- `userId` (required) - User ID to fetch notifications for
- `unreadOnly` (optional, default: false) - Filter to unread notifications only
- `page` (optional, default: 0) - Page number (0-indexed)
- `size` (optional, default: 20) - Page size

**Response:**
```json
{
  "content": [
    {
      "notificationId": "uuid",
      "userId": "uuid",
      "type": "SOLUTION_ACCEPTED",
      "title": "Solution Accepted!",
      "message": "Your solution for blocker has been accepted as the best solution",
      "relatedEntityId": "blocker-uuid",
      "relatedEntityType": "blocker",
      "read": false,
      "createdAt": "2024-01-01T12:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### Mark Notification as Read

```http
POST /api/v1/notifications/{notificationId}/mark-read?userId={userId}
```

**Response:**
```json
{
  "notificationId": "uuid",
  "userId": "uuid",
  "type": "SOLUTION_ACCEPTED",
  "title": "Solution Accepted!",
  "message": "Your solution for blocker has been accepted as the best solution",
  "relatedEntityId": "blocker-uuid",
  "relatedEntityType": "blocker",
  "read": true,
  "createdAt": "2024-01-01T12:00:00"
}
```

### Get Unread Count

```http
GET /api/v1/notifications/unread-count?userId={userId}
```

**Response:**
```json
5
```

## Events Consumed

The service listens to the following events from other services:

### BlockerCreated Event
- **Source:** blocker-service
- **Exchange:** `blocker.events`
- **Routing Key:** `blocker.created`
- **Action:** Creates notification for assigned user (if different from creator)

### CommentAdded Event
- **Source:** comment-service
- **Exchange:** `comment.events`
- **Routing Key:** `comment.added`
- **Action:** Creates notification for blocker owner (if comment is on their blocker)

### SolutionAdded Event
- **Source:** solution-service
- **Exchange:** `solution.events`
- **Routing Key:** `solution.added`
- **Action:** Creates notification for blocker owner (if solution is for their blocker)

### SolutionAccepted Event
- **Source:** solution-service
- **Exchange:** `solution.events`
- **Routing Key:** `solution.accepted`
- **Action:** Creates notification for solution author (critical - may trigger email)

## Email Notifications

Email notifications can be enabled for critical events. Configure in `application.properties`:

```properties
# Enable email notifications
notification.email.enabled=true

# Events that trigger email (comma-separated)
notification.email.critical-events=BLOCKER_CREATED,SOLUTION_ACCEPTED,USER_MENTIONED
```

**Supported Notification Types:**
- `BLOCKER_CREATED`
- `COMMENT_ADDED`
- `SOLUTION_ADDED`
- `SOLUTION_ACCEPTED`
- `SOLUTION_UPVOTED`
- `USER_MENTIONED`
- `BLOCKER_RESOLVED`
- `BLOCKER_UPDATED`

**Email Configuration:**
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Note:** In production, you would:
1. Fetch user email from user-service
2. Send emails asynchronously
3. Track email delivery status
4. Implement email templates

## Database Schema

### notifications Table

| Column | Type | Description |
|--------|------|-------------|
| notification_id | UUID | Primary key |
| user_id | UUID | User who should receive notification |
| type | VARCHAR | Notification type (enum) |
| title | VARCHAR | Notification title |
| message | TEXT | Notification message |
| related_entity_id | VARCHAR | ID of related blocker/solution/comment |
| related_entity_type | VARCHAR | Type of related entity |
| read | BOOLEAN | Whether notification is read (default: false) |
| email_sent | BOOLEAN | Whether email was sent (default: false) |
| created_at | TIMESTAMP | Creation timestamp |

**Indexes:**
- `idx_user_id` - For fast lookup by user
- `idx_read` - For filtering read/unread
- `idx_created_at` - For sorting by date
- `idx_user_read` - Composite index for user + read status

## Notification Types

| Type | Description | Critical |
|------|-------------|----------|
| BLOCKER_CREATED | New blocker created/assigned | Yes |
| COMMENT_ADDED | Comment added to blocker | No |
| SOLUTION_ADDED | Solution added to blocker | No |
| SOLUTION_ACCEPTED | Solution accepted as best | Yes |
| SOLUTION_UPVOTED | Solution upvoted | No |
| USER_MENTIONED | User mentioned in comment | Yes |
| BLOCKER_RESOLVED | Blocker resolved | No |
| BLOCKER_UPDATED | Blocker updated | No |

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
http://localhost:8086/swagger-ui.html
```

API Docs (JSON):
```
http://localhost:8086/api-docs
```

## Testing

### Manual Testing

1. **Get user notifications:**
```bash
curl "http://localhost:8086/api/v1/notifications?userId={userId}&page=0&size=20"
```

2. **Get unread notifications only:**
```bash
curl "http://localhost:8086/api/v1/notifications?userId={userId}&unreadOnly=true"
```

3. **Mark notification as read:**
```bash
curl -X POST "http://localhost:8086/api/v1/notifications/{notificationId}/mark-read?userId={userId}"
```

4. **Get unread count:**
```bash
curl "http://localhost:8086/api/v1/notifications/unread-count?userId={userId}"
```

### Testing Event Consumption

1. Create a blocker in blocker-service (triggers BlockerCreated event)
2. Add a comment in comment-service (triggers CommentAdded event)
3. Add a solution in solution-service (triggers SolutionAdded event)
4. Accept a solution in solution-service (triggers SolutionAccepted event)
5. Check notifications for relevant users

## Acceptance Criteria ✅

- ✅ Receives events (listeners consume events from other services)
- ✅ User can fetch unread notifications (GET endpoint with unreadOnly filter)
- ✅ Notifications are created when events are received
- ✅ Mark as read functionality works

## Future Enhancements

- User preferences (email frequency, notification types)
- Push notifications (web push, mobile push)
- Notification grouping (e.g., "5 new comments on blocker X")
- Notification templates
- Email templates with HTML
- Notification batching
- Real-time notifications via WebSocket
- Notification channels (in-app, email, SMS, push)

## Troubleshooting

### RabbitMQ Connection Issues
- Check RabbitMQ is running: `docker ps | grep rabbitmq`
- Verify connection settings in `application.properties`
- Check RabbitMQ management UI: `http://localhost:15677`
- Ensure queues are bound to correct exchanges

### Email Not Sending
- Verify email configuration in `application.properties`
- Check `notification.email.enabled=true`
- Verify SMTP credentials
- Check email provider settings (Gmail requires app password)

### Events Not Received
- Verify RabbitMQ exchanges exist
- Check queue bindings
- Verify routing keys match
- Check listener logs for errors

### Database Connection Issues
- Verify PostgreSQL is running
- Check database credentials
- Ensure database `notificationdb` exists

## Port Configuration

- **Service Port:** 8086
- **PostgreSQL:** 5437 (host) / 5432 (container)
- **Redis:** 6379 (host and container)
- **RabbitMQ:** 5677 (host) / 5672 (container)
- **RabbitMQ Management:** 15677 (host) / 15672 (container)

## License

Copyright © 2024 DevBlocker Team

