package com.devblocker.blocker.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Client for communicating with User Service
 * Handles synchronous REST calls to user-service
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {
    
    private final WebClient webClient;
    
    @Value("${services.user.url:http://localhost:8082}")
    private String userServiceUrl;
    
    /**
     * Get user profile by user ID
     * Used to validate user exists before creating/assigning blockers
     * 
     * @param userId User ID to fetch
     * @param authToken JWT token for authentication (optional)
     * @return User profile or null if not found
     */
    public UserProfileResponse getUserProfile(UUID userId, String authToken) {
        try {
            return webClient.get()
                    .uri(userServiceUrl + "/api/v1/users/{id}", userId)
                    .headers(headers -> {
                        if (authToken != null && !authToken.isEmpty()) {
                            headers.setBearerAuth(authToken);
                        }
                    })
                    .retrieve()
                    .bodyToMono(UserProfileResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                            .filter(throwable -> throwable instanceof WebClientResponseException 
                                    && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
                            .doBeforeRetry(retrySignal -> 
                                log.warn("Retrying getUserProfile for user: {} (attempt {})", 
                                        userId, retrySignal.totalRetries() + 1)))
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            log.debug("User not found: {}", userId);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch user profile for user: {}", userId, e);
            // Return null instead of throwing - allows blocker creation to continue
            // In production, you might want to implement a fallback or circuit breaker
            return null;
        }
    }
    
    /**
     * Validate if user exists
     * 
     * @param userId User ID to validate
     * @param authToken JWT token for authentication (optional)
     * @return true if user exists, false otherwise
     */
    public boolean userExists(UUID userId, String authToken) {
        UserProfileResponse profile = getUserProfile(userId, authToken);
        return profile != null;
    }
    
    /**
     * Get user reputation
     * 
     * @param userId User ID
     * @param authToken JWT token for authentication (optional)
     * @return User reputation points or null if not found
     */
    public Integer getUserReputation(UUID userId, String authToken) {
        try {
            return webClient.get()
                    .uri(userServiceUrl + "/api/v1/users/{id}/reputation", userId)
                    .headers(headers -> {
                        if (authToken != null && !authToken.isEmpty()) {
                            headers.setBearerAuth(authToken);
                        }
                    })
                    .retrieve()
                    .bodyToMono(UserReputationResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .map(UserReputationResponse::getPoints)
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch user reputation for user: {}", userId, e);
            return null;
        }
    }
    
    /**
     * Increment user reputation points
     * 
     * @param userId User ID
     * @param points Points to add (must be positive)
     * @param reason Reason for the increment
     * @param source Source of the increment (e.g., "SOLUTION_ACCEPTED")
     * @param authToken JWT token for authentication (optional)
     * @return true if successful, false otherwise
     */
    public boolean incrementReputation(UUID userId, Integer points, String reason, String source, String authToken) {
        try {
            IncrementReputationRequest request = new IncrementReputationRequest();
            request.setPoints(points);
            request.setReason(reason);
            request.setSource(source);
            
            webClient.post()
                    .uri(userServiceUrl + "/api/v1/users/{id}/reputation/increment", userId)
                    .headers(headers -> {
                        if (authToken != null && !authToken.isEmpty()) {
                            headers.setBearerAuth(authToken);
                        }
                    })
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(UserReputationResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                            .filter(throwable -> throwable instanceof WebClientResponseException 
                                    && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
                            .doBeforeRetry(retrySignal -> 
                                log.warn("Retrying incrementReputation for user: {} (attempt {})", 
                                        userId, retrySignal.totalRetries() + 1)))
                    .timeout(Duration.ofSeconds(5))
                    .block();
            
            log.info("Successfully incremented reputation by {} points for user: {}", points, userId);
            return true;
        } catch (Exception e) {
            log.error("Failed to increment reputation for user: {}", userId, e);
            return false;
        }
    }
    
    /**
     * Get user team codes
     * 
     * @param userId User ID
     * @param authToken JWT token for authentication (optional)
     * @return List of team codes the user belongs to, or empty list if not found
     */
    public List<String> getUserTeamCodes(UUID userId, String authToken) {
        try {
            List<TeamResponse> teams = webClient.get()
                    .uri(userServiceUrl + "/api/v1/users/{id}/teams", userId)
                    .headers(headers -> {
                        if (authToken != null && !authToken.isEmpty()) {
                            headers.setBearerAuth(authToken);
                        }
                    })
                    .retrieve()
                    .bodyToFlux(TeamResponse.class)
                    .collectList()
                    .timeout(Duration.ofSeconds(5))
                    .block();
            
            if (teams == null) {
                return new java.util.ArrayList<>();
            }
            
            return teams.stream()
                    .map(team -> team.getTeamCode() != null ? team.getTeamCode().name() : null)
                    .filter(code -> code != null)
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch user team codes for user: {}", userId, e);
            return new java.util.ArrayList<>();
        }
    }
    
    // Inner DTOs matching User Service responses
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserProfileResponse {
        private UUID userId;
        private String name;
        private String avatarUrl;
        private UUID teamId;
        private String teamName;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserReputationResponse {
        private UUID userId;
        private Integer points;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IncrementReputationRequest {
        private Integer points;
        private String reason;
        private String source;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TeamResponse {
        private UUID teamId;
        private String name;
        private TeamCode teamCode;
        private Integer memberCount;
        private java.time.LocalDateTime createdAt;
    }
    
    public enum TeamCode {
        DEVOPS,
        BACKEND,
        FRONTEND,
        QA
    }
}

