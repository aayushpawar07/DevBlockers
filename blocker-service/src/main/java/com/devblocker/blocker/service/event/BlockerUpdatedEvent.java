package com.devblocker.blocker.service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockerUpdatedEvent {
    private String blockerId;
    private String title;
    private String description;
    private String status;
    private String severity;
    private String createdBy;
    private String assignedTo;
    private String teamId;
    private List<String> tags;
    private LocalDateTime updatedAt;
}

