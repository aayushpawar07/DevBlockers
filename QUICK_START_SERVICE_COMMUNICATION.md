# Quick Start: Service Communication Setup

This is a quick reference guide for setting up inter-service communication in your microservices architecture.

## ✅ What Has Been Added

### 1. **WebClient Configuration**
   - ✅ `WebClientConfig.java` in all three services (auth, user, blocker)
   - ✅ Configured with timeouts and connection settings
   - ✅ Ready to use for synchronous REST calls

### 2. **Service URL Configuration**
   - ✅ Added to `application.properties` in all services
   - ✅ Configurable via environment variables
   - ✅ Default values for local development

### 3. **Service Clients**
   - ✅ `UserServiceClient` in blocker-service (calls user-service)
   - ✅ `BlockerServiceClient` in user-service (calls blocker-service)
   - ✅ Includes retry logic and error handling

### 4. **Dependencies**
   - ✅ Added `spring-boot-starter-webflux` to all pom.xml files
   - ✅ Provides WebClient for reactive HTTP calls

### 5. **Documentation**
   - ✅ `SERVICE_COMMUNICATION_GUIDE.md` - Comprehensive guide
   - ✅ `SERVICE_COMMUNICATION_EXAMPLES.md` - Practical examples
   - ✅ This quick start guide

## 🚀 How to Use

### Step 1: Configure Service URLs

Each service has service URLs configured in `application.properties`:

```properties
# In user-service/application.properties
services.auth.url=${AUTH_SERVICE_URL:http://localhost:8081}
services.blocker.url=${BLOCKER_SERVICE_URL:http://localhost:8083}

# In blocker-service/application.properties
services.auth.url=${AUTH_SERVICE_URL:http://localhost:8081}
services.user.url=${USER_SERVICE_URL:http://localhost:8082}

# In auth/application.properties
services.user.url=${USER_SERVICE_URL:http://localhost:8082}
services.blocker.url=${BLOCKER_SERVICE_URL:http://localhost:8083}
```

### Step 2: Inject Service Client

```java
@Service
@RequiredArgsConstructor
public class YourService {
    
    private final UserServiceClient userServiceClient; // Inject the client
    // or
    private final BlockerServiceClient blockerServiceClient;
}
```

### Step 3: Make Service Calls

```java
// Example: Validate user exists
if (!userServiceClient.userExists(userId, authToken)) {
    throw new IllegalArgumentException("User not found");
}

// Example: Get user profile
UserServiceClient.UserProfileResponse profile = 
    userServiceClient.getUserProfile(userId, authToken);

// Example: Get user's blockers
List<BlockerResponse> blockers = 
    blockerServiceClient.getBlockersByUser(userId, authToken);
```

## 📋 Communication Patterns

### Asynchronous (Event-Driven) - Already Working ✅

**Current Setup:**
- Auth Service → Publishes `UserRegistered` events
- User Service → Listens to `UserRegistered` events
- Blocker Service → Publishes blocker events

**How it works:**
1. Service publishes event to RabbitMQ
2. RabbitMQ routes to subscribed queues
3. Listening services consume events asynchronously

**Example:**
```java
// In AuthService
eventPublisher.publishUserRegistered(user); // Non-blocking
```

### Synchronous (REST API) - Now Available ✅

**New Setup:**
- Blocker Service → Can call User Service via `UserServiceClient`
- User Service → Can call Blocker Service via `BlockerServiceClient`

**How it works:**
1. Service makes HTTP call using WebClient
2. Waits for response
3. Returns result or throws exception

**Example:**
```java
// In BlockerService
UserProfileResponse profile = userServiceClient.getUserProfile(userId, authToken);
```

## 🔧 Configuration Options

### WebClient Timeouts

Configure in `application.properties`:

```properties
webclient.connect-timeout=5000      # Connection timeout in ms
webclient.read-timeout=10000         # Read timeout in ms
webclient.write-timeout=10000        # Write timeout in ms
```

### Service URLs (Environment Variables)

For Docker/production:

```bash
export AUTH_SERVICE_URL=http://auth-service:8081
export USER_SERVICE_URL=http://user-service:8082
export BLOCKER_SERVICE_URL=http://blocker-service:8083
```

## 📝 Example: Adding User Validation to Blocker Service

**Before:**
```java
public BlockerResponse createBlocker(CreateBlockerRequest request) {
    Blocker blocker = Blocker.builder()
            .createdBy(request.getCreatedBy())
            // ... other fields
            .build();
    return blockerRepository.save(blocker);
}
```

**After:**
```java
@Service
@RequiredArgsConstructor
public class BlockerService {
    
    private final BlockerRepository blockerRepository;
    private final UserServiceClient userServiceClient; // Add this
    
    public BlockerResponse createBlocker(CreateBlockerRequest request, String authToken) {
        // Validate user exists
        if (!userServiceClient.userExists(request.getCreatedBy(), authToken)) {
            throw new IllegalArgumentException("User not found");
        }
        
        Blocker blocker = Blocker.builder()
                .createdBy(request.getCreatedBy())
                // ... other fields
                .build();
        return blockerRepository.save(blocker);
    }
}
```

## 🧪 Testing

### 1. Start All Services

```bash
# Terminal 1
cd auth && mvn spring-boot:run

# Terminal 2
cd user-service && mvn spring-boot:run

# Terminal 3
cd blocker-service && mvn spring-boot:run
```

### 2. Test Service Communication

```bash
# Test user service
curl http://localhost:8082/api/v1/users/{userId}

# Test blocker service calling user service
# (This will happen when you create a blocker with user validation)
curl -X POST http://localhost:8083/api/v1/blockers \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","createdBy":"<userId>"}'
```

## 📚 Next Steps

1. **Read the full guide**: `SERVICE_COMMUNICATION_GUIDE.md`
2. **See examples**: `SERVICE_COMMUNICATION_EXAMPLES.md`
3. **Add more service clients** as needed
4. **Consider adding circuit breakers** (Resilience4j) for production
5. **Add service discovery** (Eureka/Consul) for dynamic service URLs

## 🐛 Troubleshooting

### Issue: Service not found (404)
**Solution:** Check service URL in `application.properties` and ensure service is running

### Issue: Connection timeout
**Solution:** Increase timeout in `application.properties` or check network connectivity

### Issue: WebClient not found
**Solution:** Run `mvn clean install` to download new dependencies

### Issue: RabbitMQ connection failed
**Solution:** Ensure RabbitMQ is running: `docker ps | grep rabbitmq`

## 📞 Service Ports Reference

| Service | Port | Health Check |
|---------|------|--------------|
| Auth Service | 8081 | http://localhost:8081/actuator/health |
| User Service | 8082 | http://localhost:8082/actuator/health |
| Blocker Service | 8083 | http://localhost:8083/actuator/health |
| RabbitMQ | 5672 | http://localhost:15672 (Management UI) |

## ✨ Summary

You now have:
- ✅ Asynchronous communication via RabbitMQ (already working)
- ✅ Synchronous communication via WebClient (newly added)
- ✅ Service clients ready to use
- ✅ Configuration files updated
- ✅ Comprehensive documentation

**Start using service clients in your services to enable inter-service communication!**

