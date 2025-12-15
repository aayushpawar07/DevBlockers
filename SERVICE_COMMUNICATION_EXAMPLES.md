# Service Communication - Usage Examples

This document provides practical examples of how to use the service clients for inter-service communication.

## Prerequisites

1. All services are running
2. RabbitMQ is running
3. Services are configured with correct URLs in `application.properties`

## Example 1: Blocker Service → User Service

### Scenario: Validate user exists before creating a blocker

**In BlockerService.java:**

```java
@Service
@RequiredArgsConstructor
public class BlockerService {
    
    private final BlockerRepository blockerRepository;
    private final EventPublisher eventPublisher;
    private final UserServiceClient userServiceClient; // Add this
    
    @Transactional
    public BlockerResponse createBlocker(CreateBlockerRequest request, String authToken) {
        // Validate user exists
        if (!userServiceClient.userExists(request.getCreatedBy(), authToken)) {
            throw new IllegalArgumentException("User not found: " + request.getCreatedBy());
        }
        
        // If assignedTo is provided, validate that user too
        if (request.getAssignedTo() != null) {
            if (!userServiceClient.userExists(request.getAssignedTo(), authToken)) {
                throw new IllegalArgumentException("Assigned user not found: " + request.getAssignedTo());
            }
        }
        
        // Continue with blocker creation...
        Blocker blocker = Blocker.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .severity(request.getSeverity())
                .status(BlockerStatus.OPEN)
                .createdBy(request.getCreatedBy())
                .assignedTo(request.getAssignedTo())
                .teamId(request.getTeamId())
                .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
                .build();
        
        blocker = blockerRepository.save(blocker);
        
        // Publish event
        eventPublisher.publishBlockerCreated(blocker);
        
        return mapToBlockerResponse(blocker);
    }
}
```

### Scenario: Get user reputation when resolving a blocker

```java
public BlockerResponse resolveBlocker(UUID blockerId, ResolveBlockerRequest request, String authToken) {
    Blocker blocker = blockerRepository.findByBlockerId(blockerId)
            .orElseThrow(() -> new IllegalArgumentException("Blocker not found"));
    
    blocker.setStatus(BlockerStatus.RESOLVED);
    blocker.setBestSolutionId(request.getBestSolutionId());
    blocker.setResolvedAt(LocalDateTime.now());
    blocker = blockerRepository.save(blocker);
    
    // Get user reputation to award points
    Integer currentReputation = userServiceClient.getUserReputation(
        blocker.getCreatedBy(), 
        authToken
    );
    
    // Award reputation points (this would typically be done via an event or direct call)
    log.info("User {} has {} reputation points", blocker.getCreatedBy(), currentReputation);
    
    eventPublisher.publishBlockerResolved(blocker);
    
    return mapToBlockerResponse(blocker);
}
```

## Example 2: User Service → Blocker Service

### Scenario: Get user's blocker statistics

**In UserService.java (or create a new service):**

```java
@Service
@RequiredArgsConstructor
public class UserStatsService {
    
    private final BlockerServiceClient blockerServiceClient;
    
    public UserStatsResponse getUserStats(UUID userId, String authToken) {
        // Get blocker statistics
        BlockerServiceClient.UserBlockerStats blockerStats = 
            blockerServiceClient.getBlockerStats(userId, authToken);
        
        return UserStatsResponse.builder()
                .userId(userId)
                .totalBlockers(blockerStats.getTotalBlockers())
                .openBlockers(blockerStats.getOpenBlockers())
                .resolvedBlockers(blockerStats.getResolvedBlockers())
                .build();
    }
}
```

### Scenario: Get user's blockers in profile

```java
@GetMapping("/{id}/blockers")
public ResponseEntity<List<BlockerResponse>> getUserBlockers(
        @PathVariable UUID id,
        @RequestHeader(value = "Authorization", required = false) String authToken) {
    
    List<BlockerResponse> blockers = blockerServiceClient.getBlockersByUser(id, authToken);
    return ResponseEntity.ok(blockers);
}
```

## Example 3: Extracting JWT Token from Request

### In Controller

```java
@RestController
@RequestMapping("/api/v1/blockers")
@RequiredArgsConstructor
public class BlockerController {
    
    private final BlockerService blockerService;
    
    @PostMapping
    public ResponseEntity<BlockerResponse> createBlocker(
            @Valid @RequestBody CreateBlockerRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Extract token from "Bearer <token>" format
        String token = extractToken(authHeader);
        
        BlockerResponse response = blockerService.createBlocker(request, token);
        return ResponseEntity.ok(response);
    }
    
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
```

## Example 4: Using Service Client with Error Handling

```java
public UserProfileResponse getUserProfileSafely(UUID userId, String authToken) {
    try {
        UserProfileResponse profile = userServiceClient.getUserProfile(userId, authToken);
        
        if (profile == null) {
            log.warn("User profile not found: {}", userId);
            // Return default or throw exception based on your business logic
            throw new UserNotFoundException("User not found: " + userId);
        }
        
        return profile;
    } catch (WebClientResponseException e) {
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new UserNotFoundException("User not found: " + userId);
        }
        log.error("Error calling user service: {}", e.getMessage());
        throw new ServiceUnavailableException("User service unavailable");
    }
}
```

## Example 5: Asynchronous Event Communication

### Publishing Event (Auth Service)

```java
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;
    
    public AuthResponse register(RegisterRequest request) {
        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        
        user = userRepository.save(user);
        
        // Publish event asynchronously (doesn't block)
        eventPublisher.publishUserRegistered(user);
        
        // Return response immediately
        return AuthResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .build();
    }
}
```

### Consuming Event (User Service)

```java
@Component
@RequiredArgsConstructor
public class UserRegisteredListener {
    
    private final ProfileService profileService;
    
    @RabbitListener(queues = "user.registered.queue")
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            UUID userId = UUID.fromString(event.getUserId());
            log.info("Received UserRegistered event for user: {}", userId);
            
            // Create profile asynchronously
            profileService.createProfile(userId, event.getEmail());
            
            log.info("Profile created successfully for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to process UserRegistered event: {}", event, e);
            // Event will be retried or sent to DLQ based on RabbitMQ configuration
        }
    }
}
```

## Best Practices

### 1. Always Handle Service Unavailability

```java
public UserProfileResponse getUserProfileWithFallback(UUID userId, String authToken) {
    try {
        return userServiceClient.getUserProfile(userId, authToken);
    } catch (Exception e) {
        log.error("User service unavailable, using fallback", e);
        // Return cached data or default value
        return getCachedProfile(userId);
    }
}
```

### 2. Use Timeouts

WebClient is already configured with timeouts in `WebClientConfig`, but you can override per call:

```java
return webClient.get()
        .uri(url)
        .retrieve()
        .bodyToMono(Response.class)
        .timeout(Duration.ofSeconds(2)) // Override default timeout
        .block();
```

### 3. Log All Inter-Service Calls

```java
public UserProfileResponse getUserProfile(UUID userId, String authToken) {
    log.debug("Calling user service for user: {}", userId);
    long startTime = System.currentTimeMillis();
    
    try {
        UserProfileResponse response = userServiceClient.getUserProfile(userId, authToken);
        long duration = System.currentTimeMillis() - startTime;
        log.info("User service call completed in {}ms for user: {}", duration, userId);
        return response;
    } catch (Exception e) {
        long duration = System.currentTimeMillis() - startTime;
        log.error("User service call failed after {}ms for user: {}", duration, userId, e);
        throw e;
    }
}
```

### 4. Use Circuit Breakers (Future Enhancement)

When you add Resilience4j, you can wrap service calls:

```java
@CircuitBreaker(name = "userService", fallbackMethod = "getUserProfileFallback")
public UserProfileResponse getUserProfile(UUID userId, String authToken) {
    return userServiceClient.getUserProfile(userId, authToken);
}

public UserProfileResponse getUserProfileFallback(UUID userId, String authToken, Exception e) {
    log.warn("Circuit breaker opened, using fallback for user: {}", userId);
    return getCachedProfile(userId);
}
```

## Testing Inter-Service Communication

### Unit Test Example

```java
@ExtendWith(MockitoExtension.class)
class BlockerServiceTest {
    
    @Mock
    private UserServiceClient userServiceClient;
    
    @InjectMocks
    private BlockerService blockerService;
    
    @Test
    void createBlocker_shouldValidateUserExists() {
        // Given
        CreateBlockerRequest request = new CreateBlockerRequest();
        request.setCreatedBy(UUID.randomUUID());
        
        when(userServiceClient.userExists(any(), any())).thenReturn(true);
        
        // When
        BlockerResponse response = blockerService.createBlocker(request, "token");
        
        // Then
        assertNotNull(response);
        verify(userServiceClient).userExists(request.getCreatedBy(), "token");
    }
    
    @Test
    void createBlocker_shouldThrowExceptionWhenUserNotFound() {
        // Given
        CreateBlockerRequest request = new CreateBlockerRequest();
        request.setCreatedBy(UUID.randomUUID());
        
        when(userServiceClient.userExists(any(), any())).thenReturn(false);
        
        // When/Then
        assertThrows(IllegalArgumentException.class, 
            () -> blockerService.createBlocker(request, "token"));
    }
}
```

## Troubleshooting

### Service Not Found (404)
- Check service URL in `application.properties`
- Verify service is running: `curl http://localhost:PORT/actuator/health`
- Check service logs for errors

### Connection Timeout
- Increase timeout in `application.properties`: `webclient.read-timeout=20000`
- Check network connectivity
- Verify service is accessible

### Authentication Failures
- Ensure JWT token is valid
- Check token expiration
- Verify token format: `Bearer <token>`

### RabbitMQ Connection Issues
- Check RabbitMQ is running: `docker ps | grep rabbitmq`
- Verify connection settings
- Check RabbitMQ management UI: `http://localhost:15672`

