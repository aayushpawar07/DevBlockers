# DevBlocker Project - Services Overview

## Project Architecture

This is a **microservices-based application** called **DevBlocker** - a platform for managing development blockers, solutions, and collaboration. The project consists of:

- **6 Backend Microservices** (Spring Boot, Java 21)
- **1 Frontend Application** (React + Vite)
- **1 Message Broker** (RabbitMQ)
- **1 Database** (MySQL - expected to run on host machine)

---

## Services Breakdown

### 1. **Auth Service** (Port 8081)
**Location:** `./auth/`

**Purpose:** Authentication and Authorization
- User registration and login
- JWT token generation (RS256 algorithm)
- Access and refresh token management
- Role-based access control (RBAC)
- Password hashing with BCrypt

**Database:** `authdb` (MySQL)
**Key Features:**
- Publishes `UserRegistered` events to RabbitMQ
- JWT tokens expire in 15 minutes (access) and 24 hours (refresh)
- OpenAPI/Swagger documentation available

**API Endpoints:**
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login and get tokens
- `POST /api/v1/auth/refresh` - Refresh access token
- `GET /api/v1/auth/me` - Get current user info

---

### 2. **User Service** (Port 8082)
**Location:** `./user-service/`

**Purpose:** User Profile and Reputation Management
- User profile CRUD operations
- Reputation system with increment API
- Team membership management
- Event-driven: Listens to `UserRegistered`, publishes `UserUpdated`

**Database:** `userdb` (MySQL)
**Key Features:**
- Automatically creates profile when user registers
- Initializes reputation to 0 points
- Tracks team memberships

**API Endpoints:**
- `GET /api/v1/users/{id}` - Get user profile
- `PUT /api/v1/users/{id}` - Update user profile
- `GET /api/v1/users/{id}/reputation` - Get reputation
- `POST /api/v1/users/{id}/reputation/increment` - Increment reputation
- `GET /api/v1/teams/{teamId}/members` - Get team members

---

### 3. **Blocker Service** (Port 8083)
**Location:** `./blocker-service/`

**Purpose:** Core Blocker Management
- Create, update, and resolve blockers
- Tagging system for categorization
- Assignment to users and teams
- Severity levels (CRITICAL, HIGH, MEDIUM, LOW, TRIVIAL)
- Status tracking (OPEN, IN_PROGRESS, RESOLVED, CLOSED, DUPLICATE)
- Advanced filtering and pagination
- File uploads for blocker media

**Database:** `blockerdb` (MySQL)
**Key Features:**
- Publishes events: `BlockerCreated`, `BlockerUpdated`, `BlockerResolved`
- Supports media attachments (images/videos)
- Duplicate detection (stub for search-service integration)

**API Endpoints:**
- `POST /api/v1/blockers` - Create blocker
- `GET /api/v1/blockers` - List blockers (with filters)
- `GET /api/v1/blockers/{id}` - Get blocker details
- `PUT /api/v1/blockers/{id}` - Update blocker
- `POST /api/v1/blockers/{id}/resolve` - Resolve blocker
- `POST /api/v1/blockers/upload` - Upload media files

---

### 4. **Solution Service** (Port 8084)
**Location:** `./solution-service/`

**Purpose:** Solution Management for Blockers
- Add solutions to blockers
- Upvote solutions (idempotent - one vote per user)
- Accept solutions as best solution
- File uploads for solution media (images/videos)

**Database:** `solutiondb` (MySQL)
**Key Features:**
- Publishes events: `SolutionAdded`, `SolutionUpvoted`, `SolutionAccepted`
- Inter-service communication with blocker-service
- Solutions ordered by upvotes (desc) and creation date (asc)
- Only one solution per blocker can be accepted

**API Endpoints:**
- `POST /api/v1/blockers/{blockerId}/solutions` - Add solution
- `GET /api/v1/blockers/{blockerId}/solutions` - Get solutions
- `POST /api/v1/solutions/{solutionId}/upvote` - Upvote solution
- `POST /api/v1/solutions/{solutionId}/accept` - Accept solution
- `POST /api/v1/solutions/upload` - Upload media files
- `GET /api/v1/solutions/files/{fileId}` - Get uploaded file

---

### 5. **Comment Service** (Port 8085)
**Location:** `./comment-service/`

**Purpose:** Threaded Comments System
- Add top-level comments to blockers
- Reply to comments (nested/threaded comments)
- Unlimited nesting depth
- View threaded comment structure

**Database:** `commentdb` (MySQL)
**Key Features:**
- Publishes `CommentAdded` events
- Supports unlimited comment nesting
- Returns comments in nested structure

**API Endpoints:**
- `POST /api/v1/blockers/{blockerId}/comments` - Add comment
- `GET /api/v1/blockers/{blockerId}/comments` - Get comments (threaded)
- `POST /api/v1/comments/{commentId}/reply` - Reply to comment
- `GET /api/v1/comments/{commentId}` - Get comment with replies

---

### 6. **Notification Service** (Port 8086)
**Location:** `./notification-service/`

**Purpose:** Notification Management
- Consumes events from other services
- Creates notification entries
- Marks notifications as read
- Email notifications for critical events (configurable)
- Unread notification count

**Database:** `notificationdb` (MySQL)
**Key Features:**
- Listens to events: `BlockerCreated`, `CommentAdded`, `SolutionAdded`, `SolutionAccepted`
- Pagination support
- Email notifications (optional, requires SMTP configuration)

**API Endpoints:**
- `GET /api/v1/notifications` - Get user notifications (with filters)
- `POST /api/v1/notifications/{id}/mark-read` - Mark as read
- `GET /api/v1/notifications/unread-count` - Get unread count

**Notification Types:**
- BLOCKER_CREATED, COMMENT_ADDED, SOLUTION_ADDED
- SOLUTION_ACCEPTED, SOLUTION_UPVOTED
- USER_MENTIONED, BLOCKER_RESOLVED, BLOCKER_UPDATED

---

### 7. **Frontend** (Port 3000)
**Location:** `./frontend/`

**Purpose:** User Interface
- React 18 + Vite
- Tailwind CSS for styling
- React Router for navigation
- Authentication flow
- Dashboard with statistics
- Blocker management UI
- Solution management UI
- Threaded comments UI
- Notifications UI
- User profile management

**Features:**
- Login/Register pages
- Dashboard with blocker statistics
- Create/View/Filter blockers
- Add/Upvote/Accept solutions
- Threaded comments
- Real-time notifications
- User profile editing

**Tech Stack:**
- React 18, Vite, Tailwind CSS
- Axios for API calls
- React Hot Toast for notifications
- Lucide React for icons

---

## Infrastructure Services

### **RabbitMQ** (Ports 5672, 15672)
**Purpose:** Message Broker for Event-Driven Architecture
- AMQP port: 5672
- Management UI: 15672 (guest/guest)
- Used for inter-service communication via events

**Event Exchanges:**
- `user.events` - User-related events
- `blocker.events` - Blocker-related events
- `solution.events` - Solution-related events
- `comment.events` - Comment-related events

---

## Database Setup

**Note:** The project expects MySQL to run on the host machine at `host.docker.internal:3306`

**Databases Required:**
1. `authdb` - Auth service
2. `userdb` - User service
3. `blockerdb` - Blocker service
4. `solutiondb` - Solution service
5. `commentdb` - Comment service
6. `notificationdb` - Notification service

**Initialization:** Run `init-databases.sql` to create all databases, or they will be auto-created on first startup.

---

## Service Dependencies

```
Frontend
  ├── Auth Service (8081)
  ├── User Service (8082)
  ├── Blocker Service (8083)
  ├── Solution Service (8084)
  ├── Comment Service (8085)
  └── Notification Service (8086)

Auth Service
  └── Publishes: UserRegistered

User Service
  ├── Listens: UserRegistered
  └── Publishes: UserUpdated

Blocker Service
  ├── Listens: AttachmentUploaded, SolutionAdded
  └── Publishes: BlockerCreated, BlockerUpdated, BlockerResolved

Solution Service
  ├── Calls: Blocker Service (validate blocker, update bestSolutionId)
  └── Publishes: SolutionAdded, SolutionUpvoted, SolutionAccepted

Comment Service
  └── Publishes: CommentAdded

Notification Service
  ├── Listens: BlockerCreated, CommentAdded, SolutionAdded, SolutionAccepted
  └── Creates notifications for users
```

---

## Running the Project

### Quick Start (Docker Compose)
```bash
docker-compose up -d
```

This starts all services:
- RabbitMQ (5672, 15672)
- Auth Service (8081)
- User Service (8082)
- Blocker Service (8083)
- Solution Service (8084)
- Comment Service (8085)
- Notification Service (8086)
- Frontend (3000)

### Access Points
- **Frontend:** http://localhost:3000
- **RabbitMQ Management:** http://localhost:15672 (guest/guest)
- **Auth Service:** http://localhost:8081
- **User Service:** http://localhost:8082
- **Blocker Service:** http://localhost:8083
- **Solution Service:** http://localhost:8084
- **Comment Service:** http://localhost:8085
- **Notification Service:** http://localhost:8086

### Swagger Documentation
Each service has Swagger UI available at:
- `http://localhost:{PORT}/swagger-ui.html`

---

## Technology Stack Summary

**Backend:**
- Java 21
- Spring Boot 4.0.0
- Spring Security
- Spring Data JPA
- MySQL (all services)
- RabbitMQ (event messaging)
- OpenAPI/Swagger

**Frontend:**
- React 18
- Vite
- Tailwind CSS
- React Router
- Axios

**Infrastructure:**
- Docker & Docker Compose
- RabbitMQ
- MySQL

---

## Key Features

1. **Event-Driven Architecture** - Services communicate via RabbitMQ events
2. **JWT Authentication** - RS256 algorithm with access/refresh tokens
3. **File Uploads** - Support for images/videos in blockers and solutions
4. **Threaded Comments** - Unlimited nesting depth
5. **Reputation System** - User reputation tracking
6. **Notifications** - Real-time notifications with email support
7. **Solution Voting** - Upvote and accept best solutions
8. **Advanced Filtering** - Filter blockers by status, severity, tags, etc.

---

## Current Issue

The project is experiencing network issues when building Docker images (Maven dependency downloads failing). The code changes for fixing the solution media upload issue are complete, but the services need to be rebuilt once network connectivity is stable.

