package com.devblocker.blocker.service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolutionAddedEvent {
    private String solutionId;
    private String blockerId;
    private String description;
    private String createdBy;
    private LocalDateTime createdAt;
}

