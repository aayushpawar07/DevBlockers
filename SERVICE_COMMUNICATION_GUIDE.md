# Service Communication Guide

This guide explains how services communicate with each other in the DevBlocker microservices architecture.

## Communication Patterns

### 1. **Asynchronous Communication (Event-Driven) - RabbitMQ**

**When to use:**
- Fire-and-forget operations
- Decoupled services that don't need immediate response
- Event notifications (user registered, blocker created, etc.)
- Eventual consistency scenarios

**Current Implementation:**
- ✅ **Auth Service** → Publishes `UserRegistered` events
- ✅ **User Service** → Listens to `UserRegistered` events
- ✅ **Blocker Service** → Publishes blocker events and listens to attachment/solution events

**How it works:**
1. Service publishes an event to RabbitMQ exchange
2. RabbitMQ routes the event to subscribed queues
3. Listening services consume events asynchronously
4. Services process events independently

**Example Flow:**
```
Auth Service (User Registration)
    ↓ (publishes event)
RabbitMQ Exchange: "user.events"
    ↓ (routes to queue)
User Service (listens to "user.registered.queue")
    ↓ (creates profile)
Profile Created
```

### 2. **Synchronous Communication (REST API) - WebClient**

**When to use:**
- Need immediate response
- Request-response pattern
- Data validation across services
- Transactional operations

**Implementation:**
- Using Spring WebClient (reactive, non-blocking)
- Service-to-service authentication via JWT tokens
- Circuit breakers for resilience
- Retry mechanisms for transient failures

**Example Use Cases:**
- Blocker Service needs to validate user exists → calls User Service
- User Service needs to check blocker details → calls Blocker Service
- Any service needs to validate JWT token → calls Auth Service

## Service URLs Configuration

Each service should have the following configuration in `application.properties`:

```properties
# Service URLs
services.auth.url=http://localhost:8081
services.user.url=http://localhost:8082
services.blocker.url=http://localhost:8083
```

For production, use environment variables:
```properties
services.auth.url=${AUTH_SERVICE_URL:http://localhost:8081}
services.user.url=${USER_SERVICE_URL:http://localhost:8082}
services.blocker.url=${BLOCKER_SERVICE_URL:http://localhost:8083}
```

## Service Ports

| Service | Port | Description |
|---------|------|-------------|
| Auth Service | 8081 | Authentication & Authorization |
| User Service | 8082 | User Profiles & Reputation |
| Blocker Service | 8083 | Blocker Management |

## Communication Examples

### Example 1: Blocker Service → User Service (Synchronous)

**Scenario:** When creating a blocker, validate that the user exists.

```java
// In BlockerService
public BlockerResponse createBlocker(CreateBlockerRequest request) {
    // Validate user exists via User Service
    UserProfile profile = userServiceClient.getUserProfile(request.getCreatedBy());
    
    if (profile == null) {
        throw new IllegalArgumentException("User not found");
    }
    
    // Continue with blocker creation...
}
```

### Example 2: Auth Service → User Service (Asynchronous)

**Scenario:** When a user registers, notify User Service to create a profile.

```java
// In AuthService (already implemented)
public void register(RegisterRequest request) {
    User user = userRepository.save(newUser);
    
    // Publish event asynchronously
    eventPublisher.publishUserRegistered(user);
}
```

### Example 3: User Service → Blocker Service (Synchronous)

**Scenario:** Get user's blocker statistics.

```java
// In UserService
public UserStatsResponse getUserStats(UUID userId) {
    // Call Blocker Service to get user's blockers
    List<BlockerResponse> blockers = blockerServiceClient.getBlockersByUser(userId);
    
    // Calculate stats...
}
```

## Service-to-Service Authentication

When making synchronous calls, services should:
1. Extract JWT token from incoming request (if available)
2. Forward the token to downstream services
3. Downstream services validate the token

**Implementation:**
- WebClient automatically forwards Authorization header
- Services validate JWT using shared public key

## Resilience Patterns

### Circuit Breaker
- Prevents cascading failures
- Opens circuit after failure threshold
- Automatically retries after timeout

### Retry Mechanism
- Automatic retry for transient failures
- Configurable retry attempts and backoff

### Timeout Configuration
- Request timeouts to prevent hanging
- Configurable per service

## Best Practices

1. **Prefer Asynchronous for:**
   - Event notifications
   - Non-critical operations
   - Operations that can be eventually consistent

2. **Use Synchronous for:**
   - Critical validations
   - Operations requiring immediate response
   - Transactional operations

3. **Error Handling:**
   - Always handle service unavailability
   - Use circuit breakers for resilience
   - Implement fallback mechanisms where possible

4. **Security:**
   - Always validate JWT tokens
   - Use service-to-service authentication
   - Never expose internal service URLs publicly

5. **Monitoring:**
   - Log all inter-service calls
   - Monitor circuit breaker states
   - Track service response times

## Testing Inter-Service Communication

### Local Testing
1. Start all services locally
2. Use service URLs: `http://localhost:PORT`
3. Ensure RabbitMQ is running

### Docker Testing
1. Use service names as hostnames: `http://auth-service:8081`
2. Services in same Docker network can communicate directly

## Troubleshooting

### RabbitMQ Connection Issues
- Check RabbitMQ is running: `docker ps | grep rabbitmq`
- Verify connection settings in `application.properties`
- Check RabbitMQ management UI: `http://localhost:15672`

### Service Not Found
- Verify service is running: `curl http://localhost:PORT/actuator/health`
- Check service URL configuration
- Verify network connectivity

### Authentication Failures
- Verify JWT token is valid
- Check token expiration
- Ensure public key is accessible

