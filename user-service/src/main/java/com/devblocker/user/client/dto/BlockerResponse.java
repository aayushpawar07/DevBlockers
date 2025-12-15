package com.devblocker.user.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO matching Blocker Service's BlockerResponse
 * Used for inter-service communication
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockerResponse {
    private UUID blockerId;
    private String title;
    private String description;
    private BlockerStatus status;
    private Severity severity;
    private UUID createdBy;
    private UUID assignedTo;
    private UUID teamId;
    private UUID bestSolutionId;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    
    public enum BlockerStatus {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED
    }
    
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}

