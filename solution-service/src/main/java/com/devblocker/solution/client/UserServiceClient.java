package com.devblocker.solution.client;

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

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {
    
    private final WebClient webClient;
    
    @Value("${services.user.url:http://localhost:8082}")
    private String userServiceUrl;
    
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

