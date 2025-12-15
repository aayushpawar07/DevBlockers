package com.devblocker.user.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Client for communicating with Solution Service
 * Handles synchronous REST calls to solution-service
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SolutionServiceClient {
    
    private final WebClient webClient;
    
    @Value("${services.solution.url:http://localhost:8084}")
    private String solutionServiceUrl;
    
    /**
     * Get all solutions by a user
     * 
     * @param userId User ID
     * @param authToken JWT token for authentication (optional)
     * @return List of solutions created by the user
     */
    public List<SolutionResponse> getSolutionsByUser(UUID userId, String authToken) {
        try {
            // Note: Solution service doesn't have a direct endpoint to get solutions by user
            // We'll need to fetch all solutions and filter, or add an endpoint
            // For now, return empty list and log a warning
            log.warn("Solution service doesn't have endpoint to get solutions by user. Returning empty list.");
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch solutions for user: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Get solution statistics for a user
     * 
     * @param userId User ID
     * @param authToken JWT token for authentication (optional)
     * @return Solution statistics
     */
    public UserSolutionStats getSolutionStats(UUID userId, String authToken) {
        try {
            SolutionStatsResponse response = webClient.get()
                    .uri(solutionServiceUrl + "/api/v1/users/{userId}/solutions/stats", userId)
                    .headers(headers -> {
                        if (authToken != null && !authToken.isEmpty()) {
                            headers.setBearerAuth(authToken);
                        }
                    })
                    .retrieve()
                    .bodyToMono(SolutionStatsResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                            .filter(throwable -> throwable instanceof WebClientResponseException 
                                    && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
                            .doBeforeRetry(retrySignal -> 
                                log.warn("Retrying getSolutionStats for user: {} (attempt {})", 
                                        userId, retrySignal.totalRetries() + 1)))
                    .timeout(Duration.ofSeconds(5))
                    .block();
            
            if (response != null) {
                return UserSolutionStats.builder()
                        .totalSolutions((int) response.getTotalSolutions())
                        .acceptedSolutions((int) response.getAcceptedSolutions())
                        .build();
            }
        } catch (Exception e) {
            log.error("Failed to fetch solution stats for user: {}", userId, e);
        }
        
        // Return zeros on error
        return UserSolutionStats.builder()
                .totalSolutions(0)
                .acceptedSolutions(0)
                .build();
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SolutionStatsResponse {
        private long totalSolutions;
        private long acceptedSolutions;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SolutionResponse {
        private UUID solutionId;
        private UUID blockerId;
        private UUID userId;
        private String content;
        private Integer upvotes;
        private Boolean accepted;
        private java.time.LocalDateTime createdAt;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserSolutionStats {
        private int totalSolutions;
        private int acceptedSolutions;
    }
}

