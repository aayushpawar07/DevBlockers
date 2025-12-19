package com.devblocker.solution.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client for communicating with Blocker Service
 * Used to update blocker's best solution ID when a solution is accepted
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BlockerServiceClient {
    
    private final WebClient webClient;
    
    @Value("${services.blocker.url:http://localhost:8083}")
    private String blockerServiceUrl;
    
    /**
     * Update blocker's best solution ID
     * This is called when a solution is accepted
     * 
     * @param blockerId Blocker ID
     * @param solutionId Solution ID to set as best solution
     * @param authToken JWT token for authentication (optional)
     * @return true if successful, false otherwise
     */
    public boolean updateBestSolution(UUID blockerId, UUID solutionId, String authToken) {
        try {
            // Use the dedicated endpoint to update best solution without resolving
            Map<String, String> request = new HashMap<>();
            request.put("bestSolutionId", solutionId.toString());
            
            webClient.put()
                    .uri(blockerServiceUrl + "/api/v1/blockers/{id}/best-solution", blockerId)
                    .headers(headers -> {
                        if (authToken != null && !authToken.isEmpty()) {
                            headers.setBearerAuth(authToken);
                        }
                    })
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                            .filter(throwable -> throwable instanceof WebClientResponseException 
                                    && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
                            .doBeforeRetry(retrySignal -> 
                                log.warn("Retrying updateBestSolution for blocker: {} (attempt {})", 
                                        blockerId, retrySignal.totalRetries() + 1)))
                    .timeout(Duration.ofSeconds(5))
                    .block();
            
            log.info("Successfully updated best solution for blocker: {} to solution: {}", blockerId, solutionId);
            return true;
        } catch (WebClientResponseException.NotFound e) {
            log.error("Blocker not found: {}", blockerId);
            return false;
        } catch (Exception e) {
            log.error("Failed to update best solution for blocker: {}", blockerId, e);
            // Don't throw - allow solution acceptance to proceed even if blocker update fails
            // The event will be published and blocker-service can handle it asynchronously
            return false;
        }
    }
    
    /**
     * Validate that blocker exists
     * 
     * @param blockerId Blocker ID
     * @param authToken JWT token for authentication (optional)
     * @return true if blocker exists, false otherwise
     */
    public boolean blockerExists(UUID blockerId, String authToken) {
        try {
            webClient.get()
                    .uri(blockerServiceUrl + "/api/v1/blockers/{id}", blockerId)
                    .headers(headers -> {
                        if (authToken != null && !authToken.isEmpty()) {
                            headers.setBearerAuth(authToken);
                        }
                    })
                    .retrieve()
                    .bodyToMono(Object.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return true;
        } catch (WebClientResponseException.NotFound e) {
            return false;
        } catch (Exception e) {
            log.error("Failed to check if blocker exists: {}", blockerId, e);
            return false;
        }
    }
    
    /**
     * Get blocker details including teamCode
     * 
     * @param blockerId Blocker ID
     * @param authToken JWT token for authentication (optional)
     * @return BlockerResponse with teamCode or null if not found
     */
    public BlockerResponse getBlocker(UUID blockerId, String authToken) {
        try {
            return webClient.get()
                    .uri(blockerServiceUrl + "/api/v1/blockers/{id}", blockerId)
                    .headers(headers -> {
                        if (authToken != null && !authToken.isEmpty()) {
                            headers.setBearerAuth(authToken);
                        }
                    })
                    .retrieve()
                    .bodyToMono(BlockerResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            return null;
        } catch (Exception e) {
            log.error("Failed to get blocker: {}", blockerId, e);
            return null;
        }
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BlockerResponse {
        private UUID blockerId;
        private String title;
        private String description;
        private String status;
        private String severity;
        private UUID createdBy;
        private UUID assignedTo;
        private UUID teamId;
        private String teamCode;
        private UUID bestSolutionId;
        private java.util.List<String> tags;
        private java.util.List<String> mediaUrls;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
        private java.time.LocalDateTime resolvedAt;
    }
}

