package com.devblocker.notification.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

/**
 * Client for communicating with User Service
 * Used to fetch user information (e.g., email for notifications)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {
    
    private final WebClient webClient;
    
    @Value("${services.user.url:http://localhost:8082}")
    private String userServiceUrl;
    
    /**
     * Get user email by user ID
     * Note: This is a placeholder - user-service may need to expose email endpoint
     * For now, returns null - in production, implement proper endpoint
     * 
     * @param userId User ID
     * @param authToken JWT token for authentication (optional)
     * @return User email or null if not found/not available
     */
    public String getUserEmail(UUID userId, String authToken) {
        try {
            // TODO: Implement when user-service exposes email endpoint
            // For now, return null - email will be fetched from auth-service or user-service
            log.debug("Fetching user email for user: {} (not yet implemented)", userId);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch user email for user: {}", userId, e);
            return null;
        }
    }
    
    /**
     * Get user profile to extract email or other info
     * 
     * @param userId User ID
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
            log.debug("User profile not found: {}", userId);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch user profile for user: {}", userId, e);
            return null;
        }
    }
    
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
}

