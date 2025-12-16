package com.devblocker.blocker.dto;

import com.devblocker.blocker.model.BlockerStatus;
import com.devblocker.blocker.model.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private List<String> mediaUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}

