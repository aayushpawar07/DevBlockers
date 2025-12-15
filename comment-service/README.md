# Comment Service

Comment Management microservice for DevBlocker - handles threaded comments on blockers and solutions.

## Features

- Add top-level comments to blockers
- Reply to comments (threaded/nested comments)
- View threaded comment structure
- Event-driven architecture (publishes CommentAdded events)
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
CREATE DATABASE commentdb;
```

### 2. Configuration

Update `application.properties` or use environment variables:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/commentdb
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

### Add Comment to Blocker

```http
POST /api/v1/blockers/{blockerId}/comments
Content-Type: application/json
Authorization: Bearer <token>

{
  "content": "This is a great blocker!",
  "userId": "uuid-here"
}
```

**Response:**
```json
{
  "commentId": "uuid",
  "blockerId": "uuid",
  "userId": "uuid",
  "parentCommentId": null,
  "content": "This is a great blocker!",
  "createdAt": "2024-01-01T12:00:00",
  "replies": [],
  "replyCount": 0
}
```

**Event Published:** `CommentAdded`

### Get Comments for Blocker

```http
GET /api/v1/blockers/{blockerId}/comments
```

**Response:**
```json
[
  {
    "commentId": "uuid",
    "blockerId": "uuid",
    "userId": "uuid",
    "parentCommentId": null,
    "content": "Top-level comment",
    "createdAt": "2024-01-01T12:00:00",
    "replies": [
      {
        "commentId": "uuid",
        "blockerId": "uuid",
        "userId": "uuid",
        "parentCommentId": "parent-uuid",
        "content": "Reply to comment",
        "createdAt": "2024-01-01T12:05:00",
        "replies": [],
        "replyCount": 0
      }
    ],
    "replyCount": 1
  }
]
```

Comments are returned in a threaded structure:
- Top-level comments have `parentCommentId: null`
- Replies are nested in the `replies` array
- Replies can have their own nested replies (unlimited depth)

### Reply to Comment

```http
POST /api/v1/comments/{commentId}/reply
Content-Type: application/json
Authorization: Bearer <token>

{
  "content": "I agree with this comment!",
  "userId": "uuid-here"
}
```

**Response:**
```json
{
  "commentId": "uuid",
  "blockerId": "uuid",
  "userId": "uuid",
  "parentCommentId": "parent-comment-uuid",
  "content": "I agree with this comment!",
  "createdAt": "2024-01-01T12:10:00",
  "replies": [],
  "replyCount": 0
}
```

**Event Published:** `CommentAdded`

### Get Comment by ID

```http
GET /api/v1/comments/{commentId}
```

Returns a comment with all its nested replies.

## Events

### CommentAdded Event

Published when a new comment (top-level or reply) is added.

```json
{
  "commentId": "uuid",
  "blockerId": "uuid",
  "userId": "uuid",
  "parentCommentId": "uuid-or-null",
  "content": "Comment content...",
  "createdAt": "2024-01-01T12:00:00"
}
```

**Exchange:** `comment.events`  
**Routing Key:** `comment.added`  
**Queue:** `comment.added.queue`

**Note:** `parentCommentId` is `null` for top-level comments, and contains the parent comment ID for replies.

## Database Schema

### comments Table

| Column | Type | Description |
|--------|------|-------------|
| comment_id | UUID | Primary key |
| blocker_id | UUID | Foreign key to blocker |
| user_id | UUID | User who created the comment |
| parent_comment_id | UUID | Parent comment ID (null for top-level) |
| content | TEXT | Comment content |
| created_at | TIMESTAMP | Creation timestamp |

**Indexes:**
- `idx_blocker_id` - For fast lookup of comments by blocker
- `idx_user_id` - For fast lookup of comments by user
- `idx_parent_comment_id` - For fast lookup of replies

## Threading Model

Comments support unlimited nesting depth:

```
Top-level Comment
  ├── Reply 1
  │   ├── Reply to Reply 1
  │   └── Another reply to Reply 1
  └── Reply 2
      └── Reply to Reply 2
```

The API returns comments in a nested structure where:
- Top-level comments have `parentCommentId: null`
- Each comment contains a `replies` array with its direct children
- The `replyCount` field shows the total number of direct replies

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
http://localhost:8085/swagger-ui.html
```

API Docs (JSON):
```
http://localhost:8085/api-docs
```

## Testing

### Manual Testing

1. **Add a top-level comment:**
```bash
curl -X POST http://localhost:8085/api/v1/blockers/{blockerId}/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "content": "This blocker needs attention!",
    "userId": "user-uuid"
  }'
```

2. **Get all comments for a blocker:**
```bash
curl http://localhost:8085/api/v1/blockers/{blockerId}/comments
```

3. **Reply to a comment:**
```bash
curl -X POST http://localhost:8085/api/v1/comments/{commentId}/reply \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "content": "I agree!",
    "userId": "user-uuid"
  }'
```

4. **Get a specific comment:**
```bash
curl http://localhost:8085/api/v1/comments/{commentId}
```

## Acceptance Criteria ✅

- ✅ Comments visible (GET endpoint returns threaded structure)
- ✅ Thread replies work (POST /comments/{id}/reply creates nested replies)
- ✅ CommentAdded event published for both top-level comments and replies

## Example Thread Structure

```json
{
  "commentId": "comment-1",
  "content": "This is a blocker",
  "replies": [
    {
      "commentId": "comment-2",
      "content": "I agree",
      "parentCommentId": "comment-1",
      "replies": [
        {
          "commentId": "comment-3",
          "content": "Me too!",
          "parentCommentId": "comment-2",
          "replies": []
        }
      ]
    }
  ]
}
```

## Troubleshooting

### RabbitMQ Connection Issues
- Check RabbitMQ is running: `docker ps | grep rabbitmq`
- Verify connection settings in `application.properties`
- Check RabbitMQ management UI: `http://localhost:15676`

### Database Connection Issues
- Verify PostgreSQL is running
- Check database credentials
- Ensure database `commentdb` exists

### Threading Issues
- Verify `parentCommentId` is correctly set when replying
- Check that parent comment exists before creating reply
- Ensure replies belong to the same blocker as parent

## Port Configuration

- **Service Port:** 8085
- **PostgreSQL:** 5436 (host) / 5432 (container)
- **RabbitMQ:** 5676 (host) / 5672 (container)
- **RabbitMQ Management:** 15676 (host) / 15672 (container)

## Future Enhancements

- Moderation flags (as mentioned in requirements)
- Edit/delete comments
- Comment reactions (like, dislike)
- Comment mentions (@username)
- Comment search
- Pagination for large comment threads

## License

Copyright © 2024 DevBlocker Team

