package com.devblocker.notification.client;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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
 * Client for communicating with Blocker Service
 * Used to fetch blocker details (e.g., creator, assignee) for notifications
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BlockerServiceClient {
    
    private final WebClient webClient;
    
    @Value("${services.blocker.url:http://localhost:8083}")
    private String blockerServiceUrl;
    
    /**
     * Get blocker details by ID
     * 
     * @param blockerId Blocker ID
     * @param authToken JWT token for authentication (optional)
     * @return Blocker details or null if not found
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
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                            .filter(throwable -> throwable instanceof WebClientResponseException 
                                    && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
                            .doBeforeRetry(retrySignal -> 
                                log.warn("Retrying getBlocker for blocker: {} (attempt {})", 
                                        blockerId, retrySignal.totalRetries() + 1)))
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            log.debug("Blocker not found: {}", blockerId);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch blocker details for blocker: {}", blockerId, e);
            return null;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockerResponse {
        private UUID blockerId;
        private String title;
        private String description;
        private String status;
        private String severity;
        private UUID createdBy;
        private UUID assignedTo;
        private UUID teamId;
        private UUID bestSolutionId;
        private java.util.List<String> tags;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
        private java.time.LocalDateTime resolvedAt;
    }
}

