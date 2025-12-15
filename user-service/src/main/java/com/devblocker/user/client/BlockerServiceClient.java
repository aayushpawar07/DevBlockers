package com.devblocker.user.client;

import com.devblocker.user.client.dto.BlockerResponse;
import com.devblocker.user.client.dto.PageResponse;
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
 * Client for communicating with Blocker Service
 * Handles synchronous REST calls to blocker-service
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BlockerServiceClient {
    
    private final WebClient webClient;
    
    @Value("${services.blocker.url:http://localhost:8083}")
    private String blockerServiceUrl;
    
    /**
     * Get blockers created by a specific user
     * 
     * @param userId User ID
     * @param authToken JWT token for authentication (optional)
     * @return List of blockers created by the user
     */
    public List<BlockerResponse> getBlockersByUser(UUID userId, String authToken) {
        try {
            PageResponse<BlockerResponse> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(blockerServiceUrl + "/api/v1/blockers")
                            .queryParam("createdBy", userId)
                            .queryParam("page", 0)
                            .queryParam("size", 100)
                            .build())
                    .headers(headers -> {
                        if (authToken != null && !authToken.isEmpty()) {
                            headers.setBearerAuth(authToken);
                        }
                    })
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<PageResponse<BlockerResponse>>() {})
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                            .filter(throwable -> throwable instanceof WebClientResponseException 
                                    && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
                            .doBeforeRetry(retrySignal -> 
                                log.warn("Retrying getBlockersByUser for user: {} (attempt {})", 
                                        userId, retrySignal.totalRetries() + 1)))
                    .timeout(Duration.ofSeconds(5))
                    .block();
            
            return response != null ? response.getContent() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch blockers for user: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Get a specific blocker by ID
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
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            log.debug("Blocker not found: {}", blockerId);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch blocker: {}", blockerId, e);
            return null;
        }
    }
    
    /**
     * Get blocker statistics for a user
     * 
     * @param userId User ID
     * @param authToken JWT token for authentication (optional)
     * @return Blocker statistics
     */
    public UserBlockerStats getBlockerStats(UUID userId, String authToken) {
        List<BlockerResponse> blockers = getBlockersByUser(userId, authToken);
        
        long openCount = blockers.stream()
                .filter(b -> b.getStatus() == BlockerResponse.BlockerStatus.OPEN)
                .count();
        long resolvedCount = blockers.stream()
                .filter(b -> b.getStatus() == BlockerResponse.BlockerStatus.RESOLVED)
                .count();
        
        return UserBlockerStats.builder()
                .totalBlockers(blockers.size())
                .openBlockers((int) openCount)
                .resolvedBlockers((int) resolvedCount)
                .build();
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserBlockerStats {
        private int totalBlockers;
        private int openBlockers;
        private int resolvedBlockers;
    }
}

