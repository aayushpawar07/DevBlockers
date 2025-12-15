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
public class BlockerResolvedEvent {
    private String blockerId;
    private String title;
    private String bestSolutionId;
    private String resolvedBy;
    private LocalDateTime resolvedAt;
}

